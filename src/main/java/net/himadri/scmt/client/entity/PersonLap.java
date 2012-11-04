package net.himadri.scmt.client.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.02.16. 14:41
 */
@Entity
public class PersonLap implements Serializable, Comparable<PersonLap> {
    @SuppressWarnings({"UnusedDeclaration"})
    @Id
    private Long id;
    private Long versenyId;
    private String raceNumber;
    private long time;

    @SuppressWarnings({"UnusedDeclaration"})
    public PersonLap() {
    }

    public PersonLap(Long versenyId, String raceNumber, long time) {
        this.versenyId = versenyId;
        this.raceNumber = raceNumber;
        this.time = time;
    }

    public Long getId() {
        return id;
    }

    public Long getVersenyId() {
        return versenyId;
    }

    public String getRaceNumber() {
        return raceNumber;
    }

    public void setRaceNumber(String raceNumber) {
        this.raceNumber = raceNumber;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public int compareTo(PersonLap anotherPersonLap) {
        long thisVal = this.time;
        long anotherVal = anotherPersonLap.time;
        return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PersonLap personLap = (PersonLap) o;

        if (id != null ? !id.equals(personLap.id) : personLap.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
