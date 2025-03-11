package org.example.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.TaskDto;
import org.example.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    private static final String TEST_URL = "/api/v1/tasks";
    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${task.kafka.topic.task_status_updated}")
    private String topic;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(taskController).build();
    }

    @Test
    void testCreateTask() throws Exception {
        var task = new TaskDto(1L, "Test Task", "Description", null, null);

        mockMvc.perform(post(TEST_URL)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(task)))
               .andExpect(status().isOk());

        verify(taskService, times(1)).createTask(any(TaskDto.class));
    }

    @Test
    void testGetTaskById() throws Exception {
        var task = new TaskDto(1L, "Test Task", "Description", null, null);
        when(taskService.findTask(1L)).thenReturn(ResponseEntity.ok(task));

        mockMvc.perform(get(TEST_URL + "/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(1))
               .andExpect(jsonPath("$.title").value("Test Task"));

        verify(taskService, times(1)).findTask(1L);
    }

    @Test
    void testUpdateTask() throws Exception {
        var task = new TaskDto(1L, "Updated Task", "Updated Description", null, null);

        mockMvc.perform(put(TEST_URL + "/1")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(task)))
               .andExpect(status().isOk());

        verify(taskService, times(1)).updateTask(eq(1L), any(TaskDto.class), eq(topic));
    }

    @Test
    void testDeleteTask() throws Exception {
        mockMvc.perform(delete(TEST_URL + "/1"))
               .andExpect(status().isOk());

        verify(taskService, times(1)).deleteTask(1L);
    }

    @Test
    void testGetAllTasks() throws Exception {
        var tasks = List.of(
                new TaskDto(1L, "Task 1", "Description 1", null, null),
                new TaskDto(2L, "Task 2", "Description 2", null, null)
        );
        when(taskService.findAllTasks()).thenReturn(tasks);

        mockMvc.perform(get(TEST_URL))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].id").value(1))
               .andExpect(jsonPath("$[1].id").value(2));

        verify(taskService, times(1)).findAllTasks();
    }

}
