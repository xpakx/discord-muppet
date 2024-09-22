package io.github.xpakx.discord_muppet.conversation;

import com.microsoft.playwright.Page;
import io.github.xpakx.discord_muppet.model.Friend;
import io.github.xpakx.discord_muppet.page.PageWrapper;
import io.github.xpakx.discord_muppet.websocket.WebsocketService;
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
import java.util.concurrent.TimeUnit;

@Service
public class ConversationWrapper {
    private final String serverUrl;
    private final Page page;
    private String htmlCache = "";
    private boolean watch = false;
    private List<MessageItem> messages = new ArrayList<>();
    Set<String> loadedIds = ConcurrentHashMap.newKeySet();
    Map<String, String> usernames = new ConcurrentHashMap<>();
    private final WebsocketService websocketService;

    public ConversationWrapper(
            PageWrapper pageWrapper,
            @Value("${discord.url}") String serverUrl,
            WebsocketService websocketService
    ) {
        this.page = pageWrapper.newPage();
        this.serverUrl = serverUrl;
        this.websocketService = websocketService;
    }

    public void openChannel(Friend contact) {
        stopWatching();
        loadedIds.clear();
        usernames.clear();
        page.navigate(serverUrl + contact.channelUrl());
    }

    public List<MessageItem> openChannelRest(Friend contact) throws InterruptedException {
        openChannel(contact);
        TimeUnit.SECONDS.sleep(5); // TODO
        var html = page.content();
        Document doc = Jsoup.parse(html);
        messages = processMessages(doc);
        startWatching();
        websocketService.openConversation(messages);
        return messages; // TODO: this should be deleted
    }

    public List<MessageItem> currentChannel() {
        return messages;
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
        var messages = processMessages(doc);
        // TODO Add new messages to cache
        websocketService.updateConversation(messages);
    }

    public List<MessageItem> processMessages(Document doc) {
        var messages = fetchMessages(doc)
                .stream()
                .map(this::checkUsernames)
                .toList();
        messages
                .stream()
                .filter((m) -> m.type() == MessageType.Message)
                .forEach((m) -> loadedIds.add(m.message().id()));
        return messages;
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
