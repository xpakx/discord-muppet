package io.github.xpakx.discord_muppet.model;

import java.util.Optional;

public enum Status {
    Online, Invisible, Idle, DoNotDisturb, Unknown;

    public static Status toStatus(String string) {
        return switch (string) {
            case "Online" -> Online;
            case "Invisible" -> Invisible;
            case "Idle" -> Idle;
            case "Do Not Disturb" -> DoNotDisturb;
            default -> Unknown;
        };
    }
}
