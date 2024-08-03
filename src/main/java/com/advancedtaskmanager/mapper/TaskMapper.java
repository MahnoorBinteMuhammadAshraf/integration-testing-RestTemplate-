package com.advancedtaskmanager.mapper;

import com.advancedtaskmanager.dto.TaskDTO;
import com.advancedtaskmanager.entity.Category;
import com.advancedtaskmanager.entity.Task;


public class TaskMapper {
    public static TaskDTO toDTO(Task task) {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle(task.getTitle());
        taskDTO.setDescription(task.getDescription());
        taskDTO.setScheduledDate(task.getScheduledDate());
        taskDTO.setCompleted(task.isCompleted());
        taskDTO.setPriority(task.getPriority());
        taskDTO.setCategory(task.getCategory().getName());
        return taskDTO;
    }

    public static Task toEntity(TaskDTO taskDTO) {
        Task task = new Task();
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setScheduledDate(taskDTO.getScheduledDate());
        task.setCompleted(taskDTO.isCompleted());
        task.setPriority(taskDTO.getPriority());
        Category category = new Category();
        category.setName(taskDTO.getCategory());
        task.setCategory(category);
        return task;
    }

    public static Task createTheTask(String title)
    {
        Task task = new Task();
        task.setTitle(title);
        return task;
    }
}
