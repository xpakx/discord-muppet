package io.github.xpakx.discord_muppet.conversation;

import java.time.LocalDateTime;

public record Message(
        String content,
        LocalDateTime timestamp,
        boolean chainStart,
        String id,
        String username,
        String parentId
        ) {

        public Message withUsername(String username) {
                return new Message(
                        this.content,
                        this.timestamp,
                        this.chainStart,
                        this.id,
                        username,
                        this.parentId
                );
        }
}
