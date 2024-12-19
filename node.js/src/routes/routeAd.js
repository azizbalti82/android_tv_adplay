const express = require('express');
const router = express.Router();
const controllerAd = require('../controllers/controllerAd');
const Ad = require('../models/modelAd');
const multer = require('multer')


const upload=multer({
    dest : 'media'
})


// Route to upload images:
router.post('/ads/media/:adId',upload.single('upload_media'),controllerAd.upload_media)
// Route to get media for an ad:
router.get('/ads/media/:adId', controllerAd.get_media);



// Route to get all ads
router.get('/ads', controllerAd.getAll_ad);

// Route to create a new ad
router.post('/ads', controllerAd.create_ad);

// routes/routeAd.js
//7abit naamlelha implementation fel controller ama ma habetch so aamaltha lenna toul
router.get('/ads/:adId',async (req, res) => {
    try {
        const { adId } = req.params;
        const result = await Ad.findOne({ id: adId }, req.body, { new: true });

        if (!result) {
            return res.status(404).json({ message: 'Ad not found' });
        }

        res.status(200).json({ad: result });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
});

// Route to update an ad by ID
router.put('/ads/:adId', controllerAd.update_ad);

// Route to delete an ad by ID
router.delete('/ads/:adId', controllerAd.delete_ad);


//make this routers usable
module.exports = router;
