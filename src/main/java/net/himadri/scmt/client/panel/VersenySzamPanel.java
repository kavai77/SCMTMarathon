package net.himadri.scmt.client.panel;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.view.client.ListDataProvider;
import net.himadri.scmt.client.MarathonService;
import net.himadri.scmt.client.MarathonServiceAsync;
import net.himadri.scmt.client.SCMTMarathon;
import net.himadri.scmt.client.Utils;
import net.himadri.scmt.client.callback.EmptyFailureHandlingAsyncCallback;
import net.himadri.scmt.client.dialog.VersenySzamEntryDialog;
import net.himadri.scmt.client.entity.RaceStatus;
import net.himadri.scmt.client.entity.Tav;
import net.himadri.scmt.client.entity.VersenySzam;
import net.himadri.scmt.client.entity.Versenyzo;
import net.himadri.scmt.client.gwtextras.ImageButton;
import net.himadri.scmt.client.gwtextras.SortableTextColumn;
import net.himadri.scmt.client.serializable.MarathonActionListener;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.10. 22:01
 */
public class VersenySzamPanel extends Composite {
    private MarathonServiceAsync marathonService = GWT.create(MarathonService.class);

    private CellTable<VersenySzam> versenySzamTable = new CellTable<VersenySzam>();
    private VersenySzamEntryDialog versenySzamEntryDialog;
    private ListDataProvider<VersenySzam> versenySzamList = new ListDataProvider<VersenySzam>();
    private Button btnUjVersenyszam;
    private Column<VersenySzam, VersenySzam> modositasColumn;
    private Column<VersenySzam, VersenySzam> torlesColumn;

    public VersenySzamPanel(final SCMTMarathon scmtMarathon) {
        AbsolutePanel versenySzamPanel = new AbsolutePanel();

        versenySzamEntryDialog = new VersenySzamEntryDialog(scmtMarathon);
        btnUjVersenyszam = new ImageButton("edit_add.png", "Új versenyszám", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                versenySzamEntryDialog.showDialogForNew();
            }
        });
        versenySzamPanel.add(btnUjVersenyszam, 817, 0);

        ScrollPanel tableScroll = new ScrollPanel();
        versenySzamPanel.add(tableScroll);
        tableScroll.setSize("800px", "500px");
        tableScroll.setWidget(versenySzamTable);
        versenySzamTable.setSize("100%", "100%");

        ColumnSortEvent.ListHandler<VersenySzam> listHandler = new ColumnSortEvent.ListHandler<VersenySzam>(versenySzamList.getList());
        versenySzamTable.addColumnSortHandler(listHandler);

        versenySzamTable.addColumn(new SortableTextColumn<VersenySzam>(listHandler) {
            @Override
            public String getValue(VersenySzam versenySzam) {
                return versenySzam.getTavId() != null ?
                        scmtMarathon.getTavMapCache().getTav(versenySzam.getTavId()).getMegnevezes() :
                        null;
            }
        }, "Táv");
        versenySzamTable.addColumn(new SortableTextColumn<VersenySzam>(listHandler) {
            @Override
            public String getValue(VersenySzam versenySzam) {
                return Utils.getKorosztalyMegnevezes(versenySzam.getKorTol(), versenySzam.getKorIg());
            }
        }, "Korcsoport");
        versenySzamTable.addColumn(new SortableTextColumn<VersenySzam>(listHandler) {
            @Override
            public String getValue(VersenySzam versenySzam) {
                return Utils.getFerfiMegnevezes(versenySzam.getFerfi());
            }
        }, "Nem");
        versenySzamTable.addColumn(new SortableTextColumn<VersenySzam>(listHandler) {
            @Override
            public String getValue(VersenySzam versenySzam) {
                return Utils.getVersenySzamMegnevezes(scmtMarathon, versenySzam);
            }
        }, "Megnevezés");
        modositasColumn = new Column<VersenySzam, VersenySzam>(
                new ActionCell("Módosítás", new ActionCell.Delegate<VersenySzam>() {
                    @Override
                    public void execute(VersenySzam versenySzam) {
                        versenySzamEntryDialog.showDialogForModify(versenySzam.getId(), versenySzam);
                    }
                })) {
            @Override
            public VersenySzam getValue(VersenySzam versenySzam) {
                return versenySzam;
            }
        };
        versenySzamTable.addColumn(modositasColumn);

        torlesColumn = new Column<VersenySzam, VersenySzam>(
                new ActionCell("Törlés", new ActionCell.Delegate<VersenySzam>() {
                    @Override
                    public void execute(final VersenySzam versenySzam) {
                        for (Versenyzo versenyzo : scmtMarathon.getVersenyzoMapCache().getAllVersenyzo()) {
                            if (versenySzam.getId().equals(versenyzo.getVersenySzamId())) {
                                Window.alert("Versenyszám csak akkor törölhető, ha még nem lett versenyzőhöz hozzárendelve. " +
                                        "Egyébként csak módosítani lehet.");
                                return;
                            }
                        }
                        if (Window.confirm("Biztos törölni akarod a kiválasztott elemet?")) {
                            marathonService.removeVersenySzam(versenySzam.getId(), new EmptyFailureHandlingAsyncCallback<Void>());
                        }
                    }
                })) {
            @Override
            public VersenySzam getValue(VersenySzam versenySzam) {
                return versenySzam;
            }
        };
        versenySzamTable.addColumn(torlesColumn);
        versenySzamTable.setPageSize(Integer.MAX_VALUE);
        versenySzamList.addDataDisplay(versenySzamTable);

        scmtMarathon.getPollingService().getVersenySzamSync().addMarathonActionListener(new VersenySzamActionListener());
        scmtMarathon.getPollingService().getRaceStatusSync().addMarathonActionListener(new RaceStatusActionListener());
        scmtMarathon.getPollingService().getTavSync().addMarathonActionListener(new TavActionListener());

        initWidget(versenySzamPanel);
    }

    private class VersenySzamActionListener implements MarathonActionListener<VersenySzam> {
        @Override
        public void itemAdded(List<VersenySzam> items) {
            versenySzamList.getList().addAll(items);
        }

        @Override
        public void itemRefreshed(List<VersenySzam> items) {
            versenySzamList.getList().clear();
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
                btnUjVersenyszam.setVisible(true);
                modositasColumn.setCellStyleNames("visible");
                torlesColumn.setCellStyleNames("visible");
            } else {
                btnUjVersenyszam.setVisible(false);
                modositasColumn.setCellStyleNames("hidden");
                torlesColumn.setCellStyleNames("hidden");
            }
            versenySzamTable.redraw();
        }
    }

    private class TavActionListener implements MarathonActionListener<Tav> {
        @Override
        public void itemAdded(List<Tav> items) {
        }

        @Override
        public void itemRefreshed(List<Tav> items) {
            versenySzamTable.redraw();
        }
    }
}
