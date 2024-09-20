package io.github.xpakx.discord_muppet.conversation;

public record MessageItem(MessageType type, Message message) {

    public static MessageItem separator() {
        return  new MessageItem(MessageType.Separator, null);
    }

    public static MessageItem newSeparator() {
        return  new MessageItem(MessageType.NewSeparator, null);
    }

    public static MessageItem of(Message message) {
        return  new MessageItem(MessageType.Message, message);
    }
}
