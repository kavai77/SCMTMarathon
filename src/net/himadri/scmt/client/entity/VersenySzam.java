package net.himadri.scmt.client.entity;

import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.01. 10:43
 */
public class VersenySzam implements Serializable, HasCreationTime {
    @SuppressWarnings({"UnusedDeclaration"})
    @Id
    private Long id;
    private Long versenyId;
    private Long tavId;
    private Boolean ferfi;
    private Integer korTol, korIg;
    private long creationTime;

    @SuppressWarnings({"UnusedDeclaration"})
    public VersenySzam() {
    }

    public VersenySzam(Long versenyId, Long tavId, Boolean ferfi, Integer korTol, Integer korIg) {
        this.versenyId = versenyId;
        this.tavId = tavId;
        this.ferfi = ferfi;
        this.korTol = korTol;
        this.korIg = korIg;
        creationTime = System.currentTimeMillis();
    }

    public Long getId() {
        return id;
    }

    public Long getVersenyId() {
        return versenyId;
    }

    public Long getTavId() {
        return tavId;
    }

    public void setTavId(Long tavId) {
        this.tavId = tavId;
    }

    public Boolean getFerfi() {
        return ferfi;
    }

    public void setFerfi(Boolean ferfi) {
        this.ferfi = ferfi;
    }

    public Integer getKorTol() {
        return korTol;
    }

    public void setKorTol(Integer korTol) {
        this.korTol = korTol;
    }

    public Integer getKorIg() {
        return korIg;
    }

    public void setKorIg(Integer korIg) {
        this.korIg = korIg;
    }

    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VersenySzam that = (VersenySzam) o;

        if (id != that.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
