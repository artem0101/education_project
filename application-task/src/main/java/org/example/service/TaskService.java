package org.example.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.TaskDto;
import org.example.entity.enums.TaskStatus;
import org.example.kafka.KafkaClientProducer;
import org.example.repository.TaskRepository;
import org.example.starter.aspect.annotation.AfterReturningLogging;
import org.example.starter.aspect.annotation.AfterThrowingLogging;
import org.example.starter.aspect.annotation.AroundLogging;
import org.example.starter.aspect.annotation.BeforeLogging;
import org.example.util.TaskMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final KafkaClientProducer kafkaClientProducer;
    private final TaskMapper taskMapper;

    @AroundLogging
    @Transactional
    public void createTask(TaskDto task) {
        taskRepository.save(taskMapper.fromDto(task));
    }

    @AroundLogging
    @AfterReturningLogging
    @Transactional(readOnly = true)
    public ResponseEntity<TaskDto> findTask(long id) {
        return taskRepository.findById(id)
                             .map(taskMapper::toModel)
                             .map(ResponseEntity::ok)
                             .orElseGet(() -> ResponseEntity.notFound()
                                                            .build());
    }

    @AfterThrowingLogging
    @Transactional
    public void updateTask(long id, @NonNull TaskDto dto, String topic) {
        var entity = taskRepository.findById(id)
                                 .orElseThrow(() -> new EntityNotFoundException("Task with id " + id + " not found for update."));

        var newStatus = TaskStatus.valueOf(dto.getStatus());
        var isStatusUpdated = !newStatus.equals(entity.getStatus());

        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setUserId(dto.getUserId());
        entity.setStatus(newStatus);

        taskRepository.saveAndFlush(entity);

        if (isStatusUpdated) {
            log.info("Updating task with id: {} from status: {} to status {}", id, entity.getStatus(), newStatus);
            kafkaClientProducer.sendTo(topic, dto);
        }
    }

    @BeforeLogging
    @Transactional
    public void deleteTask(long id) {
        taskRepository.deleteById(id);
    }

    @AfterReturningLogging
    @Transactional(readOnly = true)
    public Collection<TaskDto> findAllTasks() {
        return taskRepository.findAll()
                             .stream()
                             .map(taskMapper::toModel)
                             .toList();
    }

}
