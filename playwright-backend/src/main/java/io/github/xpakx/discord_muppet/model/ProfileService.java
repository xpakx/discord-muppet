package io.github.xpakx.discord_muppet.model;

import io.github.xpakx.discord_muppet.model.dto.FriendData;
import io.github.xpakx.discord_muppet.notification.NotificationService;
import io.github.xpakx.discord_muppet.websocket.WebsocketService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProfileService {
    private User user;
    private List<Friend> contacts;
    private Map<String, Integer> notifications = new HashMap<>();
    private final WebsocketService websocketService;

    public ProfileService(WebsocketService websocketService) {
        this.websocketService = websocketService;
    }

    // TODO
    public void saveUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    // TODO
    public void saveContacts(List<Friend> contacts) {
        this.contacts = contacts;
    }

    public List<Friend> getContacts() {
        return contacts;
    }

    public List<FriendData> getFriends() {
        return contacts
                .stream()
                .map(this::toFriendData)
                .toList();
    }

    private FriendData toFriendData(Friend friend) {
        var notifications = getNotificationsFor(friend.channelId());
        return new FriendData(
                friend.visibleName(),
                friend.username(),
                friend.description(),
                friend.status(),
                notifications > 0,
                notifications
        );
    }

    public void saveNotifications(Map<String, Integer> notifications) {
        this.notifications = notifications;
        websocketService.updateFriendList(getFriends());
    }

    public Integer getNotificationsFor(String channelId) {
        return this.notifications
                .getOrDefault(channelId, 0);
    }
}
