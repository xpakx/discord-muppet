package io.github.xpakx.discord_muppet.conversation;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.github.xpakx.discord_muppet.model.Friend;
import io.github.xpakx.discord_muppet.page.PageWrapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
        Document doc = Jsoup.parse(page.content()); // TODO

        var chatWrapper = doc.select("main[class^=chatContent]");
        var messageContainers = chatWrapper.select("ol[role=list]");
        return messageContainers.select("> *")
                .stream()
                .map(this::toMessageList)
                .flatMap(List::stream)
                .toList();
    }

    public List<String> toMessageList(Element e) {
        if ("separator".equals(e.attr("role"))) {
            return List.of("-----" + e.attr("aria-label") + "----");
        }
        if (e.hasAttr("class") && e.attr("class").startsWith("messageListItem")) {
            return e.select("> *")
                    .stream()
                    .map(this::toMessage)
                    .toList();
        }
        return List.of();
    }

    private String toMessage(Element element) {
        return element.select("div[id^=message-content]")
                .text();
    }
}
