const moment = require('moment-timezone');

function getCurrentDateTime() {
    // Get current time in Tunisia using moment-timezone
    const tunisiaTime = moment.tz('Africa/Tunis');
    return tunisiaTime.valueOf(); // Get time in milliseconds
}

function convertToMillis(date) {
    // Parse the date string in Tunisia's timezone
    const tunisiaDate = moment.tz(date, 'Africa/Tunis');

    // Return the time in milliseconds
    return tunisiaDate.valueOf();
}



module.exports = {
    getCurrentDateTime,
    convertToMillis
};