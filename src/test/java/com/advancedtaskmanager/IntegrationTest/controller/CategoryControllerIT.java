package com.advancedtaskmanager.IntegrationTest.controller;

import com.advancedtaskmanager.dto.CategoryDTO;
import com.advancedtaskmanager.dto.TaskDTO;
import com.advancedtaskmanager.entity.Category;
import com.advancedtaskmanager.entity.Task;
import com.advancedtaskmanager.repository.CategoryRepository;
import com.advancedtaskmanager.repository.TaskRepository;
import com.advancedtaskmanager.service.CategoryService;
import com.advancedtaskmanager.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Slf4j
public class CategoryControllerIT {
    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }

    @Autowired
    protected TaskService taskService;

    @Autowired
    protected TaskRepository taskRepository;

    @Autowired
    protected CategoryService categoryService;

    @Autowired
    protected CategoryRepository categoryRepository;

    @Autowired
    protected TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    private String rootUrl;

    Category cat1;
    Category cat2;
    Task task1;
    Task task2;

    @BeforeAll
    static void start() {
        postgresContainer.start();
    }

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        categoryRepository.deleteAll();

        rootUrl = "http://localhost:" + port + "/category";

        cat1 = Category.builder()
                .id(1)
                .name("class-work")
                .build();

        cat2 = Category.builder()
                .id(2)
                .name("home-work")
                .build();

        task1 = Task.builder()
                .id(1)
                .title("Task_1")
                .description("Task_1 description.")
                .priority(3)
                .completed(false)
                .category(cat1)
                .scheduledDate(LocalDate.of(2024,8,15))
                .build();

        task2 = Task.builder()
                .id(2)
                .title("Task_2")
                .description("Task_2 description.")
                .priority(1)
                .completed(true)
                .category(cat2)
                .scheduledDate(LocalDate.of(2024,6,9))
                .build();

        Set<Task> task_list1 = new HashSet<>();
        task_list1.add(task1);

        cat1.setTasks(task_list1);

        Set<Task> task_list2 = new HashSet<>();
        task_list2.add(task2);

        cat2.setTasks(task_list2);
    }

    @Test
    public void TestGetCategory() throws IOException
    {
        taskRepository.deleteAll();
        categoryRepository.deleteAll();

        TaskDTO taskDTO1 = objectMapper.readValue(new ClassPathResource("task1.json").getFile(), TaskDTO.class);
        taskService.createTask(taskDTO1);

        CategoryDTO categoryDTO2 = objectMapper.readValue(new ClassPathResource("category2.json").getFile(), CategoryDTO.class);
        categoryService.createCategory(categoryDTO2);

        rootUrl = rootUrl + "/get?id=2";

        ResponseEntity<CategoryDTO> response = restTemplate.getForEntity(rootUrl, CategoryDTO.class);

        log.info("category : {}", response.getBody());

        assertAll(
                () -> assertNotNull(response.getBody()),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode())
        );
    }

    @Test
    public void TestCreateCategory() throws IOException
    {
        taskRepository.deleteAll();
        categoryRepository.deleteAll();

        rootUrl = rootUrl + "/create";

        TaskDTO taskDTO1 = objectMapper.readValue(new ClassPathResource("task1.json").getFile(), TaskDTO.class);
        TaskDTO t1 =taskService.createTask(taskDTO1);
        log.info("task 1 is : {}", t1);

        TaskDTO taskDTO2 = objectMapper.readValue(new ClassPathResource("task2.json").getFile(),TaskDTO.class);
        TaskDTO t2 =taskService.createTask(taskDTO2);
        log.info("task 2 is : {}", t2);

        CategoryDTO categoryDTO = objectMapper.readValue(new ClassPathResource("category3.json").getFile(),CategoryDTO.class);

        ResponseEntity<CategoryDTO> response = restTemplate.postForEntity(rootUrl, categoryDTO, CategoryDTO.class);
        log.info("category is : {}", response.getBody());

        assertAll(
                () -> assertNotNull(response.getBody()),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode())
        );
    }

    @Test
    public void TestGetTasksInCategory() throws IOException
    {
        taskRepository.deleteAll();
        categoryRepository.deleteAll();

        TaskDTO taskDTO1 = objectMapper.readValue(new ClassPathResource("task1.json").getFile(), TaskDTO.class);
        taskService.createTask(taskDTO1);

        rootUrl = rootUrl + "/allTask?id=1";

        ResponseEntity<List<TaskDTO>> response = restTemplate.exchange(rootUrl, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
        log.info("tasks in category: {}", response.getBody());

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(response.getBody())
        );
    }

    @Test
    public void TestUpdateCategory() throws IOException
    {
        taskRepository.deleteAll();
        categoryRepository.deleteAll();

        rootUrl = rootUrl + "/update";

        TaskDTO taskDTO1 = objectMapper.readValue(new ClassPathResource("task1.json").getFile(), TaskDTO.class);
        taskService.createTask(taskDTO1);

        String new_category = "furniture";
        String url = UriComponentsBuilder.fromHttpUrl(rootUrl)
                .queryParam("new", new_category)
                .queryParam("id", 1)
                .toUriString();

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<?> requestEntity = new HttpEntity<>(requestHeaders);

        ResponseEntity<?> response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, CategoryDTO.class);
        log.info("category updated: {}", response.getStatusCode());

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(response.getBody())
        );
    }

    @Test
    public void TestDeleteCategory() throws IOException
    {
        taskRepository.deleteAll();
        categoryRepository.deleteAll();

        rootUrl = rootUrl + "/delete?id=1";

        TaskDTO taskDTO1 = objectMapper.readValue(new ClassPathResource("task1.json").getFile(), TaskDTO.class);
        taskService.createTask(taskDTO1);

        ResponseEntity<?> response = restTemplate.exchange(rootUrl, HttpMethod.DELETE, null, Void.class);
        log.info("category deleted: {}", response.getStatusCode());

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNull(response.getBody())
        );
    }

}