package net.himadri.scmt.client.serializable;

import net.himadri.scmt.client.entity.*;

import java.io.Serializable;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.02. 21:06
 */
public class PollingResult implements Serializable {
    public static class Entity<T> implements Serializable {
        @SuppressWarnings({"UnusedDeclaration"})
        public Entity() {
        }

        public Entity(List<T> items, int syncId, boolean fullSync) {
            this.items = items;
            this.syncId = syncId;
            this.fullSync = fullSync;
        }

        List<T> items;
        int syncId;
        boolean fullSync;

        public List<T> getItems() {
            return items;
        }

        public int getSyncId() {
            return syncId;
        }

        public boolean isFullSync() {
            return fullSync;
        }
    }

    private Entity<PersonLap> personLap;
    private Entity<VersenySzam> versenySzam;
    private Entity<Tav> tav;
    private Entity<Versenyzo> versenyzo;

    private RaceStatus raceStatus;

    public PollingResult() {
    }

    public void setPersonLap(Entity<PersonLap> personLap) {
        this.personLap = personLap;
    }

    public void setVersenySzam(Entity<VersenySzam> versenySzam) {
        this.versenySzam = versenySzam;
    }

    public void setTav(Entity<Tav> tav) {
        this.tav = tav;
    }

    public void setVersenyzo(Entity<Versenyzo> versenyzo) {
        this.versenyzo = versenyzo;
    }

    public void setRaceStatus(RaceStatus raceStatus) {
        this.raceStatus = raceStatus;
    }

    public Entity<PersonLap> getPersonLap() {
        return personLap;
    }

    public Entity<VersenySzam> getVersenySzam() {
        return versenySzam;
    }

    public Entity<Tav> getTav() {
        return tav;
    }

    public Entity<Versenyzo> getVersenyzo() {
        return versenyzo;
    }

    public RaceStatus getRaceStatus() {
        return raceStatus;
    }
}
