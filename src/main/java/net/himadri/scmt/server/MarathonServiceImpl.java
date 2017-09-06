package net.himadri.scmt.server;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Query;
import net.himadri.scmt.client.MarathonService;
import net.himadri.scmt.client.entity.*;
import net.himadri.scmt.client.exception.AlreadyExistingEntityException;
import net.himadri.scmt.client.exception.NotAuthorizedException;
import net.himadri.scmt.client.exception.NotExistingEntityException;
import net.himadri.scmt.client.serializable.PollingRequest;
import net.himadri.scmt.client.serializable.PollingResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class MarathonServiceImpl extends RemoteServiceServlet implements
        MarathonService {

    private static final Expiration DEFAULT_CACHE_EXPIRATION = Expiration.byDeltaSeconds(60 * 60);
    private static final long CHANNEL_EXPIRATION = 24 * 60 * 60 * 1000;

    private enum SyncValueType {PERSON_LAP, VERSENYZO, VERSENYSZAM, TAV}

    private static final long RACE_TIME_THRESHOLD = 60000;
    private static Objectify ofy = ObjectifyUtils.beginObjectify();
    private static MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService();
    private static MemcacheService configMemcacheService = MemcacheServiceFactory.getMemcacheService("config");

    @Override
    public void startRace(Long versenyId) {
        Verseny verseny = getVersenyFromCache(versenyId);
        verseny.setRaceStartTime(System.currentTimeMillis());
        verseny.setRaceStatus(RaceStatus.RACING);
        updateVerseny(verseny);
        broadcastModification();
    }

    @Override
    public void shiftRaceTime(Long versenyId, long offsetInMillis) {
        Verseny verseny = getVersenyFromCache(versenyId);
        if (verseny.getRaceStatus() == RaceStatus.RACING) {
            verseny.setRaceStartTime(verseny.getRaceStartTime() - offsetInMillis);
            updateVerseny(verseny);
            broadcastModification();
        }
    }

    @Override
    public long getRaceTime(Long versenyId) {
        Verseny verseny = getVersenyFromCache(versenyId);
        return verseny.getRaceStartTime() == null ? 0 : System.currentTimeMillis() - verseny.getRaceStartTime();
    }

    @Override
    public void stopRace(Long versenyId) {
        Verseny verseny = getVersenyFromCache(versenyId);
        verseny.setRaceStatus(RaceStatus.FINISHED);
        verseny.setVersenyzoSzam(getVersenyzoSzam(versenyId));
        updateVerseny(verseny);
        broadcastModification();
    }

    @Override
    public void restartRace(Long versenyId) {
        Verseny verseny = getVersenyFromCache(versenyId);
        verseny.setRaceStatus(RaceStatus.RACING);
        updateVerseny(verseny);
        broadcastModification();
    }

    @Override
    public void setNevezesDatum(Long versenyId, Long nevezesBegin, Long nevezesEnd, String emailSubject, String emailText,
                                Integer helysziniNevezesOsszeg) {
        Verseny verseny = getVersenyFromCache(versenyId);
        verseny.setNevezesBegin(nevezesBegin);
        verseny.setNevezesEnd(nevezesEnd);
        verseny.setNevezesEmailSubject(emailSubject);
        verseny.setNevezesEmailText(emailText);
        verseny.setHelysziniNevezesOsszeg(helysziniNevezesOsszeg);
        updateVerseny(verseny);
        broadcastModification();
    }

    @Override
    public void deleteRace(Long versenyId) {
        if (!isSuperUserAuthorized()) throw new NotAuthorizedException();
        ofy.delete(ofy.query(PersonLap.class).filter("versenyId", versenyId));
        ofy.delete(ofy.query(Versenyzo.class).filter("versenyId", versenyId));
        ofy.delete(ofy.query(VersenySzam.class).filter("versenyId", versenyId));
        ofy.delete(ofy.query(Tav.class).filter("versenyId", versenyId));
        ofy.delete(Verseny.class, versenyId);
        memcacheService.delete(getVersenyEntityCacheKey(versenyId));
    }

    @Override
    public void addVersenySzam(Long versenyId, Long tavId, Boolean ferfi, Integer korTol, Integer korIg) {
        VersenySzam ujVersenySzam = new VersenySzam(versenyId, tavId, ferfi, korTol, korIg);
        ofy.put(ujVersenySzam);
        memcacheService.put(getMaxCreationTimeCacheKey(VersenySzam.class, versenyId),
                ujVersenySzam.getCreationTime(), DEFAULT_CACHE_EXPIRATION);
        broadcastModification();
    }

    @Override
    public void modifyVersenySzam(Long versenySzamId, Long tavId, Boolean ferfi, Integer korTol, Integer korIg) {
        VersenySzam versenySzam = ofy.get(VersenySzam.class, versenySzamId);
        versenySzam.setTavId(tavId);
        versenySzam.setFerfi(ferfi);
        versenySzam.setKorTol(korTol);
        versenySzam.setKorIg(korIg);
        ofy.put(versenySzam);
        incrementSyncValue(versenySzam.getVersenyId(), SyncValueType.VERSENYSZAM);
        broadcastModification();
    }

    @Override
    public void removeVersenySzam(Long id) {
        VersenySzam versenySzam = ofy.get(VersenySzam.class, id);
        ofy.delete(VersenySzam.class, id);
        incrementSyncValue(versenySzam.getVersenyId(), SyncValueType.VERSENYSZAM);
        broadcastModification();
    }

    @Override
    public void addTav(Long versenyId, String megnevezes, Integer korszam, Integer versenySzamtol, Integer versenySzamig, long raceStartDiff, String[] korNevArray) {
        Tav tav = new Tav(versenyId, megnevezes, korszam, versenySzamtol, versenySzamig, raceStartDiff, korNevArray);
        ofy.put(tav);
        memcacheService.put(getMaxCreationTimeCacheKey(Tav.class, versenyId),
                tav.getCreationTime(), DEFAULT_CACHE_EXPIRATION);
        broadcastModification();
    }

    @Override
    public Long futamStart(long tavId) {
        Tav tav = ofy.get(Tav.class, tavId);
        Verseny verseny = getVersenyFromCache(tav.getVersenyId());
        tav.setRaceStartDiff(System.currentTimeMillis() - verseny.getRaceStartTime());
        ofy.put(tav);
        incrementSyncValue(tav.getVersenyId(), SyncValueType.TAV);
        broadcastModification();
        return tav.getRaceStartDiff();
    }

    @Override
    public void modifyTav(Long tavId, String megnevezes, Integer korszam, Integer versenySzamtol, Integer versenySzamig, long raceStartDiff, String[] korNevArray) {
        Tav tav = ofy.get(Tav.class, tavId);
        tav.setMegnevezes(megnevezes);
        tav.setKorSzam(korszam);
        tav.setVersenySzamtol(versenySzamtol);
        tav.setVersenySzamig(versenySzamig);
        tav.setVersenySzamig(versenySzamig);
        tav.setRaceStartDiff(raceStartDiff);
        tav.setKorNevArray(korNevArray);
        ofy.put(tav);
        incrementSyncValue(tav.getVersenyId(), SyncValueType.TAV);
        broadcastModification();
    }

    @Override
    public void removeTav(Long tavId) {
        Tav tav = ofy.get(Tav.class, tavId);
        ofy.delete(Tav.class, tavId);
        incrementSyncValue(tav.getVersenyId(), SyncValueType.TAV);
        broadcastModification();
    }

    @Override
    public void addVersenyzo(String raceNumber, String name, Boolean ferfi, Integer szuletesiEv, String egyesulet,
                             String email, Long versenySzamId, Long versenyId, String poloMeret, String licenszSzam,
                             Integer fizetettDij) throws AlreadyExistingEntityException {
        int count = ofy.query(Versenyzo.class)
                .filter("versenyId", versenyId)
                .filter("raceNumber", raceNumber).count();
        if (count > 0) {
            throw new AlreadyExistingEntityException();
        }
        Versenyzo versenyzo = new Versenyzo(raceNumber, name, ferfi, szuletesiEv, egyesulet, email, versenySzamId,
                versenyId, poloMeret, licenszSzam, fizetettDij);
        ofy.put(versenyzo);
        memcacheService.put(getMaxCreationTimeCacheKey(Versenyzo.class, versenyId),
                versenyzo.getCreationTime(), DEFAULT_CACHE_EXPIRATION);
        broadcastModification();
    }

    @Override
    public void modifyVersenyzo(Versenyzo versenyzo) throws AlreadyExistingEntityException {
        Versenyzo sameRaceNumber = ofy.query(Versenyzo.class)
                .filter("versenyId", versenyzo.getVersenyId())
                .filter("raceNumber", versenyzo.getRaceNumber()).get();
        if (sameRaceNumber != null && !sameRaceNumber.getId().equals(versenyzo.getId())) {
            throw new AlreadyExistingEntityException();
        }

        ofy.put(versenyzo);
        incrementSyncValue(versenyzo.getVersenyId(), SyncValueType.VERSENYZO);
        broadcastModification();
    }

    @Override
    public void removeVersenyzo(Long id) throws NotExistingEntityException {
        try {
            Versenyzo versenyzo = ofy.get(Versenyzo.class, id);
            ofy.delete(Versenyzo.class, id);
            incrementSyncValue(versenyzo.getVersenyId(), SyncValueType.VERSENYZO);
            broadcastModification();
        } catch (NotFoundException e) {
            throw new NotExistingEntityException();
        }
    }

    @Override
    public void versenyzoFeladta(Long id, boolean feladta) throws NotExistingEntityException {
        try {
            Versenyzo versenyzo = ofy.get(Versenyzo.class, id);
            versenyzo.setFeladta(feladta);
            ofy.put(versenyzo);
            incrementSyncValue(versenyzo.getVersenyId(), SyncValueType.VERSENYZO);
            broadcastModification();
        } catch (NotFoundException e) {
            throw new NotExistingEntityException();
        }
    }

    @Override
    public void versenyzoEredmenyEllenorzott(Long id, boolean ellenorzott) throws NotExistingEntityException {
        try {
            Versenyzo versenyzo = ofy.get(Versenyzo.class, id);
            versenyzo.setEllenorzott(ellenorzott);
            ofy.put(versenyzo);
            // az ellenőrzés csak adminisztrációs jellegű, és egy gépen zajlik, ezt nem broadcast-oljuk
        } catch (NotFoundException e) {
            throw new NotExistingEntityException();
        }
    }

    @Override
    public void addPersonLap(Long versenyId, String raceNumber, long raceTime, boolean withThresholdValidation) throws AlreadyExistingEntityException {
        if (withThresholdValidation) {
            PersonLap personLap = ofy.query(PersonLap.class)
                    .filter("versenyId", versenyId)
                    .filter("raceNumber", raceNumber)
                    .order("-time").get();
            if (personLap != null && raceTime - personLap.getTime() <= RACE_TIME_THRESHOLD) {
                throw new AlreadyExistingEntityException();
            }
        }
        PersonLap newPersonLap = new PersonLap(versenyId, raceNumber, raceTime);
        addPersonLap(newPersonLap);
        if (!withThresholdValidation) {
            incrementSyncValue(versenyId, SyncValueType.PERSON_LAP);
        }
        broadcastModification();
    }

    @Override
    public void removePersonLap(Long versenyId, Long personLapId) {
        try {
            PersonLap personLap = ofy.get(PersonLap.class, personLapId);
            removePersonLapFromCache(personLap);
            ofy.delete(PersonLap.class, personLapId);
        } catch(NotFoundException e) {
            memcacheService.delete(getPersonLapListKey(versenyId));
        } finally {
            incrementSyncValue(versenyId, SyncValueType.PERSON_LAP);
            broadcastModification();
        }

    }

    @Override
    public void updateRaceNumber(Long personLapId, String raceNumber) {
        PersonLap personLap = ofy.get(PersonLap.class, personLapId);
        personLap.setRaceNumber(raceNumber);
        addPersonLap(personLap);
        incrementSyncValue(personLap.getVersenyId(), SyncValueType.PERSON_LAP);
        broadcastModification();
    }

    @Override
    public void updateLapTime(Long personLapId, long lapTime) {
        PersonLap personLap = ofy.get(PersonLap.class, personLapId);
        removePersonLapFromCache(personLap);
        personLap.setTime(lapTime);
        addPersonLap(personLap);
        incrementSyncValue(personLap.getVersenyId(), SyncValueType.PERSON_LAP);
        broadcastModification();
    }

    @Override
    public PollingResult getPollingResult(Long versenyId, PollingRequest pollingRequest) {
        Verseny verseny = getVersenyFromCache(versenyId);
        PollingResult pollingResult = new PollingResult();
        pollingResult.setPersonLap(createPollingResultPersonLap(versenyId, pollingRequest.getPersonLap(), verseny.getPersonLapSyncValue()));
        pollingResult.setVersenySzam(createPollingResultEntity(VersenySzam.class, versenyId, pollingRequest.getVersenySzam(), verseny.getVersenySzamSyncValue()));
        pollingResult.setTav(createPollingResultEntity(Tav.class, versenyId, pollingRequest.getTav(), verseny.getTavSyncValue()));
        pollingResult.setVersenyzo(createPollingResultEntity(Versenyzo.class, versenyId, pollingRequest.getVersenyzo(), verseny.getVersenyzoSyncValue()));
        pollingResult.setRaceStatus(verseny.getRaceStatus());
        return pollingResult;
    }

    @Override
    public String createChannelToken() {
        ChannelService channelService = ChannelServiceFactory.getChannelService();
        Key<ClientChannel> key = ofy.put(new ClientChannel(new Date()));
        return channelService.createChannel(Long.toString(key.getId()));
    }


    @Override
    public Verseny addVerseny(String versenyMegnevezes) {
        Verseny verseny = new Verseny(versenyMegnevezes);
        updateVerseny(verseny);
        return verseny;
    }

    @Override
    public Verseny getVerseny(Long id) {
        return ofy.get(Verseny.class, id);
    }

    @Override
    public List<Verseny> getVersenyek() {
        List<Verseny> versenyList = ofy.query(Verseny.class).list();
        for (Verseny verseny: versenyList) {
            if (verseny.getVersenyzoSzam() == null) {
                verseny.setVersenyzoSzam(getVersenyzoSzam(verseny.getId()));
            }
        }
        return versenyList;
    }

    @Override
    public List<PersonLap> getAllPersonLapList(Long versenyId) {
        return getPersonList(versenyId);
    }

    @Override
    public List<PageProfile> getAllPageProfiles() {
        return ofy.query(PageProfile.class).list();
    }

    @Override
    public void savePageProfile(PageProfile pageProfile) {
        ofy.put(pageProfile);
    }

    @Override
    public void saveNev(String[] nevek, boolean ferfi) {
        Nev[] nevArray = new Nev[nevek.length];
        for (int i = 0, nevekLength = nevek.length; i < nevekLength; i++) {
            nevArray[i] = new Nev(nevek[i].trim(), ferfi);
        }
        ofy.put(nevArray);
    }

    @Override
    public Boolean isFerfiNev(String nev) {
        String normalisedNev = nev.trim().toUpperCase();
        final Nev foundNev = ofy.find(Nev.class, normalisedNev);
        if (foundNev == null && normalisedNev.length() > 2 && normalisedNev.endsWith("NÉ")) {
            final Nev neQueryNev = ofy.find(Nev.class, normalisedNev.substring(0, normalisedNev.length() - 2));
            return neQueryNev != null && neQueryNev.isFerfi() ? false : null;
        } else {
            return foundNev != null ? foundNev.isFerfi() : null;
        }
    }

    @Override
    public String getConfiguration(String key) {
        return getConfigurationStatic(key);
    }

    public static String getConfigurationStatic(String key) {
        if (configMemcacheService.contains(key)) {
            return (String) configMemcacheService.get(key);
        } else {
            String value = ofy.get(Configuration.class, key).getValue();
            configMemcacheService.put(key, value, DEFAULT_CACHE_EXPIRATION);
            return value;
        }
    }

    @Override
    public List<Configuration> getConfigurations() {
        return ofy.query(Configuration.class).list();
    }

    @Override
    public void saveConfigurations(List<Configuration> configurations) {
        if (!isSuperUserAuthorized()) throw new NotAuthorizedException();
        configMemcacheService.clearAll();
        for (Configuration conf: configurations) {
            ofy.put(conf);
        }
    }

    @Override
    public void updateNevezesiDij(Long id, Integer dij) throws NotExistingEntityException {
        try {
            Versenyzo versenyzo = ofy.get(Versenyzo.class, id);
            versenyzo.setFizetettDij(dij);
            ofy.put(versenyzo);
            // az NevezesiDij csak adminisztrációs jellegű, és egy gépen zajlik, ezt nem broadcast-oljuk
        } catch (NotFoundException e) {
            throw new NotExistingEntityException();
        }
    }

    private boolean isSuperUserAuthorized() {
        String superUser = getConfiguration(UserServiceImpl.SUPER_USER_KEY);
        return superUser.equals(UserServiceFactory.getUserService().getCurrentUser().getEmail());
    }

    private int getVersenyzoSzam(Long versenyId) {
        com.google.appengine.api.datastore.Query query = new com.google.appengine.api.datastore.Query(PersonLap.class.getSimpleName());
        query.addProjection(new PropertyProjection("raceNumber", String.class));
        query.setDistinct(true);
        query.setFilter(new com.google.appengine.api.datastore.Query.FilterPredicate("versenyId",
                com.google.appengine.api.datastore.Query.FilterOperator.EQUAL, versenyId));
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        return datastore.prepare(query).countEntities(FetchOptions.Builder.withDefaults());
    }
    
    

    private void broadcastModification() {
        ChannelService channelService = ChannelServiceFactory.getChannelService();
        long creationDeadline = System.currentTimeMillis() - CHANNEL_EXPIRATION;
        for (ClientChannel channel: ofy.query(ClientChannel.class).list()) {
            if (channel.getCreationDate().getTime() < creationDeadline) {
                ofy.delete(channel);
            } else if (channel.isConnected()) {
                channelService.sendMessage(new ChannelMessage(channel.getChannelId().toString(), "modifiedContent"));
            }
        }
    }

    private <T> PollingResult.Entity<T> createPollingResultEntity(Class<T> clazz, Long versenyId,
                                                                  PollingRequest.Entity pollingRequestEntity,
                                                                  int syncValue) {
        if (pollingRequestEntity == null) {
            return null;
        } else if (pollingRequestEntity.getSync() != syncValue) {
            List<T> items = ofy.query(clazz).filter("versenyId", versenyId).order("creationTime").list();
            return new PollingResult.Entity<T>(items, syncValue, true);
        } else {
            Long maxCreationTime = (Long) memcacheService.get(getMaxCreationTimeCacheKey(clazz, versenyId));
            if (maxCreationTime == null) {
                T item = ofy.query(clazz).filter("versenyId", versenyId).order("-creationTime").get();
                maxCreationTime = item == null ? 0 : ((HasCreationTime) item).getCreationTime();
                memcacheService.put(getMaxCreationTimeCacheKey(clazz, versenyId), maxCreationTime, DEFAULT_CACHE_EXPIRATION);
            }
            if (pollingRequestEntity.getMaxTime() >= maxCreationTime) {
                return null;
            } else {
                Query<T> versenyQuery = ofy.query(clazz).filter("versenyId", versenyId);
                if (pollingRequestEntity.getMaxTime() > 0) {
                    versenyQuery = versenyQuery.filter("creationTime >", pollingRequestEntity.getMaxTime()).order("creationTime");
                }
                return new PollingResult.Entity<T>(versenyQuery.list(), syncValue, false);
            }
        }
    }

    private PollingResult.Entity<PersonLap> createPollingResultPersonLap(Long versenyId,
                                                                         PollingRequest.Entity pollingRequestEntity,
                                                                         int syncValue) {
        if (pollingRequestEntity == null) {
            return null;
        } else if (pollingRequestEntity.getSync() != syncValue) {
            List<PersonLap> items = getPersonList(versenyId);
            return new PollingResult.Entity<PersonLap>(items, syncValue, true);
        } else {
            List<PersonLap> personLapCollection = new ArrayList<PersonLap>();
            List<PersonLap> personLapList = getPersonList(versenyId);
            for (int i = personLapList.size() - 1; i >= 0; i--) {
                PersonLap personLap = personLapList.get(i);
                if (personLap.getTime() > pollingRequestEntity.getMaxTime()) {
                    personLapCollection.add(personLap);
                } else break;
            }

            return new PollingResult.Entity<>(personLapCollection,
                    syncValue, false);
        }
    }

    private <T> String getMaxCreationTimeCacheKey(Class<T> clazz, Long versenyId) {
        return versenyId.toString() + "." + clazz.getName() + ".maxCreationTime";
    }

    private String getVersenyEntityCacheKey(Long versenyId) {
        return versenyId.toString() + "." + Verseny.class + ".entity";
    }

    private Verseny getVersenyFromCache(Long versenyId) {
        Verseny verseny = (Verseny) memcacheService.get(getVersenyEntityCacheKey(versenyId));
        if (verseny == null) {
            verseny = ofy.get(Verseny.class, versenyId);
            memcacheService.put(getVersenyEntityCacheKey(versenyId), verseny, DEFAULT_CACHE_EXPIRATION);
        }
        return verseny;
    }

    private void updateVerseny(Verseny verseny) {
        ofy.put(verseny);
        memcacheService.put(getVersenyEntityCacheKey(verseny.getId()), verseny, DEFAULT_CACHE_EXPIRATION);
    }

    private void incrementSyncValue(Long versenyId, SyncValueType syncValueType) {
        Verseny verseny = getVersenyFromCache(versenyId);
        switch (syncValueType) {
            case PERSON_LAP:
                verseny.setPersonLapSyncValue(verseny.getPersonLapSyncValue() + 1);
                break;
            case VERSENYSZAM:
                verseny.setVersenySzamSyncValue(verseny.getVersenySzamSyncValue() + 1);
                break;
            case TAV:
                verseny.setTavSyncValue(verseny.getTavSyncValue() + 1);
                break;
            case VERSENYZO:
                verseny.setVersenyzoSyncValue(verseny.getVersenyzoSyncValue() + 1);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        updateVerseny(verseny);
    }

    @SuppressWarnings("unchecked")
    private List<PersonLap> getPersonList(Long versenyId) {
        List<PersonLap> personLapList = (List<PersonLap>) memcacheService.get(
                getPersonLapListKey(versenyId));
        if (personLapList == null) {
            personLapList = new ArrayList<>(ofy.query(PersonLap.class).filter("versenyId", versenyId).order("time").list());
            putPersonLapListIntoCache(versenyId, personLapList);
        }
        return personLapList;
    }

    private void putPersonLapListIntoCache(Long versenyId, List<PersonLap> personLapList) {
        memcacheService.put(getPersonLapListKey(versenyId), personLapList, DEFAULT_CACHE_EXPIRATION);
    }

    private void addPersonLap(PersonLap personLap) {
        ofy.put(personLap);
        List<PersonLap> personLapList = getPersonList(personLap.getVersenyId());
        personLapList.add(personLap);
        Collections.sort(personLapList);
        putPersonLapListIntoCache(personLap.getVersenyId(), personLapList);
    }

    private void removePersonLapFromCache(PersonLap personLap) {
        List<PersonLap> personLapList = getPersonList(personLap.getVersenyId());
        personLapList.remove(personLap);
        putPersonLapListIntoCache(personLap.getVersenyId(), personLapList);
    }

    private String getPersonLapListKey(Long versenyId) {
        return versenyId.toString() + "." +
                PersonLap.class.getName() + ".entryTree";
    }
}
