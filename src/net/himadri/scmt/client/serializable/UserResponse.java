package net.himadri.scmt.client.serializable;

import com.google.appengine.api.users.User;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.22. 4:53
 */
public class UserResponse implements Serializable {
    private User currentUser;
    private String logoutURL;

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public String getLogoutURL() {
        return logoutURL;
    }

    public void setLogoutURL(String logoutURL) {
        this.logoutURL = logoutURL;
    }
}
