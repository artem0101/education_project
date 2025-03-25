package org.example.repository;

import org.example.entity.TaskEntity;
import org.example.entity.enums.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = {"tasks"},
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:29092",
                "log.dirs=/tmp/kafka-log"
        }
)
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void testSaveAndFindById() {
        var task = new TaskEntity();
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setStatus(TaskStatus.NEW);

        var savedTask = taskRepository.save(task);

        var foundTask = taskRepository.findById(savedTask.getId());
        assertThat(foundTask).isPresent();
        assertThat(foundTask.get().getTitle()).isEqualTo("Test Task");
    }

    @Test
    void testDeleteById() {
        var task = new TaskEntity();
        task.setTitle("To be deleted");
        task.setDescription("Delete me");
        task.setStatus(TaskStatus.NEW);

        var savedTask = taskRepository.save(task);
        assertThat(taskRepository.findById(savedTask.getId())).isPresent();

        taskRepository.deleteById(savedTask.getId());

        assertThat(taskRepository.findById(savedTask.getId())).isEmpty();
    }

}
