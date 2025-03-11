package org.example.entity;

import org.example.entity.enums.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TaskEntityTest {

    @Test
    void testBuilderAndGetters() {
        var task = TaskEntity.builder()
                             .id(1L)
                             .title("Test Task")
                             .description("Test Description")
                             .userId(123L)
                             .status(TaskStatus.IN_PROGRESS)
                             .build();

        assertNotNull(task);
        assertEquals(1L, task.getId());
        assertEquals("Test Task", task.getTitle());
        assertEquals("Test Description", task.getDescription());
        assertEquals(123L, task.getUserId());
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
    }

}
