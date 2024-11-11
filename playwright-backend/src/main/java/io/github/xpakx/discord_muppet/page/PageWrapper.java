package io.github.xpakx.discord_muppet.page;

import com.microsoft.playwright.*;
import io.github.xpakx.discord_muppet.cookie.CookieService;
import io.github.xpakx.discord_muppet.model.Friend;
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
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class PageWrapper {
    private final Page page;
    private final Browser browser;
    private final BrowserContext context;
    private final Playwright playwright;
    private final String serverUrl;
    private final CookieService cookies;
    private boolean cookiesLoaded = false;
    Logger logger = LoggerFactory.getLogger(PageWrapper.class);

    public PageWrapper(
            Playwright.CreateOptions options,
            @Value("${user.agent}") String userAgentValue,
            @Value("${discord.url}") String serverUrl,
            CookieService cookies
    ) {
        this.cookies = cookies;
        var currentCookies = cookies.loadCookies();
        final String userAgent = "--user-agent=%s".formatted(userAgentValue);
        this.serverUrl = serverUrl;
        try  {
            this.playwright = Playwright.create(options);
            logger.info("Starting browser");
            this.browser = playwright.firefox().launch(
                   new BrowserType.LaunchOptions()
                           .setArgs(List.of(userAgent))
                           // .setHeadless(false)
            );
            this.context = browser.newContext();
            if (currentCookies != null && !currentCookies.isEmpty()) {
                context.addCookies(currentCookies);
                cookiesLoaded = true;
            }
            context.addInitScript(Paths.get("scripts/preload.js"));

            logger.info("Opening new tab");
            this.page = context.newPage();
        } catch (Exception e) {
            logger.error("Couldn't start browser!");
            throw new RuntimeException(e);
        }
    }

    @DebugScreenshot(prefix = "newPage")
    public Page newPage() {
        return context.newPage();
    }

    public void goToLogin() {
        logger.info("Navigating to login page");
        page.navigate(serverUrl + "/app");
    }

    @DebugScreenshot(prefix = "login")
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

    public void saveCookies() {
        var currentCookies = context.cookies(serverUrl);
        cookies.saveCookies(currentCookies);
    }

    public String title() {
        return page.title();
    }

    public String url() {
        return page.url();
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

    public List<Friend> getContacts() {
        var privateChannels = getLocatorWithoutWaiting("div[data-list-id^='private-channels']");
        var directMsgs = getLocatorWithoutWaiting(privateChannels, "ul[class^='content_']");
         return directMsgs.locator("h2 ~ *")
                .all()
                .stream()
                .map(this::toFriend)
                .toList();
    }

    private Friend toFriend(Locator locator) {
        var link = getLocatorWithoutWaiting(locator, "a[class^=link]")
                .getAttribute("href");
        var channelSplit = link.split("/");
        var channelId = channelSplit[channelSplit.length-1];

        var name = getLocatorWithoutWaiting(locator, "div[class^=name_]")
                .innerText();
        var description = getOptionalLocator(locator, "div[class^=subtext]")
                .map(Locator::innerText)
                .orElse("");

        var statusData = getOptionalLocator(locator, "div[role=img]")
                .map((l) -> l.getAttribute("aria-label"))
                .map((l) -> l.split(", "))
                .orElse(new String[]{name, "Unknown"});
        var shortName = statusData[0];
        var status = statusData.length == 1 ? Status.Unknown: Status.toStatus(statusData[1]);

        return new Friend(
                name,
                shortName,
                description,
                link,
                channelId,
                status
        );
    }

    public User getStatus() {
        var statusWrapper = getLocatorWithoutWaiting("section[class^='panels_']");
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

    private Optional<Locator> getOptionalLocator(Locator parent, String selector) {
        var locator = parent.locator(selector);
        if (locator.count() == 0) {
            return Optional.empty();
        }
        return Optional.of(locator);
    }

    @PreDestroy
    public void preDestroy() {
        logger.info("Closing browser…");
        playwright.close();
    }

    public void goTo(String url) {
        page.navigate(url);
    }

    public Optional<String> content() {
        return page != null ? Optional.ofNullable(page.content()) : Optional.empty();
    }

    public boolean hasCookies() {
        return cookiesLoaded;
    }

    public boolean isLoggedIn() {
        var body = page.locator("ul[data-list-id='guildsnav']");
        return body.count() != 0;
    }

    public void closeModals() throws Exception {
        var modalCloseButtons = page
                .locator("div[class^=closeButton]");
        logger.info("Modals found: {}", modalCloseButtons.count());
        if (modalCloseButtons.count() == 0) {
            return;
        }
        int closedModals = 0;
        for (var button : modalCloseButtons.all()) {
            String hiddenAttr = button.getAttribute("aria-hidden");
            boolean hidden = hiddenAttr != null && hiddenAttr.equals("true");
            if (hidden) {
                continue;
            }
            button.click();
            closedModals++;
            TimeUnit.MILLISECONDS.sleep(300);
        }
        logger.info("Modals closed: {}", closedModals);
    }
}
