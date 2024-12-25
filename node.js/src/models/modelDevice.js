const mongoose = require('mongoose');
const { getCurrentDateTime} = require('../utils/date');

const DeviceSchema = new mongoose.Schema({
    id: {
        type: String,
        required: true,
        unique:true
    },
    name: { 
        type: String, 
        required: true 
    },
    status: { 
        type: String, 
        enum: ['online', 'offline'], 
        default: 'offline' 
    },
    createdAt: { 
        type: Number, 
        default: getCurrentDateTime()
    },
    lastSeen: { 
        type: Number, 
        default: getCurrentDateTime()
    }
});

module.exports = mongoose.model('Device', DeviceSchema);
