package io.github.jdlopez.selfregister;

import javax.servlet.ServletContext;
import java.lang.reflect.InvocationTargetException;

public abstract class EmailSender {

    protected ConfigStore config;

    public static EmailSender getInstance(ServletContext servletContext, ConfigStore config) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (servletContext.getAttribute(EmailSender.class.getCanonicalName()) != null)
            return (EmailSender) servletContext.getAttribute(EmailSender.class.getCanonicalName());
        else {
            EmailSender obj = (EmailSender) Class.forName(config.getEmailSenderClass()).getDeclaredConstructor(ConfigStore.class).newInstance(config);
            servletContext.setAttribute(EmailSender.class.getCanonicalName(), obj);
            return obj;
        }
    }

    public abstract void sendConfirm(UserRegistration user);

    public abstract void sendActive(UserRegistration user);
}
