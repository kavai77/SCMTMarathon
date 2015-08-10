package net.himadri.scmt.client.panel;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.FormElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;
import net.himadri.scmt.client.*;
import net.himadri.scmt.client.callback.EmptyFailureHandlingAsyncCallback;
import net.himadri.scmt.client.dialog.TavEntryDialog;
import net.himadri.scmt.client.entity.RaceStatus;
import net.himadri.scmt.client.entity.Tav;
import net.himadri.scmt.client.entity.VersenySzam;
import net.himadri.scmt.client.serializable.MarathonActionListener;

import java.util.Comparator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.05.17. 6:36
 */
public class TavPanel extends Composite {
    private MarathonServiceAsync marathonService = GWT.create(MarathonService.class);

    private CellTable<Tav> tavTable = new CellTable<Tav>();
    private TavEntryDialog tavEntryDialog;
    private ListDataProvider<Tav> tavList = new ListDataProvider<Tav>();
    private Button btnUjTav;
    private Column<Tav, Tav> modositasColumn;
    private Column<Tav, Tav> torlesColumn;
    private Column<Tav, Tav> futamInditasColumn;

    public TavPanel(final SCMTMarathon scmtMarathon) {
        AbsolutePanel tavPanel = new AbsolutePanel();

        tavEntryDialog = new TavEntryDialog(scmtMarathon);
        btnUjTav = new ImageButton("edit_add.png", "Új táv", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                tavEntryDialog.showDialogForNew();
            }
        });
        tavPanel.add(btnUjTav, 627, 0);

        final FormPanel formPanel = new FormPanel();
        final Hidden pdfServiceParam = new Hidden("tav");
        formPanel.setAction("/scmtmarathon/PrePrintedPDFService");
        formPanel.setMethod(FormPanel.METHOD_GET);
        formPanel.getElement().<FormElement>cast().setTarget("_blank");
        formPanel.add(pdfServiceParam);
        tavPanel.add(formPanel);

        ScrollPanel tableScroll = new ScrollPanel();
        tavPanel.add(tableScroll);
        tableScroll.setSize("600px", "500px");
        tableScroll.setWidget(tavTable);
        tavTable.setSize("100%", "100%");

        ColumnSortEvent.ListHandler<Tav> listHandler = new ColumnSortEvent.ListHandler<Tav>(tavList.getList());
        tavTable.addColumnSortHandler(listHandler);

        tavTable.addColumn(new SortableTextColumn<Tav>(listHandler) {
            @Override
            public String getValue(Tav tav) {
                return tav.getMegnevezes();
            }
        }, "Táv");
        tavTable.addColumn(new SortableTextColumn<Tav>(listHandler, new Comparator<Tav>() {
            @Override
            public int compare(Tav o1, Tav o2) {
                return Utils.compareInteger(o1.getKorSzam(), o2.getKorSzam());
            }
        }) {
            @Override
            public String getValue(Tav tav) {
                return tav.getKorSzam() != null ? tav.getKorSzam().toString() : null;
            }
        }, "Körök száma");
        tavTable.addColumn(new SortableTextColumn<Tav>(listHandler) {
            @Override
            public String getValue(Tav tav) {
                return (tav.getVersenySzamtol() != null ? tav.getVersenySzamtol().toString() : "") + "-" +
                       (tav.getVersenySzamig() != null ? tav.getVersenySzamig().toString() : "") ;
            }
        }, "Rajtszámok");
        tavTable.addColumn(new SortableTextColumn<Tav>(listHandler) {
            @Override
            public String getValue(Tav tav) {
                return (tav.getRaceStartDiff() == 0 ? "" : Utils.getElapsedTimeString(tav.getRaceStartDiff()));
            }
        }, "Futam Start");
        modositasColumn = new Column<Tav, Tav>(
                new ActionCell("Módosítás", new ActionCell.Delegate<Tav>() {
                    @Override
                    public void execute(Tav tav) {
                        tavEntryDialog.showDialogForModify(tav.getId(), tav);
                    }
                })) {
            @Override
            public Tav getValue(Tav tav) {
                return tav;
            }
        };
        tavTable.addColumn(modositasColumn);

        torlesColumn = new Column<Tav, Tav>(
                new ActionCell("Törlés", new ActionCell.Delegate<Tav>() {
                    @Override
                    public void execute(final Tav tav) {
                        for (VersenySzam versenySzam : scmtMarathon.getVersenyszamMapCache().getAllVersenySzam()) {
                            if (tav.getId().equals(versenySzam.getTavId())) {
                                Window.alert("Táv csak akkor törölhető, ha még nem lett versenyszámhoz hozzárendelve. " +
                                        "Egyébként csak módosítani lehet.");
                                return;
                            }
                        }
                        if (Window.confirm("Biztos törölni akarod a kiválasztott elemet?")) {
                            marathonService.removeTav(tav.getId(), new EmptyFailureHandlingAsyncCallback<Void>());
                        }
                    }
                })) {
            @Override
            public Tav getValue(Tav tav) {
                return tav;
            }
        };
        tavTable.addColumn(torlesColumn);

        futamInditasColumn = new Column<Tav, Tav>(
                new ActionCell("Futam Indítása", new ActionCell.Delegate<Tav>() {
                    @Override
                    public void execute(final Tav tav) {
                        marathonService.futamStart(tav.getId(), new EmptyFailureHandlingAsyncCallback<Long>() {
                            @Override
                            public void onSuccess(Long diff) {
                                scmtMarathon.getTavMapCache().getTav(tav.getId()).setRaceStartDiff(diff);    
                            }
                        });
                    }
                })) {
            @Override
            public Tav getValue(Tav tav) {
                return tav;
            }
        };
        tavTable.addColumn(futamInditasColumn);

        tavTable.setPageSize(Integer.MAX_VALUE);
        tavList.addDataDisplay(tavTable);

        scmtMarathon.getPollingService().getTavSync().addMarathonActionListener(new TavActionListener());
        scmtMarathon.getPollingService().getRaceStatusSync().addMarathonActionListener(new RaceStatusActionListener());

        initWidget(tavPanel);
    }

    private class TavActionListener implements MarathonActionListener<Tav> {
        @Override
        public void itemAdded(List<Tav> items) {
            tavList.getList().addAll(items);
        }

        @Override
        public void itemRefreshed(List<Tav> items) {
            tavList.getList().clear();
            itemAdded(items);
        }
    }

    private class RaceStatusActionListener implements MarathonActionListener<RaceStatus> {
        @Override
        public void itemAdded(List<RaceStatus> items) {
            itemRefreshed(items);
        }

        @Override
        public void itemRefreshed(List<RaceStatus> items) {
            RaceStatus raceStatus = items.get(0);
            if (raceStatus.isActive()) {
                btnUjTav.setVisible(true);
                modositasColumn.setCellStyleNames("visible");
                torlesColumn.setCellStyleNames("visible");
            } else {
                btnUjTav.setVisible(false);
                modositasColumn.setCellStyleNames("hidden");
                torlesColumn.setCellStyleNames("hidden");
            }
            futamInditasColumn.setCellStyleNames(raceStatus == RaceStatus.NOT_STARTED ? "hidden" : "visible");
            tavTable.redraw();
        }
    }
}
