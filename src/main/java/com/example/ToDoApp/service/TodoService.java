package com.example.ToDoApp.service;

import com.example.ToDoApp.entity.Todo;
import com.example.ToDoApp.entity.User;
import com.example.ToDoApp.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TodoService {
    
    @Autowired
    private TodoRepository todoRepository;
    
    public Todo createTodo(String text, User user) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("ToDoの内容は必須です");
        }
        
        if (text.trim().length() > 255) {
            throw new IllegalArgumentException("ToDoの内容は255文字以下で入力してください");
        }
        
        Todo todo = new Todo(text.trim(), user);
        return todoRepository.save(todo);
    }
    
    public List<Todo> getTodosByUser(User user) {
        return todoRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    public List<Todo> getTodosByUserAndStatus(User user, Boolean completed) {
        return todoRepository.findByUserAndCompletedOrderByCreatedAtDesc(user, completed);
    }
    
    public Optional<Todo> findById(Long id) {
        return todoRepository.findById(id);
    }
    
    public Todo updateTodo(Todo todo, String newText) {
        if (newText == null || newText.trim().isEmpty()) {
            throw new IllegalArgumentException("ToDoの内容は必須です");
        }
        
        if (newText.trim().length() > 255) {
            throw new IllegalArgumentException("ToDoの内容は255文字以下で入力してください");
        }
        
        todo.setText(newText.trim());
        return todoRepository.save(todo);
    }
    
    public Todo toggleTodoStatus(Todo todo) {
        todo.setCompleted(!todo.getCompleted());
        return todoRepository.save(todo);
    }
    
    public void deleteTodo(Todo todo) {
        todoRepository.delete(todo);
    }
    
    public long getTodoCountByUser(User user) {
        return todoRepository.countByUser(user);
    }
    
    public long getCompletedTodoCountByUser(User user) {
        return todoRepository.countByUserAndCompleted(user, true);
    }
    
    public long getPendingTodoCountByUser(User user) {
        return todoRepository.countByUserAndCompleted(user, false);
    }
    
    public boolean isTodoOwnedByUser(Todo todo, User user) {
        return todo.getUser().getId().equals(user.getId());
    }
}