const Ad = require('../models/modelAd');
const { generateAdId } = require('../utils/generateID');
const path = require('path'); // Import path module to handle file extensions

// Controller to create a new ad
const create_ad = async (req, res) => {
    try {
        // Generate a unique ID
        const generatedID = await generateAdId();

        // Create a new ad with the generated ID
        const newAd = new Ad({
            id:generatedID,
            title: req.body.title,
            description: req.body.description,
            type: req.body.type,
            mediaUrl: req.body.mediaUrl,
            mediaExtension: req.body.mediaExtension,
        });

        // Save the ad to the database
        await newAd.save();

        res.status(201).json({ message: 'Ad created successfully', ad: newAd });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};

// Controller to get all ads
const getAll_ad = async (req, res) => {
    try {
        const ads = await Ad.find();
        res.status(200).json(ads);
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};

// Controller to update an ad by ID
const update_ad = async (req, res) => {
    try {
        const { adId } = req.params;
        const updatedAd = await Ad.findOneAndUpdate({ id: adId }, req.body, { new: true });

        if (!updatedAd) {
            return res.status(404).json({ message: 'Ad not found' });
        }

        res.status(200).json({ message: 'Ad updated successfully', ad: updatedAd });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};

// Controller to delete an ad by ID
const delete_ad = async (req, res) => {
    try {
        const { adId } = req.params;
        const deletedAd = await Ad.findOneAndDelete({ id: adId });

        if (!deletedAd) {
            return res.status(404).json({ message: 'Ad not found' });
        }

        res.status(200).json({ message: 'Ad deleted successfully' });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};



// Controller to upload a media file

const upload_media = async (req, res) => {
    try {
        const { adId } = req.params;

        // First, check if the Ad exists
        const ad = await Ad.findOne({ id: adId });
        if (!ad) {
            return res.status(404).json({ message: 'Ad not found' });
        }

        // Check if a file was uploaded
        if (!req.file) {
            return res.status(400).json({ message: 'No file uploaded' });
        }

        // Create a new media entry with the uploaded file URL
        const mediaUrl = `/media/${req.file.filename}`; // The URL path to the uploaded media

        // Link the media to the ad and store the file extension
        ad.mediaUrl = mediaUrl;

        // Update the updatedAt timestamp
        ad.updatedAt = Date.now();

        // Save the ad with the new media information
        await ad.save();

        res.status(200).json({ message: 'Media uploaded successfully', mediaUrl: mediaUrl });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};

// Controller to delete an ad by ID
const get_media = async (req, res) => {
    try {
        const { adId } = req.params;

        // Find the ad by its ID
        const ad = await Ad.findOne({ id: adId });
        if (!ad) {
            return res.status(404).json({ message: 'Ad not found' });
        }

        // Extract the file extension from the media URL or mediaExtension field
        const fileExtension = ad.mediaExtension;

        // Set the appropriate Content-Type based on the file extension
        if (fileExtension === '.jpg' || fileExtension === '.jpeg' || fileExtension === '.png' || fileExtension === '.gif') {
            res.set('Content-Type', 'image/' + fileExtension.slice(1));
        } else if (fileExtension === '.mp4' || fileExtension === '.mkv' || fileExtension === '.avi' || fileExtension === '.webm') {
            res.set('Content-Type', 'video/' + fileExtension.slice(1));
        } else if (fileExtension === '.mp3' || fileExtension === '.wav' || fileExtension === '.ogg' || fileExtension === '.flac') {
            res.set('Content-Type', 'audio/' + fileExtension.slice(1));
        } else {
            return res.status(415).json({ message: 'Unsupported media type' });
        }

        // Send the media file as the response
        res.status(200).sendFile(path.join(__dirname, '..', ad.mediaUrl));
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};



module.exports = {
    create_ad,
    getAll_ad,
    update_ad,
    delete_ad,
    upload_media,
    get_media,
};
