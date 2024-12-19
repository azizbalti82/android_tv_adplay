const mongoose = require('mongoose');

const DeviceTempSchema = new mongoose.Schema({
    id: {
        type: String,
        required: true,
        unique:true
    },
    status: { 
        type: String, 
        enum: ['waiting', 'expired','connected'], 
        default: 'waiting' 
    },
    createdAt: { 
        type: Date, 
        default: Date.now 
    },
});

module.exports = mongoose.model('DeviceTemp', DeviceTempSchema);
