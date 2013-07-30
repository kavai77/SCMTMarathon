package net.himadri.scmt.server;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Query;
import net.himadri.scmt.client.MarathonService;
import net.himadri.scmt.client.entity.*;
import net.himadri.scmt.client.exception.AlreadyExistingEntityException;
import net.himadri.scmt.client.exception.NotAuthorizedException;
import net.himadri.scmt.client.exception.NotExistingEntityException;
import net.himadri.scmt.client.serializable.PollingRequest;
import net.himadri.scmt.client.serializable.PollingResult;

import java.util.*;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class MarathonServiceImpl extends RemoteServiceServlet implements
        MarathonService {

    public static final Expiration DEFAULT_CACHE_EXPIRATION = Expiration.byDeltaSeconds(60 * 60);

    private enum SyncValueType {PERSON_LAP, VERSENYZO, VERSENYSZAM, TAV}

    public static final long RACE_TIME_THRESHOLD = 60000;
    private static Objectify ofy = ObjectifyService.begin();
    private static MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService();

    static Set<Long> channelIdSet = new HashSet<Long>();
    static Set<Long> pendingChannelIdSet = new HashSet<Long>();

    static {
        ObjectifyService.register(PersonLap.class);
        ObjectifyService.register(VersenySzam.class);
        ObjectifyService.register(Tav.class);
        ObjectifyService.register(Versenyzo.class);
        ObjectifyService.register(Verseny.class);
        ObjectifyService.register(PageProfile.class);
        ObjectifyService.register(Nev.class);
    }

    @Override
    public void startRace(Long versenyId) {
        Verseny verseny = getVersenyFromCache(versenyId);
        verseny.setRaceStartTime(System.currentTimeMillis());
        verseny.setRaceStatus(RaceStatus.RACING);
        updateVerseny(verseny);
        broadcastModification();
    }

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
    public void deleteRace(Long versenyId) {
        if (!UserServiceImpl.isSuperUserAuthorizedStatic()) throw new NotAuthorizedException();
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
    public void addTav(Long versenyId, String megnevezes, Integer korszam, Integer versenySzamtol, Integer versenySzamig) {
        Tav tav = new Tav(versenyId, megnevezes, korszam, versenySzamtol, versenySzamig);
        ofy.put(tav);
        memcacheService.put(getMaxCreationTimeCacheKey(Tav.class, versenyId),
                tav.getCreationTime(), DEFAULT_CACHE_EXPIRATION);
        broadcastModification();
    }

    @Override
    public void modifyTav(Long tavId, String megnevezes, Integer korszam, Integer versenySzamtol, Integer versenySzamig) {
        Tav tav = ofy.get(Tav.class, tavId);
        tav.setMegnevezes(megnevezes);
        tav.setKorSzam(korszam);
        tav.setVersenySzamtol(versenySzamtol);
        tav.setVersenySzamig(versenySzamig);
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
    public void addVersenyzo(String raceNumber, String name, Boolean ferfi, Integer szuletesiEv, String egyesulet, Long versenySzamId, Long versenyId) throws AlreadyExistingEntityException {
        int count = ofy.query(Versenyzo.class)
                .filter("versenyId", versenyId)
                .filter("raceNumber", raceNumber).count();
        if (count > 0) {
            throw new AlreadyExistingEntityException();
        }
        Versenyzo versenyzo = new Versenyzo(raceNumber, name, ferfi, szuletesiEv, egyesulet, versenySzamId, versenyId);
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
    public void addPersonLap(Long versenyId, String raceNumber) throws AlreadyExistingEntityException {
        long raceTime = getRaceTime(versenyId);
        PersonLap personLap = ofy.query(PersonLap.class)
                .filter("versenyId", versenyId)
                .filter("raceNumber", raceNumber)
                .order("-time").get();
        if (personLap != null && raceTime - personLap.getTime() <= RACE_TIME_THRESHOLD) {
            throw new AlreadyExistingEntityException();
        }
        TreeMap<Long, PersonLap> personLapTreeMap = getPersonLapMap(versenyId);
        while (personLapTreeMap.containsKey(raceTime)) {
            raceTime++;
        }
        PersonLap newPersonLap = new PersonLap(versenyId, raceNumber, raceTime);
        addPersonLap(newPersonLap);
        broadcastModification();
    }

    @Override
    public void removePersonLap(Long personLapId) {
        PersonLap personLap = ofy.get(PersonLap.class, personLapId);
        removePersonLapFromCache(personLap);
        ofy.delete(PersonLap.class, personLapId);
        incrementSyncValue(personLap.getVersenyId(), SyncValueType.PERSON_LAP);
        broadcastModification();
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
        long channelId;
        Random random = new Random();
        do {
            channelId = random.nextLong();
        } while (channelIdSet.contains(channelId) || pendingChannelIdSet.contains(channelId));
        pendingChannelIdSet.add(channelId);
        return channelService.createChannel(Long.toString(channelId), 12 * 60);
    }


    @Override
    public Verseny addVerseny(String versenyMegnevezes) {
        Verseny verseny = new Verseny(versenyMegnevezes);
        updateVerseny(verseny);
        return verseny;
    }

    @Override
    public List<Verseny> getVersenyek() {
        return ofy.query(Verseny.class).list();
    }

    @Override
    public List<PersonLap> getAllPersonLapList(Long versenyId) {
        return new ArrayList<PersonLap>(getPersonLapMap(versenyId).values());
    }

    @Override
    public List<PageProfile> getAllPageProfiles() {
        List<PageProfile> pageProfiles = ofy.query(PageProfile.class).list();
        Set<String> availableKeys = new HashSet<String>();
        for (PageProfile pageProfile: pageProfiles) {
            availableKeys.add(pageProfile.getId());
        }
        for (PageProfileId pageProfileId: PageProfileId.values()) {
            if (!availableKeys.contains(pageProfileId.name())) {
                pageProfiles.add(new PageProfile(pageProfileId));
            }
        }
        Collections.sort(pageProfiles, new Comparator<PageProfile>() {
            @Override
            public int compare(PageProfile o1, PageProfile o2) {
                return PageProfileId.valueOf(o1.getId()).compareTo(PageProfileId.valueOf(o2.getId()));
            }
        });
        return pageProfiles;
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
        Nev foundNev = ofy.find(Nev.class, nev.trim().toUpperCase());
        return foundNev != null ? foundNev.isFerfi() : null;
    }

    private void broadcastModification() {
        ChannelService channelService = ChannelServiceFactory.getChannelService();
        for (Long channelId : channelIdSet) {
            channelService.sendMessage(new ChannelMessage(channelId.toString(), "modifiedContent"));
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
            List<PersonLap> items = new ArrayList<PersonLap>(getPersonLapMap(versenyId).values());
            return new PollingResult.Entity<PersonLap>(items, syncValue, true);
        } else {
            Collection<PersonLap> personLapCollection = getPersonLapMap(versenyId)
                    .tailMap(pollingRequestEntity.getMaxTime(), false).values();
            return new PollingResult.Entity<PersonLap>(new ArrayList<PersonLap>(personLapCollection),
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
    private TreeMap<Long, PersonLap> getPersonLapMap(Long versenyId) {
        TreeMap<Long, PersonLap> personLapTreeMap = (TreeMap<Long, PersonLap>) memcacheService.get(
                getPersonLapMapKey(versenyId));
        if (personLapTreeMap == null) {
            personLapTreeMap = new TreeMap<Long, PersonLap>();
            Query<PersonLap> query = ofy.query(PersonLap.class).filter("versenyId", versenyId);
            for (PersonLap personLap : query) {
                personLapTreeMap.put(personLap.getTime(), personLap);
            }
            putPersonLapTreeIntoCache(versenyId, personLapTreeMap);
        }
        return personLapTreeMap;
    }

    private void putPersonLapTreeIntoCache(Long versenyId, TreeMap<Long, PersonLap> personLapTreeMap) {
        memcacheService.put(getPersonLapMapKey(versenyId), personLapTreeMap, DEFAULT_CACHE_EXPIRATION);
    }

    private void addPersonLap(PersonLap personLap) {
        ofy.put(personLap);
        TreeMap<Long, PersonLap> personLapTreeMap = getPersonLapMap(personLap.getVersenyId());
        personLapTreeMap.put(personLap.getTime(), personLap);
        putPersonLapTreeIntoCache(personLap.getVersenyId(), personLapTreeMap);
    }

    private void removePersonLapFromCache(PersonLap personLap) {
        TreeMap<Long, PersonLap> personLapMap = getPersonLapMap(personLap.getVersenyId());
        personLapMap.remove(personLap.getTime());
        putPersonLapTreeIntoCache(personLap.getVersenyId(), personLapMap);
    }

    private String getPersonLapMapKey(Long versenyId) {
        return versenyId.toString() + "." +
                PersonLap.class.getName() + ".entryTree";
    }
}
