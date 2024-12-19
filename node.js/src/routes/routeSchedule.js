const express = require('express');
const router = express.Router();
const controllerSchedule = require('../controllers/controllerSchedule');
const Schedule = require('../models/modelSchedule');

// Route to get all schedules
router.get('/schedules', controllerSchedule.getAll_schedules);

// Route to get all schedules for a specific deviceID
router.get('/schedules/search/device/:deviceId', controllerSchedule.getAll_by_device_schedules);

//7abit naamlelha implementation fel controller ama ma habetch so aamaltha lenna toul
router.get('/schedules/:scheduleId',async (req, res) => {
    try {
        const { scheduleId } = req.params;
        const result = await Schedule.findOne({ id: scheduleId }, req.body, { new: true });

        if (!result) {
            return res.status(404).json({ message: 'schedule not found' });
        }

        res.status(200).json({Schedule: result });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Server error' });
    }
});

// Route to create a new schedule
router.post('/schedules', controllerSchedule.create_schedule);




// Route to update a schedule by ID
router.put('/schedules/:scheduleId', controllerSchedule.update_schedule);

// Route to delete a schedule by ID
router.delete('/schedules/:scheduleId', controllerSchedule.delete_schedule);

// Make these routes usable
module.exports = router;
