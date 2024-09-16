package io.github.xpakx.discord_muppet.model;

public enum Status {
    Online, Invisible, Idle, DoNotDisturb, Offline, Unknown;

    public static Status toStatus(String string) {
        return switch (string) {
            case "Online", "Dostępny" -> Online;
            case "Invisible", "Niewidoczny" -> Invisible;
            case "Idle" -> Idle;
            case "Do Not Disturb" -> DoNotDisturb;
            case "Offline", "Niedostępny" -> Offline;
            default -> Unknown;
        };
    }
}
