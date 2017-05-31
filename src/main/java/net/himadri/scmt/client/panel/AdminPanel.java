package net.himadri.scmt.client.panel;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TabPanel;
import net.himadri.scmt.client.SCMTMarathon;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.08. 10:51
 */
public class AdminPanel extends Composite {


    public AdminPanel(SCMTMarathon scmtMarathon) {
        TabPanel tabPanel = new TabPanel();
        tabPanel.setSize("100%", "600px");
        tabPanel.add(new VersenyzoPanel(scmtMarathon), "Versenyzők", false);
        tabPanel.add(new TavPanel(scmtMarathon), "Távok", false);
        tabPanel.add(new VersenySzamPanel(scmtMarathon), "Korcsoportok", false);
        tabPanel.add(new NyomtatoPanel(scmtMarathon), "Nyomtatóbeállítások", false);
        tabPanel.add(new NevezoPanel(scmtMarathon), "Online nevezés", false);
//        tabPanel.add(new VersenyzoImportPanel(scmtMarathon), "Versenyzők feltöltése", false);
//        tabPanel.add(new NevPanel(), "Nevek feltöltése", false);
        tabPanel.selectTab(0);

        initWidget(tabPanel);
    }
}
