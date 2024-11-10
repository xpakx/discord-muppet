package io.github.xpakx.discord_muppet.cookie;

import com.microsoft.playwright.options.Cookie;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CookieService {
    private final CookieRepository cookieRepository;

    public CookieService(CookieRepository cookieRepository) {
        this.cookieRepository = cookieRepository;
    }

    public void saveCookies(List<Cookie> currentCookies) {
        cookieRepository.saveAll(
                currentCookies
                        .stream()
                        .map(CookieEntity::from)
                        .toList()
        );
        System.out.println("Cookies saved");
    }

    public List<Cookie> loadCookies() {
        System.out.println("Cookies loaded");
        return cookieRepository.findAll()
                        .stream()
                        .map(CookieEntity::toCookie)
                        .toList();
    }
}
