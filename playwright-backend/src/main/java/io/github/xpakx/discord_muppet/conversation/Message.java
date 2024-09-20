package io.github.xpakx.discord_muppet.conversation;

import java.time.LocalDateTime;

public record Message(
        String content,
        LocalDateTime timestamp,
        boolean chainStart,
        String id,
        String username
        ) {
}
