const mongoose = require('mongoose');

const MediaSchema = new mongoose.Schema(
    {
        id: {
            type: String,
            required: true,
            unique:true
        },
    },
    { 
        collection: 'uploads' 
    } // GridFS uses the 'uploads' collection by convention
);

module.exports = mongoose.model('Media', MediaSchema);
