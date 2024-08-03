package com.advancedtaskmanager.service;

import com.advancedtaskmanager.dto.TaskDTO;
import com.advancedtaskmanager.entity.Category;
import com.advancedtaskmanager.entity.Task;
import com.advancedtaskmanager.mapper.TaskMapper;
import com.advancedtaskmanager.repository.CategoryRepository;
import com.advancedtaskmanager.repository.TaskRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;

    public TaskService(TaskRepository taskRepository, CategoryRepository categoryRepository) {
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAllByPriority().stream().map(TaskMapper::toDTO).collect(Collectors.toList());
        //return taskRepository.findAll().stream().map(TaskMapper::toDTO).collect(Collectors.toList());
    }

    public TaskDTO getTaskById(int id) {
        return  taskRepository.findById(id).map(TaskMapper::toDTO).
                orElseThrow(() -> new EntityNotFoundException("task for id " + id + " not found"));
    }

    public TaskDTO createTask(TaskDTO taskDTO) {
        Optional<Category> category = categoryRepository.findByName(taskDTO.getCategory());

        Task task = TaskMapper.toEntity(taskDTO);
        category.ifPresent(task::setCategory);

        return TaskMapper.toDTO(taskRepository.save(task));
    }

    public List<TaskDTO> findByCategory(String name) {
       // Optional<Category> category = categoryRepository.findByName("name");
        Integer id = categoryRepository.findByName(name).map(Category::getId).orElseThrow(
                () -> new EntityNotFoundException("category for name \" " + name + " \" not exists.")
        );
        return taskRepository.findAllByCategory_Id(id).stream().map(TaskMapper::toDTO).collect(Collectors.toList());
    }

    public List<TaskDTO> findByDate(@DateTimeFormat(iso = DATE) LocalDate date) {
        return taskRepository.findByScheduledDate(date).stream().map(TaskMapper::toDTO).collect(Collectors.toList());
    }

    public void deleteTaskById(@NotNull Integer id) {
        if (!taskRepository.existsById(id))
        {
            throw new EntityNotFoundException("task for id " + id + " not found");
        }
        taskRepository.deleteById(id);
    }

    public void markTask(Integer id, boolean tic)
    {
        Task task =  taskRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("task for id " + id + " not found."));
        task.setCompleted(tic);
        taskRepository.save(task);
    }

    public void updateTitle(Integer id, String title)
    {
        Task task =  taskRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("task for id " + id + " not found."));
        task.setTitle(title);
        taskRepository.save(task);
    }

    public void updateDescription(Integer id, String description)
    {
        Task task =  taskRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("task for id " + id + " not found."));
        task.setDescription(description);
        taskRepository.save(task);
    }

    public void updateDate(Integer id, LocalDate date)
    {
        Task task =  taskRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("task for id " + id + " not found."));
        task.setScheduledDate(date);
        taskRepository.save(task);
    }

    public void updateCategory(Integer id, String category)
    {
        Optional<Category> savedCategory = categoryRepository.findByName(category);
        Task task =  taskRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("task for id " + id + " not found."));

        if(savedCategory.isEmpty()) {
            Category newCategory = new Category();
            newCategory.setName(category);
            task.setCategory(categoryRepository.save(newCategory));
        }else{
            task.setCategory(savedCategory.get());
        }
        taskRepository.save(task);
    }

    public void postponedTask(Integer id, LocalDate date)
    {
        Task task = taskRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("task for id " + id + " not found"));
        if(date.isAfter(task.getScheduledDate()))
        {
            task.setScheduledDate(date);
            taskRepository.save(task);
        }else{
            throw new EntityNotFoundException("task for id " + id + " has already been scheduled");
        }
    }

    public void setPriority(Integer id, Integer priority)
    {
        Task task = taskRepository.findById(id).get();
        task.setPriority(priority);
        taskRepository.save(task);
    }
}
