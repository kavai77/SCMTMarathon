package net.himadri.scmt.client.panel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import net.himadri.scmt.client.SCMTMarathon;
import net.himadri.scmt.client.UserService;
import net.himadri.scmt.client.UserServiceAsync;
import net.himadri.scmt.client.entity.Verseny;
import net.himadri.scmt.client.serializable.MarathonActionListener;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.14. 22:17
 */
public class MainRootPanel extends Composite {

    public MainRootPanel(final SCMTMarathon scmtMarathon) {
        final AbsolutePanel absolutePanel = new AbsolutePanel();
        absolutePanel.setSize("990px", "auto");
        absolutePanel.addStyleName("centerWithMargin");

        Image headerImage = new Image("images/header.jpg");
        absolutePanel.add(headerImage);
        headerImage.setSize("990px", "auto");

        UserPanel userPanel = new UserPanel();
        absolutePanel.add(userPanel);

        final AbsolutePanel bodyPanel = new AbsolutePanel();

        final TabPanel foTabPanel = new TabPanel();
        foTabPanel.setSize("990px", "600px");
        foTabPanel.add(new RaceDeckPanel(scmtMarathon), "Verseny", false);
        foTabPanel.add(new AdminPanel(scmtMarathon), "Adminisztráció", false);
        foTabPanel.selectTab(0);

        UserServiceAsync userService = GWT.create(UserService.class);
        userService.isAuthorized(new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable throwable) {
                SCMTMarathon.commonFailureHandling(throwable);
            }

            @Override
            public void onSuccess(Boolean authorized) {
                if (!authorized) {
                    absolutePanel.add(new Label("Ez a felhasználó nem jogosult az alkalmazás futtatására. " +
                            "Jelentkezz be ki, majd lépj be egy jogosult felhasználóval."));
                } else if (!Storage.isLocalStorageSupported()) {
                    absolutePanel.add(new Label("A böngésződ elavult ez az alkalmazás futtatásához. Használj modernebb böngésződ!"));
                } else {
                    bodyPanel.add(new VersenyPanel(scmtMarathon));
                    absolutePanel.add(bodyPanel);
                }
            }
        });

        scmtMarathon.getVersenySyncSupport().addMarathonActionListener(new MarathonActionListener<Verseny>() {
            @Override
            public void itemAdded(List<Verseny> items) {
                itemRefreshed(items);
            }

            @Override
            public void itemRefreshed(List<Verseny> items) {
                bodyPanel.clear();
                bodyPanel.add(foTabPanel);
            }
        });

        initWidget(absolutePanel);
    }
}
