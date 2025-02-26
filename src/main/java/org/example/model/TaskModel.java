package org.example.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TaskModel {

    private Long id;

    private String title;

    private String description;

    private Long userId;

}
