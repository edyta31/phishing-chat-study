package study.service;

import org.springframework.stereotype.Service;

@Service
public class BotPolicyService {
    public boolean shouldAnswerCorrectly(String participantToken, long taskId, String condition) {
        long seed = (participantToken + ":" + taskId + ":" + condition).hashCode();
        var rnd = new java.util.Random(seed);
        switch (condition) {
            case "balanced_50": return rnd.nextBoolean();
            case "mostly_correct_80": return rnd.nextInt(10) < 8;
            case "mostly_wrong_20": return rnd.nextInt(10) < 2;
            default: return true;
        }
    }
}

