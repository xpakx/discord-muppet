package io.github.xpakx.discord_muppet.conversation;

import com.microsoft.playwright.Page;
import io.github.xpakx.discord_muppet.model.Friend;
import io.github.xpakx.discord_muppet.page.PageWrapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConversationWrapper {
    private final String serverUrl;
    private final Page page;
    private String htmlCache = "";
    private boolean watch = false;
    private List<MessageItem> messages;
    Set<String> loadedIds = ConcurrentHashMap.newKeySet();
    Map<String, String> usernames = new ConcurrentHashMap<>();

    public ConversationWrapper(
            PageWrapper pageWrapper,
            @Value("${discord.url}") String serverUrl
    ) {
        this.page = pageWrapper.newPage();
        this.serverUrl = serverUrl;
    }

    public void openChannel(Friend contact) {
        page.navigate(serverUrl + contact.channelUrl());
        stopWatching();
        loadedIds.clear();
        usernames.clear();
    }

    public void startWatching() {
        watch = true;
    }

    public void stopWatching() {
        watch = false;
    }

    @Scheduled(fixedRate= 300)
    public void checkMessages() {
        if (!watch) {
            return;
        }
        var html = page.content();
        if (html.isEmpty()) {
            return;
        }
        if (html.equals(htmlCache)) {
            return;
        }
        htmlCache = html;
        Document doc = Jsoup.parse(html);
        messages = fetchMessages(doc)
                .stream()
                .map(this::checkUsernames)
                .toList();
        messages
                .stream()
                .filter((m) -> m.type() == MessageType.Message)
                .forEach((m) -> loadedIds.add(m.message().id()));
        System.out.println(messages);
    }

    private MessageItem checkUsernames(MessageItem m) {
        if (m.type() != MessageType.Message) {
            return m;
        }
        if (m.message().chainStart()) {
            return m;
        }
        var username = usernames.getOrDefault(m.message().parentId(), null);
        if (username != null) {
            return MessageItem.of(m.message().withUsername(username));
        }
        return m;
    }

    public List<MessageItem> getMessages() {
        return messages;
    }

    public List<MessageItem> fetchMessages(Document doc) {
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
                    .filter(Objects::nonNull)
                    .toList();
        }
        return List.of();
    }

    private MessageItem toMessage(Element element) {
        var idSplit = element.attr("data-list-item-id")
                .split("-");
        var id = idSplit[idSplit.length-1];
        if (loadedIds.contains(id)) {
            return null; // TODO: check if edited
        }
        var content = element.selectFirst("div[id^=message-content]")
                        .text();
        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        var timeElem = element.selectFirst("time")
                .attr("datetime");
        var time = LocalDateTime.parse(timeElem, formatter);
        var chainStart = element.classNames()
                .stream()
                .anyMatch((s) -> s.startsWith("groupStart"));
        var username = "";
        var parentId = id;
        if (chainStart) {
            username = element.selectFirst("span[class^=username_]")
                    .text();
            usernames.put(id, username);
        } else {
            parentId = element.attr("aria-labelledby")
                    .split("message-username-")[1]
                    .split(" ")[0];
        }
        return MessageItem.of(new Message(content, time, chainStart, id, username, parentId));
    }
}