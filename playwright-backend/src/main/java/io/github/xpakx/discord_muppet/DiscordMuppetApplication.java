package io.github.xpakx.discord_muppet;

import io.github.xpakx.discord_muppet.model.ProfileService;
import io.github.xpakx.discord_muppet.page.PageWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class DiscordMuppetApplication implements CommandLineRunner {
	@Value("${discord.credentials.email}")
	String email;

	@Value("${discord.credentials.password}")
	String password;

	@Autowired
	PageWrapper page;

	@Autowired
	ProfileService profileService;

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
		System.out.println(page.getStatus());
		System.out.println(page.getContacts());
	}
}
