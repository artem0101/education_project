package org.example.controller;

import java.util.Collection;
import org.example.model.TaskModel;
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
    public void createTask(@RequestBody TaskModel task) {
        taskService.createTask(task);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskModel> getTaskById(@PathVariable("id") long id) {
        return taskService.findTask(id);
    }

    @PutMapping("/{id}")
    public void updateTask(@PathVariable("id") long id, @RequestBody TaskModel task) {
        taskService.updateTask(id, task);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable("id") long id) {
        taskService.deleteTask(id);
    }

    @GetMapping
    public Collection<TaskModel> getAllTasks() {
        return taskService.findAllTasks();
    }

}
