const DeviceTemp = require('../models/modelDeviceTemp');
const { generateDeviceId } = require('../utils/generateID');
const { getCurrentDateTime} = require('../utils/date');

// Controller to create a new device
const createDevice = async (req, res) => {
    try {
        // Generate a unique device ID
        const generatedID = await generateDeviceId();

        // Create a new device with the generated ID
        const newDevice = new DeviceTemp({
            id: generatedID,
            createdAt: getCurrentDateTime()
        });

        // Save the device to the database
        await newDevice.save();

        res.status(201).json({ message: 'Temp Device created successfully', device: newDevice });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};

// Controller to get all devices
const getAllDevices = async (req, res) => {
    try {
        const devices = await DeviceTemp.find();
        res.status(200).json(devices);
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};

// controller to get one temp device
const get = async (req, res) => {
    try {
        const { deviceId } = req.params;
        const result = await DeviceTemp.findOne({ id: deviceId }, req.body, { new: true });

        if (!result) {
            return res.status(404).json({ message: 'device not found' });
        }

        res.status(200).json({DeviceTemp: result });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
}

// Controller to update a device by ID
const updateDevice = async (req, res) => {
    try {
        const { deviceId } = req.params;
        console.log(`Updating device with ID: ${deviceId}`);
        console.log(`Update data: ${JSON.stringify(req.body)}`);
        
        const updatedDevice = await DeviceTemp.findOneAndUpdate(
            { id: deviceId },
            req.body,
            { new: true }
        );

        if (!updatedDevice) {
            return res.status(404).json({ message: 'Device not found' });
        }

        console.log(`Updated device: ${JSON.stringify(updatedDevice)}`);
        res.status(200).json({ message: 'Device updated successfully', device: updatedDevice });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};


// Controller to delete a device by ID
const deleteDevice = async (req, res) => {
    try {
        const { deviceId } = req.params;
        const deletedDevice = await DeviceTemp.findOneAndDelete({ id: deviceId });  // Use 'id' instead of 'deviceId'

        if (!deletedDevice) {
            return res.status(404).json({ message: 'Device not found' });
        }

        res.status(200).json({ message: 'Device deleted successfully' });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};


module.exports = {
    createDevice,
    getAllDevices,
    updateDevice,
    deleteDevice,
    get
};
