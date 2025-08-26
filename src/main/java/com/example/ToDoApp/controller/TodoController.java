package com.example.ToDoApp.controller;

import com.example.ToDoApp.entity.Todo;
import com.example.ToDoApp.entity.User;
import com.example.ToDoApp.service.TodoService;
import com.example.ToDoApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/todos")
public class TodoController {
    
    @Autowired
    private TodoService todoService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getTodos(
            @RequestParam(required = false) String filter,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = authentication.getName();
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "ユーザーが見つかりません");
                return ResponseEntity.status(404).body(response);
            }
            
            User user = userOpt.get();
            List<Todo> todos;
            
            if ("completed".equals(filter)) {
                todos = todoService.getTodosByUserAndStatus(user, true);
            } else if ("pending".equals(filter)) {
                todos = todoService.getTodosByUserAndStatus(user, false);
            } else {
                todos = todoService.getTodosByUser(user);
            }
            
            List<Map<String, Object>> todoList = todos.stream()
                .map(this::convertTodoToMap)
                .collect(Collectors.toList());
            
            response.put("success", true);
            response.put("todos", todoList);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Todo取得中にエラーが発生しました");
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createTodo(
            @RequestBody Map<String, String> todoRequest,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = authentication.getName();
            Optional<User> userOpt = userService.findByUsername(username);
            
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "ユーザーが見つかりません");
                return ResponseEntity.status(404).body(response);
            }
            
            String text = todoRequest.get("text");
            String dueDateStr = todoRequest.get("dueDate");
            
            Todo todo;
            if (dueDateStr != null && !dueDateStr.trim().isEmpty()) {
                // 日付のみの場合
                try {
                    LocalDate dueDate = LocalDate.parse(dueDateStr);
                    todo = todoService.createTodoWithDueDate(text, userOpt.get(), dueDate);
                } catch (DateTimeParseException e) {
                    response.put("success", false);
                    response.put("message", "無効な日付形式です");
                    return ResponseEntity.ok(response);
                }
            } else {
                // 期日なしの場合
                todo = todoService.createTodo(text, userOpt.get());
            }
            
            response.put("success", true);
            response.put("todo", convertTodoToMap(todo));
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Todo作成中にエラーが発生しました");
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateTodo(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updateRequest,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = authentication.getName();
            Optional<User> userOpt = userService.findByUsername(username);
            Optional<Todo> todoOpt = todoService.findById(id);
            
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "ユーザーが見つかりません");
                return ResponseEntity.status(404).body(response);
            }
            
            if (!todoOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Todoが見つかりません");
                return ResponseEntity.status(404).body(response);
            }
            
            Todo todo = todoOpt.get();
            User user = userOpt.get();
            
            if (!todoService.isTodoOwnedByUser(todo, user)) {
                response.put("success", false);
                response.put("message", "このTodoを変更する権限がありません");
                return ResponseEntity.status(403).body(response);
            }
            
            if (updateRequest.containsKey("text")) {
                String newText = (String) updateRequest.get("text");
                todo = todoService.updateTodo(todo, newText);
            }
            
            // 期日の更新処理
            if (updateRequest.containsKey("dueDate")) {
                String dueDateStr = (String) updateRequest.get("dueDate");
                
                try {
                    LocalDate dueDate = null;
                    
                    if (dueDateStr != null && !dueDateStr.trim().isEmpty()) {
                        // 日付のみの場合
                        dueDate = LocalDate.parse(dueDateStr);
                    }
                    // nullの場合はdueDateはnullのまま
                    
                    todo = todoService.updateTodoDueDate(todo, dueDate);
                } catch (DateTimeParseException e) {
                    response.put("success", false);
                    response.put("message", "無効な日付形式です");
                    return ResponseEntity.ok(response);
                }
            }
            
            if (updateRequest.containsKey("completed")) {
                Boolean completed = (Boolean) updateRequest.get("completed");
                if (!completed.equals(todo.getCompleted())) {
                    todo = todoService.toggleTodoStatus(todo);
                }
            }
            
            response.put("success", true);
            response.put("todo", convertTodoToMap(todo));
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Todo更新中にエラーが発生しました");
        }
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTodo(
            @PathVariable Long id,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = authentication.getName();
            Optional<User> userOpt = userService.findByUsername(username);
            Optional<Todo> todoOpt = todoService.findById(id);
            
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "ユーザーが見つかりません");
                return ResponseEntity.status(404).body(response);
            }
            
            if (!todoOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Todoが見つかりません");
                return ResponseEntity.status(404).body(response);
            }
            
            Todo todo = todoOpt.get();
            User user = userOpt.get();
            
            if (!todoService.isTodoOwnedByUser(todo, user)) {
                response.put("success", false);
                response.put("message", "このTodoを削除する権限がありません");
                return ResponseEntity.status(403).body(response);
            }
            
            todoService.deleteTodo(todo);
            
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Todo削除中にエラーが発生しました");
        }
        
        return ResponseEntity.ok(response);
    }
    
    private Map<String, Object> convertTodoToMap(Todo todo) {
        Map<String, Object> todoMap = new HashMap<>();
        todoMap.put("id", todo.getId());
        todoMap.put("text", todo.getText());
        todoMap.put("completed", todo.getCompleted());
        todoMap.put("createdAt", todo.getCreatedAt().toString());
        if (todo.getUpdatedAt() != null) {
            todoMap.put("updatedAt", todo.getUpdatedAt().toString());
        }
        if (todo.getDueDate() != null) {
            todoMap.put("dueDate", todo.getDueDate().toString());
        }
        return todoMap;
    }
}