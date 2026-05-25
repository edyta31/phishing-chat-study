package study.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.model.Trial;

public interface TrialRepository extends JpaRepository<Trial, Long> {
    long countByParticipantId(Long participantId);
}
