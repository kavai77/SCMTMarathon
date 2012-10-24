package net.himadri.scmt.client.panel;

import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import net.himadri.scmt.client.SCMTMarathon;
import net.himadri.scmt.client.Utils;
import net.himadri.scmt.client.entity.PersonLap;
import net.himadri.scmt.client.entity.Tav;
import net.himadri.scmt.client.entity.VersenySzam;
import net.himadri.scmt.client.entity.Versenyzo;
import net.himadri.scmt.client.serializable.MarathonActionListener;
import net.himadri.scmt.client.serializable.RaceStatusRow;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.07.16. 21:44
 */
public class StatisticsPanel extends Composite {
    private CellTable<StatisticsTableRow> statisticsCellTable;
    private SCMTMarathon scmtMarathon;
    private StatisticsOverview statisticsOverview = new StatisticsOverview();

    public StatisticsPanel(final SCMTMarathon scmtMarathon) {
        this.scmtMarathon = scmtMarathon;
        AbsolutePanel racePanel = new AbsolutePanel();
        racePanel.setSize("1001px", "620px");

        statisticsCellTable = new CellTable<StatisticsTableRow>(Integer.MAX_VALUE);
        statisticsCellTable.addColumn(new TextColumn<StatisticsTableRow>() {
            @Override
            public String getValue(StatisticsTableRow statisticsTable) {
                return statisticsTable.description;
            }
        }, "Megnevezés");
        statisticsCellTable.addColumn(new RightAlignmentColumn() {
            @Override
            public ArrayList<String> getRaceNumberList(StatisticsTableRow statisticsTable) {
                return statisticsTable.participants;
            }
        }, "Indulók");
        statisticsCellTable.addColumn(new RightAlignmentColumn() {
            @Override
            public ArrayList<String> getRaceNumberList(StatisticsTableRow statisticsTable) {
                return statisticsTable.racing;
            }
        }, "Versenyben");
        statisticsCellTable.addColumn(new RightAlignmentColumn() {
            @Override
            public ArrayList<String> getRaceNumberList(StatisticsTableRow statisticsTable) {
                return statisticsTable.finished;
            }
        }, "Befejezte");
        statisticsCellTable.addColumn(new RightAlignmentColumn() {
            @Override
            public ArrayList<String> getRaceNumberList(StatisticsTableRow statisticsTable) {
                return statisticsTable.notStarted;
            }
        }, "Nem kezdte el");
        statisticsCellTable.addColumn(new RightAlignmentColumn() {
            @Override
            public ArrayList<String> getRaceNumberList(StatisticsTableRow statisticsTable) {
                return statisticsTable.gaveup;
            }
        }, "Feladta");
        ScrollPanel tableScroll = new ScrollPanel(statisticsCellTable);
        tableScroll.setSize("900px", "350px");
        racePanel.add(tableScroll);
        racePanel.add(new Label("* Részletekhez kattints a számra!"), 30, 370);

        scmtMarathon.getPollingService().getVersenyzoSync().addMarathonActionListener(new TabPanelActionListener<Versenyzo>());
        scmtMarathon.getPollingService().getVersenySzamSync().addMarathonActionListener(new TabPanelActionListener<VersenySzam>());
        scmtMarathon.getPollingService().getTavSync().addMarathonActionListener(new TabPanelActionListener<Tav>());
        scmtMarathon.getPollingService().getPersonLapSync().addMarathonActionListener(new MarathonActionListener<PersonLap>() {
            @Override
            public void itemAdded(List<PersonLap> items) {
                if (recalculateNeeded(items)) {
                    recalculateStatistics();
                }
            }

            @Override
            public void itemRefreshed(List<PersonLap> items) {
                recalculateStatistics();
            }

            private boolean recalculateNeeded(List<PersonLap> items) {
                for (PersonLap personLap: items) {
                    RaceStatusRow raceStatusRow = scmtMarathon.getRaceStatusRowCache().getRaceStatusRowByRaceNumber(personLap.getRaceNumber());
                    if (raceStatusRow.getTav() != null && raceStatusRow.getTav().getKorSzam() <= raceStatusRow.getLapTimes().size()) {
                        return true;
                    }
                }
                return false;
            }
        });

        initWidget(racePanel);
    }

    private class TabPanelActionListener<T> implements MarathonActionListener<T> {
        @Override
        public void itemAdded(List<T> items) {
            recalculateStatistics();
        }

        @Override
        public void itemRefreshed(List<T> items) {
            recalculateStatistics();
        }
    }

    private void recalculateStatistics() {
        Collection<Tav> tavok = scmtMarathon.getTavMapCache().getAllTav();
        Collection<VersenySzam> versenySzamok = scmtMarathon.getVersenyszamMapCache().getAllVersenySzam();
        List<StatisticsTableRow> statisticsTableRowList = new ArrayList<StatisticsTableRow>(tavok.size() + versenySzamok.size());

        Map<Long, StatisticsTableRow> tavStatisticsTableRowMap = new HashMap<Long, StatisticsTableRow>(tavok.size());
        for (Tav tav: tavok) {
            StatisticsTableRow statisticsTableRow = new StatisticsTableRow(tav.getMegnevezes());
            tavStatisticsTableRowMap.put(tav.getId(), statisticsTableRow);
            statisticsTableRowList.add(statisticsTableRow);
        }

        Map<Long, StatisticsTableRow> versenySzamStatisticsTableRowMap = new HashMap<Long, StatisticsTableRow>(versenySzamok.size());
        for (VersenySzam versenySzam: versenySzamok) {
            StatisticsTableRow statisticsTableRow = new StatisticsTableRow(Utils.getVersenySzamMegnevezes(scmtMarathon, versenySzam));
            versenySzamStatisticsTableRowMap.put(versenySzam.getId(), statisticsTableRow);
            statisticsTableRowList.add(statisticsTableRow);
        }

        for (RaceStatusRow raceStatusRow: scmtMarathon.getRaceStatusRowCache().getAllRaceStatusRows()) {
            if (raceStatusRow.getTav() != null && raceStatusRow.getVersenySzam() != null) {
                if (raceStatusRow.getTav().getKorSzam() <= raceStatusRow.getLapTimes().size()) {
                    tavStatisticsTableRowMap.get(raceStatusRow.getTav().getId()).finished.add(raceStatusRow.getRaceNumber());
                    versenySzamStatisticsTableRowMap.get(raceStatusRow.getVersenySzam().getId()).finished.add(raceStatusRow.getRaceNumber());
                } else if (raceStatusRow.getVersenyzo() != null && raceStatusRow.getVersenyzo().isFeladta()) {
                    tavStatisticsTableRowMap.get(raceStatusRow.getTav().getId()).gaveup.add(raceStatusRow.getRaceNumber());
                    versenySzamStatisticsTableRowMap.get(raceStatusRow.getVersenySzam().getId()).gaveup.add(raceStatusRow.getRaceNumber());
                } else {
                    tavStatisticsTableRowMap.get(raceStatusRow.getTav().getId()).racing.add(raceStatusRow.getRaceNumber());
                    versenySzamStatisticsTableRowMap.get(raceStatusRow.getVersenySzam().getId()).racing.add(raceStatusRow.getRaceNumber());
                }
            }
        }

        for (Versenyzo versenyzo: scmtMarathon.getVersenyzoMapCache().getAllVersenyzo()) {
            VersenySzam versenySzam = scmtMarathon.getVersenyszamMapCache().getVersenySzam(versenyzo.getVersenySzamId());
            tavStatisticsTableRowMap.get(versenySzam.getTavId()).participants.add(versenyzo.getRaceNumber());
            versenySzamStatisticsTableRowMap.get(versenyzo.getVersenySzamId()).participants.add(versenyzo.getRaceNumber());
        }

        for (StatisticsTableRow statisticsTableRow: statisticsTableRowList) {
            HashSet<String> notStarted = new HashSet<String>(statisticsTableRow.participants);
            notStarted.removeAll(statisticsTableRow.racing);
            notStarted.removeAll(statisticsTableRow.finished);
            notStarted.removeAll(statisticsTableRow.gaveup);
            statisticsTableRow.notStarted.addAll(notStarted);
        }

        StatisticsTableRow versenyzoNelkuliSzamok = new StatisticsTableRow("Ismeretlen rajtszámok, nem rögzített versenyzők");
        for (RaceStatusRow raceStatusRow: scmtMarathon.getRaceStatusRowCache().getAllRaceStatusRows()) {
            if (raceStatusRow.getVersenyzo() == null) {
                versenyzoNelkuliSzamok.participants.add(raceStatusRow.getRaceNumber());
                versenyzoNelkuliSzamok.racing.add(raceStatusRow.getRaceNumber());
            }
        }
        statisticsTableRowList.add(versenyzoNelkuliSzamok);

        statisticsCellTable.setRowData(statisticsTableRowList);
        statisticsCellTable.redraw();
    }

    private class StatisticsTableRow {
        private String description;
        private ArrayList<String> participants = new ArrayList<String>();
        private ArrayList<String> racing = new ArrayList<String>();
        private ArrayList<String> finished = new ArrayList<String>();
        private ArrayList<String> gaveup = new ArrayList<String>();
        private ArrayList<String> notStarted = new ArrayList<String>();

        private StatisticsTableRow(String description) {
            this.description = description;
        }
    }

    private abstract class RightAlignmentColumn extends Column<StatisticsTableRow, String> {
        protected RightAlignmentColumn() {
            super(new ClickableTextCell());
            setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
            setFieldUpdater(new FieldUpdater<StatisticsTableRow, String>() {
                @Override
                public void update(int index, StatisticsTableRow object, String value) {
                    if (!getRaceNumberList(object).isEmpty()) {
                        int columnIndex = statisticsCellTable.getColumnIndex(RightAlignmentColumn.this);
                        statisticsOverview.showWidget(getRaceNumberList(object), object.description + " " +
                                statisticsCellTable.getHeader(columnIndex).getValue());
                    }
                }
            });
        }

        public abstract ArrayList<String> getRaceNumberList(StatisticsTableRow object);

        @Override
        public String getValue(StatisticsTableRow object) {
            return Integer.toString(getRaceNumberList(object).size());
        }
    }

    private class StatisticsOverview extends DialogBox {
        private CellTable<RaceStatusRow> cellTable = new CellTable<RaceStatusRow>(Integer.MAX_VALUE);
        private StatisticsOverview() {
            super(true, true);
            setAnimationEnabled(true);
            ScrollPanel mainPanel = new ScrollPanel();
            mainPanel.setWidget(cellTable);
            mainPanel.setSize("300px", "400px");
            cellTable.addColumn(new TextColumn<RaceStatusRow>() {
                @Override
                public String getValue(RaceStatusRow object) {
                    return object.getRaceNumber();
                }
            }, "Rajtszám");
            cellTable.addColumn(new TextColumn<RaceStatusRow>() {
                @Override
                public String getValue(RaceStatusRow object) {
                    Versenyzo versenyzo = object.getVersenyzo();
                    return versenyzo != null ? versenyzo.getName() : "";
                }
            }, "Név");
            cellTable.addColumn(new TextColumn<RaceStatusRow>() {
                @Override
                public String getValue(RaceStatusRow object) {
                    return Integer.toString(object.getLapTimes().size());
                }
            }, "Körszám");
            setWidget(mainPanel);
        }

        public void showWidget(ArrayList<String> raceNumbers, String description) {
            setHTML(description);
            ArrayList<RaceStatusRow> raceStatusRows = new ArrayList<RaceStatusRow>(raceNumbers.size());
            for (String raceNumber: raceNumbers) {
                RaceStatusRow raceStatusRow = scmtMarathon.getRaceStatusRowCache().getRaceStatusRowByRaceNumber(raceNumber);
                if (raceStatusRow != null) {
                    raceStatusRows.add(raceStatusRow);
                } else {
                    raceStatusRows.add(new RaceStatusRow(raceNumber, scmtMarathon));
                }
            }
            cellTable.setRowData(raceStatusRows);
            cellTable.redraw();
            center();
        }
    }
}
