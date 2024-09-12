package io.github.xpakx.discord_muppet;

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

	public static void main(String[] args) {
		SpringApplication.run(DiscordMuppetApplication.class, args);
	}

	@Override
	public void run(String... arg0) throws Exception {
		page.goToLogin();
		System.out.println(page.title());
		page.fillLoginForm(email, password);
		TimeUnit.SECONDS.sleep(5);
		page.makeScreenshot();
	}
}
