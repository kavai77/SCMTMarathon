package net.himadri.scmt.client.entity;

import com.googlecode.objectify.Key;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

@Entity
public class PrintOklevelLog implements Serializable {
    @Id
    private Long id;
    private Date dateTime;
    private String ip;
    private Key<Versenyzo> versenyzoKey;

    public PrintOklevelLog() {
    }

    public PrintOklevelLog(Date dateTime, String ip, Versenyzo versenyzo) {
        this.dateTime = dateTime;
        this.ip = ip;
        this.versenyzoKey = new Key<>(Versenyzo.class, versenyzo.getId());
    }

    public Long getId() {
        return id;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public String getIp() {
        return ip;
    }

    public Key<Versenyzo> getVersenyzoKey() {
        return versenyzoKey;
    }
}
