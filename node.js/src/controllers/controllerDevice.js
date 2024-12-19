const Device = require('../models/modelDevice');
const DeviceTemp = require('../models/modelDeviceTemp');


// Controller to create a new device
const createDevice = async (req, res) => {
    try {
        const temp_device = await DeviceTemp.findOne({ id: req.body.id });
        const device_exist = await Device.findOne({ name: req.body.name });

        if (!temp_device) {
            return res.status(404).json({ message: 'Temp device not found' });  // return to stop further execution
        }

        if (device_exist) {
            return res.status(404).json({ message: 'Device with the same name already exists' });  // return to stop further execution
        }

        // Create a new device with the generated ID
        const newDevice = new Device({
            id: req.body.id,
            name: req.body.name,
        });

        // Save the device to the database
        await newDevice.save();

        // Send a success response
        return res.status(201).json({ message: 'Device created successfully', device: newDevice });

    } catch (err) {
        console.error(err);
        return res.status(500).json({ message: 'Server error' });  // return to stop further execution
    }
};

// Controller to get all devices
const getAllDevices = async (req, res) => {
    try {
        const devices = await Device.find();
        res.status(200).json(devices);
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};

// controller to get one device
const get = async (req, res) => {
    try {
        const { deviceId } = req.params;
        const result = await Device.findOne({ id: deviceId }, req.body, { new: true });

        if (!result) {
            return res.status(404).json({ message: 'device not found' });
        }
        
        res.status(200).json({Device: result });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
}

// Controller to update a device by ID
const updateDevice = async (req, res) => {
    try {
        const { deviceId } = req.params;

        // Check if a device with the same name already exists
        const device_exist = await Device.findOne({ name: req.body.name });
        if (device_exist) {
            return res.status(404).json({ message: 'Device with the same name already exists' });
        }

        // Find and update the device by ID
        const updatedDevice = await Device.findOneAndUpdate(
            { id: deviceId },  // Search criteria (by deviceId)
            req.body,          // The fields to update (from the request body)
            { new: true }      // Return the updated device
        );

        // If no device was found with the given ID
        if (!updatedDevice) {
            return res.status(404).json({ message: 'Device not found' });
        }

        // Successfully updated device
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
        const deletedDevice = await Device.findOneAndDelete({ id: deviceId });  // Use 'id' instead of 'deviceId'

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
