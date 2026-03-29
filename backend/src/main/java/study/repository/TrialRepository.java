package study.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.model.Trial;
import java.util.List;

public interface TrialRepository extends JpaRepository<Trial, Long> {
    List<Trial> findByParticipantId(Long participantId);
    long countByParticipantId(Long participantId);

    List<Trial> findByParticipantIdAndDecidedAtIsNull(Long participantId);

    long countByParticipantIdAndDecidedAtIsNotNull(Long participantId);
}
