package io.github.xpakx.discord_muppet.websocket;

import io.github.xpakx.discord_muppet.conversation.MessageItem;
import io.github.xpakx.discord_muppet.model.dto.FriendData;
import io.github.xpakx.discord_muppet.web.dto.ConversationResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WebsocketService {
    private final SimpMessagingTemplate simpMessagingTemplate;

    public WebsocketService(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public void updateFriendList(List<FriendData> msg) {
        simpMessagingTemplate.convertAndSend("/topic/friends", msg);
    }

    public void updateConversation(List<MessageItem> msg) {
        simpMessagingTemplate.convertAndSend("/topic/current", msg);
    }

    public void openConversation(ConversationResponse msg) {
        simpMessagingTemplate.convertAndSend("/topic/open", msg);
    }
}
