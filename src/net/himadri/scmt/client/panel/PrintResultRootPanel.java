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
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.14. 22:21
 */
public class PrintResultRootPanel extends Composite {

    public static final int FIXED_COLUMN_NB = 5;
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
            case ALL:
                megnevezes = "Összesített eredménylista";
                break;
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
                throw new IllegalStateException(filter.getMode() + "missing");
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
        flexTable.setText(0, 0, "Helyezés");
        flexTable.setText(0, 1, "Rajtszám");
        flexTable.setText(0, 2, "Versenyző");
        flexTable.setText(0, 3, "Egyesület");
        flexTable.setText(0, 4, "Versenyszám");
        ArrayList<RaceStatusRow> acceptedRows = new ArrayList<RaceStatusRow>();
        for (RaceStatusRow raceStatusRow : scmtMarathon.getRaceStatusRowCache().getAllRaceStatusRows()) {
            if (TavVersenyszamFilter.isAccepted(filter, raceStatusRow.getVersenySzam())) {
                acceptedRows.add(raceStatusRow);
            }
        }
        int maxLapTime = 0;
        for (int i = 0; i < acceptedRows.size(); i++) {
            RaceStatusRow raceStatusRow = acceptedRows.get(i);
            int rowIndex = i  + 1;
            flexTable.setText(rowIndex, 0, rowIndex + ".");
            flexTable.setText(rowIndex, 1, raceStatusRow.getRaceNumber());
            if (raceStatusRow.getVersenyzo() != null) {
                flexTable.setText(rowIndex, 2, raceStatusRow.getVersenyzo().getName());
                flexTable.setText(rowIndex, 3, raceStatusRow.getVersenyzo().getEgyesulet());
            }
            if (raceStatusRow.getVersenySzam() != null) {
                flexTable.setText(rowIndex, 4, Utils.getVersenySzamMegnevezes(scmtMarathon, raceStatusRow.getVersenySzam()));
            }
            if (maxLapTime < raceStatusRow.getLapTimes().size()) {
                maxLapTime = raceStatusRow.getLapTimes().size();
            }
            for (int j = 0; j < raceStatusRow.getLapTimes().size(); j++) {
                flexTable.setText(rowIndex, j + FIXED_COLUMN_NB, Utils.getElapsedTimeString(raceStatusRow.getLapTimes().get(j)));
            }
        }
        for (int i = 0; i < maxLapTime; i++) {
            flexTable.setText(0, i + FIXED_COLUMN_NB, (i + 1) + ". kör");
        }
        for (int i = 0; i < acceptedRows.size(); i++) {
            int row = i + 1;
            Versenyzo versenyzo = acceptedRows.get(i).getVersenyzo();
            String text = versenyzo != null && versenyzo.isFeladta() ? "Feladta" : "";
            for (int column = flexTable.getCellCount(row); column < maxLapTime + FIXED_COLUMN_NB; column++) {
                flexTable.setText(row, column, text);
            }
        }
        return flexTable;
    }
}
