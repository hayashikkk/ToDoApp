package com.example.ToDoApp.service;

import com.example.ToDoApp.entity.Todo;
import com.example.ToDoApp.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@ConditionalOnProperty(name = "notification.enabled", havingValue = "true", matchIfMissing = false)
public class ScheduledService {
    
    @Autowired
    private TodoRepository todoRepository;
    
    @Autowired(required = false)
    private NotificationService notificationService;
    
    @Value("${notification.enabled:false}")
    private boolean notificationEnabled;
    
    @Value("${notification.time.hour:9}")
    private int notificationHour;
    
    @Value("${notification.time.minute:0}")
    private int notificationMinute;
    
    @PostConstruct
    public void init() {
        System.out.println("=== ScheduledService初期化 ===");
        System.out.println("通知機能: " + (notificationEnabled ? "有効" : "無効"));
        System.out.println("通知時刻: " + String.format("%02d:%02d", notificationHour, notificationMinute));
        System.out.println("NotificationService: " + (notificationService != null ? "利用可能" : "null"));
        System.out.println("現在時刻: " + LocalTime.now());
        System.out.println("================================");
    }
    
    /**
     * 毎日指定された時刻に実行される期限前日通知
     * デフォルト: 毎日9:00に実行
     */
    @Scheduled(cron = "0 ${notification.time.minute:0} ${notification.time.hour:9} * * *")
    @Transactional(readOnly = true)
    public void checkDueTomorrowTasks() {
        System.out.println("期限前日タスクチェックを開始します...");
        
        if (notificationService == null) {
            System.out.println("NotificationServiceが無効になっています");
            return;
        }
        
        try {
            // 明日が期限のタスクを取得
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            List<Todo> dueTomorrowTasks = todoRepository.findByDueDateAndCompletedFalse(tomorrow);
            
            System.out.println("明日期限のタスク数: " + dueTomorrowTasks.size());
            
            if (!dueTomorrowTasks.isEmpty()) {
                // Slack通知を送信
                notificationService.sendDueTomorrowNotification(dueTomorrowTasks);
                System.out.println("Slack通知を送信しました");
            } else {
                System.out.println("明日期限のタスクはありません");
            }
            
        } catch (Exception e) {
            System.err.println("期限前日タスクチェック中にエラーが発生しました: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * テスト用: 1分ごとに実行されるテストスケジュール
     * 動作確認用 - 本番環境では削除またはコメントアウト推奨
     */
    // @Scheduled(fixedRate = 60000)
    public void testSchedule() {
        System.out.println("【テストスケジュール】実行時刻: " + LocalTime.now() + " - ScheduledServiceは正常に動作しています");
    }
    
    /**
     * 手動でテスト通知を送信するメソッド
     */
    public void sendTestNotification() {
        if (notificationService != null) {
            notificationService.sendTestNotification();
            System.out.println("テスト通知を送信しました");
        } else {
            System.out.println("NotificationServiceが利用できません");
        }
    }
}