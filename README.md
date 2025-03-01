# 📺 Android TV Ads Streaming App

## Overview
This project is an **Android TV app** that streams advertisements fetched from a server. It works like a **kiosk**, displaying scheduled ads. A **Node.js web app** with a dashboard allows administrators to manage ads and connected TV devices.

## Features
### 🎬 Android TV App
- Fetches ads from a remote server
- Displays ads in a loop like a kiosk
- Supports various media formats (video, image, etc.)
- Caches ads for offline playback
- Auto-refreshes ads based on updates from the server

### 🌐 Web Dashboard (Node.js + Express + MongoDB)
- Secure **admin login**
- **CRUD operations** for ads (upload, edit, delete)
- **Device management** (register, remove, update TV devices)
- **Ad scheduling** for specific time slots
- **Real-time sync** between server and TV app

## Tech Stack
- **Android TV App**: Kotlin + ExoPlayer
- **Backend**: Node.js + Express
- **Database**: MongoDB
- **Frontend**: React (for admin dashboard)
- **API Communication**: RESTful APIs

## Installation
### 📺 Android TV App
1. Clone the repository
2. Open in Android Studio
3. Run the app on an Android TV emulator or device

### 🌐 Web Dashboard
1. Clone the repository
2. Install dependencies: `npm install`
3. Start the server: `npm start`
4. Access the dashboard at `http://localhost:3000`

## API Endpoints (Example)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/ads` | Fetch all ads |
| POST | `/api/ads` | Upload new ad |
| DELETE | `/api/ads/:id` | Remove ad |
| GET | `/api/devices` | Get all registered TVs |

## Future Improvements
- 📶 **WebSocket support** for real-time ad updates
- 📊 **Analytics dashboard** for ad performance tracking
- 🔑 **OAuth authentication** for admin access

## License
MIT License

---
🚀 **Contributions are welcome!** Feel free to fork and improve the project!



