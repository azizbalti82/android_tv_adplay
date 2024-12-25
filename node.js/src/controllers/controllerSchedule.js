const Schedule = require('../models/modelSchedule');
const { generateScheduleId } = require('../utils/generateID');
const { getCurrentDateTime ,convertToMillis} = require('../utils/date');

// Controller to create a new schedule
const create_schedule = async (req, res) => {
    try {
        // Generate a unique ID
        const generatedID = await generateScheduleId();
        
        console.log("start schedule text: " + req.body.start);
        // Convert the start and end to milliseconds
        const startMillis = convertToMillis(req.body.start);
        const endMillis = convertToMillis(req.body.end);
        
        // Get the current date time in milliseconds
        const current = getCurrentDateTime();
        
        // Validate start and end dates
        if (startMillis < current || startMillis > endMillis) {
            return res.status(500).json({
                message: "The date must meet the following conditions:\nThe start date must be before the end date.\nThe start date must be later than the current date."
            });
        }
        
        // Check for conflicting schedules
        const conflictingSchedules = await Schedule.find({
            device_id: req.body.device_id,
            $or: [
                { start: { $lte: endMillis }, end: { $gte: startMillis } }, // Overlaps the range
            ],
        });
        
        if (conflictingSchedules.length > 0) {
            return res.status(400).json({
                message: 'There is a conflict with an existing schedule for this device.',
            });
        }
    
        console.log("schedule start: " + startMillis);

        // Create a new schedule with the generated ID
        const newSchedule = new Schedule({
            id: generatedID,
            ad_id: req.body.ad_id,
            device_id: req.body.device_id,
            start: startMillis,
            end: endMillis,
        });

        // Save the schedule to the database
        await newSchedule.save();

        res.status(201).json({ message: 'Schedule created successfully', schedule: newSchedule });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'server error' });
    }
};


// Controller to get all schedules
const getAll_schedules = async (req, res) => {
    try {
        const schedules = await Schedule.find();
        res.status(200).json(schedules);
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};

// Controller to get schedules by a specific device id
const getAll_by_device_schedules = async (req, res) => {
    const { deviceId } = req.params;
    try {
        const schedules = await Schedule.find({ device_id: deviceId }); // Filter by device_id
        res.status(200).json(schedules);
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};

// Controller to update a schedule by ID
const update_schedule = async (req, res) => {
    try {/*
        const { scheduleId } = req.params;
       
        // check if the start and end dates are valid
        current = new Date(getCurrentDateTime());
        timeAlreadyUsed = false
        if (timeAlreadyUsed || new Date(req.body.start) < current || new Date(req.body.start) > new Date(req.body.end)) {
            return res.status(500).json({
                message: 'Date must be: \n1) unique for the current device \n2) start must be before end\n3) start must be greater than the current date'
            });
        }
        
        // Check for conflicting schedules: the period of showing this schedule already taken by other schedule for this device
        // deviceid==this_device_id && start_this>=start_other && end_this<=end_other
        const conflictingSchedules = await Schedule.find({
            device_id: req.body.device_id,
            id: { $ne: scheduleId },
            $or: [
                { start: { $lte: req.body.end }, end: { $gte: req.body.start } }, // Overlaps the range
            ],
        });
        if (conflictingSchedules.length > 0) {
            return res.status(400).json({
                message: 'There is a conflict with an existing schedule for this device.',
            });
        }

        const updatedSchedule = await Schedule.findOneAndUpdate({ id: scheduleId }, req.body, { new: true });
        if (!updatedSchedule) {
            return res.status(404).json({ message: 'Schedule not found' });
        }

        res.status(200).json({ message: 'Schedule updated successfully', schedule: updatedSchedule });
        */
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};

// Controller to delete a schedule by ID
const delete_schedule = async (req, res) => {
    try {
        const { scheduleId } = req.params;
        const deletedSchedule = await Schedule.findOneAndDelete({ id: scheduleId });

        if (!deletedSchedule) {
            return res.status(404).json({ message: 'Schedule not found' });
        }

        res.status(200).json({ message: 'Schedule deleted successfully' });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
};


module.exports = {
    create_schedule,
    getAll_schedules,
    getAll_by_device_schedules,
    update_schedule,
    delete_schedule,
};
