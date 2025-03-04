package org.example.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.example.aspect.annotation.AroundLogging;
import org.example.aspect.annotation.BeforeLogging;
import org.example.dto.TaskDto;
import org.example.entity.enums.TaskStatus;
import org.example.kafka.KafkaClientProducer;
import org.example.repository.TaskRepository;
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

    @AroundLogging
    @Transactional
    public void createTask(TaskDto task) {
        var s = TaskMapper.fromDto(task);
        log.info("Saving task: {}", s.toString());
        taskRepository.save(TaskMapper.fromDto(task));
    }

    @AroundLogging
    @AfterReturning
    @Transactional(readOnly = true)
    public ResponseEntity<TaskDto> findTask(long id) {
        return taskRepository.findById(id)
                             .map(TaskMapper::toModel)
                             .map(ResponseEntity::ok)
                             .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @AfterThrowing
    @Transactional
    public void updateTask(long id, @NonNull TaskDto dto, String topic) {
        taskRepository.findById(id)
          .ifPresentOrElse(entity -> {
              var isStatusUpdated = !entity.getStatus()
                                           .name()
                                           .equals(dto.getStatus());

              entity.setTitle(dto.getTitle());
              entity.setDescription(dto.getDescription());
              entity.setUserId(dto.getUserId());
              entity.setStatus(TaskStatus.valueOf(dto.getStatus()));

              taskRepository.saveAndFlush(entity);

              if (isStatusUpdated) {
                  log.info("Updating task with id: {} from status: {} to status {}", id, entity.getStatus(), dto.getStatus());
                  kafkaClientProducer.sendTo(topic, dto);
              }
          }, () -> {
              throw new EntityNotFoundException("Task with id " + id + " not found for update.");
          });
    }

    @BeforeLogging
    @Transactional
    public void deleteTask(long id) {
        taskRepository.deleteById(id);
    }

    @AfterReturning
    @Transactional(readOnly = true)
    public Collection<TaskDto> findAllTasks() {
        return taskRepository.findAll()
                             .stream()
                             .map(TaskMapper::toModel)
                             .toList();
    }

}
