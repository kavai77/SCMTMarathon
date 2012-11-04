package net.himadri.scmt.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.02.20. 13:44
 */
public class RaceStatusRow implements Comparable<RaceStatusRow> {
    private String raceNumber;
    private List<Long> lapTimes;

    public RaceStatusRow(String raceNumber) {
        this.raceNumber = raceNumber;
        lapTimes = new ArrayList<Long>();
    }

    public String getRaceNumber() {
        return raceNumber;
    }

    public List<Long> getLapTimes() {
        return lapTimes;
    }

    @Override
    public int compareTo(RaceStatusRow o) {
        if (lapTimes.size() != o.lapTimes.size()) {
            return o.lapTimes.size() - lapTimes.size();
        } else if (lapTimes.isEmpty() && o.lapTimes.isEmpty()) {
            return raceNumber.compareTo(o.raceNumber);
        } else {
            return lapTimes.get(lapTimes.size() - 1).compareTo(o.lapTimes.get(o.lapTimes.size() - 1));
        }
    }
}
