package net.himadri.scmt.client.entity;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.02.20. 14:31
 */
public enum RaceStatus {
    NOT_STARTED, RACING, FINISHED;

    public boolean isActive() {
        return this == NOT_STARTED || this == RACING;
    }
}
