package com.example.ToDoApp.repository;

import com.example.ToDoApp.entity.Todo;
import com.example.ToDoApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
    
    List<Todo> findByUserOrderByCreatedAtDesc(User user);
    
    List<Todo> findByUserAndCompletedOrderByCreatedAtDesc(User user, Boolean completed);
    
    @Query("SELECT COUNT(t) FROM Todo t WHERE t.user = :user")
    long countByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(t) FROM Todo t WHERE t.user = :user AND t.completed = :completed")
    long countByUserAndCompleted(@Param("user") User user, @Param("completed") Boolean completed);
}