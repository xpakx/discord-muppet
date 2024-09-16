package io.github.xpakx.discord_muppet.model;

public record Friend(String visibleName,
                     String username,
                     String description,
                     String channelUrl,
                     String channelId,
                     Status status) {
}
