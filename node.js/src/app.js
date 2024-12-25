const path = require('path')
const express = require('express')
const mongoose = require('mongoose')
const session = require('express-session'); // Add express-session for session management
const bodyParser = require('body-parser'); // Add body-parser for parsing form data

const routDevice = require('./routes/routeDevice')
const routDeviceTemp = require('./routes/routeDeviceTemp')
const routAd = require('./routes/routeAd')
const routSchedule = require('./routes/routeSchedule')
const routLogin = require('./routes/routeLogin')
const routMedia = require('./routes/routeMedia')

const {getCurrentDateTime} = require('./utils/date')

//initialisation ---------------------------------------------------------------------------------------------------
//inisialise server
const app = express()
// Initialize session middleware
app.use(
    session({
        secret: 'a0a1a2a3a4a5', // Replace with a strong secret key
        resave: false,
        saveUninitialized: true,
        cookie: { secure: false }, // Set `true` if using HTTPS
    })
);
// Middleware to parse JSON
app.use(express.json());
// Body parser middleware to parse JSON and URL-encoded data
app.use(bodyParser.json()); // To parse JSON request bodies
app.use(bodyParser.urlencoded({ extended: true })); // To parse URL-encoded form data
// Serve static files from 'views/javascript' folder
app.use('/javascript', express.static(path.join(__dirname, 'views', 'javascript')));
app.use('/images', express.static(path.join(__dirname, 'views', 'images')));

// MongoDB connection
const MONGO_URI ='mongodb+srv://azizbalti28:6Ry6oMWorQ7noiRu@projectad.vdyro.mongodb.net/?retryWrites=true&w=majority&appName=projectAd'

mongoose.connect(MONGO_URI)
.then(() => console.log('Connected to MongoDB'))
.catch((err) => console.error('Error connecting to MongoDB:', err));

//functions ---------------------------------------------------------------------------------------------------------
//verify if this user is loged in
const verify_login = (req, callback) => {
    if (req.session.user) {
        callback(true);
    } else {
        callback(false);
    }
};

// Set up Handlebars engine and views location
app.set('view engine', 'hbs')

//connect all routs with the main server
app.use(routDevice);
app.use(routAd);
app.use(routSchedule);
app.use(routDeviceTemp);
app.use(routLogin);
app.use(routMedia);

//routes ------------------------------------------------------------------------------------------------------------
app.get('/date', (req, res) => {  
    res.send(getCurrentDateTime().toString());
});


//root route
app.get('', (req, res) => {
    verify_login(req, (result) => {
        if (result) {
            return res.render('index');
        } else {
            return res.render('login');
        }
    });
});


//for all unefined routes
app.get('*',(req,res)=>{
    res.render('404')
})

//start the server
app.listen(3000, () => {
    console.log('Server is up on port 3000.')
})