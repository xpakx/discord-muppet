package io.github.xpakx.discord_muppet;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Paths;

@SpringBootApplication
public class DiscordMuppetApplication implements CommandLineRunner {
	@Value("${discord.credentials.email}")
	String email;

	@Value("${discord.credentials.password}")
	String password;

	public static void main(String[] args) {
		SpringApplication.run(DiscordMuppetApplication.class, args);
	}

	@Override
	public void run(String... arg0) {
		try (Playwright playwright = Playwright.create();
		 Browser browser = playwright.chromium().launch()) {
			Page page = browser.newPage();
			page.navigate("https://discord.com/app");
			System.out.println(page.title());
			page.locator("input[name='email']")
					.fill(email);
			page.locator("input[name='password']")
					.fill(password);
			page.screenshot(
					new Page.ScreenshotOptions().setPath(Paths.get("debug/screenshot.png"))
			);
		}
	}
}
