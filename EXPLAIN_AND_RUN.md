# How the study works and how to run it

## Why “Safari cannot find server”?

Safari shows that when **nothing is serving the page**. You have to **run both** the backend and the frontend on your computer (or use a deployed URL). If you only open a file or a URL without starting the servers, there is no “server” for Safari to talk to.

- **Backend** = Java/Spring app. You run it with `mvn spring-boot:run` in the `backend` folder. It listens on **http://localhost:8080**.
- **Frontend** = Angular app. You run it with `npm start` in the `frontend` folder. It serves the site on **http://localhost:4200** and sends API calls to the backend (via a proxy).

So: **start both**, then in Safari open **http://localhost:4200/start?token=test123**. Do not use `file://` or a random URL.

---

## The flow: questionnaire → website + questions → questionnaire

Yes. The flow is exactly:

1. **Pre-questionnaire (LimeSurvey)**  
   Participant fills your pre-questionnaire on LimeSurvey. At the end, LimeSurvey redirects them to your **study website** with a token in the URL, e.g.  
   `https://your-site.com/start?token=ABC123`.

2. **Study website (this app)**  
   - **Start page** (`/start?token=...`): The app registers the participant with that token (or resumes if they already started), then sends them to `/study`.
   - **Study page** (`/study`): They see **4 tasks** one after another. For **each task**:
     - They see one item (email / site / post — some phishing, some not).
     - They can **use the chatbot** (top right) to ask questions; the bot sometimes gives a correct answer, sometimes wrong (they don’t know).
     - They choose **Phishing** or **Legitimate** and **confidence (1–5)**, then click **Continue**.
     - They answer **after-task questions** (e.g. “Did you use the chatbot?”, “How much did you trust the bot?” 1–5), then **Next task** or **Finish study**.
   - After the **last** task they are redirected back to LimeSurvey.

3. **Post-questionnaire (LimeSurvey)**  
   They land on your LimeSurvey **post**-questionnaire (same token), e.g.  
   `https://edyta31.limesurvey.net/...?token=ABC123`.

So: **questionnaire (LimeSurvey) → website + tasks + chatbot + after-task questions → questionnaire (LimeSurvey)**.

---

## Where does what run?

### When you are developing / testing on your Mac

| Part        | Where it runs        | URL you use                    |
|------------|----------------------|---------------------------------|
| **Backend**  | Your Mac (Terminal)   | http://localhost:8080 (API only) |
| **Frontend** | Your Mac (Terminal)  | http://localhost:4200 (the site) |

You **must run both**. The frontend at 4200 talks to the backend at 8080 via the proxy (so the browser calls “/api/...” and the dev server forwards that to 8080).

### When you run the real study (participants)

| Part        | Where it runs                    | URL participants use                    |
|------------|-----------------------------------|------------------------------------------|
| **Frontend** | **GitHub Pages** (static files)   | e.g. https://yourusername.github.io/repo-name/ |
| **Backend**  | **Not on GitHub Pages.**          | e.g. https://your-backend.onrender.com  |
| **Pre-/post-questionnaire** | **LimeSurvey** (edyta31.limesurvey.net) | Your LimeSurvey survey URLs             |

- **GitHub Pages** can only serve HTML/CSS/JS (the Angular build). It **cannot** run Java or a database.
- So the **backend** must be hosted somewhere else (e.g. **Render**, Railway, or a university server). You deploy the `backend` project there and get a URL like `https://phishing-study.onrender.com`.
- The **frontend** (built with `npm run build`) is uploaded to GitHub Pages. In the frontend config you set the **backend URL** to that Render (or other) URL so the site calls the real API.

So: **Backend = e.g. Render. Frontend = GitHub Pages. Questionnaires = LimeSurvey.** All three work together.

### Render free tier – can I run the backend there later?

**Yes.** Render has a **free tier** you can use to run your Java backend:

- **What’s free:** You can deploy a **Web Service** (e.g. your Spring Boot app) and a **Postgres** database. No credit card required for the free tier.
- **Limits:**  
  - Free web services **spin down** after about 15 minutes of no traffic; the first request after that can take up to ~1 minute to wake up.  
  - You get about **750 instance-hours per month** across all free services.  
  - **100 GB** outbound bandwidth per month.  
  - Local filesystem is **ephemeral** (wiped on restart); use the Postgres database for anything that must persist.
- **Good for:** Testing, small studies, demos. For a time-limited user study with ~20 participants, this is often enough.
- **How to use it later:** Sign up at [render.com](https://render.com), create a **Web Service** connected to your GitHub repo, set the root directory to `backend`, set build command (e.g. `mvn -DskipTests package`) and start command (e.g. `java -jar target/phishing-chat-backend-0.0.1-SNAPSHOT.jar`), add a Postgres database and set `SPRING_DATASOURCE_*` and `STUDY_LIMESURVEY_POST_URL` (and `STUDY_OPENAI_API_KEY` if you use the AI chatbot) in the service’s **Environment** tab.

So you can run the backend on Render later; the frontend on GitHub Pages will call that backend URL.

---

## Is the chatbot implemented? How does it work?

Yes. The chatbot is implemented and works like this:

1. **Frontend (Angular)**  
   - The user opens the assistant (top right), types a question, clicks Send.
   - The app sends a request to the backend: `POST /api/chat` with `trialId`, `token`, and the question text (`userText`).

2. **Backend (Spring)**  
   - `StudyController.chat()`:
     - Loads the current trial and task.
     - Asks **BotPolicyService**: “For this participant and this task, should the bot answer **correctly** or **wrong**?”  
       The service uses the participant’s **condition** (e.g. `balanced_50`, `mostly_correct_80`, `mostly_wrong_20`) and a deterministic “random” so the same person+task always gets the same correctness.
     - **Answer text:**
       - If an **OpenAI API key** is set (`study.openai.api-key`), the backend uses **AiChatService** to call an LLM (e.g. GPT-4o-mini). The prompt includes the **task content** and the **user’s question**, and instructs the model to answer in a way that suggests the content is either “phishing/suspicious” or “legitimate/safe” depending on whether we want a correct or wrong answer. So you get **varied, contextual answers** (different wording for different emails/sites/questions), but we still **control** correct vs wrong for the study.
       - If no API key is set, the backend falls back to short **canned answers** (`craftAnswer(task, correct)`): one of four fixed sentences. The study still works; answers are just less varied.
     - Saves the user message and the bot reply in the database (ChatTurn, and on the Trial: botShown, botAnswerCorrect, botAnswerText).
     - Returns the answer text to the frontend.

3. **Frontend again**  
   - The reply is shown in the chatbot. The user never sees whether the bot was “correct” or “wrong”; they only see the text.

So: **Chatbot is implemented.** With an API key you get **AI-generated, scenario-specific answers**; without it you get **fixed canned answers**. You can check it works by running backend + frontend, going through a task, opening the assistant, asking something, and seeing a reply.

### Enabling the AI chatbot (varied answers)

1. Get an **OpenAI API key** at [platform.openai.com/api-keys](https://platform.openai.com/api-keys). (Usage is paid per token; small studies are usually a few dollars.)
2. In `backend/src/main/resources/application.properties` add (or uncomment):
   ```properties
   study.openai.api-key=sk-your-actual-key-here
   ```
   Or set the environment variable `STUDY_OPENAI_API_KEY` when you run the backend (e.g. on Render).
3. Restart the backend. The next time a participant asks the chatbot something, the backend will call the LLM with the task content and the user’s question, and instruct it to argue in the “correct” or “wrong” direction. You get different answers for different scenarios and questions; we still control correct vs wrong for your study design.

---

## How to check that it works (step by step)

Do this on your Mac, in two terminals.

### 1. Start the backend

Open **Terminal**, go to the project, start the backend:

```bash
cd /Users/edyta/IdeaProjects/phishing-chat-study/backend
mvn spring-boot:run
```

Wait until you see something like: `Started PhishingChatBackendApplication`. Leave this terminal open.

### 2. Start the frontend

Open a **second** Terminal window:

```bash
cd /Users/edyta/IdeaProjects/phishing-chat-study/frontend
npm install
npm start
```

Wait until it says the app is running and shows something like `http://localhost:4200`. Leave this open too.

### 3. Open the study in Safari

In Safari, go to:

**http://localhost:4200/start?token=test123**

(Use that exact URL: `localhost`, port **4200**, path **/start?token=test123**.)

You should see “Starting study…” then the first task (e.g. an email).

### 4. Test the chatbot

- On the task page, click **“Open assistant”** (top right).
- Type a question, e.g. “Is this email safe?”
- Click **Send**.
- You should see a reply from the bot in the chat. That confirms the chatbot works.

### 5. Test the rest of the flow

- Choose **Phishing** or **Legitimate** and set confidence.
- Click **Continue**.
- Answer the two after-task questions (use chatbot? trust 1–5).
- Click **Next task** (or **Finish study** on the last one).
- After the last task, you should be redirected to the LimeSurvey URL you set in the backend (`study.limesurvey.post-url`). If that URL is still a placeholder, the redirect might go to a non-existent page; that’s a config issue, not a bug in the flow.

If at step 3 Safari says “cannot find server”, either the frontend is not running (no `npm start`) or you’re not using **http://localhost:4200**. Fix that and try again.

---

## Short summary

- **Safari cannot find server** → Start both backend and frontend, then open **http://localhost:4200/start?token=test123**.
- **Flow:** LimeSurvey pre-questionnaire → study website (4 tasks, chatbot, decisions, after-task questions) → LimeSurvey post-questionnaire.
- **Where things run:** Locally = backend (8080) + frontend (4200). For the real study = frontend on **GitHub Pages**, backend on e.g. **Render**, questionnaires on **LimeSurvey**.
- **Chatbot:** Implemented; backend decides correct/wrong per participant+task and returns the right text; you can verify by opening the assistant and sending a message.
