package net.himadri.scmt.client.dialog;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;
import net.himadri.scmt.client.MarathonService;
import net.himadri.scmt.client.MarathonServiceAsync;
import net.himadri.scmt.client.SCMTMarathon;
import net.himadri.scmt.client.Utils;
import net.himadri.scmt.client.entity.PersonLap;

import java.util.*;

public class CorrectionDialogBox extends DialogBox {

    private ListDataProvider<PersonLap> personLapListDataProvider = new ListDataProvider<PersonLap>();
    private List<PersonLap> allPersonLapList;
    private Map<String, List<PersonLap>> filterPersonLapMap;
    private TextBox szuresText = new TextBox();

    MarathonServiceAsync marathonService = GWT.create(MarathonService.class);
    private SCMTMarathon scmtMarathon;

    public CorrectionDialogBox(SCMTMarathon scmtMarathon) {
        super(true);
        this.scmtMarathon = scmtMarathon;
        setHTML("Javító ablak");
        setAnimationEnabled(true);

        AbsolutePanel absolutePanel = new AbsolutePanel();
        setWidget(absolutePanel);
        absolutePanel.setSize("370px", "500px");

        absolutePanel.add(new Label("Szűrés: "), 0, 17);
        szuresText.setWidth("100px");
        absolutePanel.add(szuresText, 55, 10);
        szuresText.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent keyPressEvent) {
                new Timer() {
                    @Override
                    public void run() {
                        String filter = szuresText.getText().trim();
                        if (filter.isEmpty()) {
                            personLapListDataProvider.setList(allPersonLapList);
                        } else {
                            personLapListDataProvider.setList(filterPersonLapMap.get(filter));
                        }
                    }
                }.schedule(100);
            }
        });

        Label lblVlaszdKiA = new Label("Kattints duplán a rajtszámra vagy az időre a javításhoz");
        absolutePanel.add(lblVlaszdKiA, 0, 50);

        ScrollPanel scrollPanel = new ScrollPanel();
        absolutePanel.add(scrollPanel, 0, 74);
        scrollPanel.setSize("350px", "400px");

        final CellTable<PersonLap> cellTable = new CellTable<PersonLap>();
        scrollPanel.setWidget(cellTable);
        cellTable.setSize("100%", "100%");

        final EditTextCell raceNbEditCell = new EditTextCell();
        Column<PersonLap, String> raceNbColumn = new Column<PersonLap, String>(raceNbEditCell) {
            @Override
            public String getValue(PersonLap personLap) {
                return personLap.getRaceNumber();
            }
        };
        raceNbColumn.setFieldUpdater(new FieldUpdater<PersonLap, String>() {
            @Override
            public void update(int i, final PersonLap personLap, final String raceNumber) {
                if (raceNumber.isEmpty()) {
                    undoChanges(personLap);
                    return;
                }
                marathonService.updateRaceNumber(personLap.getId(), raceNumber, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        SCMTMarathon.commonFailureHandling(throwable);
                        undoChanges(personLap);
                    }

                    @Override
                    public void onSuccess(Void aVoid) {
                        removePersonLapFromFilterMap(personLap);
                        personLap.setRaceNumber(raceNumber);
                        putPersonLapToFilterMap(personLap);
                    }
                });
            }

            private void undoChanges(PersonLap personLap) {
                raceNbEditCell.clearViewData(personLap);
                cellTable.redraw();
            }
        });
        cellTable.addColumn(raceNbColumn, "Rajtszám");

        final EditTextCell timeEditTextCell = new EditTextCell();
        final Column<PersonLap, String> timeColumn = new Column<PersonLap, String>(timeEditTextCell) {
            @Override
            public String getValue(PersonLap personLap) {
                return Utils.getElapsedTimeString(personLap.getTime());
            }
        };
        timeColumn.setFieldUpdater(new FieldUpdater<PersonLap, String>() {
            @Override
            public void update(int index, final PersonLap personLap, String timeString) {
                try {
                    String idoFormatumUzenet = "Az időt ilyen formátumba írhatod be: 12:32 vagy 1:12:32";
                    // parsing lapTime
                    String[] stringElements = timeString.split(":");
                    if (stringElements.length != 2 && stringElements.length != 3) {
                        throw new Exception(idoFormatumUzenet);
                    }
                    long[] longElements = new long[stringElements.length];
                    for (int i = 0; i < stringElements.length; i++) {
                        try {
                            longElements[i] = Long.parseLong(stringElements[i]);
                            if (longElements[i] < 0 || longElements[i] >= 60) {
                                throw new Exception(idoFormatumUzenet);
                            }
                        } catch (NumberFormatException e) {
                            throw new Exception(idoFormatumUzenet);
                        }
                    }

                    final long lapTime;
                    if (longElements.length == 3) {
                        lapTime = (longElements[0] * 60 * 60 + longElements[1] * 60 + longElements[2]) * 1000;
                    } else {
                        lapTime = (longElements[0] * 60 + longElements[1]) * 1000;
                    }

                    marathonService.updateLapTime(personLap.getId(), lapTime, new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable throwable) {
                            SCMTMarathon.commonFailureHandling(throwable);
                            undoChange(personLap);
                        }

                        @Override
                        public void onSuccess(Void aVoid) {
                            personLap.setTime(lapTime);
                        }
                    });
                } catch (Exception e) {
                    Window.alert(e.getMessage());
                    undoChange(personLap);
                }
            }

            private void undoChange(PersonLap personLap) {
                timeEditTextCell.clearViewData(personLap);
                cellTable.redraw();
            }
        });
        cellTable.addColumn(timeColumn, "Idő");
        cellTable.addColumn(new Column<PersonLap, PersonLap>(new ActionCell<PersonLap>("Törlés", new ActionCell.Delegate<PersonLap>() {
            @Override
            public void execute(final PersonLap personLap) {
                boolean confirm = Window.confirm("Biztos törölni akarod a kiválasztott kört?");
                if (confirm) {
                    marathonService.removePersonLap(personLap.getId(), new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable throwable) {
                        }

                        @Override
                        public void onSuccess(Void aVoid) {
                            personLapListDataProvider.getList().remove(personLap);
                        }
                    });
                }
            }
        })) {
            @Override
            public PersonLap getValue(PersonLap personLap) {
                return personLap;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        cellTable.setPageSize(Integer.MAX_VALUE);
        personLapListDataProvider.addDataDisplay(cellTable);
    }

    public void showDialog() {
        marathonService.getAllPersonLapList(scmtMarathon.getVerseny().getId(), new AsyncCallback<List<PersonLap>>() {
            @Override
            public void onFailure(Throwable throwable) {
                SCMTMarathon.commonFailureHandling(throwable);
            }

            @Override
            public void onSuccess(List<PersonLap> personLaps) {
                Collections.reverse(personLaps);
                allPersonLapList = personLaps;
                szuresText.setText(null);
                personLapListDataProvider.setList(personLaps);

                // building the map
                filterPersonLapMap = new HashMap<String, List<PersonLap>>(300);
                for (PersonLap personLap : personLaps) {
                    putPersonLapToFilterMap(personLap);
                }
            }
        });
        center();
    }

    private void putPersonLapToFilterMap(PersonLap personLap) {
        for (int i = 1; i <= personLap.getRaceNumber().length(); i++) {
            String key = personLap.getRaceNumber().substring(0, i);
            List<PersonLap> filterPersonLapList = filterPersonLapMap.get(key);
            if (filterPersonLapList == null) {
                filterPersonLapList = new ArrayList<PersonLap>();
                filterPersonLapMap.put(key, filterPersonLapList);
            }
            filterPersonLapList.add(personLap);
        }
    }

    private void removePersonLapFromFilterMap(PersonLap personLap) {
        for (int i = 1; i <= personLap.getRaceNumber().length(); i++) {
            String key = personLap.getRaceNumber().substring(0, i);
            List<PersonLap> filterPersonLapList = filterPersonLapMap.get(key);
            if (filterPersonLapList != null) {
                filterPersonLapList.remove(personLap);
            }
        }
    }
}
