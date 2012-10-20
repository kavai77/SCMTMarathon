package net.himadri.scmt.client.serializable;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.02. 21:06
 */
public class PollingRequest implements Serializable {
    public static class Entity implements Serializable {
        @SuppressWarnings({"UnusedDeclaration"})
        public Entity() {
        }

        public Entity(long maxTime, int sync) {
            this.maxTime = maxTime;
            this.sync = sync;
        }

        long maxTime;
        int sync;

        public long getMaxTime() {
            return maxTime;
        }

        public int getSync() {
            return sync;
        }
    }

    private Entity personLap;
    private Entity versenySzam;
    private Entity tav;
    private Entity versenyzo;

    @SuppressWarnings({"UnusedDeclaration"})
    public PollingRequest() {
    }

    public PollingRequest(Entity personLap, Entity versenySzam, Entity tav, Entity versenyzo) {
        this.personLap = personLap;
        this.versenySzam = versenySzam;
        this.tav = tav;
        this.versenyzo = versenyzo;
    }

    public Entity getPersonLap() {
        return personLap;
    }

    public Entity getVersenySzam() {
        return versenySzam;
    }

    public Entity getTav() {
        return tav;
    }

    public Entity getVersenyzo() {
        return versenyzo;
    }
}
