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
        const writeStream = bucket.openUploadStream(adId, { contentType: file.mimetype });

        writeStream.end(file.buffer);

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

// Get media
const get_media = async (req, res) => {
    const { adId } = req.params;

    if (!adId) return res.status(400).json({ message: 'Ad ID is required' });

    try {
        const media = await Media.findOne({ id: adId });
        if (!media) return res.status(404).json({ message: 'Media not found' });

        const bucket = getBucket();
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

module.exports = { upload_media, get_media };