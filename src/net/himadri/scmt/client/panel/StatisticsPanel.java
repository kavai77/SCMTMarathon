package net.himadri.scmt.client.panel;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
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

    public StatisticsPanel(final SCMTMarathon scmtMarathon) {
        this.scmtMarathon = scmtMarathon;
        AbsolutePanel racePanel = new AbsolutePanel();
        racePanel.setSize("1001px", "620px");

        statisticsCellTable = new CellTable<StatisticsTableRow>();
        statisticsCellTable.addColumn(new TextColumn<StatisticsTableRow>() {
            @Override
            public String getValue(StatisticsTableRow statisticsTable) {
                return statisticsTable.description;
            }
        }, "Megnevezés");
        statisticsCellTable.addColumn(new RightAlignmentColumn() {
            @Override
            public String getValue(StatisticsTableRow statisticsTable) {
                return Integer.toString(statisticsTable.participants);
            }
        }, "Indulók");
        statisticsCellTable.addColumn(new RightAlignmentColumn() {
            @Override
            public String getValue(StatisticsTableRow statisticsTable) {
                return Integer.toString(statisticsTable.participants - statisticsTable.finished - statisticsTable.gaveup);
            }
        }, "Versenyben");
        statisticsCellTable.addColumn(new RightAlignmentColumn() {
            @Override
            public String getValue(StatisticsTableRow statisticsTable) {
                return Integer.toString(statisticsTable.finished);
            }
        }, "Befejezte");
        statisticsCellTable.addColumn(new RightAlignmentColumn() {
            @Override
            public String getValue(StatisticsTableRow statisticsTable) {
                return Integer.toString(statisticsTable.gaveup);
            }
        }, "Feladta");
        statisticsCellTable.setPageSize(Integer.MAX_VALUE);
        racePanel.add(statisticsCellTable);

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

        List<RaceStatusRow> statusRowCacheList = scmtMarathon.getRaceStatusRowCache().getAllRaceStatusRows();
        for (RaceStatusRow raceStatusRow: statusRowCacheList) {
            if (raceStatusRow.getTav() != null && raceStatusRow.getVersenySzam() != null) {
                if (raceStatusRow.getTav().getKorSzam() <= raceStatusRow.getLapTimes().size()) {
                    tavStatisticsTableRowMap.get(raceStatusRow.getTav().getId()).finished++;
                    versenySzamStatisticsTableRowMap.get(raceStatusRow.getVersenySzam().getId()).finished++;
                } else if (raceStatusRow.getVersenyzo() != null && raceStatusRow.getVersenyzo().isFeladta()) {
                    tavStatisticsTableRowMap.get(raceStatusRow.getTav().getId()).gaveup++;
                    versenySzamStatisticsTableRowMap.get(raceStatusRow.getVersenySzam().getId()).gaveup++;
                }
            }
        }

        for (Versenyzo versenyzo: scmtMarathon.getVersenyzoMapCache().getAllVersenyzo()) {
            VersenySzam versenySzam = scmtMarathon.getVersenyszamMapCache().getVersenySzam(versenyzo.getVersenySzamId());
            tavStatisticsTableRowMap.get(versenySzam.getTavId()).participants++;
            versenySzamStatisticsTableRowMap.get(versenyzo.getVersenySzamId()).participants++;
        }

        statisticsCellTable.setRowData(statisticsTableRowList);
        statisticsCellTable.redraw();
    }

    private class StatisticsTableRow {
        private String description;
        private int participants;
        private int finished;
        private int gaveup;

        private StatisticsTableRow(String description) {
            this.description = description;
        }
    }

    private abstract class RightAlignmentColumn extends TextColumn<StatisticsTableRow> {
        protected RightAlignmentColumn() {
            setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        }
    }
}
