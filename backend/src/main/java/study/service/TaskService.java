package study.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import javax.persistence.*;
import study.model.Task;
import study.model.UserResponse;
import study.repository.TaskRepository;
import study.repository.UserResponseRepository;


@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserResponseRepository userResponseRepository;

    public Task getTaskById(Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
    }

    public void saveUserResponse(UserResponse response) {
        userResponseRepository.save(response);
    }
}
