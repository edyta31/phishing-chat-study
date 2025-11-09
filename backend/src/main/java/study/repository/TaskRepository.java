package study.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.model.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {}
