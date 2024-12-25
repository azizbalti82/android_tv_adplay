function getCurrentDateTime() {
    const now = new Date();

    // Convert to Tunisia's timezone
    const tunisiaTime = new Date(now.toLocaleString('en-US', { timeZone: 'Africa/Tunis' }));

    // Return the time in milliseconds
    return tunisiaTime.getTime();
}



module.exports = {
    getCurrentDateTime,
};