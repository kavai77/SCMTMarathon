package net.himadri.scmt.client;

import com.google.gwt.appengine.channel.client.Channel;
import com.google.gwt.appengine.channel.client.ChannelFactory;
import com.google.gwt.appengine.channel.client.SocketError;
import com.google.gwt.appengine.channel.client.SocketListener;
import com.google.gwt.core.client.GWT;
import com.google.gwt.storage.client.Storage;
import net.himadri.scmt.client.callback.CommonAsyncCallback;
import net.himadri.scmt.client.entity.*;
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
        LOGGER.info("establishChannelConnection");
        marathonService.createChannelToken(new CommonAsyncCallback<String>() {
            @Override
            public void onSuccess(String token) {
                LOGGER.info("new token: " + token);
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
                        LOGGER.info("onMessage");
                        makeRequest();
                    }

                    @Override
                    public void onError(SocketError socketError) {
                        LOGGER.warning("Socket error" + socketError.getDescription());
                        establishChannelConnection();
                    }

                    @Override
                    public void onClose() {
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
        marathonService.getPollingResult(scmtMarathon.getVerseny().getId(), pollingRequest, new CommonAsyncCallback<PollingResult>() {
            @Override
            public void onSuccess(PollingResult pollingResult) {
                if (raceStatusSync.raceStatus != pollingResult.getRaceStatus()) {
                    raceStatusSync.raceStatus = pollingResult.getRaceStatus();
                    raceStatusSync.notifyRefreshed(Collections.singletonList(pollingResult.getRaceStatus()));
                }
                notifyOnResult(pollingResult.getTav(), tavSync, "tav");
                notifyOnResult(pollingResult.getVersenySzam(), versenySzamSync, "versenySzam");
                notifyOnResult(pollingResult.getVersenyzo(), versenyzoSync, "versenyzo");
                notifyOnResult(pollingResult.getPersonLap(), personLapSync, "personLap");
            }
        });
    }

    private <T> void notifyOnResult(PollingResult.Entity<T> pollingResultEntity, SyncSupport<T> syncSupport, String type) {
        if (pollingResultEntity != null) {
            LOGGER.info("notifyOnResult: " + type);
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
