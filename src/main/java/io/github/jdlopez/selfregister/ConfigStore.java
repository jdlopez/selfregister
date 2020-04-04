package io.github.jdlopez.selfregister;

import io.github.jdlopez.selfregister.impl.InMemoryUserStore;
import io.github.jdlopez.selfregister.impl.StdoutMailSender;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class ConfigStore {

    private String pathRegister = "/register";
    private String pathConfirm = "/confirm";
    private String parameterEmail = "email";
    private String pageError   = "/selfregister/error.jsp";
    private String pageWelcome = "/selfregister/welcome.jsp";
    private String pageConfirm = "/selfregister/confirm.jsp";
    private String pageSuccess = "/selfregister/success.jsp";
    private long expirationTimeMillis = 48 * 3600 * 1000; // 48 hours
    private String userStoreClass = InMemoryUserStore.class.getCanonicalName();
    private String emailSenderClass = StdoutMailSender.class.getCanonicalName();
    private String configResourceName = "/selfregister.properties";
    private String bundleName = SelfRegisterServlet.class.getName() + "-messages";

    private Properties externalProperties = new Properties();

    public static ConfigStore getInstance(ServletContext servletContext) throws IOException {
        if (servletContext.getAttribute(ConfigStore.class.getCanonicalName()) != null)
            return (ConfigStore) servletContext.getAttribute(ConfigStore.class.getCanonicalName());
        else {
            ConfigStore c = new ConfigStore();
            c.pathRegister = getValueIfNull(servletContext.getInitParameter("pathRegister"), c.pathRegister);
            c.pathConfirm = getValueIfNull(servletContext.getInitParameter("pathConfirm"), c.pathConfirm);
            c.parameterEmail = getValueIfNull(servletContext.getInitParameter("parameterEmail"), c.parameterEmail);
            c.pageError = getValueIfNull(servletContext.getInitParameter("pageError"), c.pageError);
            c.pageWelcome = getValueIfNull(servletContext.getInitParameter("pageWelcome"), c.pageWelcome);
            c.pageConfirm = getValueIfNull(servletContext.getInitParameter("pageConfirm"), c.pageConfirm);
            c.pageSuccess = getValueIfNull(servletContext.getInitParameter("pageSuccess"), c.pageSuccess);
            c.expirationTimeMillis = Long.parseLong(getValueIfNull(servletContext.getInitParameter("expirationTimeMillis"), String.valueOf(c.expirationTimeMillis)));
            c.userStoreClass = getValueIfNull(servletContext.getInitParameter("userStoreClass"), c.userStoreClass);
            c.emailSenderClass = getValueIfNull(servletContext.getInitParameter("emailSenderClass"), c.emailSenderClass);
            c.bundleName = getValueIfNull(servletContext.getInitParameter("bundleName"), c.bundleName);

            c.configResourceName = getValueIfNull(servletContext.getInitParameter("configResourceName"), c.configResourceName);
            URL configFile = null;
            if (c.configResourceName.startsWith("file:")) {
                configFile = new URL(c.configResourceName);
            } else {
                configFile = ConfigStore.class.getResource(c.configResourceName);
                if (configFile == null) // get from servletcontext inside web-inf
                    configFile = servletContext.getResource("/WEB-INF" + (c.configResourceName.startsWith("/")?"":"/") + c.configResourceName);
                if (configFile == null) // get from servletcontext
                    configFile = servletContext.getResource(c.configResourceName);
            }
            if (configFile != null) {
                c.externalProperties.load(configFile.openStream());
                c.pathRegister = c.externalProperties.getProperty("pathRegister", c.pathRegister);
                c.pathConfirm = c.externalProperties.getProperty("pathConfirm", c.pathConfirm);
                c.parameterEmail = c.externalProperties.getProperty("parameterEmail", c.parameterEmail);
                c.pageError = c.externalProperties.getProperty("pageError", c.pageError);
                c.pageWelcome = c.externalProperties.getProperty("pageWelcome", c.pageWelcome);
                c.pageConfirm = c.externalProperties.getProperty("pageConfirm", c.pageConfirm);
                c.pageSuccess = c.externalProperties.getProperty("pageSuccess", c.pageSuccess);
                c.expirationTimeMillis = Long.parseLong(c.externalProperties.getProperty("expirationTimeMillis", String.valueOf(c.expirationTimeMillis)));
                c.userStoreClass = c.externalProperties.getProperty("userStoreClass", c.userStoreClass);
                c.emailSenderClass = c.externalProperties.getProperty("emailSenderClass", c.emailSenderClass);
                c.bundleName = c.externalProperties.getProperty("bundleName", c.bundleName);
            }
            servletContext.setAttribute(ConfigStore.class.getCanonicalName(), c);
            return c;
        }
    }

    private static String getValueIfNull(String value, String defaultValue) {
        return value != null && !"".equals(value) ? value: defaultValue;
    }

    public String getPathConfirm() {
        return pathConfirm;
    }

    public String getPathRegister() {
        return pathRegister;
    }


    public String getPageSuccess() {
        return pageSuccess;
    }

    public String getPageError() {
        return pageError;
    }

    public String getPageWelcome() {
        return pageWelcome;
    }

    public String getPageConfirm() {
        return pageConfirm;
    }


    public String getParameterEmail() {
        return parameterEmail;
    }

    public long getExpirationTimeMillis() {
        return expirationTimeMillis;
    }

    public String getUserStoreClass() {
        return userStoreClass;
    }

    public String getEmailSenderClass() {
        return emailSenderClass;
    }

    public String getConfigResourceName() {
        return configResourceName;
    }

    public Properties getExternalProperties() {
        return externalProperties;
    }

    public String getBundleName() {
        return bundleName;
    }
}
