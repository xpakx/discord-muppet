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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    public List<MessageItem> getMessages() {
        Document doc = Jsoup.parse(page.content()); // TODO

        var chatWrapper = doc.select("main[class^=chatContent]");
        var messageContainers = chatWrapper.select("ol[role=list]");
        return messageContainers.select("> *")
                .stream()
                .map(this::toMessageList)
                .flatMap(List::stream)
                .toList();
    }

    public List<MessageItem> toMessageList(Element e) {
        if ("separator".equals(e.attr("role"))) {
            if (e.id().equals("---new-messages-bar")) {
                return List.of(MessageItem.newSeparator());
            }
            return List.of(MessageItem.separator()); // timestamp?
        }
        if (e.hasAttr("class") && e.attr("class").startsWith("messageListItem")) {
            return e.select("> *")
                    .stream()
                    .map(this::toMessage)
                    .toList();
        }
        return List.of();
    }

    private MessageItem toMessage(Element element) {
        var content = element.selectFirst("div[id^=message-content]")
                        .text();
        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        var timeElem = element.selectFirst("time")
                .attr("datetime");
        var time = LocalDateTime.parse(timeElem, formatter);
        return MessageItem.of(new Message(content, time));
    }
}
