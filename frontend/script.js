/* ============================================
   GLOW AI - FIXED JAVASCRIPT
   Clean rewrite - no mock data, real API call
============================================ */

// ============================================
// GLOBAL STATE
// ============================================
let currentPage = 'home';
let uploadedImage = null;
let analysisResults = null;

// ============================================
// PAGE NAVIGATION
// Gets elements only when needed (lazy)
// This avoids the "null" crash on startup
// ============================================
function showPage(pageName) {
    // Hide all pages
    document.getElementById('home-page').classList.remove('active');
    document.getElementById('analysis-page').classList.remove('active');
    document.getElementById('results-page').classList.remove('active');

    // Show the requested page
    if (pageName === 'home')     document.getElementById('home-page').classList.add('active');
    if (pageName === 'analysis') document.getElementById('analysis-page').classList.add('active');
    if (pageName === 'results')  document.getElementById('results-page').classList.add('active');

    currentPage = pageName;
    window.scrollTo(0, 0);
}

function updateProgressBar(step) {
    const steps = document.querySelectorAll('.progress-step');
    steps.forEach((el, index) => {
        if (index < step) el.classList.add('active');
        else el.classList.remove('active');
    });
}

// ============================================
// IMAGE UPLOAD
// ============================================
document.getElementById('upload-area').addEventListener('click', () => {
    document.getElementById('image-upload').click();
});

document.getElementById('image-upload').addEventListener('change', (event) => {
    const file = event.target.files[0];

    if (!file || !file.type.startsWith('image/')) {
        alert('Please select a valid image file!');
        return;
    }

    if (file.size > 5 * 1024 * 1024) {
        alert('File too large! Please upload under 5MB.');
        return;
    }

    const reader = new FileReader();
    reader.onload = (e) => {
        uploadedImage = e.target.result; // base64 string

        document.getElementById('preview-image').src = uploadedImage;
        document.getElementById('upload-area').style.display = 'none';
        document.getElementById('preview-area').style.display = 'block';

        updateProgressBar(2);
        checkFormValidity();
    };
    reader.readAsDataURL(file);
});

document.getElementById('change-image-btn').addEventListener('click', () => {
    document.getElementById('upload-area').style.display = 'flex';
    document.getElementById('preview-area').style.display = 'none';
    uploadedImage = null;
    document.getElementById('image-upload').value = '';
    checkFormValidity();
});

// ============================================
// FORM VALIDATION
// ============================================
function checkFormValidity() {
    const isImageUploaded = uploadedImage !== null;
    const age = document.getElementById('age').value;
    const skinType = document.getElementById('skin-type').value;
    const concern = document.getElementById('primary-concern').value;

    const isValid = isImageUploaded && age !== '' && age >= 13 && skinType !== '' && concern !== '';
    document.getElementById('analyze-btn').disabled = !isValid;
}

document.getElementById('age').addEventListener('input', checkFormValidity);
document.getElementById('skin-type').addEventListener('change', checkFormValidity);
document.getElementById('primary-concern').addEventListener('change', checkFormValidity);

// ============================================
// SLIDER LABELS
// ============================================
document.getElementById('water-intake').addEventListener('input', (e) => {
    document.querySelector('.water-value').textContent = `${e.target.value} glasses`;
});

document.getElementById('stress-level').addEventListener('input', (e) => {
    const levels = { '1': 'Very Low', '2': 'Low', '3': 'Medium', '4': 'High', '5': 'Very High' };
    document.querySelector('.stress-value').textContent = levels[e.target.value];
});

// ============================================
// LOADING STATE
// ============================================
function showLoadingState(isLoading) {
    const btn = document.getElementById('analyze-btn');
    const btnText = document.getElementById('analyze-btn-text');
    const spinner = document.getElementById('analyze-spinner');

    if (isLoading) {
        btnText.style.display = 'none';
        spinner.style.display = 'block';
        btn.disabled = true;
    } else {
        btnText.style.display = 'block';
        spinner.style.display = 'none';
        btn.disabled = false;
    }
}

// ============================================
// REAL API CALL TO SPRING BOOT BACKEND
// ============================================
async function fetchSkinAnalysis(imageData, formData) {
    const response = await fetch(API_BASE_URL + '/api/analyze', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            image: imageData,
            age: formData.age,
            skinType: formData.skinType,
            primaryConcern: formData.primaryConcern,
            waterIntake: formData.waterIntake,
            stressLevel: formData.stressLevel
        })
    });

    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`API error ${response.status}: ${errorText}`);
    }

    return await response.json();
}

// ============================================
// FORM SUBMISSION
// ============================================
document.getElementById('analysis-form').addEventListener('submit', async (event) => {
    event.preventDefault();

    const formData = {
        age: document.getElementById('age').value,
        skinType: document.getElementById('skin-type').value,
        primaryConcern: document.getElementById('primary-concern').value,
        waterIntake: document.getElementById('water-intake').value,
        stressLevel: document.getElementById('stress-level').value
    };

    showLoadingState(true);
    updateProgressBar(3);

    try {
        const results = await fetchSkinAnalysis(uploadedImage, formData);
        analysisResults = results;
        showLoadingState(false);
        displayResults(results);
        showPage('results');
    } catch (error) {
        showLoadingState(false);
        updateProgressBar(2);
        console.error('Analysis error:', error);
        alert('Analysis failed. Make sure your Spring Boot backend is running on port 8080.\n\nError: ' + error.message);
    }
});

// ============================================
// DISPLAY RESULTS
// ============================================
function displayResults(results) {
    // Show the uploaded photo
    document.getElementById('results-image').src = uploadedImage;

    // Confidence score
    document.getElementById('confidence-score').textContent = `${results.confidence}%`;

    // Diagnosis list
    const diagnosisList = document.getElementById('diagnosis-list');
    diagnosisList.innerHTML = '';
    results.diagnosis.forEach(issue => {
        const item = document.createElement('div');
        item.className = 'diagnosis-item';
        item.innerHTML = `<span class="diagnosis-icon">🔴</span><span class="diagnosis-text">${issue}</span>`;
        diagnosisList.appendChild(item);
    });

    // Severity
    document.getElementById('severity-indicator').innerHTML = `
        <span class="severity-badge severity-${results.severity.toLowerCase()}">${results.severity}</span>
    `;

    // Morning routine
    const morningList = document.getElementById('morning-routine');
    morningList.innerHTML = '';
    results.routine.morning.forEach(step => {
        const li = document.createElement('li');
        li.textContent = step;
        morningList.appendChild(li);
    });

    // Evening routine
    const eveningList = document.getElementById('evening-routine');
    eveningList.innerHTML = '';
    results.routine.evening.forEach(step => {
        const li = document.createElement('li');
        li.textContent = step;
        eveningList.appendChild(li);
    });
}

// ============================================
// NAVIGATION BUTTONS
// ============================================
document.getElementById('start-analysis-btn').addEventListener('click', () => {
    showPage('analysis');
});

document.getElementById('back-to-home').addEventListener('click', () => {
    showPage('home');
});

document.getElementById('back-to-analysis').addEventListener('click', () => {
    // Reset form and go back
    document.getElementById('analysis-form').reset();
    document.getElementById('upload-area').style.display = 'flex';
    document.getElementById('preview-area').style.display = 'none';
    uploadedImage = null;
    document.getElementById('image-upload').value = '';
    checkFormValidity();
    showPage('analysis');
});

document.getElementById('start-new-analysis-btn').addEventListener('click', () => {
    document.getElementById('analysis-form').reset();
    document.getElementById('upload-area').style.display = 'flex';
    document.getElementById('preview-area').style.display = 'none';
    uploadedImage = null;
    document.getElementById('image-upload').value = '';
    checkFormValidity();
    showPage('analysis');
});

// ============================================
// DOWNLOAD REPORT
// ============================================
document.getElementById('download-results-btn').addEventListener('click', () => {
    if (!analysisResults) return;

    const report = `
GLOW AI - SKIN ANALYSIS REPORT
================================

DETECTED ISSUES:
${analysisResults.diagnosis.map(i => `- ${i}`).join('\n')}

SEVERITY: ${analysisResults.severity}
AI CONFIDENCE: ${analysisResults.confidence}%

MORNING ROUTINE:
${analysisResults.routine.morning.map((s, i) => `${i + 1}. ${s}`).join('\n')}

EVENING ROUTINE:
${analysisResults.routine.evening.map((s, i) => `${i + 1}. ${s}`).join('\n')}

DISCLAIMER: For informational purposes only. Consult a dermatologist for medical advice.
    `.trim();

    const blob = new Blob([report], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'GlowAI_Report.txt';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
});

// ============================================
// CHATBOT
// ============================================
document.getElementById('chatbot-toggle').addEventListener('click', () => {
    const window_ = document.getElementById('chatbot-window');
    const isOpen = window_.style.display === 'flex';

    if (isOpen) {
        window_.style.display = 'none';
        document.querySelector('.chatbot-icon').style.display = 'block';
        document.querySelector('.chatbot-close-icon').style.display = 'none';
    } else {
        window_.style.display = 'flex';
        document.querySelector('.chatbot-icon').style.display = 'none';
        document.querySelector('.chatbot-close-icon').style.display = 'block';
    }
});

document.getElementById('minimize-chat').addEventListener('click', () => {
    document.getElementById('chatbot-window').style.display = 'none';
    document.querySelector('.chatbot-icon').style.display = 'block';
    document.querySelector('.chatbot-close-icon').style.display = 'none';
});

function addChatMessage(text, sender) {
    const messages = document.getElementById('chat-messages');
    const div = document.createElement('div');
    div.className = `chat-message ${sender}-message`;
    const time = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    div.innerHTML = `<div class="message-content">${text}</div><div class="message-time">${time}</div>`;
    messages.appendChild(div);
    messages.scrollTop = messages.scrollHeight;
}

async function getBotResponse(message) {
    try {
        const response = await fetch(API_BASE_URL + '/api/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ message: message })
        });
        const data = await response.json();
        return data.reply;
    } catch (error) {
        return "Sorry, I couldn't connect to the server. Make sure the backend is running! 😊";
    }
}

async function sendChatMessage(message) {
    if (!message.trim()) return;

    addChatMessage(message, 'user');
    document.getElementById('chat-input').value = '';

    // Show typing indicator
    const typingDiv = document.createElement('div');
    typingDiv.className = 'chat-message bot-message';
    typingDiv.id = 'typing-indicator';
    typingDiv.innerHTML = '<div class="message-content">GlowBot is typing... 💭</div>';
    document.getElementById('chat-messages').appendChild(typingDiv);
    document.getElementById('chat-messages').scrollTop =
        document.getElementById('chat-messages').scrollHeight;

    // Get real AI response
    const reply = await getBotResponse(message);

    // Remove typing indicator
    const typing = document.getElementById('typing-indicator');
    if (typing) typing.remove();

    addChatMessage(reply, 'bot');
}



document.getElementById('send-message').addEventListener('click', () => {
    sendChatMessage(document.getElementById('chat-input').value);
});

document.getElementById('chat-input').addEventListener('keypress', (e) => {
    if (e.key === 'Enter') sendChatMessage(document.getElementById('chat-input').value);
});

document.querySelectorAll('.quick-question').forEach(btn => {
    btn.addEventListener('click', () => sendChatMessage(btn.dataset.question));
});

// ============================================
// INIT — runs when page loads
// ============================================
document.addEventListener('DOMContentLoaded', () => {
    showPage('home');
    checkFormValidity();
    console.log('GlowAI loaded successfully! ✅');
    console.log('Backend expected at:', API_BASE_URL);
});