package net.himadri.scmt.client.serializable;

import net.himadri.scmt.client.SCMTMarathon;
import net.himadri.scmt.client.entity.Tav;
import net.himadri.scmt.client.entity.VersenySzam;
import net.himadri.scmt.client.entity.Versenyzo;

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
    private Versenyzo versenyzo;
    private VersenySzam versenySzam;
    private Tav tav;

    private SCMTMarathon scmtMarathon;

    public RaceStatusRow(String raceNumber, SCMTMarathon scmtMarathon) {
        this.raceNumber = raceNumber;
        this.scmtMarathon = scmtMarathon;
        lapTimes = new ArrayList<Long>();
        refreshVersenyData();
    }

    public void refreshVersenyData() {
        versenyzo = scmtMarathon.getVersenyzoMapCache().getVersenyzo(raceNumber);
        versenySzam = versenyzo != null ? scmtMarathon.getVersenyszamMapCache().getVersenySzam(versenyzo.getVersenySzamId()) : null;
        tav = versenySzam != null ? scmtMarathon.getTavMapCache().getTav(versenySzam.getTavId()) : null;
    }

    public String getRaceNumber() {
        return raceNumber;
    }

    public List<Long> getLapTimes() {
        return lapTimes;
    }

    public Versenyzo getVersenyzo() {
        return versenyzo;
    }

    public VersenySzam getVersenySzam() {
        return versenySzam;
    }

    public Tav getTav() {
        return tav;
    }

    @Override
    public int compareTo(RaceStatusRow o) {
        int lapCount = getLapCount(this);
        int oLapCount = getLapCount(o);
        if (lapCount != oLapCount) {
            return Integer.valueOf(oLapCount).compareTo(lapCount);
        } else if (lapCount == 0) {
            return raceNumber.compareTo(o.raceNumber);
        } else {
            return lapTimes.get(lapCount - 1).compareTo(o.lapTimes.get(oLapCount - 1));
        }
    }
    
    private int getLapCount(RaceStatusRow raceStatusRow) {
        return raceStatusRow.getTav() == null || raceStatusRow.getTav().getKorSzam() == null ? 
                raceStatusRow.getLapTimes().size() : 
                Math.min(raceStatusRow.getLapTimes().size(), raceStatusRow.getTav().getKorSzam());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RaceStatusRow that = (RaceStatusRow) o;

        if (raceNumber != null ? !raceNumber.equals(that.raceNumber) : that.raceNumber != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return raceNumber != null ? raceNumber.hashCode() : 0;
    }
}
