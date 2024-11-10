package io.github.xpakx.discord_muppet.cookie;

import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.SameSiteAttribute;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class CookieEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String value;
    private String url;
    private String domain;
    private String path;
    private Double expires;
    private Boolean httpOnly;
    private Boolean secure;
    private SameSiteAttribute sameSite;

    public static CookieEntity from(Cookie cookie) {
        CookieEntity entity = new CookieEntity();
        entity.setName(cookie.name);
        entity.setValue(cookie.value);
        entity.setUrl(cookie.url);
        entity.setDomain(cookie.domain);
        entity.setPath(cookie.path);
        entity.setExpires(cookie.expires);
        entity.setHttpOnly(cookie.httpOnly);
        entity.setSecure(cookie.secure);
        entity.setSameSite(cookie.sameSite);
        return entity;
    }

    public Cookie toCookie() {
        Cookie cookie = new Cookie(name, value);
        cookie.url = url;
        cookie.domain = domain;
        cookie.path = path;
        cookie.expires = expires;
        cookie.httpOnly = httpOnly;
        cookie.secure = secure;
        cookie.sameSite = sameSite;
        return cookie;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Double getExpires() {
        return expires;
    }

    public void setExpires(Double expires) {
        this.expires = expires;
    }

    public Boolean getHttpOnly() {
        return httpOnly;
    }

    public void setHttpOnly(Boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    public Boolean getSecure() {
        return secure;
    }

    public void setSecure(Boolean secure) {
        this.secure = secure;
    }

    public SameSiteAttribute getSameSite() {
        return sameSite;
    }

    public void setSameSite(SameSiteAttribute sameSite) {
        this.sameSite = sameSite;
    }
}

