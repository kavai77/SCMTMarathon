package net.himadri.scmt.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import net.himadri.scmt.client.serializable.UserResponse;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.23. 23:07
 */
@RemoteServiceRelativePath("UserService")
public interface UserService extends RemoteService {
    boolean isAuthorized();

    boolean isSuperUserAuthorized();

    UserResponse getCurrentUserInfo();
}
