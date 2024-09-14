package io.github.xpakx.discord_muppet.page;

import com.microsoft.playwright.*;
import io.github.xpakx.discord_muppet.model.Status;
import io.github.xpakx.discord_muppet.model.User;
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
import java.util.concurrent.TimeUnit;

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
            this.browser = playwright.firefox().launch(
                   new BrowserType.LaunchOptions()
                           .setArgs(List.of(userAgent))
                           // .setHeadless(false)
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

    public User getStatus() {
        var statusWrapper = getLocatorWithoutWaiting("section[aria-label='User area']");
        var userData = getLocatorWithoutWaiting(statusWrapper, "div[class^='nameTag']")
                .innerText();
        var statusSplit = userData.split("\n");
        if (statusSplit.length < 3) {
            throw new RuntimeException("Corrupted status");
        }
        var avatarWrapper = getLocatorWithoutWaiting(statusWrapper, "div[class^=avatarWrapper]");
        var status = getLocatorWithoutWaiting(avatarWrapper, "div[role=img]")
               .getAttribute("aria-label")
               .split(", ")[1];
        return new User(
                statusSplit[0],
                statusSplit[1],
                statusSplit[2],
                Status.toStatus(status)
        );
    }

    private Locator getLocatorWithoutWaiting(Locator parent, String selector) {
        var locator = parent.locator(selector);
        locator.waitFor(new Locator.WaitForOptions().setTimeout(0));
        return locator;
    }

    private Locator getLocatorWithoutWaiting(String selector) {
        var locator = page.locator(selector);
        locator.waitFor(new Locator.WaitForOptions().setTimeout(0));
        return locator;
    }

    @PreDestroy
    public void preDestroy() {
        logger.info("Closing browser…");
        playwright.close();
    }
}
