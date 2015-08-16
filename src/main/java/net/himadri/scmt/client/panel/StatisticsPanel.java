package net.himadri.scmt.client.panel;

import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import net.himadri.scmt.client.SCMTMarathon;
import net.himadri.scmt.client.TabChangeHandler;
import net.himadri.scmt.client.Utils;
import net.himadri.scmt.client.entity.Tav;
import net.himadri.scmt.client.entity.VersenySzam;
import net.himadri.scmt.client.entity.Versenyzo;
import net.himadri.scmt.client.gwtextras.ImageButton;
import net.himadri.scmt.client.serializable.RaceStatusRow;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.07.16. 21:44
 */
public class StatisticsPanel extends Composite implements TabChangeHandler {
    private CellTable<StatisticsTableRow> statisticsCellTable;
    private SCMTMarathon scmtMarathon;
    private StatisticsOverview statisticsOverview = new StatisticsOverview();

    public StatisticsPanel(final SCMTMarathon scmtMarathon) {
        this.scmtMarathon = scmtMarathon;
        VerticalPanel racePanel = new VerticalPanel();
        racePanel.setSpacing(20);

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
        statisticsCellTable.addColumn(new DobogoColumn(), "Eredményhirdetés" );

        ScrollPanel tableScroll = new ScrollPanel(statisticsCellTable);
        tableScroll.setSize("900px", "350px");
        racePanel.add(tableScroll);
        racePanel.add(new Label("* Részletekhez kattints a számra!"));
        racePanel.add(new ImageButton("reload.png", "Frissítés", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                recalculateStatistics();
            }
        }));

        initWidget(racePanel);
    }

    @Override
    public void activated() {
        recalculateStatistics();
    }

    private void recalculateStatistics() {
        Collection<Tav> tavok = scmtMarathon.getTavMapCache().getAllTav();
        List<VersenySzam> versenySzamok = scmtMarathon.getVersenyszamMapCache().getAllVersenySzamSorted();
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


        for (StatisticsTableRow row: versenySzamStatisticsTableRowMap.values()) {
            row.first3Finished = row.finished.size() >= 3 || row.racing.size() == 0;
        }

        for (Tav tav: tavok) {
            boolean isEveryCategoryFinished = true;
            for (VersenySzam versenySzam: versenySzamok) {
                if (versenySzam.getTavId().equals(tav.getId()) &&
                        !versenySzamStatisticsTableRowMap.get(versenySzam.getId()).first3Finished) {
                    isEveryCategoryFinished = false;
                    break;
                }
            }
            tavStatisticsTableRowMap.get(tav.getId()).first3Finished = isEveryCategoryFinished;
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
        private Boolean first3Finished;

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

    private class DobogoColumn extends Column<StatisticsTableRow, String> {
        public DobogoColumn() {
            super(new ClickableTextCell() {
                public void render(Context context, SafeHtml value, SafeHtmlBuilder sb)
                {
                    String logo = value.asString();
                    if (!logo.isEmpty()) {
                        sb.appendHtmlConstant("<img height=\"18\" src=\"images/" + logo + "\">");
                    }
                }
            });
            setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        }

        @Override
        public String getValue(StatisticsTableRow statisticsTableRow) {
            if (statisticsTableRow.first3Finished == null) {
                return "";
            } else {
                return statisticsTableRow.first3Finished ? "button_ok.png" : "button_cancel.png";
            }
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
