package net.himadri.scmt.client.panel;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;
import net.himadri.scmt.client.*;
import net.himadri.scmt.client.dialog.VersenyzoEntryDialog;
import net.himadri.scmt.client.entity.RaceStatus;
import net.himadri.scmt.client.entity.Tav;
import net.himadri.scmt.client.entity.VersenySzam;
import net.himadri.scmt.client.entity.Versenyzo;
import net.himadri.scmt.client.serializable.MarathonActionListener;

import java.util.Comparator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.10. 22:01
 */
public class VersenyzoPanel extends Composite {
    private MarathonServiceAsync marathonService = GWT.create(MarathonService.class);

    private CellTable<Versenyzo> versenyzoTable = new CellTable<Versenyzo>();
    private VersenyzoEntryDialog versenyzoEntryDialog;
    private ListDataProvider<Versenyzo> versenyzoListDataProvider = new ListDataProvider<Versenyzo>();
    private Column<Versenyzo, Versenyzo> torlesColumn;
    private Column<Versenyzo, Versenyzo> modositasColumn;
    private Column<Versenyzo, Boolean> feladtaColumn;
    private ListBox versenySzamFilter = new ListBox();
    private Button btnUjVersenyszm;

    private TavVersenySzam filter = TavVersenySzam.createAllAcceptance();
    private SCMTMarathon scmtMarathon;

    public VersenyzoPanel(final SCMTMarathon scmtMarathon) {
        this.scmtMarathon = scmtMarathon;
        versenyzoEntryDialog = new VersenyzoEntryDialog(scmtMarathon);
        AbsolutePanel versenyzoPanel = new AbsolutePanel();

        btnUjVersenyszm = new ImageButton("edit_add.png", "Új versenyző", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                versenyzoEntryDialog.showDialog(null);
            }
        });
        versenyzoPanel.add(btnUjVersenyszm, 810, 0);

        versenyzoPanel.add(new Label("Szűrés"), 810, 58);
        versenySzamFilter.setSize("150px", "23px");
        versenyzoPanel.add(versenySzamFilter, 810, 80);
        versenySzamFilter.addItem("Összes versenyző", "");
        versenySzamFilter.addChangeHandler(new VersenySzamFilterChangeHandler());

        ScrollPanel tableScroll = new ScrollPanel();
        versenyzoPanel.add(tableScroll);
        tableScroll.setSize("800px", "500px");
        tableScroll.setWidget(versenyzoTable);
        versenyzoTable.setSize("100%", "100%");

        ColumnSortEvent.ListHandler<Versenyzo> listHandler = new ColumnSortEvent.ListHandler<Versenyzo>(versenyzoListDataProvider.getList());
        versenyzoTable.addColumnSortHandler(listHandler);

        versenyzoTable.addColumn(new SortableTextColumn<Versenyzo>(listHandler) {
            @Override
            public String getValue(Versenyzo versenyzo) {
                return versenyzo.getRaceNumber();
            }
        }, "Rajtszám");
        versenyzoTable.addColumn(new SortableTextColumn<Versenyzo>(listHandler) {
            @Override
            public String getValue(Versenyzo versenyzo) {
                return versenyzo.getName();
            }
        }, "Név");
        versenyzoTable.addColumn(new SortableTextColumn<Versenyzo>(listHandler) {
            @Override
            public String getValue(Versenyzo versenyzo) {
                return Utils.getFerfiMegnevezes(versenyzo.getFerfi());
            }
        }, "Nem");
        versenyzoTable.addColumn(new SortableTextColumn<Versenyzo>(listHandler, new Comparator<Versenyzo>() {
            @Override
            public int compare(Versenyzo o1, Versenyzo o2) {
                return Utils.compareInteger(o1.getSzuletesiEv(), o2.getSzuletesiEv());
            }
        }) {
            @Override
            public String getValue(Versenyzo versenyzo) {
                return versenyzo.getSzuletesiEv() != null ? versenyzo.getSzuletesiEv().toString() : null;
            }
        }, "Születési év");
        versenyzoTable.addColumn(new SortableTextColumn<Versenyzo>(listHandler) {
            @Override
            public String getValue(Versenyzo versenyzo) {
                return versenyzo.getEgyesulet();
            }
        }, "Egyesület");
        versenyzoTable.addColumn(new SortableTextColumn<Versenyzo>(listHandler) {
            @Override
            public String getValue(Versenyzo versenyzo) {
                VersenySzam versenySzam = scmtMarathon.getVersenyszamMapCache().getVersenySzam(versenyzo.getVersenySzamId());
                return Utils.getVersenySzamMegnevezes(scmtMarathon, versenySzam);
            }
        }, "Versenyszám");

        final CheckboxCell checkboxCell = new CheckboxCell(false, false);
        feladtaColumn = new Column<Versenyzo, Boolean>(checkboxCell) {
            @Override
            public Boolean getValue(Versenyzo versenyzo) {
                return versenyzo != null && versenyzo.isFeladta();
            }
        };

        feladtaColumn.setFieldUpdater(new FieldUpdater<Versenyzo, Boolean>() {
            @Override
            public void update(int i, final Versenyzo versenyzo, Boolean feladta) {
                String confirmationText;
                if (feladta) {
                    confirmationText = "Biztos feladta " + versenyzo.getRaceNumber() + " " + versenyzo.getName() + " a versenyt?";
                } else {
                    confirmationText = "Biztos folytatja " + versenyzo.getRaceNumber() + " " + versenyzo.getName() + " a versenyt?";
                }
                if (Window.confirm(confirmationText)) {
                    marathonService.versenyzoFeladta(versenyzo.getId(), feladta, new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable throwable) {
                            undoChanges(versenyzo);
                            SCMTMarathon.commonFailureHandling(throwable);
                        }

                        @Override
                        public void onSuccess(Void aVoid) {
                        }
                    });
                } else {
                    undoChanges(versenyzo);
                }
            }

            private void undoChanges(Versenyzo versenyzo) {
                checkboxCell.clearViewData(versenyzo);
                versenyzoTable.redraw();
            }
        });

        versenyzoTable.addColumn(feladtaColumn, "Feladta");

        modositasColumn = new Column<Versenyzo, Versenyzo>(
                new ActionCell("Módosítás", new ActionCell.Delegate<Versenyzo>() {
                    @Override
                    public void execute(Versenyzo versenyzo) {
                        versenyzoEntryDialog.showDialog(versenyzo);
                    }
                })) {
            @Override
            public Versenyzo getValue(Versenyzo versenyzo) {
                return versenyzo;
            }
        };
        versenyzoTable.addColumn(modositasColumn);

        torlesColumn = new Column<Versenyzo, Versenyzo>(
                new ActionCell("Törlés", new ActionCell.Delegate<Versenyzo>() {
                    @Override
                    public void execute(final Versenyzo versenyzo) {
                        if (Window.confirm("Biztos törölni akarod a kiválasztott versenyzőt?")) {
                            marathonService.removeVersenyzo(versenyzo.getId(), new EmptyFailureHandlingAsyncCallback<Void>());
                        }
                    }
                })) {
            @Override
            public Versenyzo getValue(Versenyzo versenyzo) {
                return versenyzo;
            }
        };
        versenyzoTable.addColumn(torlesColumn);
        versenyzoTable.setPageSize(Integer.MAX_VALUE);
        versenyzoTable.setRowStyles(new RowStyles<Versenyzo>() {
            @Override
            public String getStyleNames(Versenyzo versenyzo, int i) {
                return versenyzo.isFeladta() ? "strikethrough" : null;
            }
        });
        versenyzoListDataProvider.addDataDisplay(versenyzoTable);

        scmtMarathon.getPollingService().getVersenyzoSync().addMarathonActionListener(new VersenyzoActionListener());
        scmtMarathon.getPollingService().getVersenySzamSync().addMarathonActionListener(new VersenyszamActionListener<VersenySzam>());
        scmtMarathon.getPollingService().getTavSync().addMarathonActionListener(new VersenyszamActionListener<Tav>());
        scmtMarathon.getPollingService().getRaceStatusSync().addMarathonActionListener(new RaceStatusActionListener());

        initWidget(versenyzoPanel);
    }

    private class VersenyzoActionListener implements MarathonActionListener<Versenyzo> {
        @Override
        public void itemAdded(List<Versenyzo> items) {
            for (Versenyzo versenyzo: items) {
                if (TavVersenyszamFilter.isAccepted(filter, versenyzo, scmtMarathon)) {
                    versenyzoListDataProvider.getList().add(versenyzo);
                }
            }
        }

        @Override
        public void itemRefreshed(List<Versenyzo> items) {
            versenyzoListDataProvider.getList().clear();
            itemAdded(items);
        }
    }

    private class VersenyszamActionListener<T> implements MarathonActionListener<T> {
        @Override
        public void itemAdded(List<T> items) {
            refreshVersenySzamValaszto();
        }

        @Override
        public void itemRefreshed(List<T> items) {
            versenyzoTable.redraw();
            refreshVersenySzamValaszto();
        }

        private void refreshVersenySzamValaszto() {
            versenySzamFilter.clear();
            versenySzamFilter.addItem("Összes versenyző", TavVersenySzamToken.encode(TavVersenySzam.createAllAcceptance()));
            for (Tav tav : scmtMarathon.getTavMapCache().getAllTav()) {
                versenySzamFilter.addItem(tav.getMegnevezes() + " összes",
                        TavVersenySzamToken.encode(TavVersenySzam.createTav(tav.getId())));
            }
            for (VersenySzam versenySzam : scmtMarathon.getVersenyszamMapCache().getAllVersenySzam()) {
                versenySzamFilter.addItem(Utils.getVersenySzamMegnevezes(scmtMarathon, versenySzam),
                        TavVersenySzamToken.encode(TavVersenySzam.createVersenyszamFilter(versenySzam.getId())));
            }
            new VersenySzamFilterChangeHandler().onChange(null);
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
                btnUjVersenyszm.setVisible(true);
                modositasColumn.setCellStyleNames("visible");
                torlesColumn.setCellStyleNames("visible");
                feladtaColumn.setCellStyleNames("visible");
            } else {
                btnUjVersenyszm.setVisible(false);
                modositasColumn.setCellStyleNames("hidden");
                torlesColumn.setCellStyleNames("hidden");
                feladtaColumn.setCellStyleNames("hidden");
            }
            versenyzoTable.redraw();
        }
    }

    private class VersenySzamFilterChangeHandler implements ChangeHandler {
        @Override
        public void onChange(ChangeEvent changeEvent) {
            int selectedIndex = versenySzamFilter.getSelectedIndex();
            String selectedValue = versenySzamFilter.getValue(selectedIndex);
            filter = TavVersenySzamToken.decode(selectedValue);

            versenyzoListDataProvider.getList().clear();
            for (Versenyzo versenyzo: scmtMarathon.getVersenyzoMapCache().getAllVersenyzo()) {
                if (TavVersenyszamFilter.isAccepted(filter, versenyzo, scmtMarathon)) {
                    versenyzoListDataProvider.getList().add(versenyzo);
                }
            }
        }
    }
}
