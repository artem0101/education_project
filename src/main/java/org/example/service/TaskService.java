package org.example.service;

import java.util.Collection;
import java.util.Optional;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.example.aspect.annotation.AroundLogging;
import org.example.aspect.annotation.BeforeLogging;
import org.example.entity.TaskEntity;
import org.example.repository.TaskRepository;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {this.taskRepository = taskRepository;}

    @AroundLogging
    @Transactional
    public void createTask(TaskEntity task) {
        taskRepository.save(task);
    }

    @AroundLogging
    @AfterReturning
    @Transactional(readOnly = true)
    public Optional<TaskEntity> findTask(long id) {
        return taskRepository.findById(id);
    }

    @AfterThrowing
    @Transactional
    public void updateTask(long id, @NonNull TaskEntity task) {
        var result = taskRepository.findById(id);
        if (result.isEmpty()) {
            throw new RuntimeException();
        }

        taskRepository.update(id, task.getTitle(), task.getDescription(), task.getUserId());
    }

    @BeforeLogging
    @Transactional
    public void deleteTask(long id) {
        taskRepository.deleteById(id);
    }

    @AfterReturning
    @Transactional(readOnly = true)
    public Collection<TaskEntity> findAllTasks() {
        return taskRepository.findAll();
    }

}
