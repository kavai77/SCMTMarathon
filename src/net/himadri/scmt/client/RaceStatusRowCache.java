package net.himadri.scmt.client;

import net.himadri.scmt.client.entity.PersonLap;
import net.himadri.scmt.client.entity.Tav;
import net.himadri.scmt.client.entity.VersenySzam;
import net.himadri.scmt.client.entity.Versenyzo;
import net.himadri.scmt.client.serializable.MarathonActionListener;
import net.himadri.scmt.client.serializable.RaceStatusRow;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.14. 22:34
 */
public class RaceStatusRowCache {
    private List<RaceStatusRow> allRaceStatusRows = new ArrayList<RaceStatusRow>();
    private Map<String, RaceStatusRow> raceNumberStatusRowMap = new HashMap<String, RaceStatusRow>();
    private long maxTime;
    private SCMTMarathon scmtMarathon;

    public RaceStatusRowCache(SCMTMarathon scmtMarathon) {
        this.scmtMarathon = scmtMarathon;
        scmtMarathon.getPollingService().getPersonLapSync().addMarathonActionListener(
                SyncSupport.Priority.HIGH, new PersonLapActionListener());
        scmtMarathon.getPollingService().getTavSync().addMarathonActionListener(
                SyncSupport.Priority.MEDIUM, new TavVersenyszamActionListener<Tav>());
        scmtMarathon.getPollingService().getVersenySzamSync().addMarathonActionListener(
                SyncSupport.Priority.MEDIUM, new TavVersenyszamActionListener<VersenySzam>());
        scmtMarathon.getPollingService().getVersenyzoSync().addMarathonActionListener(
                SyncSupport.Priority.MEDIUM, new VersenyzoActionListener());
    }

    public List<RaceStatusRow> getAllRaceStatusRows() {
        return allRaceStatusRows;
    }

    public RaceStatusRow getRaceStatusRowByRaceNumber(String raceNumber) {
        return raceNumberStatusRowMap.get(raceNumber);
    }

    public long getMaxTime() {
        return maxTime;
    }

    private class PersonLapActionListener implements MarathonActionListener<PersonLap> {
        @Override
        public void itemAdded(List<PersonLap> items) {
            addPersonLaps(items);
            Collections.sort(allRaceStatusRows);
        }

        @Override
        public void itemRefreshed(List<PersonLap> items) {
            allRaceStatusRows.clear();
            raceNumberStatusRowMap.clear();
            maxTime = 0;
            itemAdded(items);
        }

        private void addPersonLaps(List<PersonLap> personLaps) {
            for (PersonLap personLap : personLaps) {
                RaceStatusRow raceStatusRow = raceNumberStatusRowMap.get(personLap.getRaceNumber());
                if (raceStatusRow == null) {
                    raceStatusRow = new RaceStatusRow(personLap.getRaceNumber(), scmtMarathon);
                    allRaceStatusRows.add(raceStatusRow);
                    raceNumberStatusRowMap.put(personLap.getRaceNumber(), raceStatusRow);
                }
                List<Long> lapTimes = raceStatusRow.getLapTimes();
                lapTimes.add(personLap.getTime());
                if (maxTime < personLap.getTime()) {
                    maxTime = personLap.getTime();
                }
            }
        }
    }

    private class VersenyzoActionListener implements MarathonActionListener<Versenyzo> {
        @Override
        public void itemAdded(List<Versenyzo> items) {
            itemRefreshed(items);
        }

        @Override
        public void itemRefreshed(List<Versenyzo> items) {
            for (RaceStatusRow raceStatusRow : allRaceStatusRows) {
                raceStatusRow.refreshVersenyData();
            }
        }
    }

    private class TavVersenyszamActionListener<T> implements MarathonActionListener<T> {
        @Override
        public void itemAdded(List<T> items) {
        }

        @Override
        public void itemRefreshed(List<T> items) {
            for (RaceStatusRow raceStatusRow : allRaceStatusRows) {
                raceStatusRow.refreshVersenyData();
            }
        }
    }
}
