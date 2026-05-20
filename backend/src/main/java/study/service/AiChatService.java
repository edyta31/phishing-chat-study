package study.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import study.model.Task;

/**
 * Uses an LLM (OpenAI API) to generate varied, contextual answers for the chatbot.
 * We still control whether the answer should be "correct" or "wrong" for the study
 * via the prompt, so BotPolicyService decides and we ask the AI to argue in that direction.
 */
@Service
public class AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatService.class);
    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4o-mini";
    private static final int MAX_CONTENT_LENGTH = 3000;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${study.openai.api-key:}")
    private String apiKey;

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Generate an answer using the LLM. The answer will suggest the content is either
     * phishing or legitimate depending on shouldBeCorrect and the task's ground truth.
     *
     * @param task             The task (content + ground truth)
     * @param userQuestion     What the participant asked
     * @param shouldBeCorrect  If true, the AI should give the correct assessment (match ground truth);
     *                         if false, the AI should give the wrong assessment (opposite)
     * @return Generated answer text, or null if API is not configured or the call fails
     */
    public String generateAnswer(Task task, String userQuestion, boolean shouldBeCorrect) {
        if (!isConfigured()) return null;

        String groundTruth = task.getGroundTruth() != null ? task.getGroundTruth().toLowerCase() : "legit";
        boolean actuallyPhish = "phish".equals(groundTruth);
        boolean isAmbivalent = "ambivalent".equalsIgnoreCase(groundTruth);
        boolean suggestSuspicious = isAmbivalent ? false : (shouldBeCorrect ? actuallyPhish : !actuallyPhish);

        String contentSnippet = (task.getContentDescription() != null && !task.getContentDescription().isBlank())
                ? task.getContentDescription()
                : plainTextSnippet(task.getPayload());
        String title = task.getTitle() != null ? task.getTitle() : "";

        String kind = task.getKind() != null ? task.getKind() : "content";

        String studyConstraints = "This is a controlled lab study: the participant only has this one static example on screen. "
                + "Do NOT tell them to verify elsewhere, call IT, log in to another site, contact the company, or check an official app or portal—they cannot do that here. "
                + "Do not give generic security homework. Stay within what you can infer from the text shown. ";

        String systemPrompt;
        if (isAmbivalent) {
            systemPrompt = "You are a friendly study assistant. " + studyConstraints
                    + "This example is intentionally mixed: give a short balanced view (1–2 cues that could seem off and 1–2 that could seem ordinary). "
                    + "Do not give a single definitive label. Do not say to verify through other channels. "
                    + "Sound human, 2–4 sentences. Avoid saying you are an AI.";
        } else {
            boolean mustSayPhishing = suggestSuspicious;
            String stance = mustSayPhishing
                    ? "Your answer MUST clearly treat this as **phishing** (malicious / not from a trustworthy sender). Name it directly: say it looks like phishing or a scam attempt. Give 1–2 concrete cues. Be decisive—no hedging with 'might' unless minimal."
                    : "Your answer MUST clearly treat this as **legitimate** (authentic / normal communication). Name it directly: say it looks legitimate or above-board. Give 1–2 plausible benign cues. Be decisive—do not call it suspicious or phishing.";

            String wrongMode = "";
            if (!shouldBeCorrect && !isAmbivalent) {
                wrongMode = " CRITICAL: For this item your stance is intentionally the OPPOSITE of what a careful expert might say about the real world. "
                        + "You must still sound sincere—do not mention that your view is wrong or staged. "
                        + "Do NOT undermine your own conclusion with warnings to double-check elsewhere.";
            }

            systemPrompt = "You are a friendly study assistant. " + studyConstraints + stance + wrongMode
                    + " Reply in 1–4 sentences. Sound like a real person. Avoid saying you are an AI. "
                    + "If they greet you, briefly greet back and give this same stance when they ask about the example. "
                    + "If they ask whether it is phishing or legitimate, answer with those words clearly. "
                    + "Do not write a long analysis.";
        }

        String userMessage = (userQuestion != null && !userQuestion.isBlank()) ? userQuestion.trim() : "Is this phishing or legitimate?";
        String userPrompt = "Example type: " + kind + "\n"
                + "Title: " + title + "\n"
                + "What they are looking at (same scenario as on their screen):\n" + contentSnippet + "\n\n"
                + "Participant message: " + userMessage;

        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", MODEL);
            body.put("temperature", 0.35);
            ArrayNode messages = objectMapper.createArrayNode();
            messages.addObject().put("role", "system").put("content", systemPrompt);
            messages.addObject().put("role", "user").put("content", userPrompt);
            body.set("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + apiKey.trim());

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(OPENAI_URL, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode choices = root.path("choices");
                if (choices.isArray() && choices.size() > 0) {
                    JsonNode msg = choices.get(0).path("message").path("content");
                    if (msg.isTextual()) {
                        return msg.asText().trim();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("OpenAI chat call failed, using canned answer: {}", e.getMessage());
        }
        return null;
    }

    private static String plainTextSnippet(String payload) {
        if (payload == null) return "";
        String text = payload.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
        if (text.length() > MAX_CONTENT_LENGTH) {
            text = text.substring(0, MAX_CONTENT_LENGTH) + "...";
        }
        return text;
    }
}