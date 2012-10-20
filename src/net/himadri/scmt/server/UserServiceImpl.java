package net.himadri.scmt.server;

import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import net.himadri.scmt.client.UserService;
import net.himadri.scmt.client.serializable.UserResponse;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.23. 23:07
 */
public class UserServiceImpl extends RemoteServiceServlet implements UserService {
    static final String SUPER_USER_NAME = "kavai.csaba@gmail.com";

    @Override
    public boolean isAuthorized() {
        return UserServiceFactory.getUserService().isUserAdmin();
    }

    @Override
    public boolean isSuperUserAuthorized() {
        return isSuperUserAuthorizedStatic();
    }

    @Override
    public UserResponse getCurrentUserInfo() {
        com.google.appengine.api.users.UserService userService = UserServiceFactory.getUserService();
        UserResponse userResponse = new UserResponse();
        userResponse.setCurrentUser(userService.getCurrentUser());
        userResponse.setLogoutURL(userService.createLogoutURL(userService.createLoginURL("http://scmtmarathon.appspot.com")));
        return userResponse;
    }

    static boolean isSuperUserAuthorizedStatic() {
        return SUPER_USER_NAME.equals(UserServiceFactory.getUserService().getCurrentUser().getEmail());
    }
}