package com.advancedtaskmanager.controller;

import com.advancedtaskmanager.dto.TaskDTO;
import com.advancedtaskmanager.service.TaskService;
import jakarta.validation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@RestController()
@RequestMapping("/task")
public class TaskController {
    TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/all")
    public List<TaskDTO> tasks() {
        return taskService.getAllTasks();
    }

    @GetMapping("/task")
    public TaskDTO task(@RequestParam("id") Integer id) {
        return taskService.getTaskById(id);
    }

    @GetMapping("/taskByCategory")
    public List<TaskDTO> getTasksByCategory(@RequestParam("name") String name) {
        return taskService.findByCategory(name);
    }

    @GetMapping("/taskByDate")
    public List<TaskDTO> getTasksByDate(@RequestParam("scheduledDate") @DateTimeFormat(iso = DATE) LocalDate startDate) {
        return taskService.findByDate(startDate);
    }

    @PostMapping("/create")
    public TaskDTO createTask(@Valid @RequestBody TaskDTO taskDTO) {
        return taskService.createTask(taskDTO);
    }

    @DeleteMapping("/delete")
    public void deleteTask(@RequestParam("id") Integer id) {
        taskService.deleteTaskById(id);
    }

    @PostMapping("/completed")
    public void complete(@RequestParam("id") Integer id, @RequestParam("completed") boolean completed) {
        taskService.markTask(id, completed);
    }

    @PutMapping("/postpone")
    public void postponeTask(@RequestParam("id") Integer id, @RequestParam("newDate") @DateTimeFormat(iso = DATE) LocalDate date) {
        taskService.postponedTask(id, date);
    }

    @PutMapping("/update")
    public void updateTaskByTitle(@RequestParam("id") Integer id, @RequestParam("title") String title) {
        taskService.updateTitle(id, title);
    }

    @PutMapping("/updateDescription")
    public void updateTaskByDescription(@RequestParam("id") Integer id, @RequestParam("description") String des) {
        taskService.updateDescription(id, des);
    }

    @PutMapping("/updateDate")
    public void updateTaskByDate(@RequestParam("id") Integer id, @RequestParam("scheduledDate") @DateTimeFormat(iso = DATE) LocalDate date) {
        taskService.updateDate(id, date);
    }

    @PutMapping("/updateCategory")
    public void updateTaskByCategory(@RequestParam("id") Integer id, @RequestParam("category_name") String category) {
        taskService.updateCategory(id, category);
    }

    @PutMapping("/priority")
    public void setTaskPriority(@RequestParam("id") Integer id, @RequestParam("priority") int priority) {
        taskService.setPriority(id, priority);
    }
}


