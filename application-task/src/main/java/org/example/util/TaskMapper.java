package org.example.util;

import java.util.Objects;
import org.example.dto.TaskDto;
import org.example.entity.TaskEntity;
import org.example.entity.enums.TaskStatus;
import org.springframework.stereotype.Component;

@Component
public final class TaskMapper {

    private TaskMapper() {}

    public TaskDto toModel(TaskEntity entity) {
        return TaskDto.builder()
                        .id(entity.getId())
                        .title(entity.getTitle())
                        .description(entity.getDescription())
                        .userId(entity.getUserId())
                        .status(entity.getStatus().name())
                        .build();
    }

    public TaskEntity fromDto(TaskDto dto) {
        var status = Objects.isNull(dto.getStatus()) ? TaskStatus.NEW : TaskStatus.valueOf(dto.getStatus());
        return TaskEntity.builder()
                         .title(dto.getTitle())
                         .description(dto.getDescription())
                         .userId(dto.getUserId())
                         .status(status)
                         .build();
    }

}
