package io.github.xpakx.discord_muppet;

import io.github.xpakx.discord_muppet.conversation.ConversationWrapper;
import io.github.xpakx.discord_muppet.model.ProfileService;
import io.github.xpakx.discord_muppet.notification.NotificationService;
import io.github.xpakx.discord_muppet.page.PageWrapper;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableScheduling
public class DiscordMuppetApplication implements CommandLineRunner {
	@Value("${discord.credentials.email}")
	String email;

	@Value("${discord.credentials.password}")
	String password;

	@Autowired
	PageWrapper page;

	@Autowired
	ProfileService profileService;

	@Autowired
	NotificationService notificationService;

	public static void main(String[] args) {
		SpringApplication.run(DiscordMuppetApplication.class, args);
	}

	@Override
	public void run(String... arg0) throws Exception {
		page.goToLogin();
		if (!page.hasCookies()) {
			page.fillLoginForm(email, password);
			TimeUnit.SECONDS.sleep(5);
			// TODO: test login
			page.saveCookies();
		}
		System.out.println(page.url());
		page.makeScreenshot();

		var status = page.getStatus();
		profileService.saveUser(status);
		var contacts = page.getContacts();
		profileService.saveContacts(contacts);

		var doc = Jsoup.parse(page.content().get());
		var notifications = notificationService.getNotifications(doc);
		profileService.saveNotifications(notifications);

		System.out.println(status);
		System.out.println(contacts);
		System.out.println(notifications);

		notificationService.startWatching();
	}
}
