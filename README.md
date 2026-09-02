# Phishing Chat User Study

User study: participants judge emails/sites/posts as phishing or legitimate, with an optional chatbot that is sometimes correct and sometimes wrong. Pre- and post-questionnaires are in LimeSurvey.

You must run **two** things at the same time.

**Terminal 1 – backend:**
```bash
cd backend
mvn spring-boot:run
```
Wait until you see `Started PhishingChatBackendApplication`.

**Terminal 2 – frontend:**
```bash
cd frontend
npm install
npm start
```
Wait until it says the app is running.

Open **http://localhost:4200/start?token=test123**

If says “cannot find server”, the frontend is not running or you’re not using `http://localhost:4200`. Start the frontend and use that URL.

## Full explanation and flow

- **Flow:** LimeSurvey pre-questionnaire → study website (4 tasks + chatbot + after-task questions) → LimeSurvey post-questionnaire.
- **Where things run:** Locally = backend on port 8080, frontend on port 4200. For the real study = frontend on **Render**, backend on **Render**, questionnaires on **LimeSurvey**.
- **Chatbot:** Implemented; backend decides correct/wrong per participant+task and returns the answer text.