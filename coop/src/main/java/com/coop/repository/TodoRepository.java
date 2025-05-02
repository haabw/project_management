package com.coop.repository;

  import com.coop.entity.TodoEntity;
  import com.coop.entity.UserEntity;
  import org.springframework.data.jpa.repository.JpaRepository;
  import java.util.List;

  public interface TodoRepository extends JpaRepository<TodoEntity, Integer> {
      List<TodoEntity> findByUser(UserEntity user);
  }