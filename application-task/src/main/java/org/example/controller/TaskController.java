package org.example.controller;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.example.dto.TaskDto;
import org.example.service.TaskService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @Value("${task.kafka.topic.task_status_updated}")
    private String topic;

    @PostMapping
    public void createTask(@RequestBody TaskDto task) {
        taskService.createTask(task);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable("id") long id) {
        return taskService.findTask(id);
    }

    @PutMapping("/{id}")
    public void updateTask(@PathVariable("id") long id, @RequestBody TaskDto task) {
        taskService.updateTask(id, task, topic);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable("id") long id) {
        taskService.deleteTask(id);
    }

    @GetMapping
    public Collection<TaskDto> getAllTasks() {
        return taskService.findAllTasks();
    }

}
