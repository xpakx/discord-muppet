package io.github.xpakx.discord_muppet.conversation;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class MessageProcessor {
    private final ConversationWrapper conversation;
    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private Thread thread;
    private volatile boolean running = true;

    public MessageProcessor(ConversationWrapper conversation) {
        this.conversation = conversation;
    }

    public void pushMessage(String message) {
        messageQueue.add(message);
    }

    @PostConstruct
    public void start() {
        thread = new Thread(() -> {
            while (running) {
                try {
                    String message = messageQueue.take();
                    System.out.println("New message " + message);
                    conversation.sendMessage(message);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    if (!running) {
                        break;
                    }
                }
            }
        });
        thread.start();;
    }

    @PreDestroy
    public void stop() {
        running = false;
        thread.interrupt();
    }
}
