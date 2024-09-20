package io.github.xpakx.discord_muppet.conversation;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.github.xpakx.discord_muppet.model.Friend;
import io.github.xpakx.discord_muppet.page.PageWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationWrapper {
    private final String serverUrl;
    private final Page page;

    public ConversationWrapper(
            PageWrapper pageWrapper,
            @Value("${discord.url}") String serverUrl
    ) {
        this.page = pageWrapper.newPage();
        this.serverUrl = serverUrl;
    }

    public void openChannel(Friend contact) {
        page.navigate(serverUrl + contact.channelUrl());
    }

    public List<String> getMessages() {
        var chatWrapper = getLocatorWithoutWaiting("main[class^=chatContent]");
        var messageContainers = getLocatorWithoutWaiting(chatWrapper, "ol[role=list]");
        return messageContainers.locator("> *")
                .all()
                .stream()
                .map(this::toMessageList)
                .flatMap(List::stream)
                .toList();
    }

    public List<String> toMessageList(Locator locator) {
        if ("separator".equals(locator.getAttribute("role"))) {
            return List.of("-----" + locator.getAttribute("aria-label") + "----");
        }
        if (locator.getAttribute("class") != null && locator.getAttribute("class").startsWith("messageListItem")) {
            return locator.locator("> *")
                    .all()
                    .stream()
                    .map(this::toMessage)
                    .toList();
        }
        return List.of();
    }

    private String toMessage(Locator locator) {
        return getLocatorWithoutWaiting(locator, "div[id^=message-content]")
                .innerText();
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
}
