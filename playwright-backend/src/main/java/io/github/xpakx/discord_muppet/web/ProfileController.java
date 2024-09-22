package io.github.xpakx.discord_muppet.web;

import io.github.xpakx.discord_muppet.conversation.MessageItem;
import io.github.xpakx.discord_muppet.model.ProfileService;
import io.github.xpakx.discord_muppet.model.User;
import io.github.xpakx.discord_muppet.model.dto.FriendData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ProfileController {
    private final ProfileService service;

    public ProfileController(ProfileService service) {
        this.service = service;
    }

    @GetMapping("/profile")
    public User getProfile() {
        return service.getUser();
    }

    @GetMapping("/contacts")
    public List<FriendData> getContacts() {
        return service.getFriends();
    }

    @GetMapping("/contacts/{username}")
    public List<MessageItem> openChannel(@PathVariable String username) {
        return service.openChannel(username);
    }

    @GetMapping("/current")
    public List<MessageItem> currentChannel() {
        return service.currentChannel();
    }
}
