package org.example.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testBuilderAndGetters() {
        var task = TaskDto.builder()
                          .id(1L)
                          .title("Test Task")
                          .description("Test Description")
                          .userId(123L)
                          .status("IN_PROGRESS")
                          .build();

        assertNotNull(task);
        assertEquals(1L, task.getId());
        assertEquals("Test Task", task.getTitle());
        assertEquals("Test Description", task.getDescription());
        assertEquals(123L, task.getUserId());
        assertEquals("IN_PROGRESS", task.getStatus());
    }

    @Test
    void testJsonSerialization() throws JsonProcessingException {
        var task = TaskDto.builder()
                              .id(2L)
                              .title("Serialize Task")
                              .status("COMPLETED")
                              .build();

        var json = objectMapper.writeValueAsString(task);
        assertTrue(json.contains("\"id\":2"));
        assertTrue(json.contains("\"title\":\"Serialize Task\""));
        assertTrue(json.contains("\"status\":\"COMPLETED\""));
        assertFalse(json.contains("description"));
        assertFalse(json.contains("userId"));
    }

    @Test
    void testJsonDeserialization() throws JsonProcessingException {
        var json = "{\"id\":3,\"title\":\"Deserialize Task\",\"status\":\"NEW\"}";

        var task = objectMapper.readValue(json, TaskDto.class);

        assertNotNull(task);
        assertEquals(3L, task.getId());
        assertEquals("Deserialize Task", task.getTitle());
        assertEquals("NEW", task.getStatus());
        assertNull(task.getDescription()); // Поле отсутствовало в JSON
        assertNull(task.getUserId());
    }

}
