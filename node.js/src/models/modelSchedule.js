const mongoose = require('mongoose');

const ScheduleSchema = new mongoose.Schema({
    id: {
        type: String,
        required: true,
        unique:true
    },
    ad_id: { 
        type: String, 
        required: true 
    },
    device_id: { 
        type: String, 
        required: true 
    },
    start: { 
        type: Date, 
        required: true 
    },
    end: { 
        type: Date, 
        required: true 
    },
    orientation: {
        type: String, 
        enum: ['landscape', 'portrait'], 
        default:'portrait'
    },
    status: {
        type: String, 
        enum: ['in progress', 'completed','waiting'], 
        default: 'waiting'
    }
});

module.exports = mongoose.model('Schedule', ScheduleSchema);
