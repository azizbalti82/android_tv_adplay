const moment = require('moment-timezone');

function getCurrentDateTime() {
    // Get current time in Tunisia using moment-timezone
    const tunisiaTime = moment.tz('Africa/Tunis');
    return tunisiaTime.valueOf(); // Get time in milliseconds
}

function convertToMillis(dateString) {
    const date = new Date(dateString);  // Parse the ISO 8601 string
    return date.getTime();  // Return the time in milliseconds
}



module.exports = {
    getCurrentDateTime,
    convertToMillis
};