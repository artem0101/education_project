package org.example.controller;

import java.util.Collection;
import org.example.entity.TaskEntity;
import org.example.service.TaskService;
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
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {this.taskService = taskService;}

    @PostMapping
    public void createTask(@RequestBody TaskEntity task) {
        taskService.createTask(task);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskEntity> getTaskById(@PathVariable("id") long id) {
        var result = taskService.findTask(id);
        return result.map(ResponseEntity::ok)
                     .orElseGet(() -> ResponseEntity.notFound().build());

    }

    @PutMapping("/{id}")
    public void updateTask(@PathVariable("id") long id, @RequestBody TaskEntity task) {
        taskService.updateTask(id, task);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable("id") long id) {
        taskService.deleteTask(id);
    }

    @GetMapping
    public Collection<TaskEntity> getAllTasks() {
        return taskService.findAllTasks();
    }

}
