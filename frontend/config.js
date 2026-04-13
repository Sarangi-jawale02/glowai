// config.js
// Change this to your deployed backend URL once you deploy to Render.
// If running locally, it defaults to localhost.
const API_BASE_URL = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1'
    ? 'http://localhost:8080'
    : 'https://glowai-backend.onrender.com'; // Wait, let's leave a generic name for now. We can tell them to update it later.

// When deployed, you'll need to update this URL to your actual Render URL.
window.API_BASE_URL = API_BASE_URL;
