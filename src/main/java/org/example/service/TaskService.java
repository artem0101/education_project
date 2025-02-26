package org.example.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.Collection;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.example.aspect.annotation.AroundLogging;
import org.example.aspect.annotation.BeforeLogging;
import org.example.entity.TaskEntity;
import org.example.model.TaskModel;
import org.example.repository.TaskRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {this.taskRepository = taskRepository;}

    @AroundLogging
    @Transactional
    public void createTask(TaskModel task) {
        taskRepository.save(TaskEntity.fromModel(task));
    }

    @AroundLogging
    @AfterReturning
    @Transactional(readOnly = true)
    public ResponseEntity<TaskModel> findTask(long id) {
        return taskRepository.findById(id)
                             .map(TaskEntity::toModel)
                             .map(ResponseEntity::ok)
                             .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @AfterThrowing
    @Transactional
    public void updateTask(long id, @NonNull TaskModel task) {
        var result = taskRepository.findById(id);
        if (result.isEmpty()) {
            throw new EntityNotFoundException("Task with id " + id + " not found for update.");
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
    public Collection<TaskModel> findAllTasks() {
        return taskRepository.findAll()
                             .stream()
                             .map(TaskEntity::toModel)
                             .toList();
    }

}
