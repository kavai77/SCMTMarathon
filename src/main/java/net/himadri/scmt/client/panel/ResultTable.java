package net.himadri.scmt.client.panel;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.view.client.ListDataProvider;
import net.himadri.scmt.client.*;
import net.himadri.scmt.client.entity.PersonLap;
import net.himadri.scmt.client.entity.Tav;
import net.himadri.scmt.client.entity.VersenySzam;
import net.himadri.scmt.client.entity.Versenyzo;
import net.himadri.scmt.client.serializable.MarathonActionListener;
import net.himadri.scmt.client.serializable.RaceStatusRow;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.08. 18:57
 */
public class ResultTable extends Composite {
    public static final int FIX_COLUMN_COUNT = 4;
    private SCMTMarathon scmtMarathon;
    private TavVersenySzam filter;
    private CellTable<RaceStatusRow> statusTable = new CellTable<RaceStatusRow>();
    private ListDataProvider<RaceStatusRow> raceStatusRowList = new ListDataProvider<RaceStatusRow>();
    private Map<RaceStatusRow, Integer> helyezesMap = new HashMap<RaceStatusRow, Integer>();
    private ColumnSortEvent.ListHandler<RaceStatusRow> listHandler = new ColumnSortEvent.ListHandler<RaceStatusRow>(raceStatusRowList.getList());

    class TimeCellData {
        Long actualTime;
        Long elapsedTime;
        boolean feladta;
    }

    public ResultTable(final SCMTMarathon scmtMarathon, TavVersenySzam filter) {
        this.scmtMarathon = scmtMarathon;
        this.filter = filter;
        ScrollPanel statusScrollPanel = new ScrollPanel();
        statusScrollPanel.setSize("980px", "500px");
        statusScrollPanel.setWidget(statusTable);
        statusTable.setSize("100%", "100%");
        statusTable.addColumnSortHandler(listHandler);
        statusTable.addColumn(new TavColumn(listHandler), "Táv");
        statusTable.addColumn(new SortableTextColumn<RaceStatusRow>(listHandler) {
            @Override
            public String getValue(RaceStatusRow raceStatusRow) {
                return raceStatusRow.getRaceNumber();
            }
        }, "Rajtszám");
        statusTable.addColumn(new SortableTextColumn<RaceStatusRow>(listHandler) {
            @Override
            public String getValue(RaceStatusRow raceStatusRow) {
                return raceStatusRow.getVersenyzo() != null ? raceStatusRow.getVersenyzo().getName() : null;
            }
        }, "Versenyző");
        statusTable.addColumn(new SortableTextColumn<RaceStatusRow>(listHandler) {
            @Override
            public String getValue(RaceStatusRow raceStatusRow) {
                return raceStatusRow.getVersenySzam() != null ? Utils.getVersenySzamMegnevezes(scmtMarathon, raceStatusRow.getVersenySzam()) : null;
            }
        }, "Versenyszám");
        raceStatusRowList.addDataDisplay(statusTable);
        statusTable.setPageSize(Integer.MAX_VALUE);
        scmtMarathon.getPollingService().getPersonLapSync().addMarathonActionListener(new RefilterMarathonActionListener<PersonLap>());
        scmtMarathon.getPollingService().getVersenyzoSync().addMarathonActionListener(new RefilterMarathonActionListener<Versenyzo>());
        scmtMarathon.getPollingService().getVersenySzamSync().addMarathonActionListener(new RefilterMarathonActionListener<VersenySzam>());
        scmtMarathon.getPollingService().getTavSync().addMarathonActionListener(new RefilterMarathonActionListener<Tav>());

        initWidget(statusScrollPanel);
    }

    public TavVersenySzam getFilter() {
        return filter;
    }

    public void refilterRaceStatusRows(TavVersenySzam filter) {
        this.filter = filter;

        if (filter.getMode() == TavVersenySzam.Mode.ALL && !(statusTable.getColumn(0) instanceof TavColumn)) {
            statusTable.removeColumn(0);
            statusTable.insertColumn(0, new TavColumn(listHandler), "Táv");
        } else if (filter.getMode() != TavVersenySzam.Mode.ALL && !(statusTable.getColumn(0) instanceof HelyezesColumn)) {
            statusTable.removeColumn(0);
            statusTable.insertColumn(0, new HelyezesColumn(listHandler), "Helyezés");
        }

        refilterRaceStatusRows();
    }

    private void refilterRaceStatusRows() {
        raceStatusRowList.getList().clear();
        helyezesMap.clear();
        int hely = 1;
        for (RaceStatusRow raceStatusRow : scmtMarathon.getRaceStatusRowCache().getAllRaceStatusRows()) {
            if (TavVersenyszamFilter.isAccepted(filter, raceStatusRow.getVersenySzam())) {
                raceStatusRowList.getList().add(raceStatusRow);
                helyezesMap.put(raceStatusRow, hely++);
            }
        }
        adjustColumnCount();
    }

    private void adjustColumnCount() {
        int maxLapCount = 0;
        for (RaceStatusRow raceStatusRow : raceStatusRowList.getList()) {
            maxLapCount = Math.max(maxLapCount, raceStatusRow.getLapTimes().size());
        }
        for (int i = statusTable.getColumnCount() - FIX_COLUMN_COUNT; i < maxLapCount; i++) {
            final int lapNb = i;
            statusTable.addColumn(new Column<RaceStatusRow, TimeCellData>(new TimeCell()) {
                @Override
                public TimeCellData getValue(RaceStatusRow raceStatusRow) {
                    TimeCellData cellTime = new TimeCellData();
                    cellTime.feladta = raceStatusRow.getVersenyzo() != null &&
                            raceStatusRow.getVersenyzo().isFeladta();
                    if (lapNb < raceStatusRow.getLapTimes().size()) {
                        cellTime.actualTime = raceStatusRow.getLapTimes().get(lapNb);
                        if (lapNb > 0) {
                            cellTime.elapsedTime = cellTime.actualTime - raceStatusRow.getLapTimes().get(lapNb - 1);
                        }
                    }
                    return cellTime;
                }
            }, (lapNb + 1) + ". kör");
        }
        while (maxLapCount + FIX_COLUMN_COUNT < statusTable.getColumnCount()) {
            statusTable.removeColumn(statusTable.getColumnCount() - 1);
        }
    }

    private class RefilterMarathonActionListener<T> implements MarathonActionListener<T> {
        @Override
        public void itemAdded(List<T> items) {
            refilterRaceStatusRows();
        }

        @Override
        public void itemRefreshed(List<T> items) {
            refilterRaceStatusRows();
        }
    }

    private class HelyezesColumn extends SortableColumn<RaceStatusRow, Integer> {
        private HelyezesColumn(ColumnSortEvent.ListHandler<RaceStatusRow> listHandler) {
            super(listHandler, new AbstractCell<Integer>() {
                        @Override
                        public void render(Context context, Integer helyezes, SafeHtmlBuilder safeHtmlBuilder) {
                            if (helyezes != null) {
                                safeHtmlBuilder.append(helyezes).append('.');
                            }
                        }
                    }, new Comparator<RaceStatusRow>() {
                @Override
                public int compare(RaceStatusRow o1, RaceStatusRow o2) {
                    return Utils.compareInteger(helyezesMap.get(o1), helyezesMap.get(o2));
                }
            }
            );
        }

        @Override
        public Integer getValue(RaceStatusRow raceStatusRow) {
            return helyezesMap.get(raceStatusRow);
        }
    }

    private class TavColumn extends SortableTextColumn<RaceStatusRow> {
        private TavColumn(ColumnSortEvent.ListHandler<RaceStatusRow> listHandler) {
            super(listHandler);
        }

        @Override
        public String getValue(RaceStatusRow raceStatusRow) {
            return raceStatusRow.getTav() != null ? raceStatusRow.getTav().getMegnevezes() : null;
        }
    }
}
