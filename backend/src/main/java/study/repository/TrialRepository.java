package study.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.model.Trial;

import java.util.List;

public interface TrialRepository extends JpaRepository<Trial, Long> {
    List<Trial> findByParticipantId(Long participantId);
    long countByParticipantId(Long participantId);

    /** Explicit JPQL — derived queries like {@code ...DecidedAtIsNotNull} can parse badly in some setups. */
    @Query("SELECT t FROM Trial t WHERE t.participantId = :participantId AND t.decidedAt IS NULL")
    List<Trial> findOpenTrials(@Param("participantId") Long participantId);

    @Query("SELECT COUNT(t) FROM Trial t WHERE t.participantId = :participantId AND t.decidedAt IS NOT NULL")
    long countDecidedTrials(@Param("participantId") Long participantId);
}
