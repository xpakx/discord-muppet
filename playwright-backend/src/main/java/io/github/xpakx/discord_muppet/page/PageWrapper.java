package io.github.xpakx.discord_muppet.page;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.Instant;

@Component
public class PageWrapper {
    private final Page page;
    private final Browser browser;
    private final Playwright playwright;
    Logger logger = LoggerFactory.getLogger(PageWrapper.class);

    public PageWrapper() {
        try  {
            this.playwright = Playwright.create();
            logger.info("Starting browser");
            this.browser = playwright.chromium().launch();
            logger.info("Opening new tab");
            this.page = browser.newPage();
        } catch (Exception e) {
            logger.error("Couldn't start browser!");
            throw new RuntimeException(e);
        }
    }

    public void goToLogin() {
        logger.info("Navigating to login page");
        page.navigate("https://discord.com/app");
    }

    public void fillLoginForm(String email, String password) {
        logger.info(
                "Filling login form with email: {} and password {}",
                email,
                "*".repeat(password.length())
        );
        page.locator("input[name='email']")
                .fill(email);
        page.locator("input[name='password']")
                .fill(password);
    }

    public String title() {
       return page.title();
    }

    public void makeScreenshot() {
        Path root  = Path.of("debug");
        String name = "screenshot_" + Instant.now().toString() + ".png";
        page.screenshot(
                new Page.ScreenshotOptions().setPath(root.resolve(name))
        );
    }

    @PreDestroy
    public void preDestroy() {
        logger.info("Closing browser…");
        browser.close();
        playwright.close();
    }
}
