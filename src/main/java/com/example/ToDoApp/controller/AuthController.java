package com.example.ToDoApp.controller;

import com.example.ToDoApp.entity.User;
import com.example.ToDoApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> loginRequest,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");
            
            Optional<User> userOpt = userService.authenticateUser(username, password);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                session.setAttribute("userId", user.getId());
                session.setAttribute("username", user.getUsername());
                
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                
                response.put("success", true);
                response.put("user", userInfo);
            } else {
                response.put("success", false);
                response.put("message", "ユーザー名またはパスワードが正しくありません");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "ログイン処理中にエラーが発生しました");
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @RequestBody Map<String, String> registerRequest,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = registerRequest.get("username");
            String password = registerRequest.get("password");
            
            User user = userService.registerUser(username, password);
            
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            
            response.put("success", true);
            response.put("user", userInfo);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "ユーザー登録中にエラーが発生しました");
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            session.invalidate();
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "ログアウト処理中にエラーが発生しました");
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkAuth(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        Long userId = (Long) session.getAttribute("userId");
        String username = (String) session.getAttribute("username");
        
        if (userId != null && username != null) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", userId);
            userInfo.put("username", username);
            
            response.put("authenticated", true);
            response.put("user", userInfo);
        } else {
            response.put("authenticated", false);
        }
        
        return ResponseEntity.ok(response);
    }
}