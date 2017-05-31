package net.himadri.scmt.server;

import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import net.himadri.scmt.client.MarathonService;
import net.himadri.scmt.client.UserService;
import net.himadri.scmt.client.serializable.UserResponse;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.23. 23:07
 */
public class UserServiceImpl extends RemoteServiceServlet implements UserService {
    public static final String SUPER_USER_KEY = "SUPER_USER";
    private final MarathonService marathonService = new MarathonServiceImpl();


    @Override
    public boolean isAuthorized() {
        return UserServiceFactory.getUserService().isUserAdmin();
    }

    @Override
    public boolean isSuperUserAuthorized() {
        String superUser = marathonService.getConfiguration(SUPER_USER_KEY);
        return superUser.equals(UserServiceFactory.getUserService().getCurrentUser().getEmail());
    }

    @Override
    public UserResponse getCurrentUserInfo() {
        com.google.appengine.api.users.UserService userService = UserServiceFactory.getUserService();
        UserResponse userResponse = new UserResponse();
        userResponse.setCurrentUser(userService.getCurrentUser());
        userResponse.setLogoutURL(userService.createLogoutURL(userService.createLoginURL("http://scmtmarathon.appspot.com")));
        return userResponse;
    }


}