package io.github.xpakx.discord_muppet.page;

import com.microsoft.playwright.*;
import io.github.xpakx.discord_muppet.screenshot.DebugScreenshot;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;

@Component
public class PageWrapper {
    private final Page page;
    private final Browser browser;
    private final BrowserContext context;
    private final Playwright playwright;
    Logger logger = LoggerFactory.getLogger(PageWrapper.class);

    public PageWrapper(Playwright.CreateOptions options, @Value("${user.agent}") String userAgentValue) {
        final String userAgent = "--user-agent=%s".formatted(userAgentValue);
        try  {
            this.playwright = Playwright.create(options);
            logger.info("Starting browser");
            this.browser = playwright.chromium().launch(
                   new BrowserType.LaunchOptions()
                           .setArgs(List.of(userAgent))
            );
            this.context = browser.newContext();
            context.addInitScript(Paths.get("scripts/preload.js"));
            logger.info("Opening new tab");
            this.page = context.newPage();
        } catch (Exception e) {
            logger.error("Couldn't start browser!");
            throw new RuntimeException(e);
        }
    }

    public void goToLogin() {
        logger.info("Navigating to login page");
        page.navigate("https://discord.com/app");
    }

    @DebugScreenshot()
    public void fillLoginForm(String email, String password) {
        logger.info(
                "Filling login form with email: {} and password {}",
                email,
                "*".repeat(password.length())
        );
        var emailInput = page.locator("input[name='email']");
        emailInput.pressSequentially(email,  new Locator.PressSequentiallyOptions());
        var passwordInput = page.locator("input[name='password']");
        passwordInput.pressSequentially(password,  new Locator.PressSequentiallyOptions().setDelay(100));

        var button = page.locator("button[type=\"submit\"]");
        var box = button.boundingBox();
        page.mouse().move(box.x + box.width/2, box.y + box.height/2);
        button.hover();
        button.click();
    }

    public String title() {
        return page.title();
    }

    public void makeScreenshot() {
        String name = "screenshot_" + Instant.now().toString() + ".png";
        makeScreenshot(name);
    }

    public void makeScreenshot(String name) {
        Path root  = Path.of("debug");
        page.screenshot(
                new Page.ScreenshotOptions().setPath(root.resolve(name))
        );
    }

    @PreDestroy
    public void preDestroy() {
        logger.info("Closing browser…");
        playwright.close();
    }
}
