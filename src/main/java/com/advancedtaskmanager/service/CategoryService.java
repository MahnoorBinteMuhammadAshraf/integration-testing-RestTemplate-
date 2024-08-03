package com.advancedtaskmanager.service;

import com.advancedtaskmanager.dto.CategoryDTO;
import com.advancedtaskmanager.dto.TaskDTO;
import com.advancedtaskmanager.entity.Task;
import com.advancedtaskmanager.mapper.CategoryMapper;
import com.advancedtaskmanager.repository.CategoryRepository;
import com.advancedtaskmanager.entity.Category;

import java.util.List;
import java.util.Optional;

import com.advancedtaskmanager.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TaskRepository taskRepository;
    private final TaskService taskService;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository, TaskRepository taskRepository, TaskService taskService) {
        this.categoryRepository = categoryRepository;
        this.taskRepository = taskRepository;
        this.taskService = taskService;
    }

    public CategoryDTO getCategoryById(int id) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isPresent()) {
            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.setName(category.get().getName());
            List<Task> t = category.get().getTasks().stream().toList();
            List<String> s = t.stream().map(Task::getTitle).toList();
            categoryDTO.setTasks(s);
            return categoryDTO;

        } else
            throw new EntityNotFoundException("Category not found");
    }

    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category c = categoryRepository.save(CategoryMapper.toEntity(categoryDTO, taskRepository));
        return CategoryMapper.toDTO(c);
    }

    public CategoryDTO updateCategory(String newName, Integer id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("category for id \" " + id + " \" not exists."));
        category.setName(newName);
        categoryRepository.save(category);
        return CategoryMapper.toDTO(category);
    }

    public List<TaskDTO> tasksInCategory(Integer id) {
        return taskService.findByCategory(categoryRepository.findById(id).get().getName());
    }

    public void deleteCategory(Integer id) {
        Category c = categoryRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("category for id \" " + id + " \" not exists."));
        List<Task> tasks = taskRepository.findAllByCategory_Id(c.getId());
        taskRepository.deleteAll(tasks);
        categoryRepository.deleteById(id);
    }
}
