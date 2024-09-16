package io.github.xpakx.discord_muppet.model;

import io.github.xpakx.discord_muppet.model.dto.FriendData;
import io.github.xpakx.discord_muppet.notification.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileService {
    private final NotificationService notificationService;
    private User user;
    private List<Friend> contacts;

    public ProfileService(NotificationService notificationService) {
        this.notificationService = notificationService;
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
        var notifications = notificationService.getNotificationsFor(friend.channelId());
        return new FriendData(
                friend.visibleName(),
                friend.username(),
                friend.description(),
                friend.status(),
                notifications > 0,
                notifications
        );
    }
}
