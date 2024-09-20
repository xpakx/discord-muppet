package io.github.xpakx.discord_muppet;

import io.github.xpakx.discord_muppet.conversation.ConversationWrapper;
import io.github.xpakx.discord_muppet.model.ProfileService;
import io.github.xpakx.discord_muppet.notification.NotificationService;
import io.github.xpakx.discord_muppet.page.PageWrapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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

	@Autowired
	ConversationWrapper conversationWrapper;

	public static void main(String[] args) {
		SpringApplication.run(DiscordMuppetApplication.class, args);
	}

	@Override
	public void run(String... arg0) throws Exception {
		page.goToLogin();
		page.fillLoginForm(email, password);
		TimeUnit.SECONDS.sleep(5);
		System.out.println(page.url());
		// TODO: test login
		page.makeScreenshot();

		var status = page.getStatus();
		profileService.saveUser(status);
		var contacts = page.getContacts();
		profileService.saveContacts(contacts);
		conversationWrapper.openChannel(contacts.getFirst());
		conversationWrapper.startWatching();
		var messages = conversationWrapper.getMessages();

		Document doc = Jsoup.parse(page.content().get());
		var notificationMap = notificationService.getNotifications(doc);
		notificationService.saveNotifications(notificationMap);

		System.out.println(status);
		System.out.println(contacts);
		System.out.println(notificationMap);
		System.out.println(messages);

		notificationService.startWatching();
	}
}
