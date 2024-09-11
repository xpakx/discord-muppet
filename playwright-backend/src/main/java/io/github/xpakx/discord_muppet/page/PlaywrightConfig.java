package io.github.xpakx.discord_muppet.page;

import com.microsoft.playwright.Playwright;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class PlaywrightConfig {

    @Bean
    public Playwright.CreateOptions createOptions(Environment environment) {
        Map<String, String> optionsMap = new HashMap<>();
        String skipDownload = environment.getProperty("playwright.skip.browser.download");
        if(skipDownload != null) {
            optionsMap.put("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", skipDownload);
        }
        // TODO: get more options
        var options = new Playwright.CreateOptions();
        options.setEnv(optionsMap);
        return options;
    }
}
