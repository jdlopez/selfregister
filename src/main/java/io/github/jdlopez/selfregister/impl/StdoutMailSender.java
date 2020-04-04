package io.github.jdlopez.selfregister.impl;

import io.github.jdlopez.selfregister.ConfigStore;
import io.github.jdlopez.selfregister.EmailSender;
import io.github.jdlopez.selfregister.UserRegistration;

public class StdoutMailSender extends EmailSender {

    public StdoutMailSender(ConfigStore config) {
        this.config = config;
    }

    public void sendConfirm(UserRegistration user) {
        System.out.println("This user is trying to register: " + user);
    }

    public void sendActive(UserRegistration user) {
        System.out.println("This user is now ACTIVE: " + user);
    }
}
