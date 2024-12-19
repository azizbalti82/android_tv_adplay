const express = require('express');
const router = express.Router();
const controllerDevice = require('../controllers/controllerDevice');

// Route to get all devices
router.get('/devices', controllerDevice.getAllDevices);

// Route to create a new device
router.post('/devices', controllerDevice.createDevice);

// route to get one device
router.get('/devices/:deviceId',controllerDevice.get);

// Route to update a device by ID
router.put('/devices/:deviceId', controllerDevice.updateDevice);

// Route to delete a device by ID
router.delete('/devices/:deviceId', controllerDevice.deleteDevice);


//make this routers usable
module.exports = router;
