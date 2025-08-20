package com.example.ToDoApp.service;

import com.example.ToDoApp.entity.User;
import com.example.ToDoApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public User registerUser(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("このユーザー名は既に使用されています");
        }
        
        if (username == null || username.trim().length() < 3) {
            throw new IllegalArgumentException("ユーザー名は3文字以上で入力してください");
        }
        
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("パスワードは6文字以上で入力してください");
        }
        
        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(username.trim(), hashedPassword);
        return userRepository.save(user);
    }
    
    public Optional<User> authenticateUser(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }
        
        Optional<User> userOpt = userRepository.findByUsername(username.trim());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return Optional.of(user);
            }
        }
        
        return Optional.empty();
    }
    
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
}