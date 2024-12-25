function getCurrentDateTime() {
    const now = new Date();

    now.setHours(now.getHours());

    // Get the current date in YYYY-MM-DD format
    const date = now.toISOString().split('T')[0];

    // Get the current time in HH:mm format
    const time = now.toISOString().split('T')[1].substring(0, 5);

    // Combine the date and time
    return `${date}T${time}`;
}


module.exports = {
    getCurrentDateTime,
};