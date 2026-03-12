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
import java.util.List;
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

    @PostMapping("/register")
    public ParticipantDTO register(@RequestBody RegisterReq req) {
        var p = participants.findByToken(req.getToken())
                .orElseGet(() -> {
                    var np = new Participant();
                    np.setToken(req.getToken());
                    np.setCondition(assignCondition(req.getToken()));
                    np.setTaskOrderCsv(shuffleTaskOrder(tasks.findAll(), req.getToken()));
                    return participants.save(np);
                });

        if (p.getTaskOrderCsv() == null || p.getTaskOrderCsv().isBlank()) {
            var order = shuffleTaskOrder(tasks.findAll(), p.getToken());
            if (!order.isBlank()) {
                p.setTaskOrderCsv(order);
                participants.save(p);
            }
        }
        return ParticipantDTO.from(p, tasks.count());
    }

    @GetMapping("/next")
    public NextTaskDTO next(@RequestParam String token) {
        var p = participants.findByToken(token).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Please start the study from the start page first: open /start?token=YOUR_TOKEN in your browser."));
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
        boolean correct = botPolicy.shouldAnswerCorrectly(p.getToken(), t.getId(), p.getCondition());
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
        boolean isCorrect = t.getGroundTruth().equalsIgnoreCase(req.getDecision());
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

    @GetMapping("/complete")
    public CompleteResp complete(@RequestParam String token) {
        String redirect = limesurveyPostUrlBase.endsWith("=") ? limesurveyPostUrlBase + URLEncoder.encode(token, UTF_8) : limesurveyPostUrlBase;
        return new CompleteResp(redirect);
    }

    private static String assignCondition(String token) {
        int h = token.hashCode();
        if (h % 3 == 0) return "mostly_correct_80";
        if (h % 3 == 1) return "mostly_wrong_20";
        return "balanced_50";
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
        if (correct) {
            if ("phish".equalsIgnoreCase(truth)) return "This looks like a phishing attempt. I'd recommend treating it as suspicious and not clicking links or sharing information.";
            return "This looks legitimate to me. The sender and content appear consistent with a safe source.";
        }
        if ("phish".equalsIgnoreCase(truth)) return "This looks legitimate. I don't see strong signs of phishing here.";
        return "This looks suspicious to me. I'd be cautious and avoid clicking links or sharing personal details.";
    }
}
