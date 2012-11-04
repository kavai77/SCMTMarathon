package net.himadri.scmt.client.panel;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TabPanel;
import net.himadri.scmt.client.SCMTMarathon;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.08. 10:51
 */
public class RaceTabPanel extends Composite {


    public RaceTabPanel(SCMTMarathon scmtMarathon) {
        TabPanel tabPanel = new TabPanel();
        tabPanel.setSize("100%", "600px");
        tabPanel.add(new RacePanel(scmtMarathon), "Körrögzítés", false);
        tabPanel.add(new ResultPanel(scmtMarathon), "Eredmények", false);
        tabPanel.add(new StatisticsPanel(scmtMarathon), "Statisztikák", false);
        tabPanel.selectTab(0);

        initWidget(tabPanel);
    }
}
