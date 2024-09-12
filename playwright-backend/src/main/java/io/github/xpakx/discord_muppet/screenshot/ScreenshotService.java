package io.github.xpakx.discord_muppet.screenshot;

import com.microsoft.playwright.Page;
import io.github.xpakx.discord_muppet.page.PageWrapper;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

    public ScreenshotService(PageWrapper page) {
        this.page = page;
    }

    @Pointcut("@annotation(debugScreenshot)")
    public void debugScreenshotPointcut(DebugScreenshot debugScreenshot) {
    }

    @AfterReturning(value = "debugScreenshotPointcut(cacheIncrement)", argNames = "joinPoint,cacheIncrement")
    public void incrementAfterReturning(JoinPoint joinPoint, DebugScreenshot cacheIncrement) {
        String prefix = cacheIncrement.prefix();
        String name = prefix  + "_" + Instant.now().toString() + ".png";
        page.makeScreenshot(name);
    }
}
