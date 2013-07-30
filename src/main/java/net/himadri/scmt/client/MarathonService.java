package net.himadri.scmt.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import net.himadri.scmt.client.entity.PageProfile;
import net.himadri.scmt.client.entity.PersonLap;
import net.himadri.scmt.client.entity.Verseny;
import net.himadri.scmt.client.entity.Versenyzo;
import net.himadri.scmt.client.exception.AlreadyExistingEntityException;
import net.himadri.scmt.client.exception.NotExistingEntityException;
import net.himadri.scmt.client.serializable.PollingRequest;
import net.himadri.scmt.client.serializable.PollingResult;

import java.util.List;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("marathon")
public interface MarathonService extends RemoteService {
    void startRace(Long versenyId);

    void shiftRaceTime(Long versenyId, long offsetInMillis);

    void stopRace(Long versenyId);

    void restartRace(Long versenyId);

    void deleteRace(Long versenyId);

    long getRaceTime(Long versenyId);

    void addPersonLap(Long versenyId, String raceNumber) throws AlreadyExistingEntityException;

    void removePersonLap(Long personLapId);

    void updateRaceNumber(Long personLapId, String raceNumber);

    void updateLapTime(Long personLapId, long lapTime);

    void addVersenySzam(Long versenyId, Long tavId, Boolean ferfi, Integer korTol, Integer korIg);

    void modifyVersenySzam(Long versenySzamId, Long tavId, Boolean ferfi, Integer korTol, Integer korIg);

    void removeVersenySzam(Long versenySzamId);

    void addTav(Long versenyId, String megnevezes, Integer korszam, Integer versenySzamtol, Integer versenySzamig);

    void modifyTav(Long tavId, String megnevezes, Integer korszam, Integer versenySzamtol, Integer versenySzamig);

    void removeTav(Long tavId);

    void addVersenyzo(String raceNumber, String name, Boolean ferfi, Integer eletkor, String egyesulet, Long versenySzamId, Long versenyId) throws AlreadyExistingEntityException;

    void modifyVersenyzo(Versenyzo versenyzo) throws AlreadyExistingEntityException;

    void removeVersenyzo(Long versenyzoId) throws NotExistingEntityException;

    void versenyzoFeladta(Long versenyzoId, boolean feladta) throws NotExistingEntityException;

    PollingResult getPollingResult(Long versenyId, PollingRequest pollingRequest);

    String createChannelToken();

    Verseny addVerseny(String versenyMegnevezes);

    List<Verseny> getVersenyek();

    List<PersonLap> getAllPersonLapList(Long versenyId);

    List<PageProfile> getAllPageProfiles();

    void savePageProfile(PageProfile pageProfile);

    void saveNev(String[] nevek, boolean ferfi);

    Boolean isFerfiNev(String nev);
}
