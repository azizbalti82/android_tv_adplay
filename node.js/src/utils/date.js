function getCurrentDateTime() {
    const now = new Date();

    // Convert to Tunisia's timezone
    const tunisiaTime = new Date(now.toLocaleString('en-US', { timeZone: 'Africa/Tunis' }));

    // Get the current date in YYYY-MM-DD format
    const date = tunisiaTime.toISOString().split('T')[0];

    // Get the current time in HH:mm format
    const time = tunisiaTime.toISOString().split('T')[1].substring(0, 5);

    // Combine the date and time
    return `${date}T${time}`;
}



module.exports = {
    getCurrentDateTime,
};