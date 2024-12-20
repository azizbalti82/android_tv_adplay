const express = require('express');
const multer = require('multer');
const { upload_media, get_media } = require('../controllers/controllerMedia');

const router = express.Router();

// Configure multer
const upload = multer({ dest: 'uploads/' });

// Upload media for a specific ad
router.post('/media/:adId', upload.single('file'), upload_media);

// Get media by its ID
router.get('/media/:adId', get_media);

module.exports = router;
