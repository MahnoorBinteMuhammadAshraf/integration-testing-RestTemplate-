package com.advancedtaskmanager.ServiceUnitTest;

import com.advancedtaskmanager.dto.CategoryDTO;
import com.advancedtaskmanager.dto.TaskDTO;
import com.advancedtaskmanager.entity.Category;
import com.advancedtaskmanager.entity.Task;
import com.advancedtaskmanager.repository.CategoryRepository;
import com.advancedtaskmanager.repository.TaskRepository;
import com.advancedtaskmanager.service.CategoryService;
import com.advancedtaskmanager.service.TaskService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.*;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskService taskService;

    Category category;
    CategoryDTO categoryDTO;
    Task task;
    TaskDTO taskDTO;
    HashSet<Task> hashset;
    ArrayList<Task> taskList;
    ArrayList<String> taskStringList;
    ArrayList<TaskDTO> taskDTOList;

    @BeforeEach
    public void setUp() {
        category = new Category();
        category.setId(1);
        category.setName("test");

        task = new Task();
        task.setId(1);
        task.setDescription("Test Description is bla bla bla");
        task.setTitle("Test Title is bla bla bla");
        task.setCategory(category);
        task.setScheduledDate(LocalDate.ofEpochDay(2024/7/21));

        taskList = new ArrayList<>();
        taskList.add(task);

        hashset = new HashSet<>();
        hashset.add(task);
        category.setTasks(hashset);

        taskDTO = new TaskDTO();
        taskDTO.setDescription("Test Description is bla bla bla");
        taskDTO.setTitle("Test Title is bla bla bla");
        taskDTO.setScheduledDate(LocalDate.ofEpochDay(2024/7/21));
        taskDTO.setCompleted(false);
        taskDTO.setCategory("test");

        taskDTOList = new ArrayList<>();
        taskDTOList.add(taskDTO);

        categoryDTO = new CategoryDTO();
        categoryDTO.setName("test");
        taskStringList = new ArrayList<>();
        taskStringList.add("Test Title is bla bla bla");
        categoryDTO.setTasks(taskStringList);

        categoryService = new CategoryService(categoryRepository, taskRepository, taskService);
    }

    @Test
    void getCategoryByIdTest()
    {
        when(categoryRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(category));

        categoryService.getCategoryById(1);
    }

    @Test
    void getCategoryByIdTest_CategoryNotFound()
    {
        when(categoryRepository.findById(Mockito.anyInt())).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> categoryService.getCategoryById(1));
        assertEquals(exception.getMessage(), "Category not found");
    }

    @Test
    void createCategoryTest()
    {
        when(categoryRepository.save(Mockito.any(Category.class))).thenReturn(category);

        categoryService.createCategory(categoryDTO);
    }

    @Test
    void updateCategoryTest()
    {
        when(categoryRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(category));
        when(categoryRepository.save(Mockito.any(Category.class))).thenReturn(category);

        categoryService.updateCategory(category.getName(), 1);
    }

    @Test
    void updateCategoryTest_CategoryNotFound()
    {
        when(categoryRepository.findById(2)).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> categoryService.updateCategory("vgtt",2));
        assertEquals(exception.getMessage(), "category for id \" " + 2 + " \" not exists.");
    }

    @Test
    void tasksInCategoryTest()
    {
        when(categoryRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(category));
        when(taskService.findByCategory(Mockito.any())).thenReturn(taskDTOList);

        categoryService.tasksInCategory(category.getId());
    }

    @Test
    public void deleteCategoryTest()
    {
        when(categoryRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(category));
        when(taskRepository.findAllByCategory_Id(Mockito.anyInt())).thenReturn(taskList);
        Mockito.doNothing().when(taskRepository).deleteAll(taskList);
        Mockito.doNothing().when(categoryRepository).deleteById(Mockito.anyInt());

        categoryService.deleteCategory(category.getId());
    }

    @Test
    void deleteCategoryTest_CategoryNotFound()
    {
        when(categoryRepository.findById(2)).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> categoryService.deleteCategory(2));
        assertEquals(exception.getMessage(), "category for id \" " + 2 + " \" not exists.");
    }

}
