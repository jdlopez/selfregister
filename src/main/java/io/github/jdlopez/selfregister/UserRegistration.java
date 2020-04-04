package io.github.jdlopez.selfregister;

import java.util.Date;

public class UserRegistration {
    private String email;
    private String confirmCode;
    private boolean active = false;
    private Date creationDate;
    private Date lastModified;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getConfirmCode() {
        return confirmCode;
    }

    public void setConfirmCode(String confirmCode) {
        this.confirmCode = confirmCode;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String toString() {
        return "UserRegistration{" +
                "email='" + email + '\'' +
                ", confirmCode='" + confirmCode + '\'' +
                ", active=" + active +
                ", creationDate=" + creationDate +
                ", lastModified=" + lastModified +
                '}';
    }
}
