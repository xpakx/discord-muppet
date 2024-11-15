package io.github.xpakx.discord_muppet.web;

import io.github.xpakx.discord_muppet.conversation.MessageItem;
import io.github.xpakx.discord_muppet.model.ProfileService;
import io.github.xpakx.discord_muppet.model.User;
import io.github.xpakx.discord_muppet.model.dto.FriendData;
import io.github.xpakx.discord_muppet.web.dto.MessageRequest;
import io.github.xpakx.discord_muppet.web.dto.ConversationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ConversationResponse openChannel(@PathVariable String username) {
        return new ConversationResponse(
                service.openChannel(username),
                username
        );
    }

    @GetMapping("/current")
    public ConversationResponse currentChannel() {
        return service.currentChannel();
    }

    @PostMapping("/messages")
    public ResponseEntity<?> sendMessage(@RequestBody MessageRequest message) { // TODO
        service.sendMessage(message.message());
        return ResponseEntity.ok(null);
    }
}
