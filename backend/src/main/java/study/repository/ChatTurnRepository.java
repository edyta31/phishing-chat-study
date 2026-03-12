package study.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import study.model.ChatTurn;

import java.util.List;

public interface ChatTurnRepository extends JpaRepository<ChatTurn, Long> {
    List<ChatTurn> findByTrialId(Long trialId);
}