package net.himadri.scmt.client.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.05.13. 7:09
 */

@Entity
public class Tav implements Serializable, HasCreationTime {
    @SuppressWarnings({"UnusedDeclaration"})
    @Id
    private Long id;
    private Long versenyId;
    private String megnevezes;
    private Integer korSzam;
    private Integer versenySzamtol, versenySzamig;
    private long raceStartDiff;
    private long creationTime;
    private String[] korNevArray = new String[]{};

    @SuppressWarnings({"UnusedDeclaration"})
    public Tav() {
    }

    public Tav(Long versenyId, String megnevezes, Integer korSzam, Integer versenySzamtol, Integer versenySzamig, long raceStartDiff, String[] korNevArray) {
        this.versenyId = versenyId;
        this.megnevezes = megnevezes;
        this.korSzam = korSzam;
        this.versenySzamtol = versenySzamtol;
        this.versenySzamig = versenySzamig;
        this.raceStartDiff = raceStartDiff;
        this.korNevArray = korNevArray;
        creationTime = System.currentTimeMillis();
    }

    public Long getId() {
        return id;
    }

    public Long getVersenyId() {
        return versenyId;
    }

    public String getMegnevezes() {
        return megnevezes;
    }

    public void setMegnevezes(String megnevezes) {
        this.megnevezes = megnevezes;
    }

    public Integer getKorSzam() {
        return korSzam;
    }

    public void setKorSzam(Integer korSzam) {
        this.korSzam = korSzam;
    }

    public Integer getVersenySzamtol() {
        return versenySzamtol;
    }

    public void setVersenySzamtol(Integer versenySzamtol) {
        this.versenySzamtol = versenySzamtol;
    }

    public Integer getVersenySzamig() {
        return versenySzamig;
    }

    public void setVersenySzamig(Integer versenySzamig) {
        this.versenySzamig = versenySzamig;
    }

    public long getRaceStartDiff() {
        return raceStartDiff;
    }

    public void setRaceStartDiff(long raceStartDiff) {
        this.raceStartDiff = raceStartDiff;
    }

    public String[] getKorNevArray() {
        return korNevArray;
    }

    public void setKorNevArray(String[] korNevArray) {
        this.korNevArray = korNevArray;
    }

    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public String toString() {
        return "Tav{" +
                "id=" + id +
                ", versenyId=" + versenyId +
                ", megnevezes='" + megnevezes + '\'' +
                ", korSzam=" + korSzam +
                ", versenySzamtol=" + versenySzamtol +
                ", versenySzamig=" + versenySzamig +
                ", raceStartDiff=" + raceStartDiff +
                ", creationTime=" + creationTime +
                ", korNevArray=" + Arrays.toString(korNevArray) +
                '}';
    }
}
