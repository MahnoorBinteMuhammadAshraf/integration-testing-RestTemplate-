package com.advancedtaskmanager.mapper;

import com.advancedtaskmanager.dto.CategoryDTO;
import com.advancedtaskmanager.entity.Category;
import com.advancedtaskmanager.entity.Task;
import com.advancedtaskmanager.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CategoryMapper {

    public static CategoryDTO toDTO(Category category) {
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setName(category.getName());
        categoryDTO.setTasks(category.getTasks().stream().map(Task::getTitle).toList());
        return categoryDTO;
    }

    public static Category toEntity(CategoryDTO categoryDTO, TaskRepository taskRepository) {
        Category category = new Category();
        category.setName(categoryDTO.getName());
        List<String> stringList = categoryDTO.getTasks();

        if(stringList != null)
        {
            Set<Task> tasks = taskRepository.findAllTaskByTitle(stringList);
            if(!tasks.isEmpty()) {
                for(Task task : tasks) {
                    task.setCategory(category);
                    taskRepository.save(task);
                }
                category.setTasks(tasks);
            }
            else {
                Set<Task> taskList = new HashSet<>();
                for (String s : stringList) {
                    taskList.add(TaskMapper.createTheTask(s));
                    taskRepository.save(TaskMapper.createTheTask(s));
                }
                category.setTasks(taskList);
            }
        }
        return category;
    }
}
