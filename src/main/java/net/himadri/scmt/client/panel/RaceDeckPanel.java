package net.himadri.scmt.client.panel;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import net.himadri.scmt.client.SCMTMarathon;
import net.himadri.scmt.client.entity.RaceStatus;
import net.himadri.scmt.client.serializable.MarathonActionListener;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.08. 18:25
 */
public class RaceDeckPanel extends Composite {
    public RaceDeckPanel(SCMTMarathon scmtMarathon) {
        final DeckPanel raceDeckPanel = new DeckPanel();
        raceDeckPanel.setSize("990px", "600px");
        raceDeckPanel.add(new NoRacePanel(scmtMarathon));
        raceDeckPanel.add(new RaceTabPanel(scmtMarathon));

        scmtMarathon.getPollingService().getRaceStatusSync().addMarathonActionListener(
                new MarathonActionListener<RaceStatus>() {
                    @Override
                    public void itemAdded(List<RaceStatus> items) {
                        itemRefreshed(items);
                    }

                    @Override
                    public void itemRefreshed(List<RaceStatus> items) {
                        switch (items.get(0)) {
                            case NOT_STARTED:
                                raceDeckPanel.showWidget(0);
                                break;
                            case RACING:
                            case FINISHED:
                                raceDeckPanel.showWidget(1);
                                break;
                        }
                    }
                });

        initWidget(raceDeckPanel);
    }
}
