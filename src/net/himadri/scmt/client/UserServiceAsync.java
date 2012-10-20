package net.himadri.scmt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import net.himadri.scmt.client.serializable.UserResponse;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.23. 23:07
 */
public interface UserServiceAsync {
    void isAuthorized(AsyncCallback<Boolean> async);

    void getCurrentUserInfo(AsyncCallback<UserResponse> async);

    void isSuperUserAuthorized(AsyncCallback<Boolean> async);
}
