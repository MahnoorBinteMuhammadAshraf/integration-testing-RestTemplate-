package com.advancedtaskmanager.ServiceUnitTest;

import com.advancedtaskmanager.dto.TaskDTO;
import com.advancedtaskmanager.entity.Category;
import com.advancedtaskmanager.entity.Task;
import com.advancedtaskmanager.repository.CategoryRepository;
import com.advancedtaskmanager.repository.TaskRepository;
import com.advancedtaskmanager.service.TaskService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @InjectMocks
    private TaskService taskService;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private Task task;

    private Category category;

    private TaskDTO taskDTO;

    private HashSet<Task> hashset;

    private ArrayList<Task> taskList;

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

        taskDTO = new TaskDTO();
        taskDTO.setDescription("Test Description is bla bla bla");
        taskDTO.setTitle("Test Title is bla bla bla");
        taskDTO.setScheduledDate(LocalDate.ofEpochDay(2024/7/21));
        taskDTO.setCompleted(false);
        taskDTO.setCategory("test");

        hashset = new HashSet<>();
        hashset.add(task);
        category.setTasks(hashset);

        taskList = new ArrayList<>();
        taskList.add(task);

        taskService = new TaskService(taskRepository, categoryRepository);
    }

    @Test
    public void getAllTasksTest()
    {
        when(taskRepository.findAllByPriority()).thenReturn(taskList);

        List<TaskDTO> t = taskService.getAllTasks();
        assertEquals(taskDTO, t.get(0));
    }

    @Test
    public void getTaskByIdTest()
    {
        when(taskRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(task));

        TaskDTO t = taskService.getTaskById(1);
        assertEquals(taskDTO, t);
    }

    @Test
    public void getTaskByIdTest_TaskNotFound()
    {
        when(taskRepository.findById(2)).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->  taskService.getTaskById(2));
        assertEquals(exception.getMessage(), "task for id " + 2 + " not found");
    }

    @Test
    public void createTaskTest()
    {
        when(categoryRepository.findByName(Mockito.any(String.class))).thenReturn(Optional.of(category));

        when(taskRepository.save(Mockito.any(Task.class))).thenReturn(task);

        TaskDTO t = taskService.createTask(taskDTO);
        assertEquals(taskDTO.getTitle(), t.getTitle());
    }

    @Test
    public void findByCategoryTest()
    {
        ArrayList<Task> taskList = new ArrayList<>();
        taskList.add(task);

        when(categoryRepository.findByName(Mockito.any(String.class))).thenReturn(Optional.of(category));
        when(taskRepository.findAllByCategory_Id(Mockito.anyInt())).thenReturn(taskList);

        List<TaskDTO> t = taskService.findByCategory("test");

        assertEquals(taskDTO, t.get(0));
    }

    @Test
    public void findByCategory_CategoryNotFound()
    {
        when(categoryRepository.findByName("name")).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> taskService.findByCategory("name"));
        assertEquals(exception.getMessage(), "category for name \" " + "name" + " \" not exists.");
    }

    @Test
    public void findByDateTest()
    {
        when(taskRepository.findByScheduledDate(Mockito.any(LocalDate.class))).thenReturn(taskList);

        List<TaskDTO> t = taskService.findByDate(LocalDate.ofEpochDay(2024/7/21));

        assertEquals(taskDTO, t.get(0));
    }

    @Test
    public void deleteTaskByIdTest()
    {
        when(taskRepository.existsById(Mockito.anyInt())).thenReturn(true);
        Mockito.doNothing().when(taskRepository).deleteById(Mockito.anyInt());
        taskService.deleteTaskById(1);
    }

    @Test
    public void deleteTaskByIdTest_TaskNotFound()
    {
        when(taskRepository.existsById(2)).thenReturn(false);
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> taskService.deleteTaskById(2));
        assertEquals(exception.getMessage(), "task for id " + 2 + " not found");
    }

    @Test
    public void markTaskTest()
    {
        when(taskRepository.findById(Mockito.anyInt())).thenReturn(Optional.ofNullable(task));
        when(taskRepository.save(Mockito.any(Task.class))).thenReturn(task);
        taskService.markTask(1, true);
    }

    @Test
    public void markTaskTest_TaskNotFound()
    {
        when(taskRepository.findById(2)).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> taskService.markTask(2, false));
        assertEquals(exception.getMessage(), "task for id " + 2 + " not found.");
    }

    @Test
    public void updateTitleTest()
    {
        when(taskRepository.findById(Mockito.anyInt())).thenReturn(Optional.ofNullable(task));
        when(taskRepository.save(Mockito.any(Task.class))).thenReturn(task);
        taskService.updateTitle(1, "task new title");
    }

    @Test
    public void updateTitleTest_TaskNotFound()
    {
        when(taskRepository.findById(2)).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> taskService.updateTitle(2, "task new title"));
        assertEquals(exception.getMessage(), "task for id " + 2 + " not found.");
    }

    @Test
    public void updateDescriptionTest()
    {
        when(taskRepository.findById(Mockito.anyInt())).thenReturn(Optional.ofNullable(task));
        when(taskRepository.save(Mockito.any(Task.class))).thenReturn(task);
        taskService.updateDescription(1, "task new description");
    }

    @Test
    public void updateDescriptionTest_TaskNotFound()
    {
        when(taskRepository.findById(2)).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> taskService.updateDescription(2, "task new description"));
        assertEquals(exception.getMessage(), "task for id " + 2 + " not found.");
    }

    @Test
    public void updateDateTest()
    {
        when(taskRepository.findById(Mockito.anyInt())).thenReturn(Optional.ofNullable(task));
        when(taskRepository.save(Mockito.any(Task.class))).thenReturn(task);

        taskService.updateDate(1, LocalDate.now());
    }

    @Test
    public void updateDateTest_TaskNotFound()
    {
        when(taskRepository.findById(2)).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> taskService.updateDate(2, LocalDate.now()));
        assertEquals(exception.getMessage(), "task for id " + 2 + " not found.");
    }

    @Test
    public void updateCategoryTest()
    {
        when(categoryRepository.findByName(Mockito.any(String.class))).thenReturn(Optional.of(category));
        when(taskRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(task));
        when(taskRepository.save(Mockito.any(Task.class))).thenReturn(task);

        taskService.updateCategory(1, "task new category");
    }

    @Test
    public void updateCategoryTest_TaskNotFound()
    {
        when(taskRepository.findById(2)).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> taskService.updateCategory(2, "task new category"));
        assertEquals(exception.getMessage(), "task for id " + 2 + " not found.");
    }

    @Test
    public void updateCategoryTest_CategoryNotFound()
    {
        when(categoryRepository.findByName(Mockito.any(String.class))).thenReturn(Optional.empty());
        when(categoryRepository.save(any())).thenReturn(category);
        when(taskRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(task));
        when(taskRepository.save(Mockito.any(Task.class))).thenReturn(task);

        taskService.updateCategory(1, "task new category");
    }

    @Test
    public void postponedTaskTest()
    {
        when(taskRepository.findById(Mockito.anyInt())).thenReturn(Optional.ofNullable(task));
        taskService.postponedTask(1, LocalDate.now());
    }

    @Test
    public void postponedTaskTest_TaskNotFound()
    {
        when(taskRepository.findById(1)).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> taskService.postponedTask(1, LocalDate.now()));
        assertEquals(exception.getMessage(), "task for id " + 1 + " not found");
    }

    @Test
    public void postponedTaskTest_AlreadyPostponedTask()
    {
        when(taskRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(task));
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> taskService.postponedTask(1, LocalDate.ofEpochDay(2024/7/28)));
        assertEquals(exception.getMessage(), "task for id " + 1 + " has already been scheduled");
    }

    @Test
    public void setPriorityTest()
    {
        when(taskRepository.findById(Mockito.anyInt())).thenReturn(Optional.ofNullable(task));
        when(taskRepository.save(Mockito.any(Task.class))).thenReturn(task);
        taskService.setPriority(1, 2);
    }
}
