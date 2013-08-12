package net.himadri.scmt.client.panel;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import net.himadri.scmt.client.*;
import net.himadri.scmt.client.entity.Verseny;

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

    public VersenyPanel(SCMTMarathon scmtMarathon) {
        this.scmtMarathon = scmtMarathon;


        VerticalPanel rootPanel = new VerticalPanel();
        rootPanel.setSpacing(10);
//        rootPanel.setSize("500px", "330px");
        rootPanel.addStyleName("centerWithMargin");

        initWidget(rootPanel);

        Label lblVlasszVersenyt = new Label("Válassz versenyt");
        rootPanel.add(lblVlasszVersenyt);

        final ListDataProvider<Verseny> currentYearVersenyek = new ListDataProvider<Verseny>();
        final StackPanel stackPanel = new StackPanel();

        marathonService.getVersenyek(new AsyncCallback<List<Verseny>>() {
            @Override
            public void onFailure(Throwable throwable) {
                SCMTMarathon.commonFailureHandling(throwable);
            }

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
                String versenyNev = Window.prompt("Verseny neve:", null);
                if (!versenyNev.isEmpty()) {
                    marathonService.addVerseny(versenyNev, new AsyncCallback<Verseny>() {
                        @Override
                        public void onFailure(Throwable throwable) {
                            SCMTMarathon.commonFailureHandling(throwable);
                        }

                        @Override
                        public void onSuccess(Verseny verseny) {
                            currentYearVersenyek.getList().add(verseny);
                        }
                    });
                }
            }
        });

        rootPanel.add(addVersenyButton);
    }

    private CellTable<Verseny> createVersenyTable(final ListDataProvider<Verseny> versenyList) {
        final CellTable<Verseny> versenyTable = new CellTable<Verseny>();
        versenyTable.setSize("100%", "100%");
        versenyList.addDataDisplay(versenyTable);
        versenyTable.setPageSize(Integer.MAX_VALUE);
        versenyTable.addColumn(new TextColumn<Verseny>() {
            @Override
            public String getValue(Verseny verseny) {
                return verseny.getNev();
            }
        }, "Név");
        versenyTable.addColumn(new TextColumn<Verseny>() {
            @Override
            public String getValue(Verseny verseny) {
                return verseny.getRaceStartTime() != null ?
                        dateTimeFormat.format(new Date(verseny.getRaceStartTime()))
                        : null;
            }
        }, "Dátum");
        versenyTable.addColumn(new TextColumn<Verseny>() {
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
        versenyTable.addColumn(new RightAlignmentTextColumn<Verseny>() {
            @Override
            public String getValue(Verseny verseny) {
                return Integer.toString(verseny.getVersenyzoSzam());
            }
        }, "Versenyzők");
        UserServiceAsync userService = GWT.create(UserService.class);
        userService.isSuperUserAuthorized(new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable throwable) {
                SCMTMarathon.commonFailureHandling(throwable);
            }

            @Override
            public void onSuccess(Boolean authorized) {
                if (authorized) {
                    versenyTable.addColumn(new Column<Verseny, Verseny>(
                            new ActionCell<Verseny>("Törlés", new ActionCell.Delegate<Verseny>() {
                        @Override
                        public void execute(final Verseny verseny) {
                            if (Window.confirm("Azt a műveletet csak a rendszer adminisztrátor tudja megtenni. Biztos folytatod?")) {
                                marathonService.deleteRace(verseny.getId(), new AsyncCallback<Void>() {
                                    @Override
                                    public void onFailure(Throwable throwable) {
                                        SCMTMarathon.commonFailureHandling(throwable);
                                    }

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
        final SingleSelectionModel<Verseny> versenySelectionModel = new SingleSelectionModel<Verseny>
                (new ProvidesKey<Verseny>() {
            @Override
            public Object getKey(Verseny verseny) {
                return verseny.getId();
            }
        });
        versenyTable.setSelectionModel(versenySelectionModel);

        versenySelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
                scmtMarathon.getVersenySyncSupport().notifyRefreshed(Collections.singletonList(versenySelectionModel.getSelectedObject()));
            }
        });
        return versenyTable;
    }

    private static abstract class RightAlignmentTextColumn<T> extends TextColumn<T> {
        protected RightAlignmentTextColumn() {
            setHorizontalAlignment(ALIGN_RIGHT);
        }
    }
}
