package io.github.xpakx.discord_muppet.page;

import com.microsoft.playwright.Playwright;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class PlaywrightConfig {

    // TODO: get all options
    @Bean
    public Playwright.CreateOptions createOptions(@Value("${playwright.skip.browser.download}") String skipDownload) {
        Map<String, String> optionsMap = new HashMap<>();
        optionsMap.put("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", skipDownload);
        var options = new Playwright.CreateOptions();
        options.setEnv(optionsMap);
        return options;
    }
}
