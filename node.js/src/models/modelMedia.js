const mongoose = require('mongoose');

const MediaSchema = new mongoose.Schema(
    {
        filename: { type: String, required: true },
        Type: { type: String, required: true },
        adId: { type: mongoose.Schema.Types.ObjectId, ref: 'Ad', required: true },
        createdAt: { type: Date, default: Date.now },
    },
    { 
        collection: 'uploads' 
    } // GridFS uses the 'uploads' collection by convention
);

module.exports = mongoose.model('Media', MediaSchema);
