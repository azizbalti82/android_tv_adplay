function getCurrentDateTime() {
    const now = new Date();

    // Convert to Tunisia's timezone
    const tunisiaTime = new Date(now.toLocaleString('en-US', { timeZone: 'Africa/Tunis' }));

    // Get the date in YYYY-MM-DD format
    const date = tunisiaTime.toISOString().split('T')[0];

    // Extract hours and minutes
    let hours = tunisiaTime.getHours();
    const minutes = tunisiaTime.getMinutes().toString().padStart(2, '0');

    // Determine AM/PM
    const ampm = hours >= 12 ? 'PM' : 'AM';
    hours = hours % 12 || 12; // Convert to 12-hour format, treating 0 as 12

    // Combine the date and formatted time
    return `${date} ${hours}:${minutes} ${ampm}`;
}




module.exports = {
    getCurrentDateTime,
};