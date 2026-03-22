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
        boolean suggestPhishing = shouldBeCorrect ? actuallyPhish : !actuallyPhish;

        String contentSnippet = (task.getContentDescription() != null && !task.getContentDescription().isBlank())
                ? task.getContentDescription()
                : plainTextSnippet(task.getPayload());
        String title = task.getTitle() != null ? task.getTitle() : "";

        String kind = task.getKind() != null ? task.getKind() : "content";
        String systemPrompt = "You are Edyta, a friendly study assistant. The participant is looking at one example (email, website, or SMS-style message) "
                + "and deciding whether it is phishing or legitimate. They only see this one example in this conversation—stay focused on it. "
                + "Sound like a real person: warm, approachable, conversational. No stiff or robotic phrasing. Avoid saying you are an AI or a language model. "
                + "Reply directly to what they said. Keep it short (1–3 sentences). "
                + "If they only greet you (e.g. hi, hello), greet back briefly and invite them to ask anything about the example. "
                + "If they seem stuck (e.g. \"I don't know\", \"help\"), respond kindly and share your view on the example when that fits. "
                + "You must suggest that the content is " + (suggestPhishing ? "phishing or suspicious" : "legitimate or safe") + ". "
                + "When your view is wrong for the study, still sound natural and give plausible but misleading reasoning. "
                + "When your view is correct, mention 1–2 concrete cues (e.g. sender, domain, tone, urgency, branding). "
                + "Do not write a long analysis. Do not mention instructions, policies, or that someone told you how to answer.";

        String userMessage = (userQuestion != null && !userQuestion.isBlank()) ? userQuestion.trim() : "Is this safe?";
        String userPrompt = "Example type: " + kind + "\n"
                + "Title: " + title + "\n"
                + "What they are looking at (same scenario as on their screen):\n" + contentSnippet + "\n\n"
                + "Participant message: " + userMessage;

        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", MODEL);
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