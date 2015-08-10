package net.himadri.scmt.client.panel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import net.himadri.scmt.client.ImageButton;
import net.himadri.scmt.client.MarathonService;
import net.himadri.scmt.client.MarathonServiceAsync;
import net.himadri.scmt.client.SCMTMarathon;
import net.himadri.scmt.client.callback.EmptyFailureHandlingAsyncCallback;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.08. 11:14
 */
public class NoRacePanel extends Composite {
    public NoRacePanel(final SCMTMarathon scmtMarathon) {
        AbsolutePanel noRacePanel = new AbsolutePanel();

        Label lblAVersenyMg = new Label("A verseny még nem kezdődött el.");
        noRacePanel.add(lblAVersenyMg, 10, 10);
        lblAVersenyMg.addStyleName("bigger");
        lblAVersenyMg.setSize("958px", "auto");
        lblAVersenyMg.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        final MarathonServiceAsync marathonService = GWT.create(MarathonService.class);
        Button btnStartRace = new ImageButton("player_play.png", "Verseny indítása",
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        marathonService.startRace(scmtMarathon.getVerseny().getId(),
                                new EmptyFailureHandlingAsyncCallback<Void>());
                    }
                });
        noRacePanel.add(btnStartRace, 420, 60);

        initWidget(noRacePanel);
    }
}
