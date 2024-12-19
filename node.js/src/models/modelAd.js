const mongoose = require('mongoose');

const AdSchema = new mongoose.Schema({
    id: {
        type: String,
        required: true,
        unique:true
    },
    title: { 
        type: String, 
        required: true
    },
    description: { 
        type: String,
        default:'N/A'
    },
    type: { 
        type: String, 
        enum: ['image', 'music', 'video'], 
        required: true 
    },
    mediaUrl: { 
        type: String,
        required:true,
        default: ''
    },
    mediaExtension: { 
        type: String,
        required:true,
        default: ''
    },
    createdAt: { 
        type: Date, 
        default: Date.now 
    },
    updatedAt: { 
        type: Date, 
        default: Date.now 
    }
});

module.exports = mongoose.model('Ad', AdSchema);
