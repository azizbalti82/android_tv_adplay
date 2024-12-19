//call the models, because we gonna use them to check if the new generated ID exists already or not
const Device = require('../models/modelDevice');
const DeviceTemp = require('../models/modelDeviceTemp');
const Ad = require('../models/modelAd');
const Schedule = require('../models/modelSchedule');

// Function to generate a unique 6-digit ID for devices
const generateDeviceId = async () => {
    let uniqueId = '';
    let isUnique = false;

    while (!isUnique) {
        uniqueId = Math.floor(100000 + Math.random() * 900000).toString(); // Generate 6-digit number

        // Check if the generated ID already exists in the device collection
        const existingDevice = await Device.findOne({ deviceId: uniqueId });
        const existingDeviceTemp = await DeviceTemp.findOne({ deviceId: uniqueId });

        if (!existingDevice || !existingDeviceTemp) {
            isUnique = true; // ID is unique, exit the loop
        }
    }
    return uniqueId;
};

// Function to generate a unique 6-digit ID for ads
const generateAdId = async () => {
    let uniqueId = '';
    let isUnique = false;

    while (!isUnique) {
        uniqueId = Math.floor(100000 + Math.random() * 900000).toString(); // Generate 6-digit number

        // Check if the generated ID already exists in the ad collection
        const existingAd = await Ad.findOne({ adId: uniqueId });

        if (!existingAd) {
            isUnique = true; // ID is unique, exit the loop
        }
    }
    return uniqueId;
};


// Function to generate a unique 6-digit ID for schedules
const generateScheduleId = async () => {
    let uniqueId = '';
    let isUnique = false;

    while (!isUnique) {
        uniqueId = Math.floor(100000 + Math.random() * 900000).toString(); // Generate 6-digit number

        // Check if the generated ID already exists in the schedule collection
        const existingSchedule = await Schedule.findOne({ scheduleId: uniqueId });

        if (!existingSchedule) {
            isUnique = true; // ID is unique, exit the loop
        }
    }
    return uniqueId;
};


//make these functions accessable when this module is required
module.exports = {
    generateDeviceId,
    generateAdId,
    generateScheduleId
};
