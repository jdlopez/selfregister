package io.github.jdlopez.selfregister.impl;

import io.github.jdlopez.selfregister.ConfigStore;
import io.github.jdlopez.selfregister.UserRegistration;
import io.github.jdlopez.selfregister.UserStore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class InMemoryUserStore extends UserStore {

    private Map<String, UserRegistration> users = new HashMap<String, UserRegistration>();

    public InMemoryUserStore(ConfigStore config) {
        this.config = config;
    }

    public UserRegistration findUserByCode(String confirmcode) {
        for (UserRegistration u: users.values())
            if (u.getConfirmCode().equals(confirmcode))
                return u;
        return null;
    }

    public UserRegistration findUserByEmail(String email) {
        return users.get(email);
    }

    public UserRegistration createUser(String email) {
        UserRegistration u = new UserRegistration();
        u.setActive(false);
        u.setConfirmCode(generateConfirmCode());
        u.setCreationDate(new Date());
        u.setEmail(email);
        u.setLastModified(u.getCreationDate());
        saveUser(u);
        return u;
    }

    public void saveUser(UserRegistration user) {
        users.remove(user.getEmail());
        users.put(user.getEmail(), user);
    }
}
