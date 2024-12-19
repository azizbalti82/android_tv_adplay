const express = require('express');
const router = express.Router();

const users = [
    { username: 'admin', password: '1234' },
];

// Login route to authenticate user
router.post('/login', (req, res) => {
    const { username, password } = req.body;
    console.log('username is:'+username)
    const user = users.find(u => u.username === username && u.password === password);
    if (user) {
        req.session.user = user; // Store user in session
        res.redirect('/');
    } else {
        res.redirect('/');
    }
});

// Logout route to destroy session
router.post('/logout', (req, res) => {
    req.session.destroy((err) => {
        if (err) {
            console.error('Error destroying session:', err);
            return res.status(500).send('An error occurred while logging out.');
        }
        res.redirect('/'); // Redirect to login page
    });
});


// Make these routes usable
module.exports = router;
