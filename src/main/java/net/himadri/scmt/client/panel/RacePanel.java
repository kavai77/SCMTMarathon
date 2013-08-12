package net.himadri.scmt.client.panel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import net.himadri.scmt.client.*;
import net.himadri.scmt.client.dialog.CorrectionDialogBox;
import net.himadri.scmt.client.entity.PersonLap;
import net.himadri.scmt.client.entity.RaceStatus;
import net.himadri.scmt.client.entity.Tav;
import net.himadri.scmt.client.entity.Versenyzo;
import net.himadri.scmt.client.exception.AlreadyExistingEntityException;
import net.himadri.scmt.client.serializable.MarathonActionListener;
import net.himadri.scmt.client.serializable.RaceStatusRow;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.08. 11:06
 */
public class RacePanel extends Composite {
    public static final int CLOCK_TIMER_PERIOD = 1000;
    public static final int MAX_LAST_PERSON_LAP = 5;

    private MarathonServiceAsync marathonService = GWT.create(MarathonService.class);
    private CellTable<RaceStatusRowWithLapNb> lastRaceNbList = new CellTable<RaceStatusRowWithLapNb>();
    private Label clockLabel = new Label();
    private DeckPanel raceDeckBar = new DeckPanel();
    private Label versenyzoSuggestionLabel = new Label();
    private TextBox raceNumberInputText = new TextBox();
    private Label raceNbSendFeedbackLabel = new Label();

    private LinkedList<RaceStatusRowWithLapNb> lastPersonLaps = new LinkedList<RaceStatusRowWithLapNb>();
    private long raceStartTime;

    private ClockTimer clockTimer = new ClockTimer();

    private CorrectionDialogBox correctionDialogBox;
    private SCMTMarathon scmtMarathon;

    public RacePanel(final SCMTMarathon scmtMarathon) {
        this.scmtMarathon = scmtMarathon;
        AbsolutePanel racePanel = new AbsolutePanel();
        racePanel.setSize("1001px", "620px");
        clockLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        clockLabel.addStyleName("bigger");

        racePanel.add(clockLabel, 10, 10);
        clockLabel.setSize("958px", "auto");

        lastRaceNbList.addColumn(new TextColumn<RaceStatusRowWithLapNb>() {
            @Override
            public String getValue(RaceStatusRowWithLapNb raceStatusRow) {
                return raceStatusRow.raceStatusRow.getRaceNumber();
            }
        }, "Rajtszám");
        lastRaceNbList.addColumn(new TextColumn<RaceStatusRowWithLapNb>() {
            @Override
            public String getValue(RaceStatusRowWithLapNb raceStatusRow) {
                return raceStatusRow.raceStatusRow.getVersenyzo() != null ?
                        raceStatusRow.raceStatusRow.getVersenyzo().getName() : null;
            }
        }, "Versenyző");
        lastRaceNbList.addColumn(new TextColumn<RaceStatusRowWithLapNb>() {
            @Override
            public String getValue(RaceStatusRowWithLapNb raceStatusRow) {
                return raceStatusRow.raceStatusRow.getVersenyzo() != null ?
                        raceStatusRow.raceStatusRow.getVersenyzo().getEgyesulet() : null;
            }
        }, "Egyesület");
        lastRaceNbList.addColumn(new TextColumn<RaceStatusRowWithLapNb>() {
            @Override
            public String getValue(RaceStatusRowWithLapNb raceStatusRow) {
                return raceStatusRow.raceStatusRow.getVersenySzam() != null ?
                        Utils.getVersenySzamMegnevezes(scmtMarathon, raceStatusRow.raceStatusRow.getVersenySzam()) : null;
            }
        }, "Versenyszám");
        lastRaceNbList.addColumn(new TextColumn<RaceStatusRowWithLapNb>() {
            @Override
            public String getValue(RaceStatusRowWithLapNb raceStatusRow) {
                return Integer.toString(raceStatusRow.lapNb);
            }
        }, "Körök");
        lastRaceNbList.addColumn(new TextColumn<RaceStatusRowWithLapNb>() {
            @Override
            public String getValue(RaceStatusRowWithLapNb raceStatusRow) {
                return Utils.getElapsedTimeString(raceStatusRow.raceStatusRow.getLapTimes().get(raceStatusRow.lapNb - 1));
            }
        }, "Idő");
        lastRaceNbList.addColumn(new TextColumn<RaceStatusRowWithLapNb>() {
            @Override
            public String getValue(RaceStatusRowWithLapNb raceStatusRow) {
                return raceStatusRow.raceStatusRow.getTav() != null && raceStatusRow.raceStatusRow.getTav().getKorSzam() <= raceStatusRow.lapNb ? "BEFUTÓ!!!" : null;
            }
        });
        lastRaceNbList.setRowData(lastPersonLaps);

        racePanel.add(raceDeckBar, 10, 61);
        raceDeckBar.setSize("980px", "520px");

        AbsolutePanel raceInProgressBar = new AbsolutePanel();
        raceDeckBar.add(raceInProgressBar);

        raceInProgressBar.add(raceNbSendFeedbackLabel, 180, 44);
        raceNbSendFeedbackLabel.setSize("574px", "18px");

        raceNumberInputText.addKeyUpHandler(new RajtszamSuggestionHandler());
        raceNumberInputText.addKeyDownHandler(new RajtszamEnterHandler());
        raceInProgressBar.add(raceNumberInputText, 0, 32);
        raceNumberInputText.setFocus(true);

        versenyzoSuggestionLabel.addStyleName("suggestion");
        versenyzoSuggestionLabel.setVisible(false);
        raceInProgressBar.add(versenyzoSuggestionLabel, 15, 67);

        Label lblRajtszm = new Label("Rajtszám");
        raceInProgressBar.add(lblRajtszm, 0, 10);

        Button btnRajtszmJavtsa = new ImageButton("pencil.png", "Hibás felvitel javítása", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (correctionDialogBox == null) {
                    correctionDialogBox = new CorrectionDialogBox(scmtMarathon);
                }
                correctionDialogBox.showDialog();
            }
        });
        raceInProgressBar.add(btnRajtszmJavtsa, 0, 103);

        Button btnStopRace = new ImageButton("player_stop.png", "Verseny leállítása",
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        checkAuthorization();
                        marathonService.stopRace(scmtMarathon.getVerseny().getId(),
                                new EmptyFailureHandlingAsyncCallback<Void>());
                    }
                });
        raceInProgressBar.add(btnStopRace, 180, 103);

        Button btnShiftRaceStartTime = new ImageButton("clock.png", "Versenyidő állítása",
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        checkAuthorization();
                        String miliSecStr = Window.prompt("Add meg a csúsztatás idejét ezredmásodpercben", null);
                        try {
                            if (miliSecStr != null) {
                                long miliSec = Long.parseLong(miliSecStr);
                                marathonService.shiftRaceTime(scmtMarathon.getVerseny().getId(), miliSec, new AsyncCallback<Void>() {
                                    @Override
                                    public void onFailure(Throwable throwable) {
                                        SCMTMarathon.commonFailureHandling(throwable);
                                    }

                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Window.alert("A versenyidő sikeresen megváltozott. Újra kell frissíteni az alkalmazást az összes kliensen!");
                                    }
                                });
                            }
                        } catch (NumberFormatException e) {
                            Window.alert("A megadott forma nem megfelelő.");
                        }
                    }
                });
        raceInProgressBar.add(btnShiftRaceStartTime, 340, 103);

        raceInProgressBar.add(new Label("Utoljára rögzített rajtszámok"),  0, 150);
        raceInProgressBar.add(lastRaceNbList,  0, 180);

        AbsolutePanel raceFinishedBar = new AbsolutePanel();
        raceDeckBar.add(raceFinishedBar);

        Button btnVersenyRestart = new ImageButton("player_play.png", "Verseny újraindítása", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                marathonService.restartRace(scmtMarathon.getVerseny().getId(), new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        SCMTMarathon.commonFailureHandling(throwable);
                    }

                    @Override
                    public void onSuccess(Void aVoid) {
                        scmtMarathon.getPollingService().establishChannelConnection();
                    }
                });
            }
        });
        btnVersenyRestart.setTitle("Verseny újraindtása időkiesés nélkül (mintha nem lett volna megállítva)");
        raceFinishedBar.add(btnVersenyRestart, 0, 103);

        scmtMarathon.getPollingService().getPersonLapSync().addMarathonActionListener(new LastFivePersonLapActionListener());
        scmtMarathon.getPollingService().getRaceStatusSync().addMarathonActionListener(new RaceStatusActionListener());

        initWidget(racePanel);
    }

    private void checkAuthorization() {
        String password = Window.prompt("Jelszó megadása szükséges:", null);
        if (!"scmt".equals(password)) {
            throw new RuntimeException("Permission denied");
        }
    }

    private class LastFivePersonLapActionListener implements MarathonActionListener<PersonLap> {
        @Override
        public void itemAdded(List<PersonLap> items) {
            for (PersonLap personLap : items) {
                RaceStatusRow raceStatusRow = scmtMarathon.getRaceStatusRowCache().getRaceStatusRowByRaceNumber(personLap.getRaceNumber());
                lastPersonLaps.addFirst(new RaceStatusRowWithLapNb(raceStatusRow, raceStatusRow.getLapTimes().size()));
                if (lastPersonLaps.size() > MAX_LAST_PERSON_LAP) {
                    lastPersonLaps.removeLast();
                }
            }
            lastRaceNbList.setRowData(lastPersonLaps);
            lastRaceNbList.redraw();
        }

        @Override
        public void itemRefreshed(List<PersonLap> items) {
            lastPersonLaps.clear();
            for (int i = items.size() - 1, minIndex = Math.max(0, items.size() - MAX_LAST_PERSON_LAP); i >= minIndex; i--) {
                RaceStatusRow raceStatusRow = scmtMarathon.getRaceStatusRowCache().getRaceStatusRowByRaceNumber(items.get(i).getRaceNumber());
                int lastIndex = lastPersonLaps.lastIndexOf(new RaceStatusRowWithLapNb(raceStatusRow, 0));
                lastPersonLaps.addLast(new RaceStatusRowWithLapNb(raceStatusRow, lastIndex == -1
                        ? raceStatusRow.getLapTimes().size()
                        : lastPersonLaps.get(lastIndex).lapNb - 1));
            }
            lastRaceNbList.setRowData(lastPersonLaps);
            lastRaceNbList.redraw();
        }
    }

    private class ClockTimer extends Timer {
        @Override
        public void run() {
            long elapsedTime = System.currentTimeMillis() - raceStartTime;
            long seconds = elapsedTime / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            clockLabel.setText("Versenyidő: " + hours + " : " +
                    formatTwoDigitDecimal((int) (minutes % 60)) + " : " +
                    formatTwoDigitDecimal((int) (seconds % 60)));
        }
    }

    private class RaceStatusActionListener implements MarathonActionListener<RaceStatus> {
        @Override
        public void itemAdded(List<RaceStatus> items) {
            itemRefreshed(items);
        }

        @Override
        public void itemRefreshed(List<RaceStatus> items) {
            switch (items.get(0)) {
                case RACING:
                    marathonService.getRaceTime(scmtMarathon.getVerseny().getId(), new AsyncCallback<Long>() {
                        @Override
                        public void onFailure(Throwable throwable) {
                            SCMTMarathon.commonFailureHandling(throwable);
                        }

                        @Override
                        public void onSuccess(Long raceTime) {
                            raceStartTime = System.currentTimeMillis() - raceTime;
                            clockTimer.scheduleRepeating(CLOCK_TIMER_PERIOD);
                        }
                    });
                    raceDeckBar.showWidget(0);
                    break;
                case FINISHED:
                    raceDeckBar.showWidget(1);
                    clockTimer.cancel();
                    clockLabel.setText("A verseny véget ért.");
                    break;
            }
        }
    }

    private class RajtszamSuggestionHandler implements KeyUpHandler {
        @Override
        public void onKeyUp(KeyUpEvent event) {
            new Timer() {
                @Override
                public void run() {
                    String raceNumber = raceNumberInputText.getText().trim();
                    Versenyzo versenyzo = scmtMarathon.getVersenyzoMapCache().getVersenyzo(raceNumber);
                    if (versenyzo != null) {
                        final RaceStatusRow raceStatusRow = scmtMarathon.getRaceStatusRowCache().getRaceStatusRowByRaceNumber(raceNumber);
                        int korszam = raceStatusRow == null ? 1 : raceStatusRow.getLapTimes().size() + 1;
                        boolean befuto = false;
                        boolean befejezte = false;
                        if (raceStatusRow != null) {
                            Tav tav = scmtMarathon.getTavMapCache().getTav(raceStatusRow.getVersenySzam().getTavId());
                            befuto = tav.getKorSzam() != null && tav.getKorSzam() == korszam;
                            befejezte = tav.getKorSzam() != null && tav.getKorSzam() < korszam;
                        }
                        if (!befejezte) {
                            String label = versenyzo.getName() + " " +
                                    (!versenyzo.getEgyesulet().isEmpty() ? versenyzo.getEgyesulet() + " versenyzője " : "")
                                    + korszam + ". körét fejezi. ";
                            if (befuto) label += "BEFUTÓ!!! ";
                            if (versenyzo.isFeladta()) label += "VIGYÁZAT!!! Ez a versenyző már feladta a versenyt, " +
                                    "ha rögzíted a rajtszámát, ismét versenybe kerül!!!";
                            versenyzoSuggestionLabel.setText(label);
                        } else {
                            versenyzoSuggestionLabel.setText("VIGYÁZAT!!!" + versenyzo.getName() +
                                    " már befejezte a versenyét. Módosítsd a versenyszámát, ha szükséges!");
                        }
                        versenyzoSuggestionLabel.setVisible(true);
                    } else {
                        versenyzoSuggestionLabel.setVisible(false);
                    }
                }
            }.schedule(100);
        }
    }

    private class RajtszamEnterHandler implements KeyDownHandler {
        @Override
        public void onKeyDown(KeyDownEvent keyPressEvent) {
            final String raceNb = raceNumberInputText.getText().trim();
            if (keyPressEvent.getNativeKeyCode() == KeyCodes.KEY_ENTER && !raceNb.isEmpty()) {
                raceNumberInputText.setText(null);
                versenyzoSuggestionLabel.setVisible(false);
                marathonService.addPersonLap(scmtMarathon.getVerseny().getId(), raceNb, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        if (throwable instanceof AlreadyExistingEntityException) {
                            raceNbSendFeedbackLabel.setText(raceNb + " versenyző körét más már rögzítette 1 percen belül. Nem rögzítettük újra.");
                        } else {
                            SCMTMarathon.commonFailureHandling(throwable);
                        }
                    }

                    @Override
                    public void onSuccess(Void aVoid) {
                        String label = raceNb + " versenyző körét mentettük. ";
                        Versenyzo versenyzo = scmtMarathon.getVersenyzoMapCache().getVersenyzo(raceNb);
                        if (versenyzo != null && versenyzo.isFeladta()) {
                            label += "A versenyző korábban feladta a versenyt, most ismét versenybe került.";
                            marathonService.versenyzoFeladta(versenyzo.getId(), false,
                                    new EmptyFailureHandlingAsyncCallback<Void>());
                        }
                        raceNbSendFeedbackLabel.setText(label);
                    }
                });
            }
        }
    }

    private String formatTwoDigitDecimal(int decimal) {
        return (decimal < 10 ? "0" : "") + decimal;
    }

    private class RaceStatusRowWithLapNb {
        private RaceStatusRow raceStatusRow;
        private int lapNb;

        private RaceStatusRowWithLapNb(RaceStatusRow raceStatusRow, int lapNb) {
            this.raceStatusRow = raceStatusRow;
            this.lapNb = lapNb;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RaceStatusRowWithLapNb that = (RaceStatusRowWithLapNb) o;

            if (raceStatusRow != null ? !raceStatusRow.equals(that.raceStatusRow) : that.raceStatusRow != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            return raceStatusRow != null ? raceStatusRow.hashCode() : 0;
        }
    }
}
