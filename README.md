# Phishing Chat User Study

This repository contains the implementation of a bachelor thesis user study on phishing detection with a fallible AI assistant. In the study, participants evaluate realistic emails, websites, and messages as either phishing or legitimate while receiving support from an embedded chatbot.

The assistant is intentionally imperfect. Depending on the task condition, it can provide advice that matches the ground truth, misleading advice, or a non-committal response for an ambivalent stimulus. The goal of the study is to examine how participants interact with such an assistant, how their final decisions align with its recommendations, and how they perceive its helpfulness, credibility, and trustworthiness.

## Thesis context

This repository was developed as part of a bachelor thesis investigating user interaction with a fallible AI phishing assistant. The study examines descriptive patterns of decision accuracy, confidence, chatbot use, agreement with assistant recommendations, possible overreliance, and post-study perceptions of the assistant.

## Study flow

The study consists of three main parts:

1. **Pre-questionnaire in LimeSurvey**  
   Participants provide demographic information and answer questions about AI use, phishing confidence, previous scam experience, and perceived online safety.

2. **Interactive study website**  
   Participants complete phishing-classification tasks. Each task shows one stimulus and an assistant panel. Participants can read the assistant's initial recommendation, optionally interact with the chatbot, make a final decision, and rate their confidence.

3. **Post-questionnaire in LimeSurvey**  
   Participants evaluate the assistant after completing the tasks, including perceived helpfulness, credibility, trustworthiness, perceived safety, willingness to trust a browser/email integration, and whether they noticed incorrect assistant advice.

The pre-questionnaire, task data, and post-questionnaire are linked using an anonymous participant ID.

For the deployed study, this flow was:

**LimeSurvey pre-questionnaire → study website → LimeSurvey post-questionnaire**

## Repository structure

```text
phishing-chat-study/
├── backend/      # Spring Boot backend for task delivery, assistant control, and data logging
├── frontend/     # Angular frontend for the participant-facing study website
└── README.md
```

## Main components

### Frontend

The frontend is implemented with Angular. It handles the participant-facing study flow, including:

- redirecting participants from and to LimeSurvey
- displaying the phishing-related stimuli
- showing the assistant panel
- collecting final phishing/legitimate decisions
- collecting confidence ratings
- recording whether the assistant was actively used
- collecting task-specific trust ratings after active assistant use

### Backend

The backend is implemented with Spring Boot. It handles:

- participant registration
- task delivery
- assistant behaviour control
- chatbot requests
- storing task decisions and ratings
- logging chat interactions
- generating redirect URLs for LimeSurvey

The backend controls whether the assistant should provide advice that matches the ground truth, contradicts the ground truth, or remains non-committal for the ambivalent stimulus.

### Chatbot

The chatbot uses the OpenAI Chat Completions API when configured. The model used in the study was **gpt-4o-mini**.

The implementation sets:

- `temperature = 0.35`

Other inference parameters, such as `max_tokens` and `top_p`, are not set explicitly and therefore use the API defaults.

If the OpenAI API is not configured or a request fails, the backend can return predefined fallback responses.

The prompt structure, model information, inference settings, and fallback responses are documented in the thesis appendix.

## Running the study locally

You need to run the backend and frontend at the same time.

**Terminal 1: start the backend**

```bash
cd backend
mvn spring-boot:run
```

Wait until the backend has started successfully, for example:

```text
Started PhishingChatBackendApplication
```

By default, the backend runs on `http://localhost:8080`.

**Terminal 2: start the frontend**

```bash
cd frontend
npm install
npm start
```

Wait until Angular reports that the app is running. By default, the frontend runs on `http://localhost:4200`.

Then open the local study start page:

```text
http://localhost:4200/start?token=test123
```

If the browser shows a connection error, make sure the frontend is running and that you are using `http://localhost:4200`.

## Deployment used for the study

For the actual study deployment:

- the frontend was hosted on Render
- the backend was hosted on Render
- the pre- and post-questionnaires were hosted in LimeSurvey

## Data collected

The backend stores study-related data such as:

- anonymous participant ID
- task ID
- task order
- ground truth
- assistant condition
- final participant decision
- confidence rating
- active chatbot use
- task-specific trust rating after chatbot use
- chat messages and assistant responses

No directly identifying information such as names or email addresses is required by the study website.

## Notes on reproducibility

The assistant behaviour is controlled by the backend at the level of recommendation direction. This means that the system determines whether the assistant should argue for a correct, misleading, or non-committal recommendation.

Because chatbot replies are generated dynamically when the OpenAI API is configured, the exact free-text responses may differ between participant interactions. The study therefore controls the direction of the assistant recommendation, but not the exact wording of every generated response.