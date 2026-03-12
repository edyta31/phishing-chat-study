package study.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.model.Participant;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    Optional<Participant> findByToken(String token);
}