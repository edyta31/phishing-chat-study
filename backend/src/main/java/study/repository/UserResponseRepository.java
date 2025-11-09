package study.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.model.UserResponse;

public interface UserResponseRepository extends JpaRepository<UserResponse, Long> {}
