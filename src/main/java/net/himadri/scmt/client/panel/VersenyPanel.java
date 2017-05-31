package net.himadri.scmt.client.panel;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextButtonCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.NoSelectionModel;
import net.himadri.scmt.client.*;
import net.himadri.scmt.client.callback.CommonAsyncCallback;
import net.himadri.scmt.client.callback.ReloadAsyncCallback;
import net.himadri.scmt.client.entity.Verseny;
import net.himadri.scmt.client.gwtextras.ImageButton;
import net.himadri.scmt.client.token.VersenyToken;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.22. 5:37
 */
public class VersenyPanel extends Composite {

    private static final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.YEAR_MONTH_DAY);

    private final SCMTMarathon scmtMarathon;
    private final MarathonServiceAsync marathonService = GWT.create(MarathonService.class);
    private final UserServiceAsync userService = GWT.create(UserService.class);

    public VersenyPanel(final SCMTMarathon scmtMarathon) {
        this.scmtMarathon = scmtMarathon;

        VerticalPanel rootPanel = new VerticalPanel();
        rootPanel.setSpacing(10);
        rootPanel.addStyleName("centerWithMargin");

        initWidget(rootPanel);

        Label lblVlasszVersenyt = new Label("Válassz versenyt");
        rootPanel.add(lblVlasszVersenyt);

        final ListDataProvider<Verseny> currentYearVersenyek = new ListDataProvider<Verseny>();
        final StackPanel stackPanel = new StackPanel();

        marathonService.getVersenyek(new CommonAsyncCallback<List<Verseny>>() {
            @Override
            public void onSuccess(List<Verseny> versenyek) {
                int currentYear = new Date().getYear();
                Map<Integer, ListDataProvider<Verseny>> evVersenyMap =
                        new TreeMap<Integer, ListDataProvider<Verseny>>();
                for (Verseny verseny: versenyek) {
                    int year = verseny.getRaceStartTime() != null ?
                            new Date(verseny.getRaceStartTime()).getYear() :
                            currentYear;
                    ListDataProvider<Verseny> versenyListDataProvider = evVersenyMap.get(year);
                    if (versenyListDataProvider == null) {
                        if (year == currentYear) {
                            versenyListDataProvider = currentYearVersenyek;
                        } else {
                            versenyListDataProvider = new ListDataProvider<Verseny>();
                        }
                        evVersenyMap.put(year, versenyListDataProvider);
                    }
                    versenyListDataProvider.getList().add(verseny);
                }
                for (Integer year: evVersenyMap.keySet()) {
                    stackPanel.add(createVersenyTable(evVersenyMap.get(year)), Integer.toString(year + 1900));
                }
                stackPanel.showStack(evVersenyMap.size() - 1);
            }
        });
        rootPanel.add(stackPanel);

        Button addVersenyButton = new ImageButton("edit_add.png", "Új verseny", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                String versenyNev = Window.prompt("Verseny neve:", "");
                if (!versenyNev.isEmpty()) {
                    marathonService.addVerseny(versenyNev, new CommonAsyncCallback<Verseny>() {
                        @Override
                        public void onSuccess(Verseny verseny) {
                            currentYearVersenyek.getList().add(verseny);
                        }
                    });
                }
            }
        });


        final HorizontalPanel horizontalPanel = new HorizontalPanel();
        horizontalPanel.setSpacing(10);
        horizontalPanel.add(addVersenyButton);
        userService.isSuperUserAuthorized(new CommonAsyncCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean authorized) {
                if (authorized) {
                    Button configButton = new ImageButton( "settings.png", "Konfiguráció", new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent clickEvent) {
                            scmtMarathon.getMainRootPanel().showConfigurationPanel();
                        }
                    });
                    horizontalPanel.add(configButton);
                }
            }
        });

        rootPanel.add(horizontalPanel);
    }

    private CellTable<Verseny> createVersenyTable(final ListDataProvider<Verseny> versenyList) {
        final CellTable<Verseny> versenyTable = new CellTable<Verseny>();
        versenyTable.setSize("100%", "100%");
        versenyList.addDataDisplay(versenyTable);
        versenyTable.setPageSize(Integer.MAX_VALUE);
        versenyTable.addColumn(new VersenyTextColumn() {
            @Override
            public String getValue(Verseny verseny) {
                return verseny.getNev();
            }
        }, "Név");
        versenyTable.addColumn(new VersenyTextColumn() {
            @Override
            public String getValue(Verseny verseny) {
                return verseny.getRaceStartTime() != null ?
                        dateTimeFormat.format(new Date(verseny.getRaceStartTime()))
                        : null;
            }
        }, "Dátum");
        versenyTable.addColumn(new VersenyTextColumn() {
            @Override
            public String getValue(Verseny verseny) {
                switch (verseny.getRaceStatus()) {
                    case NOT_STARTED:
                        return "Nincs elindítva";
                    case RACING:
                        return "Verseny folyamatban";
                    case FINISHED:
                        return "Befejezve";
                    default:
                        return null;
                }
            }
        }, "Státusz");
        versenyTable.addColumn(new VersenyTextColumn(HasHorizontalAlignment.ALIGN_RIGHT) {
            @Override
            public String getValue(Verseny verseny) {
                return Integer.toString(verseny.getVersenyzoSzam());
            }
        }, "Versenyzők");
        versenyTable.addColumn(new VersenyActionColumn(), "Műveletek");
        UserServiceAsync userService = GWT.create(UserService.class);
        userService.isSuperUserAuthorized(new CommonAsyncCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean authorized) {
                if (authorized) {
                    versenyTable.addColumn(new Column<Verseny, Verseny>(
                            new ActionCell<Verseny>("Törlés", new ActionCell.Delegate<Verseny>() {
                                @Override
                                public void execute(final Verseny verseny) {
                                    if (Window.confirm("Azt a műveletet csak a rendszer adminisztrátor tudja megtenni. Biztos folytatod?")) {
                                        marathonService.deleteRace(verseny.getId(), new CommonAsyncCallback<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                versenyList.getList().remove(verseny);
                                            }
                                        });
                                    }
                                }
                            })) {
                        @Override
                        public Verseny getValue(Verseny verseny) {
                            return verseny;
                        }
                    });
                }
            }
        });
        versenyTable.setSelectionModel(new NoSelectionModel<Verseny>());
        return versenyTable;
    }

    private abstract class VersenyTextColumn extends Column<Verseny, String> {
        public VersenyTextColumn() {
            super(new ClickableTextCell());
            setFieldUpdater(new FieldUpdater<Verseny, String>() {
                @Override
                public void update(int i, Verseny verseny, String s) {
                    scmtMarathon.getVersenySyncSupport().notifyRefreshed(Collections.singletonList(verseny));
                    History.newItem(VersenyToken.encode(verseny.getId()), false);
                }
            });
        }

        public VersenyTextColumn(HorizontalAlignmentConstant align) {
            this();
            setHorizontalAlignment(align);
        }
    }


    private class VersenyActionColumn extends Column<Verseny, String> {
        private VersenyActionColumn() {
            super(new TextButtonCell());
            setFieldUpdater(new FieldUpdater<Verseny, String>() {
                @Override
                public void update(int i, Verseny verseny, String s) {
                    switch (verseny.getRaceStatus()) {
                        case NOT_STARTED:
                            marathonService.startRace(verseny.getId(), new ReloadAsyncCallback());
                        case RACING:
                            marathonService.stopRace(verseny.getId(), new ReloadAsyncCallback());
                            break;
                        case FINISHED:
                            marathonService.restartRace(verseny.getId(), new ReloadAsyncCallback());
                    }

                }
            });
            setHorizontalAlignment(ALIGN_CENTER);
        }

        @Override
        public String getValue(Verseny verseny) {
            switch (verseny.getRaceStatus()) {
                case NOT_STARTED:
                    return "Indítás";
                case RACING:
                    return "Leállítás";
                case FINISHED:
                    return "Újraindítás";
                default:
                    return null;
            }
        }
    }

}
