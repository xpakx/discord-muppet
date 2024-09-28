package io.github.xpakx.discord_muppet.model;

import io.github.xpakx.discord_muppet.conversation.ConversationWrapper;
import io.github.xpakx.discord_muppet.conversation.MessageItem;
import io.github.xpakx.discord_muppet.conversation.MessageProcessor;
import io.github.xpakx.discord_muppet.model.dto.FriendData;
import io.github.xpakx.discord_muppet.web.dto.ConversationResponse;
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
    private final ConversationWrapper conversationWrapper;
    private final MessageProcessor messageProcessor;

    public ProfileService(WebsocketService websocketService,
                          ConversationWrapper conversationWrapper,
                          MessageProcessor messageProcessor) {
        this.websocketService = websocketService;
        this.conversationWrapper = conversationWrapper;
        this.messageProcessor = messageProcessor;
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

    public List<MessageItem> openChannel(String friendUsername) {
        var friend = contacts.stream()
                .filter((f) -> f.username().equals(friendUsername))
                .findAny()
                .orElseThrow();
        try {
            return conversationWrapper.openChannelRest(friend);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public ConversationResponse currentChannel() {
        return conversationWrapper.currentChannel();
    }

    public void sendMessage(String message) {
        messageProcessor.pushMessage(message);
    }
}
