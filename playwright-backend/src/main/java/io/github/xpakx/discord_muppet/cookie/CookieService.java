package io.github.xpakx.discord_muppet.cookie;

import com.microsoft.playwright.options.Cookie;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CookieService {

    public void saveCookies(List<Cookie> currentCookies) {
        // TODO
        System.out.println("Cookies saved");
    }

    public List<Cookie> loadCookies() {
        // TODO
        System.out.println("Cookies loaded");
        return null;
    }
}
