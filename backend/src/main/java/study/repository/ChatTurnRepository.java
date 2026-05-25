package study.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import study.model.ChatTurn;

public interface ChatTurnRepository extends JpaRepository<ChatTurn, Long> {
}