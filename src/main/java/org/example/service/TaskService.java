package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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
import org.springframework.beans.factory.annotation.Value;
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
        var result = taskRepository.findById(id);
        if (result.isEmpty()) {
            throw new EntityNotFoundException("Task with id " + id + " not found for update.");
        } else if (!result.get().getStatus().name().equals(dto.getStatus())) {
            log.info("Updating task with id: {} from status: {} to status {}", id, result.get().getStatus(), dto.getStatus());
            kafkaClientProducer.sendTo(topic, dto);
        }

        result.ifPresent(entity -> {
            entity.setTitle(dto.getTitle());
            entity.setDescription(dto.getDescription());
            entity.setUserId(dto.getUserId());
            entity.setStatus(TaskStatus.valueOf(dto.getStatus()));

            taskRepository.saveAndFlush(entity);
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

    public List<TaskDto> parseJson() {
        var mapper = new ObjectMapper();
        TaskDto[] tasks;
        try {
            tasks = mapper.readValue(new File("src/main/resources/MOCK_DATA.json"), TaskDto[].class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Arrays.asList(tasks);
    }

}
