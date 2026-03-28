package study.service;

import org.springframework.stereotype.Service;

@Service
public class BotPolicyService {
    /**
     * Deterministic "study policy" for whether the assistant should be correct.
     *
     * Study design:
     * - first two examples: correct (matches ground truth)
     * - next two examples: wrong (opposite of ground truth)
     * - last example: ambivalent (assistant stays non-binary; we still allow it to be treated as "correct")
     */
    public boolean shouldAnswerCorrectly(int indexInSequence, study.model.Task task) {
        if (task != null && task.getGroundTruth() != null && "ambivalent".equalsIgnoreCase(task.getGroundTruth())) {
            return true;
        }
        if (indexInSequence <= 1) return true;
        if (indexInSequence <= 3) return false;
        return true;
    }
}

