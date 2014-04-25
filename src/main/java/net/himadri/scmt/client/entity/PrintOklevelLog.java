package net.himadri.scmt.client.entity;

import com.googlecode.objectify.Key;

import java.io.Serializable;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity public class PrintOklevelLog implements Serializable
{
    private Date dateTime;
    @Id private Long id;
    private String ip;
    private Key<Versenyzo> versenyzoKey;

    public PrintOklevelLog()
    {
    }

    public PrintOklevelLog(Date dateTime, String ip, Versenyzo versenyzo)
    {
        this.dateTime = dateTime;
        this.ip = ip;
        this.versenyzoKey = new Key<Versenyzo>(Versenyzo.class, versenyzo.getId());
    }

    public Date getDateTime()
    {
        return dateTime;
    }

    public Long getId()
    {
        return id;
    }

    public String getIp()
    {
        return ip;
    }

    public Key<Versenyzo> getVersenyzoKey()
    {
        return versenyzoKey;
    }
}
