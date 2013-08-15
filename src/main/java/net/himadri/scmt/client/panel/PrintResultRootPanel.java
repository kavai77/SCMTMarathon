package net.himadri.scmt.client.panel;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import net.himadri.scmt.client.*;
import net.himadri.scmt.client.entity.PersonLap;
import net.himadri.scmt.client.entity.VersenySzam;
import net.himadri.scmt.client.entity.Versenyzo;
import net.himadri.scmt.client.serializable.MarathonActionListener;
import net.himadri.scmt.client.serializable.RaceStatusRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.14. 22:21
 */
public class PrintResultRootPanel extends Composite {

    private TavVersenySzam filter;
    private VerticalPanel verticalPanel = new VerticalPanel();
    private SCMTMarathon scmtMarathon;

    public PrintResultRootPanel(SCMTMarathon scmtMarathon) {
        this.scmtMarathon = scmtMarathon;
        initWidget(verticalPanel);
        scmtMarathon.getPollingService().getPersonLapSync().addMarathonActionListener(new RefreshSyncRequest<PersonLap>());
        scmtMarathon.getPollingService().getVersenySzamSync().addMarathonActionListener(new RefreshSyncRequest<VersenySzam>());
        scmtMarathon.getPollingService().getVersenyzoSync().addMarathonActionListener(new RefreshSyncRequest<Versenyzo>());
    }

    public void showResultPanel(TavVersenySzam filter) {
        this.filter = filter;
        buildUi();
    }

    private void buildUi() {
        verticalPanel.clear();
        String megnevezes;
        switch (filter.getMode()) {
            case VERSENYSZAM:
                megnevezes = Utils.getVersenySzamMegnevezes(scmtMarathon,
                        scmtMarathon.getVersenyszamMapCache().getVersenySzam(
                                filter.getVersenySzamId())) + " eredménylistája";
                break;
            case TAV:
                megnevezes = scmtMarathon.getTavMapCache().getTav(filter.getTavId()).getMegnevezes() +
                        " eredménylistája";
                break;
            default:
                throw new IllegalStateException(filter.getMode() + "not supported");
        }
        verticalPanel.add(new HTML("<h1>" + megnevezes + "</h1>"));
        verticalPanel.add(createFlexTable());
    }

    private class RefreshSyncRequest<T> implements MarathonActionListener<T> {
        @Override
        public void itemAdded(List<T> items) {
            if (TavVersenySzamToken.isHistoryMatches(History.getToken())) {
                buildUi();
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
        flexTable.setText(0, 0, "Hely");
        flexTable.setText(0, 1, "Rajtszám");
        flexTable.setText(0, 2, "Név");
        flexTable.setText(0, 3, "Szül.év");
        flexTable.setText(0, 4, "Egyesület");
        flexTable.setText(0, 5, "Kategória");
        flexTable.setText(0, 6, "Kat. hely");
        flexTable.setText(0, 7, "Idő");
        ArrayList<RaceStatusRow> acceptedRows = new ArrayList<RaceStatusRow>();
        for (RaceStatusRow raceStatusRow : scmtMarathon.getRaceStatusRowCache().getAllRaceStatusRows()) {
            if (TavVersenyszamFilter.isAccepted(filter, raceStatusRow.getVersenySzam()) &&
                    raceStatusRow.getTav() != null && raceStatusRow.getTav().getKorSzam() <= raceStatusRow.getLapTimes().size()) {
                acceptedRows.add(raceStatusRow);
            }
        }
        Map<Long, Integer> tavHelyCounter = new HashMap<Long, Integer>();
        Map<Long, Integer> kategoriaHelyCounter = new HashMap<Long, Integer>();
        int rowIndex = 0;
        for (RaceStatusRow raceStatusRow: acceptedRows) {
            rowIndex++;
            Integer tavHely = getHelyezes(tavHelyCounter, raceStatusRow.getTav().getId());
            flexTable.setText(rowIndex, 0, tavHely + ".");
            flexTable.setText(rowIndex, 1, raceStatusRow.getRaceNumber());
            if (raceStatusRow.getVersenyzo() != null) {
                flexTable.setText(rowIndex, 2, raceStatusRow.getVersenyzo().getName());
                flexTable.setText(rowIndex, 3, raceStatusRow.getVersenyzo().getSzuletesiEv().toString());
                flexTable.setText(rowIndex, 4, raceStatusRow.getVersenyzo().getEgyesulet());
            }
            if (raceStatusRow.getVersenySzam() != null) {
                flexTable.setText(rowIndex, 5, Utils.getVersenySzamMegnevezes(scmtMarathon, raceStatusRow.getVersenySzam()));
                Integer katHely = getHelyezes(kategoriaHelyCounter, raceStatusRow.getVersenySzam().getId());
                flexTable.setText(rowIndex, 6, katHely + ".");
            }
            flexTable.setText(rowIndex, 7, Utils.getElapsedTimeString(raceStatusRow.getLapTimes().get(raceStatusRow.getTav().getKorSzam() - 1)));
        }
        return flexTable;
    }

    private Integer getHelyezes(Map<Long, Integer> helyCounter, Long id) {
        Integer hely = helyCounter.get(id);
        if (hely == null) {
            hely = 0;
        }
        helyCounter.put(id, ++hely);
        return hely;
    }
}
