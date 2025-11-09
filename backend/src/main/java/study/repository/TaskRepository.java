package study.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.model.Task;
import javax.persistence.*;

public interface TaskRepository extends JpaRepository<Task, Long> {}
