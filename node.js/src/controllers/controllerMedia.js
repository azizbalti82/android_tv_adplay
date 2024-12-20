const path = require('path');
const fs = require('fs');
const mongoose = require('mongoose');
const Grid = require('gridfs-stream');
const Media = require('../models/modelMedia');
const Ad = require('../models/modelAd');

// Initialize GridFS
let gfs;
const conn = mongoose.connection;
conn.once('open', () => {
    gfs = Grid(conn.db, mongoose.mongo);
    gfs.collection('uploads');
});

// Upload media
const upload_media = async (req, res) => {
    try {
        const { adId } = req.params;

        // Validate adId
        if (!adId) {
            return res.status(400).json({ message: 'Ad ID is required' });
        }

        // Check if media already exists
        const existingMedia = await Media.findOne({ id: adId });
        if (existingMedia) {
            return res.status(400).json({ message: 'Media for this Ad ID already exists' });
        }

        // Handle the uploaded file using multer
        const file = req.file; // Multer provides the file object

        if (!file) {
            return res.status(400).json({ message: 'No file uploaded' });
        }

        // Save metadata to Media collection
        const media = new Media({
            id: adId,
        });

        await media.save();

        // Save file to GridFS with adId as the filename
        const writeStream = gfs.createWriteStream({
            filename: adId, // Save file with adId as the filename
            content_type: file.mimetype,
        });

        writeStream.write(file.buffer); // Write file content
        writeStream.end();

        writeStream.on('close', () => {
            res.status(201).json({ message: 'Media uploaded successfully' });
        });

        writeStream.on('error', (err) => {
            console.error(err);
            res.status(500).json({ message: 'Error uploading file to GridFS' });
        });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};


// Get media
const get_media = async (req, res) => {
    try {
        const { adId } = req.params;

        // Find the media metadata by ID
        const media = await Media.findOne({ id: adId });

        if (!media) {
            return res.status(404).json({ message: 'Media not found' });
        }

        // Fetch the file from GridFS using adId as the filename
        const file = await gfs.files.findOne({ filename: adId });

        if (!file) {
            return res.status(404).json({ message: 'File not found in GridFS' });
        }

        // Set the Content-Type and pipe the file to the response
        res.set('Content-Type', file.contentType);
        const readStream = gfs.createReadStream({ filename: adId });
        readStream.pipe(res);

        readStream.on('error', (err) => {
            console.error(err);
            res.status(500).json({ message: 'Error reading media' });
        });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};


module.exports = { upload_media, get_media };
