const express = require('express');
const multer = require('multer');
const { upload_media, get_media,delete_media,verify_media_exists } = require('../controllers/controllerMedia');

const router = express.Router();

// Configure multer
const storage = multer.memoryStorage();
const upload = multer({ storage, limits: { fileSize: 200 * 1024 * 1024 } }); // Limit to 50MB

// Upload media for a specific ad
router.post('/media/:adId', upload.single('file'), upload_media);

// Get media by its ID
router.get('/media/:adId', get_media);

// delete media by its ID
router.delete('/media/:adId', delete_media);

// check if media exist
router.get('/media/exist/:adId', verify_media_exists);

module.exports = router;
