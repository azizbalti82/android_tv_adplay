const mongoose = require('mongoose');
const { GridFSBucket } = require('mongodb');
const Media = require('../models/modelMedia');

// Initialize GridFSBucket
const getBucket = () => new GridFSBucket(mongoose.connection.db, { bucketName: 'uploads' });

// Upload media
const upload_media = async (req, res) => {
    const { adId } = req.params;

    if (!adId) return res.status(400).json({ message: 'Ad ID is required' });

    try {
        const existingMedia = await Media.findOne({ id: adId });
        if (existingMedia) {
            return res.status(400).json({ message: 'Media for this Ad ID already exists' });
        }

        const file = req.file;
        if (!file) return res.status(400).json({ message: 'No file uploaded' });

        const bucket = getBucket();

        // Create a stream for the uploaded file
        const readStream = file.stream;

        const writeStream = bucket.openUploadStream(adId, {
            contentType: file.mimetype,
            allowDiskUse: true // Allow MongoDB to use disk space for large files
        });

        readStream.pipe(writeStream); // Pipe the file directly into GridFS

        writeStream.on('finish', async () => {
            await new Media({ id: adId }).save();
            res.status(201).json({ message: 'Media uploaded successfully' });
        });

        writeStream.on('error', (err) => {
            console.error('Error writing to GridFS:', err);
            res.status(500).json({ message: 'Error uploading file to GridFS' });
        });
    } catch (err) {
        console.error('Error in upload_media:', err);
        res.status(500).json({ message: 'Server error' });
    }
};


const delete_media = async (req, res) => {
    const { adId } = req.params;

    if (!adId) return res.status(400).json({ message: 'Ad ID is required' });

    try {
        const media = await Media.findOne({ id: adId });
        if (!media) return res.status(404).json({ message: 'Media not found' });

        const bucket = getBucket();
        
        // Find the file in GridFS
        const files = await bucket.find({ filename: adId }).toArray();
        if (files.length === 0) {
            return res.status(404).json({ message: 'File not found in GridFS' });
        }

        // Delete the file by its ObjectId
        const fileId = files[0]._id;
        await bucket.delete(fileId);

        // Remove the record from the Media collection
        await Media.deleteOne({ id: adId });

        res.status(200).json({ message: 'Media deleted successfully' });
    } catch (err) {
        console.error('Error in delete_media:', err);
        res.status(500).json({ message: 'Server error' });
    }
};

// Get media
const get_media = async (req, res) => {
    const { adId } = req.params;

    if (!adId) return res.status(400).json({ message: 'Ad ID is required' });

    try {
        console.log('Fetching media for Ad ID:', adId);

        const media = await Media.findOne({ id: adId });
        if (!media) return res.status(404).json({ message: 'Media not found' });

        const bucket = getBucket();

        // Confirm file exists in GridFS
        const files = await bucket.find({ filename: adId }).toArray({ allowDiskUse: true });

        if (files.length === 0) {
            return res.status(404).json({ message: 'File not found in GridFS' });
        }
        
        // Open download stream and pipe to response
        const downloadStream = bucket.openDownloadStreamByName(adId);
        downloadStream.pipe(res);

        downloadStream.on('error', (err) => {
            console.error('Error reading file from GridFS:', err);
            res.status(404).json({ message: 'File not found in GridFS' });
        });
    } catch (err) {
        console.error('Error in get_media:', err);
        res.status(500).json({ message: 'Server error' });
    }
};



// Verify if media exists
const verify_media_exists = async (req, res) => {
    const { adId } = req.params;

    if (!adId) return res.status(400).json({ message: 'Ad ID is required' });

    try {
        const media = await Media.findOne({ id: adId });
        if (!media) return res.status(404).json({ message: 'Media not found' });

        const bucket = getBucket();

        // Query with a limit and allowDiskUse
        const files = await bucket.find({ filename: adId }, { limit: 1, allowDiskUse: true }).toArray();
        if (files.length === 0) {
            return res.status(404).json({ message: 'File not found in GridFS' });
        }

        const readStream = bucket.openDownloadStreamByName(adId);
        readStream.on('data', () => {}); // Validate stream reads
        readStream.on('error', (err) => {
            console.error('File data validation failed:', err);
            return res.status(500).json({ message: 'File data is corrupted or unreadable' });
        });

        readStream.on('end', () => {
            res.status(200).json({ message: 'File exists and is readable in GridFS' });
        });
    } catch (err) {
        console.error('Error in verify_media_exists:', err);
        res.status(500).json({ message: 'Server error' });
    }
};



module.exports = { upload_media, get_media, delete_media, verify_media_exists };