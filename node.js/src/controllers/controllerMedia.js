const mongoose = require('mongoose');
const { GridFSBucket } = require('mongodb');
const Media = require('../models/modelMedia');

// Initialize GridFSBucket with options
const getBucket = () => new GridFSBucket(mongoose.connection.db, { 
    bucketName: 'uploads',
    chunkSizeBytes: 255000,
    // Add these options for better sorting handling
    readPreference: 'primary',
    writeConcern: { w: 'majority' }
});

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
        const writeStream = bucket.openUploadStream(adId, {
            contentType: file.mimetype,
            metadata: { uploadDate: new Date() }
        });

        writeStream.end(file.buffer);

        await new Promise((resolve, reject) => {
            writeStream.on('finish', resolve);
            writeStream.on('error', reject);
        });

        await new Media({ id: adId }).save();
        res.status(201).json({ message: 'Media uploaded successfully' });
    } catch (err) {
        console.error('Error in upload_media:', err);
        res.status(500).json({ message: 'Server error' });
    }
};

const get_media = async (req, res) => {
    const { adId } = req.params;

    if (!adId) return res.status(400).json({ message: 'Ad ID is required' });

    try {
        const bucket = getBucket();
        
        // Find the file metadata first
        const files = await bucket.find({ filename: adId })
            .limit(1)
            .toArray();
        
        if (files.length === 0) {
            return res.status(404).json({ message: 'File not found in GridFS' });
        }

        const file = files[0];

        // Set headers
        res.set('Content-Type', file.contentType);
        res.set('Content-Length', file.length);
        
        // Create download stream with sort optimization
        const downloadStream = bucket.openDownloadStreamByName(adId, {
            sort: { n: 1 },  // Sort chunks by number
            disableMD5: true // Disable MD5 verification for better performance
        });

        // Handle errors before piping
        downloadStream.on('error', (err) => {
            console.error('Streaming error:', err);
            if (!res.headersSent) {
                res.status(500).json({ message: 'Error streaming file' });
            }
        });

        // Pipe the stream with error handling
        downloadStream.pipe(res).on('error', (err) => {
            console.error('Pipe error:', err);
            if (!res.headersSent) {
                res.status(500).json({ message: 'Error streaming file' });
            }
        });

    } catch (err) {
        console.error('Error in get_media:', err);
        if (!res.headersSent) {
            res.status(500).json({ message: 'Server error' });
        }
    }
};

// Delete media with proper cleanup
const delete_media = async (req, res) => {
    const { adId } = req.params;

    if (!adId) return res.status(400).json({ message: 'Ad ID is required' });

    try {
        const media = await Media.findOne({ id: adId });
        if (!media) return res.status(404).json({ message: 'Media not found' });

        const bucket = getBucket();
        
        // Use cursor for efficient querying
        const cursor = bucket.find({ filename: adId }).limit(1);
        const files = await cursor.toArray();
        
        if (files.length === 0) {
            return res.status(404).json({ message: 'File not found in GridFS' });
        }

        await bucket.delete(files[0]._id);
        await Media.deleteOne({ id: adId });

        res.status(200).json({ message: 'Media deleted successfully' });
    } catch (err) {
        console.error('Error in delete_media:', err);
        res.status(500).json({ message: 'Server error' });
    }
};

// Verify media exists with optimized validation
const verify_media_exists = async (req, res) => {
    const { adId } = req.params;

    if (!adId) return res.status(400).json({ message: 'Ad ID is required' });

    try {
        const media = await Media.findOne({ id: adId });
        if (!media) return res.status(404).json({ message: 'Media not found' });

        const bucket = getBucket();
        
        // Use cursor for efficient querying
        const cursor = bucket.find({ filename: adId }).limit(1);
        const files = await cursor.toArray();
        
        if (files.length === 0) {
            return res.status(404).json({ message: 'File not found in GridFS' });
        }

        // Just check file metadata instead of reading the stream
        res.status(200).json({ 
            message: 'File exists in GridFS',
            metadata: {
                size: files[0].length,
                contentType: files[0].contentType,
                uploadDate: files[0].uploadDate
            }
        });
    } catch (err) {
        console.error('Error in verify_media_exists:', err);
        res.status(500).json({ message: 'Server error' });
    }
};

module.exports = { upload_media, get_media, delete_media, verify_media_exists };