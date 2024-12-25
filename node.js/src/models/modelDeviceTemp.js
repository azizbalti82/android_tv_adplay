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
        type: Number, 
        required: true,
    },
});

module.exports = mongoose.model('DeviceTemp', DeviceTempSchema);
