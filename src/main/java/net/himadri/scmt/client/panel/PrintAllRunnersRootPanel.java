package net.himadri.scmt.client.panel;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import net.himadri.scmt.client.SCMTMarathon;
import net.himadri.scmt.client.Utils;
import net.himadri.scmt.client.VersenyszamMapCache;
import net.himadri.scmt.client.entity.VersenySzam;
import net.himadri.scmt.client.entity.Versenyzo;
import net.himadri.scmt.client.serializable.MarathonActionListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PrintAllRunnersRootPanel extends Composite {
    public static final String HISTORY_TOKEN = "StartList";

    private VerticalPanel verticalPanel = new VerticalPanel();
    private SCMTMarathon scmtMarathon;

    public PrintAllRunnersRootPanel(SCMTMarathon scmtMarathon) {
        this.scmtMarathon = scmtMarathon;
        initWidget(verticalPanel);
        scmtMarathon.getPollingService().getVersenySzamSync().addMarathonActionListener(new RefreshSyncRequest<VersenySzam>());
        scmtMarathon.getPollingService().getVersenyzoSync().addMarathonActionListener(new RefreshSyncRequest<Versenyzo>());
    }

    public void showAllRunnersPanel() {
        verticalPanel.clear();
        verticalPanel.add(new HTML("<h1>Rajtlista</h1>"));
        verticalPanel.add(createFlexTable());
    }

    private class RefreshSyncRequest<T> implements MarathonActionListener<T> {
        @Override
        public void itemAdded(List<T> items) {
            if (HISTORY_TOKEN.equals(History.getToken())) {
                showAllRunnersPanel();
            }
        }

        @Override
        public void itemRefreshed(List<T> items) {
            itemAdded(items);
        }
    }

    private FlexTable createFlexTable() {
        FlexTable flexTable = new FlexTable();
        flexTable.setBorderWidth(1);
        flexTable.setCellPadding(5);
        flexTable.addStyleName("collapse");
        flexTable.setText(0, 0, "Rajtszám");
        flexTable.setText(0, 1, "Név");
        flexTable.setText(0, 2, "Szül.év");
        flexTable.setText(0, 3, "Kategória");
        flexTable.setText(0, 4, "Egyesület");
        int rowIndex = 1;
        VersenyszamMapCache versenyszamMapCache = scmtMarathon.getVersenyszamMapCache();
        List<Versenyzo> allVersenyzo = new ArrayList<Versenyzo>(scmtMarathon.getVersenyzoMapCache().getAllVersenyzo());
        Collections.sort(allVersenyzo, new Comparator<Versenyzo>() {
            @Override
            public int compare(Versenyzo o1, Versenyzo o2) {
                try {
                    Integer rajtszam1 = Integer.parseInt(o1.getRaceNumber());
                    Integer rajtszam2 = Integer.parseInt(o2.getRaceNumber());
                    return rajtszam1.compareTo(rajtszam2);
                } catch (NumberFormatException e) {
                    return o1.getRaceNumber().compareTo(o2.getRaceNumber());
                }
            }
        });
        for (Versenyzo versenyzo: allVersenyzo) {
            flexTable.setText(rowIndex, 0, versenyzo.getRaceNumber());
            flexTable.setText(rowIndex, 1, versenyzo.getName());
            flexTable.setText(rowIndex, 2, versenyzo.getSzuletesiEv().toString());
            flexTable.setText(rowIndex, 3, Utils.getVersenySzamMegnevezes(scmtMarathon,
                    versenyszamMapCache.getVersenySzam(versenyzo.getVersenySzamId())));
            flexTable.setText(rowIndex, 4, versenyzo.getEgyesulet());
            rowIndex++;
        }
        return flexTable;
    }
}
