const mongoose = require('mongoose');

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
        required: true,
    },
    lastSeen: { 
        type: Number, 
        required: true,
    }
});

module.exports = mongoose.model('Device', DeviceSchema);
