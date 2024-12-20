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

        // Check if the Ad exists
        const ad = await Ad.findById(adId);
        if (!ad) {
            return res.status(404).json({ message: 'Ad not found' });
        }

        if (!req.file) {
            return res.status(400).json({ message: 'No file uploaded' });
        }

        // Store the file in GridFS
        const writeStream = gfs.createWriteStream({
            filename: req.file.originalname,
            content_type: req.file.mimetype,
        });

        // Pipe the file buffer to GridFS
        fs.createReadStream(req.file.path).pipe(writeStream);

        writeStream.on('close', async (file) => {
            // Save metadata in the Media collection
            const media = new Media({
                filename: file.filename,
                contentType: file.contentType,
                adId: ad._id,
            });
            await media.save();

            // Delete local file
            fs.unlinkSync(req.file.path);

            res.status(200).json({
                message: 'Media uploaded successfully',
                mediaId: media._id,
                filename: file.filename,
            });
        });

        writeStream.on('error', (err) => {
            console.error(err);
            res.status(500).json({ message: 'Failed to upload media' });
        });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};

// Get media
const get_media = async (req, res) => {
    try {
        const { mediaId } = req.params;

        // Find the media metadata by ID
        const media = await Media.findById(mediaId);
        if (!media) {
            return res.status(404).json({ message: 'Media not found' });
        }

        // Fetch the file from GridFS
        const file = await gfs.files.findOne({ filename: media.filename });
        if (!file) {
            return res.status(404).json({ message: 'File not found in GridFS' });
        }

        // Set the Content-Type and pipe the file to the response
        res.set('Content-Type', file.contentType);
        const readStream = gfs.createReadStream({ filename: file.filename });
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
