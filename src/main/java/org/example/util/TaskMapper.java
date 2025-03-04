package org.example.util;

import java.util.Objects;
import org.example.dto.TaskDto;
import org.example.entity.TaskEntity;
import org.example.entity.enums.TaskStatus;
import org.springframework.stereotype.Component;

@Component
public final class TaskMapper {

    private TaskMapper() {}

    public static TaskDto toModel(TaskEntity entity) {
        return TaskDto.builder()
                        .id(entity.getId())
                        .title(entity.getTitle())
                        .description(entity.getDescription())
                        .status(entity.getStatus().name())
                        .build();
    }

    public static TaskEntity fromDto(TaskDto dto) {
        var status = Objects.isNull(dto.getStatus()) ? TaskStatus.NEW : TaskStatus.valueOf(dto.getStatus());
        return TaskEntity.builder()
                         .title(dto.getTitle())
                         .description(dto.getDescription())
                         .status(status)
                         .build();
    }

}
