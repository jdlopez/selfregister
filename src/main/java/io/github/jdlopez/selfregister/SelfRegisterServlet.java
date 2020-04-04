package io.github.jdlopez.selfregister;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class SelfRegisterServlet extends HttpServlet {

    private ConfigStore config;
    private UserStore store;
    private EmailSender mailSender;

    private void addMessages(HttpServletRequest req) {
        req.setAttribute("messages", ResourceBundle.getBundle(config.getBundleName(), req.getLocale()));
    }

    private ResourceBundle getBundle(HttpServletRequest req) {
        return (ResourceBundle) req.getAttribute("messages");
    }

    private void doForward(HttpServletRequest req, HttpServletResponse resp, String page) throws ServletException, IOException {
        req.getServletContext().getRequestDispatcher(page).forward(req, resp);
    }

    private void doForwardError(HttpServletRequest req, HttpServletResponse resp, String messageKey, String... args) throws ServletException, IOException {
        req.setAttribute("error", String.format(getBundle(req).getString(messageKey), args));
        req.getServletContext().getRequestDispatcher(config.getPageError()).forward(req, resp);
    }

    private void doForwardSuccess(HttpServletRequest req, HttpServletResponse resp,
                                  String messageKey, String messageKeyInfo, String... args) throws ServletException, IOException {
        ResourceBundle rb = getBundle(req);
        if (messageKey != null)
            req.setAttribute("message", String.format(rb.getString(messageKey), args));
        if (messageKeyInfo != null)
            req.setAttribute("messageInfo", String.format(rb.getString(messageKeyInfo), args));
        doForward(req, resp, config.getPageSuccess());
    }

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init();
        try {
            config = ConfigStore.getInstance(servletConfig.getServletContext());
            store = UserStore.getInstance(servletConfig.getServletContext(), config);
            mailSender = EmailSender.getInstance(servletConfig.getServletContext(), config);
            servletConfig.getServletContext().log(this.getClass().getName() + " init: config=" + config + " store=" + store + " sender=" + mailSender);
        } catch (Exception e) {
            throw new ServletException("Unable to load Config/UserStore/EmailSender bean", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        addMessages(req);
        if (req.getRequestURI().endsWith(config.getPathRegister()))
            registerDoGet(req, resp);
        else if (req.getRequestURI().endsWith(config.getPathConfirm()))
            confirmDoGet(req, resp);
        else
            doForwardError(req, resp, "error.action_required", config.getPathRegister());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        addMessages(req);
        if (req.getRequestURI().endsWith(config.getPathRegister()))
            registerDoPost(req, resp);
        else if (req.getRequestURI().endsWith(config.getPathConfirm()))
            confirmDoPost(req, resp);
        else
            doForward(req, resp, config.getPageError());
    }

    private void registerDoGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doForward(req, resp, config.getPageWelcome());
    }

    private void registerDoPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter(config.getParameterEmail());
        if (email != null) {
            try {
                UserRegistration user = store.findUserByEmail(email);
                if (user == null) {
                    user = store.createUser(email);
                    mailSender.sendConfirm(user);
                    doForwardSuccess(req, resp, "info.register_message_sent", null);
                } else {
                    if (!user.isActive() && store.checkUserRequestTimeout(user)) {
                        Date previousMod = user.getLastModified();
                        user.setLastModified(new Date());
                        user.setConfirmCode(store.generateConfirmCode());
                        store.saveUser(user);
                        mailSender.sendConfirm(user);
                        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.DEFAULT, req.getLocale());
                        doForwardSuccess(req, resp, "info.register_message_sent", "info.register_previous",
                                df.format(previousMod));
                    } else
                        doForwardError(req, resp, "error.mail_already_exists", email);
                }
            } catch (SelfRegisterException e) {
                throw new ServletException(e.getMessage(), e);
            }
        } else
            doForwardError(req, resp, "error.no_email");
    }

    private void confirmDoGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            UserRegistration user = store.findUserByCode(req.getParameter("confirmcode"));
            if (user != null) {
                if (!user.isActive()) {
                    if (store.checkUserRequestTimeout(user)) {
                        doForwardError(req, resp, "error.confirmcode_too_old", String.valueOf(config.getExpirationTimeMillis() / 3600000));
                    } else {
                        req.setAttribute("user", user);
                        doForward(req, resp, config.getPageConfirm());
                    }
                } else {
                    doForwardError(req, resp, "error.user_already_active", user.getEmail());
                }
            } else
                doForwardError(req, resp, "error.confirmcode_incorrect");
        } catch (SelfRegisterException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    private void confirmDoPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            UserRegistration user = store.findUserByCode(req.getParameter("confirmcode"));
            if (user != null) {
                if (!user.isActive()) {
                    if (store.checkUserRequestTimeout(user)) {
                        doForwardError(req, resp, "error.confirmcode_too_old", String.valueOf(config.getExpirationTimeMillis() / 3600000));
                    } else {
                        user.setActive(true);
                        user.setLastModified(new Date());
                        store.saveUser(user);
                        mailSender.sendActive(user);
                        doForwardSuccess(req, resp, "info.final_thanks", "info.final_info");
                    }
                } else {
                    doForwardError(req, resp, "error.user_already_active", user.getEmail());
                }
            } else
                doForwardError(req, resp, "error.confirmcode_incorrect");
        } catch (SelfRegisterException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }
}
