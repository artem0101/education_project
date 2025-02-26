package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import org.example.model.TaskModel;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "education", name = "tasks")
public class TaskEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tasks_id_seq")
    @SequenceGenerator(name = "tasks_id_seq", sequenceName = "education.tasks_id_seq", allocationSize = 1)
    private long id;

    private String title;

    private String description;

    @Column(name = "user_id")
    private Long userId;

    public TaskModel toModel() {
        return TaskModel.builder()
                        .id(this.id)
                        .title(this.title)
                        .description(this.description)
                        .build();
    }

    public static TaskEntity fromModel(TaskModel model) {
        return TaskEntity.builder()
                         .title(model.getTitle())
                         .description(model.getDescription())
                         .build();
    }

}
