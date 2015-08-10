package net.himadri.scmt.client.panel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.*;
import net.himadri.scmt.client.UserService;
import net.himadri.scmt.client.UserServiceAsync;
import net.himadri.scmt.client.callback.CommonAsyncCallback;
import net.himadri.scmt.client.serializable.UserResponse;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.22. 4:46
 */
public class UserPanel extends Composite {
    private HTML userHtml = new HTML("", false);

    public UserPanel() {
        HorizontalPanel userHorizontalPanel = new HorizontalPanel();
        userHorizontalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        userHorizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        userHorizontalPanel.setSize("990px", "30px");

        userHorizontalPanel.add(userHtml);

        initWidget(userHorizontalPanel);

        UserServiceAsync userService = GWT.create(UserService.class);

        userService.getCurrentUserInfo(new CommonAsyncCallback<UserResponse>() {
            @Override
            public void onSuccess(UserResponse userResponse) {
                StringBuilder htmlBuilder = new StringBuilder();
                htmlBuilder.append("<div class='gwt-Label'><b>")
                        .append(userResponse.getCurrentUser().getEmail())
                        .append("</b> | ")
                        .append("<a href='")
                        .append(userResponse.getLogoutURL())
                        .append("'>Kijelentkezés</a></div>");
                userHtml.setHTML(htmlBuilder.toString());
            }
        });
    }
}
