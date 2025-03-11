package org.example.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.example.dto.TaskDto;
import org.example.entity.TaskEntity;
import org.example.entity.enums.TaskStatus;
import org.example.kafka.KafkaClientProducer;
import org.example.repository.TaskRepository;
import org.example.util.TaskMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private KafkaClientProducer kafkaClientProducer;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    @Test
    void testCreateTask() {
        var taskDto = new TaskDto(1L, "Test Task", "Test Description", 100L, "NEW");
        var taskEntity = new TaskEntity(1L, "Test Task", "Test Description", 100L, TaskStatus.NEW);

        when(taskMapper.fromDto(taskDto)).thenReturn(taskEntity);
        when(taskRepository.save(taskEntity)).thenReturn(taskEntity);

        taskService.createTask(taskDto);

        verify(taskRepository, times(1)).save(taskEntity);
    }

    @Test
    void testFindTask() {
        var taskEntity = new TaskEntity(1L, "Test Task", "Test Description", 100L, TaskStatus.NEW);
        var taskDto = new TaskDto(1L, "Test Task", "Test Description", 100L, "NEW");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(taskEntity));
        when(taskMapper.toModel(taskEntity)).thenReturn(taskDto);

        var response = taskService.findTask(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(taskDto);
    }

    @Test
    void testFindNonExistedTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        var response = taskService.findTask(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testUpdateTask() {
        var existingTask = new TaskEntity(1L, "Old Task", "Old Desc", 100L, TaskStatus.NEW);
        var updatedTaskDto = new TaskDto(1L, "Updated Task", "Updated Desc", 100L, "IN_PROGRESS");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.saveAndFlush(any(TaskEntity.class))).thenReturn(existingTask);

        taskService.updateTask(1L, updatedTaskDto, "test-topic");

        verify(taskRepository, times(1)).saveAndFlush(any(TaskEntity.class));
        verify(kafkaClientProducer, times(1)).sendTo(eq("test-topic"), eq(updatedTaskDto));
    }

    @Test
    void testUpdateNonExistedTask() {
        var updatedTaskDto = new TaskDto(1L, "Updated Task", "Updated Desc", 100L, "IN_PROGRESS");
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> taskService.updateTask(1L, updatedTaskDto, "test-topic"));
    }

    @Test
    void testDeleteTask() {
        doNothing().when(taskRepository).deleteById(1L);

        taskService.deleteTask(1L);

        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    void testFindAllTasks() {
        var taskEntity1 = new TaskEntity(1L, "Task 1", "Desc 1", 100L, TaskStatus.NEW);
        var taskEntity2 = new TaskEntity(2L, "Task 2", "Desc 2", 101L, TaskStatus.IN_PROGRESS);

        var taskDto1 = new TaskDto(1L, "Task 1", "Desc 1", 100L, "NEW");
        var taskDto2 = new TaskDto(2L, "Task 2", "Desc 2",  101L, "IN_PROGRESS");

        when(taskRepository.findAll()).thenReturn(List.of(taskEntity1, taskEntity2));
        when(taskMapper.toModel(taskEntity1)).thenReturn(taskDto1);
        when(taskMapper.toModel(taskEntity2)).thenReturn(taskDto2);

        var result = taskService.findAllTasks();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(taskDto1, taskDto2);
    }

}

