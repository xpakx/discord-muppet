package io.github.xpakx.discord_muppet.model;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileService {
    private User user;
    private List<Friend> contacts;

    // TODO
    public void saveUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    // TODO
    public void saveContacts(List<Friend> contacts) {
        this.contacts = contacts;
    }

    public List<Friend> getContacts() {
        return contacts;
    }

}
