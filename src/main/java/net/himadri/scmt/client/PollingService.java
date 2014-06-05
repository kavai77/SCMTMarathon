package net.himadri.scmt.client;

import com.google.gwt.appengine.channel.client.Channel;
import com.google.gwt.appengine.channel.client.ChannelFactory;
import com.google.gwt.appengine.channel.client.SocketError;
import com.google.gwt.appengine.channel.client.SocketListener;
import com.google.gwt.core.client.GWT;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.rpc.AsyncCallback;
import net.himadri.scmt.client.entity.PersonLap;
import net.himadri.scmt.client.entity.RaceStatus;
import net.himadri.scmt.client.entity.Tav;
import net.himadri.scmt.client.entity.VersenySzam;
import net.himadri.scmt.client.entity.Versenyzo;
import net.himadri.scmt.client.serializable.PollingRequest;
import net.himadri.scmt.client.serializable.PollingResult;

import java.util.Collections;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.06. 15:58
 */
public class PollingService {
    public static final String CHANNEL_TOKEN_KEY = "channelToken";
    public static final String CHANNEL_TOKEN_CREATION_KEY = "channelTokenCreation";
    public static final Logger LOGGER = Logger.getLogger(PollingService.class.getName());
    private MarathonServiceAsync marathonService = GWT.create(MarathonService.class);
    private static Storage localStorage = Storage.getLocalStorageIfSupported();

    public static class RaceStatusSyncSupport extends SyncSupport<RaceStatus> {
        private RaceStatus raceStatus;
    }

    private SyncSupport<PersonLap> personLapSync = new SyncSupport<PersonLap>();
    private SyncSupport<VersenySzam> versenySzamSync = new SyncSupport<VersenySzam>();
    private SyncSupport<Tav> tavSync = new SyncSupport<Tav>();
    private SyncSupport<Versenyzo> versenyzoSync = new SyncSupport<Versenyzo>();
    private RaceStatusSyncSupport raceStatusSync = new RaceStatusSyncSupport();

    private SCMTMarathon scmtMarathon;

    public PollingService(SCMTMarathon scmtMarathon) {
        this.scmtMarathon = scmtMarathon;
    }

    public void establishChannelConnection() {
        String channelToken = localStorage.getItem(CHANNEL_TOKEN_KEY);
        if (channelToken != null) {
            String channelTokenCreationStr = localStorage.getItem(CHANNEL_TOKEN_CREATION_KEY);
            long channelTokenCreation = channelTokenCreationStr != null ?
                    Long.parseLong(channelTokenCreationStr) : 0;
            if (System.currentTimeMillis() - channelTokenCreation > 60 * 60 * 1000) {
                requestNewKey();
            } else {
                connectToChannel(channelToken);
            }
        } else {
            requestNewKey();
        }
    }

    private void requestNewKey() {
        marathonService.createChannelToken(new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable throwable) {
                SCMTMarathon.commonFailureHandling(throwable);
            }

            @Override
            public void onSuccess(String token) {
                localStorage.setItem(CHANNEL_TOKEN_KEY, token);
                localStorage.setItem(CHANNEL_TOKEN_CREATION_KEY, Long.toString(System.currentTimeMillis()));
                connectToChannel(token);
            }
        });
    }


    private void connectToChannel(String token) {
        ChannelFactory.createChannel(token, new ChannelFactory.ChannelCreatedCallback() {
            @Override
            public void onChannelCreated(Channel channel) {
                channel.open(new SocketListener() {
                    @Override
                    public void onOpen() {
                        LOGGER.info("Socket open");
                        makeRequest();
                    }

                    @Override
                    public void onMessage(String s) {
                        makeRequest();
                    }

                    @Override
                    public void onError(SocketError socketError) {
                        localStorage.removeItem(CHANNEL_TOKEN_KEY);
                        LOGGER.warning("Socket error" + socketError.getDescription());
                        establishChannelConnection();
                    }

                    @Override
                    public void onClose() {
                        localStorage.removeItem(CHANNEL_TOKEN_KEY);
                        LOGGER.info("Socket close");
                        establishChannelConnection();
                    }
                });
            }
        });
    }

    public void makeRequest() {
        PollingRequest pollingRequest = new PollingRequest(
                new PollingRequest.Entity(scmtMarathon.getRaceStatusRowCache().getMaxTime(), personLapSync.getSyncId()),
                new PollingRequest.Entity(scmtMarathon.getVersenyszamMapCache().getMaxTime(), versenySzamSync.getSyncId()),
                new PollingRequest.Entity(scmtMarathon.getTavMapCache().getMaxTime(), tavSync.getSyncId()),
                new PollingRequest.Entity(scmtMarathon.getVersenyzoMapCache().getMaxTime(), versenyzoSync.getSyncId()));
        marathonService.getPollingResult(scmtMarathon.getVerseny().getId(), pollingRequest, new AsyncCallback<PollingResult>() {
            @Override
            public void onFailure(Throwable throwable) {
                SCMTMarathon.commonFailureHandling(throwable);
            }

            @Override
            public void onSuccess(PollingResult pollingResult) {
                if (raceStatusSync.raceStatus != pollingResult.getRaceStatus()) {
                    raceStatusSync.raceStatus = pollingResult.getRaceStatus();
                    raceStatusSync.notifyRefreshed(Collections.singletonList(pollingResult.getRaceStatus()));
                }
                notifyOnResult(pollingResult.getTav(), tavSync);
                notifyOnResult(pollingResult.getVersenySzam(), versenySzamSync);
                notifyOnResult(pollingResult.getVersenyzo(), versenyzoSync);
                notifyOnResult(pollingResult.getPersonLap(), personLapSync);
            }
        });
    }

    private <T> void notifyOnResult(PollingResult.Entity<T> pollingResultEntity, SyncSupport<T> syncSupport) {
        if (pollingResultEntity != null) {
            syncSupport.setSyncId(pollingResultEntity.getSyncId());
            if (pollingResultEntity.isFullSync()) {
                syncSupport.notifyRefreshed(pollingResultEntity.getItems());
            } else if (!pollingResultEntity.getItems().isEmpty()) {
                syncSupport.notifyItemsAdded(pollingResultEntity.getItems());
            }
        }
    }

    public SyncSupport<PersonLap> getPersonLapSync() {
        return personLapSync;
    }

    public SyncSupport<VersenySzam> getVersenySzamSync() {
        return versenySzamSync;
    }

    public SyncSupport<Tav> getTavSync() {
        return tavSync;
    }

    public SyncSupport<Versenyzo> getVersenyzoSync() {
        return versenyzoSync;
    }

    public SyncSupport<RaceStatus> getRaceStatusSync() {
        return raceStatusSync;
    }
}
