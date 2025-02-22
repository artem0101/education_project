package org.example.repository;

import java.util.Optional;
import org.example.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {

    Optional<TaskEntity> findById(long id);

    @Modifying
    @Query("UPDATE TaskEntity t SET t.title = :title, t.description = :description, t.userId = :userId WHERE t.id = :id")
    void update(long id, String title, String description, Long userId);

    void deleteById(long id);

}
