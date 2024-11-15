package io.github.xpakx.discord_muppet.notification;

import io.github.xpakx.discord_muppet.model.ProfileService;
import io.github.xpakx.discord_muppet.page.NotificationElem;
import io.github.xpakx.discord_muppet.page.PageWrapper;
import io.github.xpakx.discord_muppet.websocket.WebsocketService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    private final PageWrapper page;
    private boolean watch = false;
    private String htmlCache = "";
    private final ProfileService profileService;

    public NotificationService(PageWrapper page, ProfileService profileService) {
        this.page = page;
        this.profileService = profileService;
    }

    @Scheduled(cron= "0/10 * * ? * *")
    public void checkNotifications() {
        if (!watch) {
            return;
        }
        var html = page.content();
        if (html.isEmpty()) {
            return;
        }
        if (html.get().equals(htmlCache)) {
            return;
        }
        htmlCache = html.get();
        Document doc = Jsoup.parse(html.get());
        var notifications = getNotifications(doc);
        System.out.println(notifications);
        profileService.saveNotifications(notifications);
    }

    public void startWatching() {
        watch = true;
    }

    public void stopWatching() {
        watch = false;
    }

    public Map<String, Integer> getNotifications(Document doc) {
        var nav = doc.select("nav[class^=wrapper]");
        return nav.select("div[class^=listItem_]")
                .stream()
                .map(this::toNotification)
                .filter((n) -> n.count() > 0)
                .collect(
                        Collectors.toMap(
                                NotificationElem::channel,
                                NotificationElem::count,
                                (oldValue, newValue) -> newValue,
                                HashMap::new
                        )
                );
    }

    private NotificationElem toNotification(Element element) {
        var channel = element.select("div[data-list-item-id^=guildsnav]");
        if (channel.isEmpty()) {
            return new NotificationElem("", 0);
        }
        var channelId = channel
                .getFirst()
                .attr("data-list-item-id")
                .split("___")[1];
        var count = element.select("div[class^=lowerBadge]");
        if (count.isEmpty()) {
            return new NotificationElem(channelId, 0);
        }
        var countValue = Integer.parseInt(count.getFirst().text());
        return new NotificationElem(channelId, countValue);
    }
}
