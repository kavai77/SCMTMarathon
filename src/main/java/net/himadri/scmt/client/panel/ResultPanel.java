package net.himadri.scmt.client.panel;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import net.himadri.scmt.client.*;
import net.himadri.scmt.client.entity.Tav;
import net.himadri.scmt.client.entity.VersenySzam;
import net.himadri.scmt.client.serializable.MarathonActionListener;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.07.16. 21:44
 */
public class ResultPanel extends Composite {
    private ResultTable resultPanel;
    private ListBox versenySzamValaszto = new ListBox();
    private SCMTMarathon scmtMarathon;
    private Button nyomtatasButton;

    public ResultPanel(SCMTMarathon scmtMarathon) {
        this.scmtMarathon = scmtMarathon;
        AbsolutePanel racePanel = new AbsolutePanel();
        racePanel.setSize("100%", "620px");

        versenySzamValaszto.setSize("190px", "33px");
        racePanel.add(versenySzamValaszto, 10, 10);
        versenySzamValaszto.addChangeHandler(new VersenySzamValasztoChangeHandler());

        nyomtatasButton = new ImageButton("fileprint.png", "Nyomatási kép", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                History.newItem(TavVersenySzamToken.encode(resultPanel.getFilter()));
            }
        });
        racePanel.add(nyomtatasButton, 215, 10);
        nyomtatasButton.setVisible(false);

        resultPanel = new ResultTable(scmtMarathon, TavVersenySzam.createAllAcceptance());
        racePanel.add(resultPanel, 0, 60);

        scmtMarathon.getPollingService().getVersenySzamSync().addMarathonActionListener(new TabPanelActionListener<VersenySzam>());
        scmtMarathon.getPollingService().getTavSync().addMarathonActionListener(new TabPanelActionListener<Tav>());

        initWidget(racePanel);
    }

    private class VersenySzamValasztoChangeHandler implements ChangeHandler {
        @Override
        public void onChange(ChangeEvent changeEvent) {
            int selectedIndex = versenySzamValaszto.getSelectedIndex();
            String value = versenySzamValaszto.getValue(selectedIndex);
            resultPanel.refilterRaceStatusRows(TavVersenySzamToken.decode(value));
            nyomtatasButton.setVisible(selectedIndex > 0);
        }
    }

    private class TabPanelActionListener<T> implements MarathonActionListener<T> {
        @Override
        public void itemAdded(List<T> items) {
            refreshVersenySzamValaszto();
        }

        @Override
        public void itemRefreshed(List<T> items) {
            refreshVersenySzamValaszto();
        }

        private void refreshVersenySzamValaszto() {
            versenySzamValaszto.clear();
            versenySzamValaszto.addItem("Összes versenyző", TavVersenySzamToken.encode(TavVersenySzam.createAllAcceptance()));
            for (Tav tav : scmtMarathon.getTavMapCache().getAllTav()) {
                versenySzamValaszto.addItem(tav.getMegnevezes() + " összes",
                        TavVersenySzamToken.encode(TavVersenySzam.createTav(tav.getId())));
            }
            for (VersenySzam versenySzam : scmtMarathon.getVersenyszamMapCache().getAllVersenySzamSorted()) {
                versenySzamValaszto.addItem(Utils.getVersenySzamMegnevezes(scmtMarathon, versenySzam),
                        TavVersenySzamToken.encode(TavVersenySzam.createVersenyszamFilter(versenySzam.getId())));
            }
            resultPanel.refilterRaceStatusRows(TavVersenySzam.createAllAcceptance());
        }
    }
}
