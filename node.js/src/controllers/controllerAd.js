const Ad = require('../models/modelAd');
const { generateAdId } = require('../utils/generateID');
const Schedule = require('../models/modelSchedule');

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

        //const deletedSchedules = await Schedule.deleteMany({ ad_id: adId });
        // Delete schedules related to those devices (all schedules contains this id)
        const deletedSchedules = await Schedule.deleteMany({ ad_id: { $regex: adId, $options: "i" } });
        console.log(`${deletedSchedules.deletedCount} schedules deleted for ad ID: ${adId}`);
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
};
