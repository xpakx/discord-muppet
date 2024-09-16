package io.github.xpakx.discord_muppet.notification;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService {
    private Map<String, Integer> notifications = new HashMap<>();

    public void saveNotifications(Map<String, Integer> notifications) {
        this.notifications = notifications;
    }

    public Integer getNotificationsFor(String channelId) {
        return this.notifications
                .getOrDefault(channelId, 0);
    }
}
