package io.github.xpakx.discord_muppet.screenshot;

import com.microsoft.playwright.Page;
import io.github.xpakx.discord_muppet.conversation.ConversationWrapper;
import io.github.xpakx.discord_muppet.page.PageWrapper;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Pointcut;

import java.time.Instant;

@Aspect
@Service
@ConditionalOnProperty(prefix = "debug", name = "screenshot", havingValue = "true")
public class ScreenshotService {
    Logger logger = LoggerFactory.getLogger(ScreenshotService.class);
    private final PageWrapper page;
    private final ConversationWrapper conversation;

    public ScreenshotService(PageWrapper page, @Lazy ConversationWrapper conversation) {
        this.page = page;
        this.conversation = conversation;
    }

    @Pointcut("@annotation(debugScreenshot)")
    public void debugScreenshotPointcut(DebugScreenshot debugScreenshot) {
    }

    @AfterReturning(value = "debugScreenshotPointcut(cacheIncrement)", argNames = "joinPoint,cacheIncrement")
    public void screenshot(JoinPoint joinPoint, DebugScreenshot cacheIncrement) {
        String prefix = cacheIncrement.prefix();
        String name = prefix  + "_" + Instant.now().toString() + ".png";
        switch (cacheIncrement.wrapper()) {
            case Main -> page.makeScreenshot(name);
            case Conversation -> conversation.makeScreenshot(name);
            default -> {}
        }
    }
}
