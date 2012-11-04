package net.himadri.scmt.client;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.02.24. 11:32
 */
public class UpdateLapTimeResult implements Serializable {
    public enum Status {
        SUCCESSFUL, UNDER_PREV_LAP, OVER_NEXT_LAP
    }

    private Status status;
    private long lapTime;

    public UpdateLapTimeResult() {
    }

    public UpdateLapTimeResult(Status status, long lapTime) {
        this.status = status;
        this.lapTime = lapTime;
    }

    public Status getStatus() {
        return status;
    }

    public long getLapTime() {
        return lapTime;
    }
}
