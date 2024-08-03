package com.advancedtaskmanager.IntegrationTest.controller;

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

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Slf4j
public class TaskControllerIT {
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
    public void setUp() {
        rootUrl = "http://localhost:" + port + "/task";

        taskRepository.deleteAll();
        categoryRepository.deleteAll();

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
                .priority(2)
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
    public void TestAllTasks() throws IOException
    {
        rootUrl = rootUrl + "/all";

        Task t1 = taskRepository.save(task1);

        assertNotNull(t1.getId());
        System.out.println("Task ID: " + t1.getId());

        Task t2 = taskRepository.save(task2);

        assertNotNull(t2.getId());
        System.out.println("Task ID: " + t2.getId());

        ParameterizedTypeReference<List<TaskDTO>> response_type = new ParameterizedTypeReference<List<TaskDTO>>() {};
        ResponseEntity<List<TaskDTO>> response = restTemplate.exchange(rootUrl, HttpMethod.GET, null, response_type);
        log.info("tasks list: {}", response.getBody());

        assertAll(
                () -> assertNotNull(response.getBody()),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode())
        );
    }

    @Test
    public void TestCreateTask() throws IOException
    {
        rootUrl = rootUrl + "/create";

        TaskDTO taskDTO1 = objectMapper.readValue(new ClassPathResource("task1.json").getFile(), TaskDTO.class);
        ResponseEntity<TaskDTO> response1 = restTemplate.postForEntity(rootUrl, taskDTO1, TaskDTO.class);
        log.info("task 1 is : {}", response1.getBody());

        TaskDTO taskDTO2 = objectMapper.readValue(new ClassPathResource("task2.json").getFile(),TaskDTO.class);
        ResponseEntity<TaskDTO> response2 = restTemplate.postForEntity(rootUrl, taskDTO2, TaskDTO.class);
        log.info("task 2 is : {}", response2.getBody());

        assertAll(
                () -> assertNotNull(response1.getBody()),
                () -> assertNotNull(response2.getBody()),
                () -> assertEquals(HttpStatus.OK, response1.getStatusCode()),
                () -> assertEquals(HttpStatus.OK, response2.getStatusCode()),
                () -> assertEquals(taskDTO1, response1.getBody()),
                () -> assertEquals(taskDTO2, response2.getBody())
        );
    }

    @Test
    public void TestGetTask() throws IOException
    {
        rootUrl = rootUrl + "/task?id=1";

        TaskDTO taskDTO = objectMapper.readValue(new ClassPathResource("task2.json").getFile(), TaskDTO.class);
        taskService.createTask(taskDTO);

        ResponseEntity<TaskDTO> response = restTemplate.getForEntity(rootUrl, TaskDTO.class);
        log.info("task dto is : {}", response.getBody());
        assertAll(
                () -> assertNotNull(response.getBody()),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode())
        );
    }

    @Test
    public void TestGetTasksByCategory() throws IOException
    {
        TaskDTO taskDTO1 = objectMapper.readValue(new ClassPathResource("task1.json").getFile(), TaskDTO.class);
        TaskDTO t1 = taskService.createTask(taskDTO1);
        log.info("taskByCategory 1 is : {}", t1);

        TaskDTO taskDTO2 = objectMapper.readValue(new ClassPathResource("task2.json").getFile(),TaskDTO.class);
        TaskDTO t2 = taskService.createTask(taskDTO2);
        log.info("taskByCategory 2 is : {}", t2);

        String categoryName = "class-work";
        rootUrl = rootUrl + "/taskByCategory?name=" + categoryName;
        ParameterizedTypeReference<List<TaskDTO>> response_type = new ParameterizedTypeReference<List<TaskDTO>>() {};
        ResponseEntity<List<TaskDTO>> response = restTemplate.exchange(rootUrl, HttpMethod.GET, null, response_type);
        log.info("tasks : {}", response.getBody());
        assertAll(
                () -> assertNotNull(response.getBody()),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode())
        );
    }

    @Test
    public void TestGetTasksByDate() throws IOException
    {
        TaskDTO taskDTO1 = objectMapper.readValue(new ClassPathResource("task1.json").getFile(), TaskDTO.class);
        taskService.createTask(taskDTO1);

        TaskDTO taskDTO2 = objectMapper.readValue(new ClassPathResource("task2.json").getFile(),TaskDTO.class);
        taskService.createTask(taskDTO2);

        rootUrl = rootUrl + "/taskByDate?scheduledDate=2024-07-15";
        ParameterizedTypeReference<List<TaskDTO>> response_type = new ParameterizedTypeReference<List<TaskDTO>>() {};
        ResponseEntity<List<TaskDTO>> response = restTemplate.exchange(rootUrl, HttpMethod.GET, null, response_type);
        log.info("tasksByDate : {}", response.getBody());

        assertAll(
                () -> assertNotNull(response.getBody()),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode())
        );
    }

    @Test
    public void TestDeleteTask() throws IOException
    {
        TaskDTO taskDTO1 = objectMapper.readValue(new ClassPathResource("task1.json").getFile(), TaskDTO.class);
        taskService.createTask(taskDTO1);

        TaskDTO taskDTO2 = objectMapper.readValue(new ClassPathResource("task2.json").getFile(),TaskDTO.class);
        taskService.createTask(taskDTO2);

        TaskDTO taskDTO3 = objectMapper.readValue(new ClassPathResource("task3.json").getFile(),TaskDTO.class);
        taskService.createTask(taskDTO3);

        Optional<Category> del_cat = categoryRepository.findByName(taskDTO3.getCategory());

        rootUrl = rootUrl + "/delete?id=3";

        ResponseEntity<?> response = restTemplate.exchange(rootUrl, HttpMethod.DELETE, null, Void.class);
        categoryService.deleteCategory(del_cat.get().getId());
        log.info("deleted? {}", response.getStatusCode());

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode())
        );
    }

    @Test
    public void TestUpdateTaskByTitle() throws IOException
    {
        TaskDTO taskDTO1 = objectMapper.readValue(new ClassPathResource("task1.json").getFile(), TaskDTO.class);
        taskService.createTask(taskDTO1);

        rootUrl = rootUrl + "/update?id=1&title=task_01";

        ResponseEntity<?> response = restTemplate.exchange(rootUrl, HttpMethod.PUT, null, Void.class);
        log.info("task updated: {}", response.getStatusCode());

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode())
        );
    }

    @Test
    public void TestUpdateTaskByDescription() throws IOException
    {
        TaskDTO taskDTO1 = objectMapper.readValue(new ClassPathResource("task1.json").getFile(), TaskDTO.class);
        taskService.createTask(taskDTO1);

        String description = "task_01 description_01 is there.";
        rootUrl = rootUrl + "/updateDescription";
        String url = UriComponentsBuilder.fromHttpUrl(rootUrl)
                .queryParam("id", 1)
                .queryParam("description", description)
                .toUriString();

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<?> requestEntity = new HttpEntity<>(requestHeaders);

        ResponseEntity<?> response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);

        log.info("task description updated: {}", response.getStatusCode());

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode())
        );
    }

    @Test
    public void TestUpdateTaskByCategory() throws IOException
    {
        TaskDTO taskDTO1 = objectMapper.readValue(new ClassPathResource("task1.json").getFile(), TaskDTO.class);
        taskService.createTask(taskDTO1);

        String categoryName = "social-work";

        rootUrl = rootUrl + "/updateCategory";

        String url = UriComponentsBuilder.fromHttpUrl(rootUrl)
                .queryParam("id", 1)
                .queryParam("category_name", categoryName)
                .toUriString();

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<?> requestEntity = new HttpEntity<>(requestHeaders);

        ResponseEntity<?> response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);

        log.info("task category updated: {}", response.getStatusCode());

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode())
        );
    }

    @Test
    public void TestUpdateDate() throws IOException
    {
        TaskDTO taskDTO1 = objectMapper.readValue(new ClassPathResource("task1.json").getFile(), TaskDTO.class);
        taskService.createTask(taskDTO1);

        rootUrl = rootUrl + "/updateDate";

        LocalDate localDate = LocalDate.of(2024, 1,23);

        String url = UriComponentsBuilder.fromHttpUrl(rootUrl)
                .queryParam("id", 1)
                .queryParam("scheduledDate", localDate)
                .toUriString();

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<?> requestEntity = new HttpEntity<>(requestHeaders);

        ResponseEntity<?> response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);

        log.info("task date updated: {}", response.getStatusCode());

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode())
        );
    }

    @Test
    public void TestSetTaskPriority()  throws IOException
    {
        TaskDTO taskDTO1 = objectMapper.readValue(new ClassPathResource("task1.json").getFile(), TaskDTO.class);
        taskService.createTask(taskDTO1);

        rootUrl = rootUrl + "/priority";

        String url = UriComponentsBuilder.fromHttpUrl(rootUrl)
                .queryParam("id", 1)
                .queryParam("priority", 3)
                .toUriString();

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<?> requestEntity = new HttpEntity<>(requestHeaders);

        ResponseEntity<?> response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);

        log.info("task priority: {}", response.getStatusCode());

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode())
        );
    }

    @Test
    public void TestCompleteTask() throws IOException
    {
        TaskDTO taskDTO = objectMapper.readValue(new ClassPathResource("task1.json").getFile(), TaskDTO.class);
        taskService.createTask(taskDTO);

        rootUrl = rootUrl + "/completed";

        String url = UriComponentsBuilder.fromHttpUrl(rootUrl)
                .queryParam("id", 1)
                .queryParam("completed", true)
                .toUriString();

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<?> requestEntity = new HttpEntity<>(requestHeaders);

        ResponseEntity<?> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Void.class);

        log.info("task completed: {}", response.getStatusCode());

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode())
        );
    }

    @Test
    public void TestPostponeTask() throws IOException
    {
        TaskDTO taskDTO1 = objectMapper.readValue(new ClassPathResource("task1.json").getFile(), TaskDTO.class);
        taskService.createTask(taskDTO1);

        rootUrl = rootUrl + "/postpone";

        LocalDate localDate = LocalDate.of(2024, 10,27);

        String url = UriComponentsBuilder.fromHttpUrl(rootUrl)
                .queryParam("id", 1)
                .queryParam("newDate", localDate)
                .toUriString();

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<?> requestEntity = new HttpEntity<>(requestHeaders);

        ResponseEntity<?> response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);

        log.info("task date postponed: {}", response.getStatusCode());

        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode())
        );


    }
}
