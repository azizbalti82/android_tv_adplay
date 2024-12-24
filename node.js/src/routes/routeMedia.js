const express = require('express');
const multer = require('multer');
const { upload_media, get_media,delete_media } = require('../controllers/controllerMedia');

const router = express.Router();

// Configure multer
// Use multer memory storage
const storage = multer.memoryStorage();
const upload = multer({ storage });

// Upload media for a specific ad
router.post('/media/:adId', upload.single('file'), upload_media);

// Get media by its ID
router.get('/media/:adId', get_media);

// delete media by its ID
router.delete('/media/:adId', delete_media);

module.exports = router;
