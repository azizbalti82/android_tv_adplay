const mongoose = require('mongoose');
const { getCurrentDateTime} = require('../utils/date');

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
        default: getCurrentDateTime()
    },
});

module.exports = mongoose.model('DeviceTemp', DeviceTempSchema);
