package com.example.ToDoApp.controller;

import com.example.ToDoApp.entity.User;
import com.example.ToDoApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    @GetMapping("/register")
    public String register() {
        return "register";
    }
    
    @PostMapping("/register")
    public String registerUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {
        
        try {
            if (!password.equals(confirmPassword)) {
                model.addAttribute("error", "パスワードが一致しません");
                model.addAttribute("username", username);
                return "register";
            }
            
            userService.registerUser(username, password);
            return "redirect:/login?registered=true";
            
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", username);
            return "register";
        } catch (Exception e) {
            model.addAttribute("error", "ユーザー登録中にエラーが発生しました");
            model.addAttribute("username", username);
            return "register";
        }
    }
}