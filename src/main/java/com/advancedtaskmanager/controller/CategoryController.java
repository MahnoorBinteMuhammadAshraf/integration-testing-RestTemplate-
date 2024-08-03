package com.advancedtaskmanager.controller;

import com.advancedtaskmanager.dto.CategoryDTO;
import com.advancedtaskmanager.dto.TaskDTO;
import com.advancedtaskmanager.service.CategoryService;
import jakarta.validation.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/get")
    public CategoryDTO getCategory(@RequestParam("id") Integer id) {
       return categoryService.getCategoryById(id);
    }

    @PostMapping("/create") //@Valid
    public CategoryDTO createCategory(@RequestBody CategoryDTO category) {
       return categoryService.createCategory(category);
    }

    @PutMapping("/update")
    public CategoryDTO updateCategory(@RequestParam("new") String name, @RequestParam("id") Integer id) {
        return categoryService.updateCategory(name, id);
    }

    @GetMapping("/allTask")
    public List<TaskDTO> getTasksInCategory(@RequestParam("id") Integer id) {
        return categoryService.tasksInCategory(id);
    }

    @DeleteMapping("/delete")
    public void deleteCategory(@RequestParam("id") Integer id) {
        categoryService.deleteCategory(id);
    }
}
