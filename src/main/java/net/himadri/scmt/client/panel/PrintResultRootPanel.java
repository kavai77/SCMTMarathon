package net.himadri.scmt.client.panel;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import net.himadri.scmt.client.SCMTMarathon;
import net.himadri.scmt.client.TavVersenySzam;
import net.himadri.scmt.client.TavVersenyszamFilter;
import net.himadri.scmt.client.Utils;
import net.himadri.scmt.client.entity.PersonLap;
import net.himadri.scmt.client.entity.Tav;
import net.himadri.scmt.client.entity.VersenySzam;
import net.himadri.scmt.client.entity.Versenyzo;
import net.himadri.scmt.client.serializable.MarathonActionListener;
import net.himadri.scmt.client.serializable.RaceStatusRow;
import net.himadri.scmt.client.token.TavVersenySzamToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.14. 22:21
 */
public class PrintResultRootPanel extends Composite
{
    private TavVersenySzam filter;
    private SCMTMarathon scmtMarathon;
    private VerticalPanel verticalPanel = new VerticalPanel();

    public PrintResultRootPanel(SCMTMarathon scmtMarathon)
    {
        this.scmtMarathon = scmtMarathon;
        initWidget(verticalPanel);
        scmtMarathon.getPollingService().getPersonLapSync().addMarathonActionListener(new RefreshSyncRequest<PersonLap>());
        scmtMarathon.getPollingService().getVersenySzamSync().addMarathonActionListener(new RefreshSyncRequest<VersenySzam>());
        scmtMarathon.getPollingService().getVersenyzoSync().addMarathonActionListener(new RefreshSyncRequest<Versenyzo>());
        scmtMarathon.getPollingService().getTavSync().addMarathonActionListener(new RefreshSyncRequest<Tav>());
    }

    public void showResultPanel(TavVersenySzam filter)
    {
        this.filter = filter;
        buildUi();
    }

    private boolean isKorSzamFinished(RaceStatusRow raceStatusRow)
    {
        return (raceStatusRow.getTav() != null) && (raceStatusRow.getTav().getKorSzam() <= raceStatusRow.getLapTimes().size());
    }

    private boolean isNemAccepted(RaceStatusRow raceStatusRow, Boolean ferfi)
    {
        return (raceStatusRow.getVersenyzo() != null) && (raceStatusRow.getVersenyzo().getFerfi() != null)
              && ((ferfi == null) || (raceStatusRow.getVersenyzo().getFerfi().booleanValue() == ferfi.booleanValue()));
    }

    private Integer getHelyezes(Map<Long, Integer> helyCounter, Long id)
    {
        Integer hely = helyCounter.get(id);

        if (hely == null)
        {
            hely = 0;
        }

        helyCounter.put(id, ++hely);

        return hely;
    }

    private void buildUi()
    {
        verticalPanel.clear();
        switch (filter.getMode())
        {

            case VERSENYSZAM:
                String versenySzamMegnevezes = Utils.getVersenySzamMegnevezes(scmtMarathon,
                        scmtMarathon.getVersenyszamMapCache().getVersenySzam(filter.getVersenySzamId())) + " eredménylistája";

                verticalPanel.add(new HTML("<h1>" + versenySzamMegnevezes + "</h1>"));
                verticalPanel.add(createFlexTable(null));
                break;

            case TAV:
                String tavMegnevezes = scmtMarathon.getTavMapCache().getTav(filter.getTavId()).getMegnevezes() + " eredménylistája";

                verticalPanel.add(new HTML("<h1>" + tavMegnevezes + " - Férfi</h1>"));
                verticalPanel.add(createFlexTable(true));
                verticalPanel.add(new HTML("<br><br><br><h1>" + tavMegnevezes + " - Női</h1>"));
                verticalPanel.add(createFlexTable(false));
                break;

            default:
                throw new IllegalStateException(filter.getMode() + "not supported");
        }
    }

    private FlexTable createFlexTable(Boolean ferfi)
    {
        FlexTable flexTable = new FlexTable();
        int colIndex = 0;

        flexTable.setBorderWidth(1);
        flexTable.setCellPadding(5);
        flexTable.addStyleName("collapse");
        flexTable.setText(0, colIndex++, "Hely");
        flexTable.setText(0, colIndex++, "Rajtszám");
        flexTable.setText(0, colIndex++, "Név");
        flexTable.setText(0, colIndex++, "Szül.év");
        flexTable.setText(0, colIndex++, "Egyesület");
        if (filter.getMode() == TavVersenySzam.Mode.TAV) {
            flexTable.setText(0, colIndex++, "Korcsoport");
            flexTable.setText(0, colIndex++, "KCS. hely");
        }
        flexTable.setText(0, colIndex++, "Idő");
        addKorNevek(flexTable, colIndex++);
        ArrayList<RaceStatusRow> acceptedRows = new ArrayList<>();

        for (RaceStatusRow raceStatusRow : scmtMarathon.getRaceStatusRowCache().getAllRaceStatusRows())
        {
            if (TavVersenyszamFilter.isAccepted(filter, raceStatusRow) && isKorSzamFinished(raceStatusRow)
                  && isNemAccepted(raceStatusRow, ferfi))
            {
                acceptedRows.add(raceStatusRow);
            }
        }

        Map<Long, Integer> tavHelyCounter = new HashMap<>();
        Map<Long, Integer> kategoriaHelyCounter = new HashMap<>();
        int rowIndex = 0;

        for (RaceStatusRow raceStatusRow : acceptedRows)
        {
            rowIndex++;
            colIndex = 0;
            Integer tavHely = getHelyezes(tavHelyCounter, raceStatusRow.getTav().getId());

            flexTable.setText(rowIndex, colIndex++, tavHely + ".");
            flexTable.setText(rowIndex, colIndex++, raceStatusRow.getRaceNumber());
            if (raceStatusRow.getVersenyzo() != null)
            {
                flexTable.setText(rowIndex, colIndex++, raceStatusRow.getVersenyzo().getName());
                flexTable.setText(rowIndex, colIndex++, raceStatusRow.getVersenyzo().getSzuletesiEv().toString());
                flexTable.setText(rowIndex, colIndex++, raceStatusRow.getVersenyzo().getEgyesulet());
            } else colIndex += 3;

            if (raceStatusRow.getVersenySzam() != null)
            {
                if (filter.getMode() == TavVersenySzam.Mode.TAV) {
                    flexTable.setText(rowIndex, colIndex++, Utils.getVersenySzamMegnevezes(scmtMarathon, raceStatusRow.getVersenySzam()));
                    Integer katHely = getHelyezes(kategoriaHelyCounter, raceStatusRow.getVersenySzam().getId());

                    flexTable.setText(rowIndex, colIndex++, katHely + ".");
                }
            } else colIndex += 2;

            flexTable.setText(rowIndex, colIndex++,
                Utils.getElapsedTimeString(raceStatusRow, raceStatusRow.getTav().getKorSzam() - 1));

            addKorIdok(flexTable, raceStatusRow, rowIndex, colIndex++);
        }

        return flexTable;
    }

    private void addKorIdok(FlexTable flexTable, RaceStatusRow raceStatusRow, int rowIndex, int columnIndex) {
        Tav tav = getTavFromFilter();
        if (tav != null) {
            int previousLap = -1;
            String[] korNevArray = tav.getKorNevArray();
            for (int i = 0; i < korNevArray.length; i++) {
                if (!Utils.isEmpty(korNevArray[i])) {
                    String elapsedTime = previousLap == -1 ? Utils.getElapsedTimeString(raceStatusRow, i) :
                            Utils.getElapsedTimeString(raceStatusRow.getLapTimes().get(i) - raceStatusRow.getLapTimes().get(previousLap));
                    flexTable.setText(rowIndex, columnIndex++, elapsedTime);
                    previousLap = i;
                }
            }
        }
    }

    private void addKorNevek(FlexTable flexTable, int columnIndex) {
        Tav tav = getTavFromFilter();
        if (tav != null) {
            for (String korNev : tav.getKorNevArray()) {
                if (!Utils.isEmpty(korNev)) flexTable.setText(0, columnIndex++, korNev);
            }
        }
    }

    private Tav getTavFromFilter() {
        Tav tav;
        switch (filter.getMode()) {
            case TAV:
                tav = scmtMarathon.getTavMapCache().getTav(filter.getTavId());
                break;
            case VERSENYSZAM:
                VersenySzam versenySzam = scmtMarathon.getVersenyszamMapCache().getVersenySzam(filter.getVersenySzamId());
                tav = scmtMarathon.getTavMapCache().getTav(versenySzam.getTavId());
                break;
            default:
                tav = null;
        }
        return tav;
    }

    private class RefreshSyncRequest<T> implements MarathonActionListener<T>
    {
        @Override public void itemAdded(List<T> items)
        {
            if (TavVersenySzamToken.isHistoryMatches(History.getToken()))
            {
                buildUi();
            }
        }

        @Override public void itemRefreshed(List<T> items)
        {
            itemAdded(items);
        }
    }
}
