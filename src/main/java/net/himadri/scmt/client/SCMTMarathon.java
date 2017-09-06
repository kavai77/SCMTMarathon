package net.himadri.scmt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import net.himadri.scmt.client.callback.CommonAsyncCallback;
import net.himadri.scmt.client.entity.Verseny;
import net.himadri.scmt.client.exception.NotAuthorizedException;
import net.himadri.scmt.client.panel.MainRootPanel;
import net.himadri.scmt.client.panel.PrintPreRegistrationRootPanel;
import net.himadri.scmt.client.panel.PrintResultRootPanel;
import net.himadri.scmt.client.panel.PrintStartListRootPanel;
import net.himadri.scmt.client.serializable.MarathonActionListener;
import net.himadri.scmt.client.token.TavVersenySzamToken;
import net.himadri.scmt.client.token.VersenyToken;

import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unchecked"})
public class SCMTMarathon implements EntryPoint {
    private PollingService pollingService = new PollingService(this);
    private SyncSupport<Verseny> versenySyncSupport = new SyncSupport<Verseny>();

    private VersenyzoMapCache versenyzoMapCache = new VersenyzoMapCache(this);
    private VersenyszamMapCache versenyszamMapCache = new VersenyszamMapCache(this);
    private TavMapCache tavMapCache = new TavMapCache(this);
    private RaceStatusRowCache raceStatusRowCache = new RaceStatusRowCache(this);

    private final MarathonServiceAsync marathonService = GWT.create(MarathonService.class);

    private Verseny verseny;
    private MainRootPanel mainRootPanel;

    public void onModuleLoad() {
        final RootPanel rootPanel = RootPanel.get();
        mainRootPanel = new MainRootPanel(this);
        final PrintResultRootPanel printResultRootPanel = new PrintResultRootPanel(this);
        final PrintStartListRootPanel printStartListRootPanel = new PrintStartListRootPanel(this);
        final PrintPreRegistrationRootPanel printPreRegistrationRootPanel = new PrintPreRegistrationRootPanel(this);

        History.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> stringValueChangeEvent) {
                rootPanel.clear();
                String historyToken = stringValueChangeEvent.getValue();
                if (TavVersenySzamToken.isHistoryMatches(historyToken)) {
                    TavVersenySzam tavVersenySzam = TavVersenySzamToken.decode(historyToken);
                    printResultRootPanel.showResultPanel(tavVersenySzam);
                    rootPanel.add(printResultRootPanel);
                } else if (PrintStartListRootPanel.HISTORY_TOKEN.equals(historyToken)) {
                    printStartListRootPanel.showAllRunnersPanel();
                    rootPanel.add(printStartListRootPanel);
                } else if (PrintPreRegistrationRootPanel.HISTORY_TOKEN.equals(historyToken)) {
                    printPreRegistrationRootPanel.showAllRunnersPanel();
                    rootPanel.add(printPreRegistrationRootPanel);
                } else if (VersenyToken.isHistoryMatches(historyToken)) {
                    rootPanel.add(mainRootPanel);
                    Long versenyId = VersenyToken.decode(historyToken);
                    marathonService.getVerseny(versenyId, new CommonAsyncCallback<Verseny>() {
                        @Override
                        public void onSuccess(Verseny verseny) {
                            versenySyncSupport.notifyRefreshed(Collections.singletonList(verseny));
                        }
                    });
                } else {
                    rootPanel.add(mainRootPanel);
                    mainRootPanel.showVersenyPanel();
                }
            }
        });

        versenySyncSupport.addMarathonActionListener(SyncSupport.Priority.HIGH, new MarathonActionListener<Verseny>() {
            @Override
            public void itemAdded(List<Verseny> items) {
                itemRefreshed(items);
            }

            @Override
            public void itemRefreshed(List<Verseny> items) {
                verseny = items.get(0);
                if (verseny.getRaceStatus().isActive()) {
                    pollingService.establishChannelConnection();
                } else {
                    pollingService.makeRequest();
                }
            }
        });

        History.fireCurrentHistoryState();
    }

    public VersenyzoMapCache getVersenyzoMapCache() {
        return versenyzoMapCache;
    }

    public VersenyszamMapCache getVersenyszamMapCache() {
        return versenyszamMapCache;
    }

    public TavMapCache getTavMapCache() {
        return tavMapCache;
    }

    public RaceStatusRowCache getRaceStatusRowCache() {
        return raceStatusRowCache;
    }

    public PollingService getPollingService() {
        return pollingService;
    }

    public SyncSupport<Verseny> getVersenySyncSupport() {
        return versenySyncSupport;
    }

    public Verseny getVerseny() {
        return verseny;
    }

    public MainRootPanel getMainRootPanel() {
        return mainRootPanel;
    }

    public static void commonFailureHandling(Throwable throwable) {
        commonFailureHandling(throwable, null);
    }

    public static void commonFailureHandling(Throwable throwable, String message) {
        if (throwable instanceof NotAuthorizedException) {
            Window.alert("Nincs jogosultságod erre a műveletre");
        } else {
            if (message == null) {
                Window.alert("Hiba történt: " + throwable.getMessage());
            } else {
                Window.alert(message);
            }
        }
        throwable.printStackTrace();
    }
}
