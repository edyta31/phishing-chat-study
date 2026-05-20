package study.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import study.controller.dto.*;
import study.model.ChatTurn;
import study.model.Participant;
import study.model.Task;
import study.model.Trial;
import study.repository.ChatTurnRepository;
import study.repository.ParticipantRepository;
import study.repository.TaskRepository;
import study.repository.TrialRepository;
import study.service.AiChatService;
import study.service.BotPolicyService;

import java.net.URLEncoder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class StudyController {
    @Autowired
    ParticipantRepository participants;
    @Autowired
    TaskRepository tasks;
    @Autowired
    TrialRepository trials;
    @Autowired
    ChatTurnRepository chats;
    @Autowired
    BotPolicyService botPolicy;
    @Autowired
    AiChatService aiChatService;

    /** Base URL for LimeSurvey post-questionnaire */
    @Value("${study.limesurvey.post-url:https://example.com/limesurvey/index.php/123456?token=}")
    private String limesurveyPostUrlBase;

    @Value("${study.limesurvey.pre-url:https://example.com/limesurvey/index.php/123456?uid=}")
    private String limesurveyPreUrlBase;

    /**
     * When true, the frontend may open {@code /start?skipPre=1} to register and go straight to the study
     * without visiting the LimeSurvey pre-questionnaire. Keep false in production unless you are testing.
     */
    @Value("${study.allow-skip-pre-questionnaire:false}")
    private boolean allowSkipPreQuestionnaire;

    /** Local-only: bot correctness preview. Disabled in production (default false). */
    @Value("${study.debug.enabled:false}")
    private boolean debugEnabled;

    /**
     * Public flags for the client (e.g. whether test-only shortcuts are enabled on this deployment).
     */
    @GetMapping("/config")
    public Map<String, Boolean> publicConfig() {
        return Map.of("allowSkipPreQuestionnaire", allowSkipPreQuestionnaire);
    }

    @PostMapping("/register")
    public ParticipantDTO register(@RequestBody RegisterReq req) {
        var p = participants.findByToken(req.getToken())
                .orElseGet(() -> {
                    var np = new Participant();
                    np.setToken(req.getToken());
                    np.setCondition(assignCondition(req.getToken()));
                    np.setTaskOrderCsv(fixedTaskOrderCsv());
                    return participants.save(np);
                });

        // Keep participant order in sync with canonical task order (e.g. after deploy fixes sortOrder).
        var order = fixedTaskOrderCsv();
        if (!order.isBlank() && !order.equals(p.getTaskOrderCsv())) {
            p.setTaskOrderCsv(order);
            participants.save(p);
        }
        return ParticipantDTO.from(p, tasks.count());
    }

    @PostMapping("/next")
    public NextTaskDTO next(@RequestBody RegisterReq req) {
        var p = participants.findByToken(req.getToken()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Please start the study from the start page first."));
        var order = parseOrder(p.getTaskOrderCsv());
        if (order.isEmpty()) {
            throw new IllegalStateException("No tasks configured for this study. Please add tasks and try again from the start page.");
        }
        var idx = nextIndex(trials.countByParticipantId(p.getId()), order.size());
        if (idx >= order.size()) return new NextTaskDTO(true, null, idx, order.size());
        var task = tasks.findById(order.get(idx)).orElseThrow();
        var trial = trials.save(newTrial(p.getId(), task.getId(), idx));
        return NextTaskDTO.of(trial.getId(), idx, order.size(), task);
    }

    @PostMapping("/chat")
    public ChatResp chat(@RequestBody ChatReq req) {
        var trial = trials.findById(req.getTrialId()).orElseThrow();
        chats.save(new ChatTurn(trial.getId(), "user", req.getUserText()));
        var p = participants.findByToken(req.getToken()).orElseThrow();
        var t = tasks.findById(trial.getTaskId()).orElseThrow();
        int indexInSequence = trial.getIndexInSequence() != null ? trial.getIndexInSequence() : 0;
        boolean correct = botPolicy.shouldAnswerCorrectly(indexInSequence, t);
        String answer = null;
        if (aiChatService.isConfigured()) {
            answer = aiChatService.generateAnswer(t, req.getUserText(), correct);
        }
        if (answer == null || answer.isBlank()) {
            answer = craftAnswer(t, correct);
        }
        chats.save(new ChatTurn(trial.getId(), "bot", answer));
        trial.setBotShown(true);
        trial.setBotAnswerCorrect(correct);
        trial.setBotAnswerText(answer);
        trials.save(trial);
        return new ChatResp(answer, correct);
    }

    @PostMapping("/decide")
    public DecideResp decide(@RequestBody DecideReq req) {
        var trial = trials.findById(req.getTrialId()).orElseThrow();
        var t = tasks.findById(trial.getTaskId()).orElseThrow();
        String truth = t.getGroundTruth();
        Boolean isCorrect;
        if (truth == null || "ambivalent".equalsIgnoreCase(truth)) {
            // Ambivalent examples are intentionally unclear; we don't force a strict correct/incorrect.
            isCorrect = null;
        } else {
            isCorrect = truth.equalsIgnoreCase(req.getDecision());
        }
        trial.setFinalDecision(req.getDecision());
        trial.setFinalCorrect(isCorrect);
        trial.setConfidence(req.getConfidence());
        trial.setDecidedAt(Instant.now());
        trial.setUsedChat(Boolean.TRUE.equals(req.getUsedChatbot()) || Boolean.TRUE.equals(trial.getBotShown()));
        if (req.getTrustInBot() != null) trial.setTrustInBot(req.getTrustInBot());
        trials.save(trial);
        boolean done = isLast(trial);
        return new DecideResp(done);
    }

    @PostMapping("/complete")
    public CompleteResp complete(@RequestBody RegisterReq req) {
        String token = req.getToken();
        String redirect = limesurveyPostUrlBase.endsWith("=") ? limesurveyPostUrlBase + URLEncoder.encode(token, UTF_8) : limesurveyPostUrlBase;
        return new CompleteResp(redirect);
    }

    @GetMapping("/pre")
    public java.util.Map<String, String> pre(@RequestParam String uid) {
        String redirect = limesurveyPreUrlBase.endsWith("=")
                ? limesurveyPreUrlBase + URLEncoder.encode(uid, UTF_8)
                : limesurveyPreUrlBase;
        return java.util.Map.of("redirect", redirect);
    }

    /**
     * Debug (only if {@code study.debug.enabled=true}): bot correctness preview for a task index.
     * Example: POST /api/debug/expected body {@code {"token":"...","taskIndex":0}}
     */
    @PostMapping("/debug/expected")
    public java.util.Map<String, Object> debugExpected(@RequestBody DebugExpectedReq req) {
        if (!debugEnabled) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        var p = participants.findByToken(req.getToken()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown token. Register first."));
        int taskIndex = req.getTaskIndex();
        var order = parseOrder(p.getTaskOrderCsv());
        if (order.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No task order for this participant. Ensure tasks are seeded and participant has registered.");
        }
        if (taskIndex < 0 || taskIndex >= order.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "taskIndex must be 0.." + (order.size() - 1) + " for this study");
        }
        var task = tasks.findById(order.get(taskIndex)).orElseThrow();
        boolean botWillBeCorrect = botPolicy.shouldAnswerCorrectly(taskIndex, task);
        return java.util.Map.of(
                "taskIndex", taskIndex,
                "taskTitle", task.getTitle() != null ? task.getTitle() : "",
                "groundTruth", task.getGroundTruth() != null ? task.getGroundTruth() : "",
                "condition", p.getCondition() != null ? p.getCondition() : "",
                "botWillBeCorrect", botWillBeCorrect
        );
    }


    private static String assignCondition(String token) {
        // Same study condition for everyone (no per-participant accuracy partitioning).
        return "mostly_correct_80";
    }

    private String fixedTaskOrderCsv() {
        var all = tasks.findAll();
        if (all.isEmpty()) return "";
        all.sort(Comparator
                .comparing(Task::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Task::getId));
        return all.stream()
                .map(Task::getId)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    private static String shuffleTaskOrder(List<Task> all, String seed) {
        var ids = all.stream().map(Task::getId).collect(Collectors.toList());
        Collections.shuffle(ids, new java.util.Random(seed.hashCode()));
        return ids.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private static List<Long> parseOrder(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        var list = new ArrayList<Long>();
        for (String s : csv.split(",")) {
            try { list.add(Long.parseLong(s.trim())); } catch (NumberFormatException ignored) {}
        }
        return list;
    }

    private static int nextIndex(long completedCount, int totalTasks) {
        return (int) completedCount;
    }

    private static Trial newTrial(long participantId, long taskId, int indexInSequence) {
        var t = new Trial();
        t.setParticipantId(participantId);
        t.setTaskId(taskId);
        t.setIndexInSequence(indexInSequence);
        t.setStartedAt(Instant.now());
        return t;
    }

    private boolean isLast(Trial trial) {
        var p = participants.findById(trial.getParticipantId()).orElseThrow();
        var order = parseOrder(p.getTaskOrderCsv());
        return trial.getIndexInSequence() != null && trial.getIndexInSequence() + 1 >= order.size();
    }

    private static String craftAnswer(Task t, boolean correct) {
        String truth = t.getGroundTruth();
        if ("ambivalent".equalsIgnoreCase(truth)) {
            return "Some parts of this look a bit generic (wording or timing), but other bits could match a normal message. I wouldn’t call it clearly one way or the other from what’s on screen.";
        }

        if (correct) {
            if ("phish".equalsIgnoreCase(truth)) {
                return "To me this looks like phishing: the sender and the ask don’t line up with a trustworthy source, and the setup is meant to get you to act fast. I’d label it phishing.";
            }
            return "This looks legitimate to me: branding and flow match what I’d expect from a normal login page, and nothing here screams a scam.";
        }

        // Intentionally misleading (opposite of ground truth) for the study.
        if ("phish".equalsIgnoreCase(truth)) {
            return "I’d call this legitimate: it reads like a routine mailbox notice and the details are plausible enough that I wouldn’t flag it as phishing.";
        }
        return "I’d treat this as phishing: the sender and wording feel off in a way that doesn’t match a genuine message, even if parts look polished.";
    }
}
