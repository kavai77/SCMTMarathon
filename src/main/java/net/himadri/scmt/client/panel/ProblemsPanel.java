package net.himadri.scmt.client.panel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import net.himadri.scmt.client.ImageButton;
import net.himadri.scmt.client.SCMTMarathon;
import net.himadri.scmt.client.Utils;
import net.himadri.scmt.client.serializable.RaceStatusRow;

import java.text.ParseException;
import java.util.List;

public class ProblemsPanel extends Composite {
    private SCMTMarathon scmtMarathon;

    private ListDataProvider<StatusTableType> raceStatusRowList = new ListDataProvider<StatusTableType>();
    private IntegerBox elteresBox = new IntegerBox();

    private static class StatusTableType {
        RaceStatusRow raceStatusRow;
        String description;

        private StatusTableType(RaceStatusRow raceStatusRow, String description) {
            this.raceStatusRow = raceStatusRow;
            this.description = description;
        }
    }

    public ProblemsPanel(final SCMTMarathon scmtMarathon) {
        this.scmtMarathon = scmtMarathon;
        CellTable<StatusTableType> statusTable = new CellTable<StatusTableType>();
        ScrollPanel statusScrollPanel = new ScrollPanel();
        statusScrollPanel.setSize("100%", "370px");
        statusScrollPanel.setWidget(statusTable);
        statusTable.setSize("100%", "100%");
        statusTable.addColumn(new TextColumn<StatusTableType>() {
            @Override
            public String getValue(StatusTableType statusTableType) {
                return statusTableType.raceStatusRow.getRaceNumber();
            }
        }, "Rajtszám");
        statusTable.addColumn(new TextColumn<StatusTableType>() {
            @Override
            public String getValue(StatusTableType statusTableType) {
                return statusTableType.raceStatusRow.getVersenyzo() != null ? statusTableType.raceStatusRow.getVersenyzo().getName() : null;
            }
        }, "Versenyző");
        statusTable.addColumn(new TextColumn<StatusTableType>() {
            @Override
            public String getValue(StatusTableType statusTableType) {
                return statusTableType.raceStatusRow.getVersenySzam() != null ? Utils.getVersenySzamMegnevezes(scmtMarathon, statusTableType.raceStatusRow.getVersenySzam()) : null;
            }
        }, "Versenyszám");
        statusTable.addColumn(new TextColumn<StatusTableType>() {
            @Override
            public String getValue(StatusTableType statusTableType) {
                StringBuilder stringBuilder = new StringBuilder();
                List<Long> lapTimes = statusTableType.raceStatusRow.getLapTimes();
                stringBuilder.append("1. kör: ").append(Utils.getElapsedTimeString(statusTableType.raceStatusRow, 0));
                for (int i = 0; i < lapTimes.size() - 1; i++) {
                    stringBuilder.append(", ").append(i + 2).append(" kör: ");
                    stringBuilder.append(Utils.getElapsedTimeString(lapTimes.get(i + 1) - lapTimes.get(i)));
                }
                return stringBuilder.toString();
            }
        }, "Köridők");
        statusTable.addColumn(new TextColumn<StatusTableType>() {
            @Override
            public String getValue(StatusTableType statusTableType) {
                return statusTableType.description;
            }
        }, "Probléma");
        raceStatusRowList.addDataDisplay(statusTable);
        statusTable.setPageSize(Integer.MAX_VALUE);

        HorizontalPanel elteresHorizontal = new HorizontalPanel();
        elteresHorizontal.setSpacing(10);
        elteresHorizontal.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        elteresHorizontal.add(new Label("Eltérés százalékban: "));
        elteresBox.setVisibleLength(3);
        elteresBox.setMaxLength(3);
        elteresBox.setValue(150);
        elteresHorizontal.add(elteresBox);
        elteresHorizontal.add(new ImageButton("viewmag.png", "Keresés", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                findAllProblems();
            }
        }));

        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setSize("100%", "620px");
        mainPanel.setSpacing(10);
        mainPanel.add(statusScrollPanel);
        mainPanel.add(elteresHorizontal);

        initWidget(mainPanel);
    }

    private void findAllProblems() {
        raceStatusRowList.getList().clear();
        findBigLapTimeDifference();
        findSmallLapTimeDifference();
        findExtraLaps();
    }

    private void findBigLapTimeDifference() {
        try {
            int elteres = elteresBox.getValueOrThrow();
            List<RaceStatusRow> raceStatusRows = scmtMarathon.getRaceStatusRowCache ().getAllRaceStatusRows();
            for (RaceStatusRow raceStatusRow: raceStatusRows) {
                List<Long> lapTimes = raceStatusRow.getLapTimes();
                if (lapTimes.size() >= 2) {
                    long maxDiff = findMaximumDiff(lapTimes);
                    long sumDiff = getSumDiff(lapTimes);
                    double avgDiffWithoutMax = (sumDiff - maxDiff) / (lapTimes.size() - 1);
                    double avgDiffThreshold = avgDiffWithoutMax * (elteres / 100.0);
                    if (maxDiff >= avgDiffThreshold) {
                        raceStatusRowList.getList().add(new StatusTableType(raceStatusRow, "Túl lassú kör"));
                    }
                }
            }
        } catch (ParseException e) {
            Window.alert("Nem értelmezhető szám az eltérésnél.");
        }
    }

    private void findSmallLapTimeDifference() {
        try {
            int elteres = elteresBox.getValueOrThrow() - 100;
            List<RaceStatusRow> raceStatusRows = scmtMarathon.getRaceStatusRowCache ().getAllRaceStatusRows();
            for (RaceStatusRow raceStatusRow: raceStatusRows) {
                List<Long> lapTimes = raceStatusRow.getLapTimes();
                if (lapTimes.size() >= 2) {
                    long minDiff = findMinimumDiff(lapTimes);
                    long sumDiff = getSumDiff(lapTimes);
                    double avgDiffWithoutMin = (sumDiff - minDiff) / (lapTimes.size() - 1);
                    double avgDiffThreshold = avgDiffWithoutMin * (elteres / 100.0);
                    if (minDiff <= avgDiffThreshold) {
                        raceStatusRowList.getList().add(new StatusTableType(raceStatusRow, "Túl gyors kör"));
                    }
                }
            }
        } catch (ParseException e) {
            Window.alert("Nem értelmezhető szám az eltérésnél.");
        }
    }

    private long findMaximumDiff(List<Long> longList) {
        long maxValue = longList.get(0);
        for (int i = 0; i < longList.size() - 1; i++) {
            maxValue = Math.max(maxValue, longList.get(i + 1) - longList.get(i));
        }
        return maxValue;
    }

    private long findMinimumDiff(List<Long> longList) {
        long minValue = longList.get(0);
        for (int i = 0; i < longList.size() - 1; i++) {
            minValue = Math.min(minValue, longList.get(i + 1) - longList.get(i));
        }
        return minValue;
    }

    private long getSumDiff(List<Long> longList) {
        long sum = longList.get(0);
        for (int i = 0; i < longList.size() - 1; i++) {
            sum += longList.get(i + 1) - longList.get(i);
        }
        return sum;
    }

    private void findExtraLaps() {
        List<RaceStatusRow> raceStatusRows = scmtMarathon.getRaceStatusRowCache ().getAllRaceStatusRows();
        for (RaceStatusRow raceStatusRow: raceStatusRows) {
            if (raceStatusRow.getLapTimes().size() > raceStatusRow.getTav().getKorSzam()) {
                raceStatusRowList.getList().add(new StatusTableType(raceStatusRow, "Extra kör."));
            }
        }
    }
}
