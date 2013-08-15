package net.himadri.scmt.client.panel;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;
import net.himadri.scmt.client.SCMTMarathon;
import net.himadri.scmt.client.TabChangeHandler;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.08. 10:51
 */
public class RaceTabPanel extends Composite {


    public RaceTabPanel(SCMTMarathon scmtMarathon) {
        final TabPanel tabPanel = new TabPanel();
        tabPanel.setSize("100%", "600px");
        tabPanel.add(new RacePanel(scmtMarathon), "Körrögzítés", false);
        tabPanel.add(new ResultPanel(scmtMarathon), "Eredmények", false);
        tabPanel.add(new StatisticsPanel(scmtMarathon), "Statisztikák", false);

        tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
            @Override
            public void onSelection(SelectionEvent<Integer> integerSelectionEvent) {
                Widget widget = tabPanel.getWidget(integerSelectionEvent.getSelectedItem());
                if (widget instanceof TabChangeHandler) {
                    ((TabChangeHandler) widget).activated();
                }
            }
        });

        tabPanel.selectTab(0);
        initWidget(tabPanel);
    }
}
