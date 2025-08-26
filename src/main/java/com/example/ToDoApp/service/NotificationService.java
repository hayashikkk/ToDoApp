package com.example.ToDoApp.service;

import com.example.ToDoApp.entity.Todo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "notification.enabled", havingValue = "true", matchIfMissing = false)
public class NotificationService {
    
    @Value("${slack.webhook.url}")
    private String webhookUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    public void sendDueTomorrowNotification(List<Todo> dueTomorrowTasks) {
        if (dueTomorrowTasks.isEmpty()) {
            return; // 通知対象がない場合は何もしない
        }
        
        String message = createNotificationMessage(dueTomorrowTasks);
        sendSlackMessage(message);
    }
    
    private String createNotificationMessage(List<Todo> tasks) {
        StringBuilder message = new StringBuilder();
        message.append("📅 *明日が期限のタスクがあります！*\n\n");
        
        for (Todo task : tasks) {
            message.append("• ").append(task.getText())
                   .append(" (ユーザー: ").append(task.getUser().getUsername()).append(")")
                   .append(" - 期限: ").append(task.getDueDate().format(DateTimeFormatter.ofPattern("MM月dd日")))
                   .append("\n");
        }
        
        message.append("\n忘れずに完了させましょう！ 💪");
        return message.toString();
    }
    
    private void sendSlackMessage(String message) {
        try {
            // Slack Webhook用のペイロード作成
            Map<String, Object> payload = new HashMap<>();
            payload.put("text", message);
            payload.put("username", "TodoBot");
            payload.put("icon_emoji", ":calendar:");
            
            // HTTPヘッダー設定
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // HTTPリクエスト作成
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            
            // Slack Webhookに送信
            restTemplate.postForEntity(webhookUrl, request, String.class);
            
            System.out.println("Slack通知を送信しました: " + message);
            
        } catch (Exception e) {
            System.err.println("Slack通知の送信に失敗しました: " + e.getMessage());
        }
    }
    
    public void sendTestNotification() {
        sendSlackMessage("🧪 テスト通知: ToDoアプリの通知機能が正常に動作しています！");
    }
}