package com.example.ToDoApp.controller;

import com.example.ToDoApp.entity.Todo;
import com.example.ToDoApp.entity.User;
import com.example.ToDoApp.service.TodoService;
import com.example.ToDoApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
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
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                response.put("success", false);
                response.put("message", "認証が必要です");
                return ResponseEntity.status(401).body(response);
            }
            
            Optional<User> userOpt = userService.findById(userId);
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
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                response.put("success", false);
                response.put("message", "認証が必要です");
                return ResponseEntity.status(401).body(response);
            }
            
            Optional<User> userOpt = userService.findById(userId);
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "ユーザーが見つかりません");
                return ResponseEntity.status(404).body(response);
            }
            
            String text = todoRequest.get("text");
            Todo todo = todoService.createTodo(text, userOpt.get());
            
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
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                response.put("success", false);
                response.put("message", "認証が必要です");
                return ResponseEntity.status(401).body(response);
            }
            
            Optional<User> userOpt = userService.findById(userId);
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
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                response.put("success", false);
                response.put("message", "認証が必要です");
                return ResponseEntity.status(401).body(response);
            }
            
            Optional<User> userOpt = userService.findById(userId);
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
        return todoMap;
    }
}