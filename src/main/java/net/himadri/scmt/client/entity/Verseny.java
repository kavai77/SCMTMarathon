package net.himadri.scmt.client.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.22. 5:51
 */
@Entity
public class Verseny implements Serializable {
    @SuppressWarnings({"UnusedDeclaration"})
    @Id
    private Long id;
    private String nev;
    private Long raceStartTime;
    private RaceStatus raceStatus = RaceStatus.NOT_STARTED;
    private Integer versenyzoSzam;
    private int personLapSyncValue = 1;
    private int versenyzoSyncValue = 1;
    private int versenySzamSyncValue = 1;
    private int tavSyncValue = 1;
    private Long nevezesBegin;
    private Long nevezesEnd;
    private Long raceDate;
    private String nevezesEmailSubject;
    private String nevezesEmailText;
    private Integer helysziniNevezesOsszeg;
    private Boolean triatlonLicensz;

    @SuppressWarnings({"UnusedDeclaration"})
    public Verseny() {
    }

    public Verseny(String nev) {
        this.nev = nev;
    }

    public Long getId() {
        return id;
    }

    public String getNev() {
        return nev;
    }

    public void setNev(String nev) {
        this.nev = nev;
    }

    public Long getRaceStartTime() {
        return raceStartTime;
    }

    public void setRaceStartTime(Long raceStartTime) {
        this.raceStartTime = raceStartTime;
    }

    public RaceStatus getRaceStatus() {
        return raceStatus;
    }

    public void setRaceStatus(RaceStatus raceStatus) {
        this.raceStatus = raceStatus;
    }

    public Integer getVersenyzoSzam() {
        return versenyzoSzam;
    }

    public void setVersenyzoSzam(Integer versenyzoSzam) {
        this.versenyzoSzam = versenyzoSzam;
    }

    public int getPersonLapSyncValue() {
        return personLapSyncValue;
    }

    public int getVersenyzoSyncValue() {
        return versenyzoSyncValue;
    }

    public int getVersenySzamSyncValue() {
        return versenySzamSyncValue;
    }

    public int getTavSyncValue() {
        return tavSyncValue;
    }

    public Long getNevezesBegin() {
        return nevezesBegin;
    }

    public Long getNevezesEnd() {
        return nevezesEnd;
    }

    public Long getRaceDate() {
        return raceDate;
    }

    public void setPersonLapSyncValue(int personLapSyncValue) {
        this.personLapSyncValue = personLapSyncValue;
    }

    public void setVersenyzoSyncValue(int versenyzoSyncValue) {
        this.versenyzoSyncValue = versenyzoSyncValue;
    }

    public void setVersenySzamSyncValue(int versenySzamSyncValue) {
        this.versenySzamSyncValue = versenySzamSyncValue;
    }

    public void setTavSyncValue(int tavSyncValue) {
        this.tavSyncValue = tavSyncValue;
    }

    public void setNevezesBegin(Long nevezesBegin) {
        this.nevezesBegin = nevezesBegin;
    }

    public void setNevezesEnd(Long nevezesEnd) {
        this.nevezesEnd = nevezesEnd;
    }

    public void setRaceDate(Long raceDate) {
        this.raceDate = raceDate;
    }

    public String getNevezesEmailSubject() {
        return nevezesEmailSubject;
    }

    public void setNevezesEmailSubject(String nevezesEmailSubject) {
        this.nevezesEmailSubject = nevezesEmailSubject;
    }

    public String getNevezesEmailText() {
        return nevezesEmailText;
    }

    public Integer getHelysziniNevezesOsszeg() {
        return helysziniNevezesOsszeg;
    }

    public void setHelysziniNevezesOsszeg(Integer helysziniNevezesOsszeg) {
        this.helysziniNevezesOsszeg = helysziniNevezesOsszeg;
    }

    public void setNevezesEmailText(String nevezesEmailText) {
        this.nevezesEmailText = nevezesEmailText;
    }

    public Boolean getTriatlonLicensz() {
        return triatlonLicensz;
    }

    public void setTriatlonLicensz(Boolean triatlonLicensz) {
        this.triatlonLicensz = triatlonLicensz;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Verseny verseny = (Verseny) o;

        return (id != null ? id.equals(verseny.id) : verseny.id == null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
