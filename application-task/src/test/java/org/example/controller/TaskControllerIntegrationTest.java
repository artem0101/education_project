package org.example.controller;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.common.mapper.TypeRef;
import io.restassured.filter.log.LogDetail;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import java.util.List;
import org.example.dto.TaskDto;
import org.example.entity.enums.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.google.common.net.HttpHeaders;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.core.Is.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@EmbeddedKafka(
        partitions = 1,
        topics = {"tasks"},
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:9092",
                "log.dirs=/tmp/kafka-logs"
        }
)
class TaskControllerIntegrationTest {

    private static final String TEST_URL = "/api/v1/tasks";

    @LocalServerPort
    private Integer port;

    private RequestSpecification requestSpecification;

    @BeforeEach
    void setUpAbstractIntegrationTest() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        requestSpecification = new RequestSpecBuilder()
                .setPort(port)
                .addHeader(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .build();
    }

    @Test
    void findNonExistedTaskTest() {
        getTaskById(33, HttpStatus.NOT_FOUND.value());
    }

    @Test
    void findAllTasksWhenDoesntCreatedTaskTest() {
        given(requestSpecification)
                .when()
                .get(TEST_URL)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(0))
                .log()
                .ifValidationFails(LogDetail.ALL);
    }

    @Test
    void createTaskTest() {
        var title = "Task Title";
        var description = "Description";
        var taskDto = TaskDto.builder()
                             .title(title)
                             .description(description)
                             .build();

        createTask(taskDto, HttpStatus.OK.value());

        var task = getLastTask();
        var taskId = task.getId();

        validateNewTask(task, taskId, title, description);

        task = getTaskById(taskId, HttpStatus.OK.value()).extract()
                                                         .body()
                                                         .as(TaskDto.class);

        validateNewTask(task, taskId, title, description);
    }

    @Test
    void updateUnExistedTaskTest() {
        var id = 404L;
        var title = "Task Title unexisted";
        var description = "Description unexisted";
        var dto = TaskDto.builder()
                         .id(id)
                         .title(title)
                         .description(description)
                         .build();

        updateTask(id, dto, HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void updateTaskTest() {
        var title = "Task Title for update";
        var description = "Description for update";

        var dto = TaskDto.builder()
                         .title(title)
                         .description(description)
                         .build();

        createTask(dto, HttpStatus.OK.value());

        var task = getLastTask();
        var taskId = task.getId();

        var newTitle = "Task Title new";
        var newDescription = "Description new";
        var status = TaskStatus.IN_PROGRESS.toString();
        var userId = 100L;
        dto = TaskDto.builder()
                     .id(taskId)
                     .title(newTitle)
                     .description(newDescription)
                     .status(status)
                     .userId(userId)
                     .build();

        updateTask(taskId, dto, HttpStatus.OK.value());


        task = getTaskById(taskId, HttpStatus.OK.value()).extract()
                                                                   .body()
                                                                   .as(TaskDto.class);

        validateTask(task, taskId, newTitle, newDescription, userId, status);

        task = getLastTask();

        validateTask(task, taskId, newTitle, newDescription, userId, status);
    }

    @Test
    void deleteUnExistedTaskTest() {
        var id = 3333;
        getTaskById(id, HttpStatus.NOT_FOUND.value());

        deleteTask(id, HttpStatus.OK.value());

        given(requestSpecification)
                .when()
                .delete(TEST_URL + "/" + id)
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    private ValidatableResponse createTask(TaskDto dto, int statusCode) {
        return given(requestSpecification)
                .header("Content-type", "application/json")
                .and()
                .body(dto)
                .when()
                .post(TEST_URL)
                .then()
                .statusCode(statusCode)
                .log()
                .ifValidationFails(LogDetail.ALL);
    }

    private ValidatableResponse updateTask(long id, TaskDto dto, int statusCode) {
        return given(requestSpecification)
                .header("Content-type", "application/json")
                .and()
                .body(dto)
                .when()
                .put(TEST_URL + "/{id}", id)
                .then()
                .statusCode(statusCode)
                .log()
                .ifValidationFails(LogDetail.ALL);
    }

    private ValidatableResponse getTaskById(long id, int statusCode) {
        return given(requestSpecification)
                .when()
                .get(TEST_URL + "/" + id)
                .then()
                .statusCode(statusCode)
                .log()
                .ifValidationFails(LogDetail.ALL);
    }

    private List<TaskDto> getAllTasks(int size, int statusCode) {
        return given(requestSpecification)
                .when()
                .get(TEST_URL)
                .then()
                .statusCode(statusCode)
                .body("size()", greaterThanOrEqualTo(size))
                .log()
                .ifValidationFails(LogDetail.ALL)
                .extract()
                .body()
                .as(new TypeRef<>() {});
    }

    private ValidatableResponse deleteTask(long id, int statusCode) {
        return given(requestSpecification)
                .when()
                .delete(TEST_URL + "/" + id)
                .then()
                .statusCode(statusCode);
    }

    private TaskDto getLastTask() {
        return getAllTasks(1, HttpStatus.OK.value()).getLast();
    }

    private void validateNewTask(TaskDto dto, long taskId, String title, String description) {
        assertThat(dto.getId()).isEqualTo(taskId);
        assertThat(dto.getTitle()).isEqualTo(title);
        assertThat(dto.getDescription()).isEqualTo(description);
        assertThat(dto.getUserId()).isNull();
        assertThat(dto.getStatus()).isEqualTo(TaskStatus.NEW.toString());
    }

    private void validateTask(TaskDto dto, long taskId, String title, String description, long userId, String status) {
        assertThat(dto.getId()).isEqualTo(taskId);
        assertThat(dto.getTitle()).isEqualTo(title);
        assertThat(dto.getDescription()).isEqualTo(description);
        assertThat(dto.getUserId()).isEqualTo(userId);
        assertThat(dto.getStatus()).isEqualTo(status);
    }

}
