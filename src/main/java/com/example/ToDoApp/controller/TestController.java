package com.example.ToDoApp.controller;

import com.example.ToDoApp.service.ScheduledService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @Autowired(required = false)
    private ScheduledService scheduledService;
    
    /**
     * Slack通知のテストエンドポイント
     * ブラウザで http://localhost:8081/api/test/notification にアクセス
     */
    @GetMapping("/notification")
    public ResponseEntity<Map<String, Object>> testNotification() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (scheduledService != null) {
                scheduledService.sendTestNotification();
                response.put("success", true);
                response.put("message", "テスト通知を送信しました");
            } else {
                response.put("success", false);
                response.put("message", "ScheduledServiceが利用できません（通知機能が無効になっている可能性があります）");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "エラーが発生しました: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 期限前日チェックを手動実行するテストエンドポイント
     * ブラウザで http://localhost:8081/api/test/check-tomorrow にアクセス
     */
    @GetMapping("/check-tomorrow")
    public ResponseEntity<Map<String, Object>> testCheckTomorrowTasks() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (scheduledService != null) {
                scheduledService.checkDueTomorrowTasks();
                response.put("success", true);
                response.put("message", "期限前日チェックを実行しました。コンソールログを確認してください。");
            } else {
                response.put("success", false);
                response.put("message", "ScheduledServiceが利用できません");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "エラーが発生しました: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}