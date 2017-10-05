package net.himadri.scmt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import net.himadri.scmt.client.entity.*;
import net.himadri.scmt.client.serializable.PollingRequest;
import net.himadri.scmt.client.serializable.PollingResult;

import java.util.List;

public interface MarathonServiceAsync {

    void startRace(Long versenyId, AsyncCallback<Void> async);

    void stopRace(Long versenyId, AsyncCallback<Void> async);

    void restartRace(Long versenyId, AsyncCallback<Void> async);

    void getRaceTime(Long versenyId, AsyncCallback<Long> async);

    void addPersonLap(Long versenyId, String raceNumber, long raceTime, boolean withThresholdValidation, AsyncCallback<Void> async);

    void setNevezesDatum(Long versenyId, Long nevezesBegin, Long nevezesEnd, Long raceDate, String emailSubject,
                         String emailText, Integer helysziniNevezesOsszeg, AsyncCallback<Void> async);

    void updateRaceNumber(Long personLapId, String raceNumber, AsyncCallback<Void> async);

    void updateLapTime(Long personLapId, long lapTime, AsyncCallback<Void> async);

    void addVersenySzam(Long versenyId, Long tavId, Boolean ferfi, Integer korTol, Integer korIg, AsyncCallback<Void> async);

    void modifyVersenySzam(Long versenySzamId, Long tavId, Boolean ferfi, Integer korTol, Integer korIg, AsyncCallback<Void> async);

    void removeVersenySzam(Long versenySzamId, AsyncCallback<Void> async);

    void addVersenyzo(String raceNumber, String name, Boolean ferfi, Integer szuletesiEv, String egyesulet, String email,
                      Long versenySzamId, Long versenyId, String poloMeret, String licenszSzam,
                      Integer fizetettDij, AsyncCallback<Void> async);

    void modifyVersenyzo(Versenyzo versenyzo, AsyncCallback<Void> async);

    void removeVersenyzo(Long versenyzoId, AsyncCallback<Void> async);

    void getPollingResult(Long versenyId, PollingRequest pollingRequest, AsyncCallback<PollingResult> async);

    void getVersenyek(AsyncCallback<List<Verseny>> async);

    void getAllPersonLapList(Long versenyId, AsyncCallback<List<PersonLap>> async);

    void addVerseny(String versenyMegnevezes, AsyncCallback<Verseny> async);

    void deleteRace(Long versenyId, AsyncCallback<Void> async);

    void removePersonLap(Long versenyId, Long personLapId, AsyncCallback<Void> async);

    void addTav(Long versenyId, String megnevezes, Integer korszam, Integer versenySzamtol, Integer versenySzamig, long raceStartDiff, String[] korNevArray, AsyncCallback<Void> async);

    void modifyTav(Long tavId, String megnevezes, Integer korszam, Integer versenySzamtol, Integer versenySzamig, long raceStartDiff, String[] korNevArray, AsyncCallback<Void> async);

    void futamStart(long tavId, AsyncCallback<Long> async);

    void removeTav(Long tavId, AsyncCallback<Void> async);

    void getAllPageProfiles(AsyncCallback<List<PageProfile>> async);

    void savePageProfile(PageProfile pageProfile, AsyncCallback<Void> async);

    void saveNev(String[] nevek, boolean ferfi, AsyncCallback<Void> async);

    void isFerfiNev(String nev, AsyncCallback<Boolean> async);

    void versenyzoFeladta(Long versenyzoId, boolean feladta, AsyncCallback<Void> async);
    
    void versenyzoEredmenyEllenorzott(Long versenyzoId, boolean ellenorzott, AsyncCallback<Void> async);

    void shiftRaceTime(Long versenyId, long offsetInMillis, AsyncCallback<Void> async);

    void getVerseny(Long id, AsyncCallback<Verseny> async);

    void getConfiguration(String key, AsyncCallback<String> async);

    void getConfigurations(AsyncCallback<List<Configuration>> async);

    void saveConfigurations(List<Configuration> configurations, AsyncCallback<Void> async);

    void updateNevezesiDij(Long id, Integer dij, AsyncCallback<Void> asyncCallback);
}
