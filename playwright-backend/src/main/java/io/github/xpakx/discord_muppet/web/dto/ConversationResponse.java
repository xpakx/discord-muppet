package io.github.xpakx.discord_muppet.web.dto;

import io.github.xpakx.discord_muppet.conversation.MessageItem;

import java.util.List;

public record ConversationResponse(List<MessageItem> messages, String username) {
}
