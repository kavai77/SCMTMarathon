package net.himadri.scmt.client.entity;

import com.googlecode.objectify.annotation.AlsoLoad;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.02. 21:09
 */
@Entity
public class Versenyzo implements Serializable, HasCreationTime {
    @SuppressWarnings({"UnusedDeclaration"})
    @Id
    private Long id;
    private String raceNumber;
    private String name;
    private Boolean ferfi;
    private Integer szuletesiEv;
    private String egyesulet;
    private String email;
    private Long versenySzamId;
    private Long versenyId;
    private boolean feladta;
    private boolean ellenorzott;
    private long creationTime;

    @SuppressWarnings({"UnusedDeclaration"})
    void transformEletkor(@AlsoLoad("eletkor") Integer eletkor) {
        if (eletkor != null) {
            szuletesiEv = 2012 - eletkor;
        }
    }


    public Versenyzo() {
    }


    public Versenyzo(String raceNumber, String name, Boolean ferfi, Integer szuletesiEv, String egyesulet, String email, Long versenySzamId, Long versenyId) {
        this.raceNumber = raceNumber;
        this.name = name;
        this.ferfi = ferfi;
        this.szuletesiEv = szuletesiEv;
        this.egyesulet = egyesulet;
        this.email = email;
        this.versenySzamId = versenySzamId;
        this.versenyId = versenyId;
        creationTime = System.currentTimeMillis();
    }

    public Long getId() {
        return id;
    }

    public String getRaceNumber() {
        return raceNumber;
    }

    public String getName() {
        return name;
    }

    public Boolean getFerfi() {
        return ferfi;
    }

    public Integer getSzuletesiEv() {
        return szuletesiEv;
    }

    public String getEgyesulet() {
        return egyesulet;
    }

    public String getEmail() {
        return email;
    }

    public Long getVersenySzamId() {
        return versenySzamId;
    }

    public Long getVersenyId() {
        return versenyId;
    }

    public boolean isFeladta() {
        return feladta;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setRaceNumber(String raceNumber) {
        this.raceNumber = raceNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFerfi(Boolean ferfi) {
        this.ferfi = ferfi;
    }

    public void setSzuletesiEv(Integer szuletesiEv) {
        this.szuletesiEv = szuletesiEv;
    }

    public void setEgyesulet(String egyesulet) {
        this.egyesulet = egyesulet;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setVersenySzamId(Long versenySzamId) {
        this.versenySzamId = versenySzamId;
    }

    public void setVersenyId(Long versenyId) {
        this.versenyId = versenyId;
    }

    public void setFeladta(boolean feladta) {
        this.feladta = feladta;
    }

    public boolean isEllenorzott() {
        return ellenorzott;
    }

    public void setEllenorzott(boolean ellenorzott) {
        this.ellenorzott = ellenorzott;
    }
}
