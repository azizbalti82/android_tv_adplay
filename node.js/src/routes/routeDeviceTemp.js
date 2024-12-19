const express = require('express');
const router = express.Router();
const controllerDevice = require('../controllers/controllerDeviceTemp');

// Route to get all devices
router.get('/temp_devices', controllerDevice.getAllDevices);

// Route to create a new device
router.post('/temp_devices', controllerDevice.createDevice);

// route to get one device
router.get('/temp_devices/:deviceId',controllerDevice.get);

// Route to update a device by ID
router.put('/temp_devices/:deviceId', controllerDevice.updateDevice);

// Route to delete a device by ID
router.delete('/temp_devices/:deviceId', controllerDevice.deleteDevice);


//make this routers usable
module.exports = router;
