package io.github.jdlopez.selfregister;

import javax.servlet.ServletContext;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public abstract class UserStore {

    protected ConfigStore config;

    public static UserStore getInstance(ServletContext servletContext, ConfigStore config) throws SelfRegisterException {
        if (servletContext.getAttribute(UserStore.class.getCanonicalName()) != null)
            return (UserStore) servletContext.getAttribute(UserStore.class.getCanonicalName());
        else {
            UserStore obj = null;
            try {
                obj = (UserStore) Class.forName(config.getUserStoreClass()).getDeclaredConstructor(ConfigStore.class).newInstance(config);
            } catch (InstantiationException | IllegalAccessException  | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
                throw new SelfRegisterException(e.getMessage(), e);
            }
            servletContext.setAttribute(UserStore.class.getCanonicalName(), obj);
            return obj;
        }
    }

    public String generateConfirmCode() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /** Check expired */
    public boolean checkUserRequestTimeout(UserRegistration user) {
        return user.getLastModified().getTime() + config.getExpirationTimeMillis() < System.currentTimeMillis();
    }

    public abstract UserRegistration findUserByCode(String confirmcode) throws SelfRegisterException;

    public abstract UserRegistration findUserByEmail(String email) throws SelfRegisterException;

    public abstract UserRegistration createUser(String email) throws SelfRegisterException;

    public abstract void saveUser(UserRegistration user) throws SelfRegisterException;

}
