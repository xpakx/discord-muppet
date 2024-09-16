package io.github.xpakx.discord_muppet.model.dto;

import io.github.xpakx.discord_muppet.model.Status;

public record FriendData(String visibleName,
                         String username,
                         String description,
                         Status status,
                         boolean newMessages,
                         Integer notifications
) {
}
