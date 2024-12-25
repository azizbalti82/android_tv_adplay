const moment = require('moment-timezone');

function getCurrentDateTime() {
    // Get current time in Tunisia using moment-timezone
    const tunisiaTime = moment.tz('Africa/Tunis');
    return tunisiaTime.valueOf(); // Get time in milliseconds
}

function convertToMillis(date) {
    return new Date(date).getTime();
}



module.exports = {
    getCurrentDateTime,
    convertToMillis
};