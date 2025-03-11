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

import org.testcontainers.shaded.com.google.common.net.HttpHeaders;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
        getRequest(33, HttpStatus.NOT_FOUND.value());
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

        postRequest(taskDto, HttpStatus.OK.value());

        var taskListResult = given(requestSpecification)
                .when()
                .get(TEST_URL)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(1))
                .log()
                .ifValidationFails(LogDetail.ALL)
                .extract()
                .body()
                .as(new TypeRef<List<TaskDto>>() {})
                .getLast();

        var taskId = taskListResult.getId();

        assertThat(taskListResult.getId()).isEqualTo(taskId);
        assertThat(taskListResult.getTitle()).isEqualTo(title);
        assertThat(taskListResult.getDescription()).isEqualTo(description);
        assertThat(taskListResult.getUserId()).isNull();
        assertThat(taskListResult.getStatus()).isEqualTo(TaskStatus.NEW.toString());

        var taskResult = getRequest(taskId, HttpStatus.OK.value()).extract()
                                                                  .body()
                                                                  .as(TaskDto.class);

        assertThat(taskResult.getId()).isEqualTo(taskId);
        assertThat(taskResult.getTitle()).isEqualTo(title);
        assertThat(taskResult.getDescription()).isEqualTo(description);
        assertThat(taskResult.getUserId()).isNull();
        assertThat(taskResult.getStatus()).isEqualTo(TaskStatus.NEW.toString());
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

        given(requestSpecification)
                .header("Content-type", "application/json")
                .and()
                .body(dto)
                .when()
                .put(TEST_URL + "/{id}", id)
                .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .log()
                .ifValidationFails(LogDetail.ALL);
    }

    @Test
    void deleteUnExistedTaskTest() {
        var id = 3333;
        getRequest(id, HttpStatus.NOT_FOUND.value());

        given(requestSpecification)
                .when()
                .delete(TEST_URL + "/" + id)
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    private ValidatableResponse postRequest(TaskDto dto, int statusCode) {
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

    private ValidatableResponse getRequest(long id, int statusCode) {
        return given(requestSpecification)
                .when()
                .get(TEST_URL + "/" + id)
                .then()
                .statusCode(statusCode)
                .log()
                .ifValidationFails(LogDetail.ALL);
    }

}
