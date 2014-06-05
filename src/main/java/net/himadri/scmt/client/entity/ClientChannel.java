package net.himadri.scmt.client.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Csaba Kávai
 */
@Entity
public class ClientChannel implements Serializable{
    @Id
    private Long channelId;
    private Date creationDate;
    private boolean connected;

    public ClientChannel() {
    }

    public ClientChannel(Date creationDate) {
        this.creationDate = creationDate;
    }

    public ClientChannel(Long channelId, Date creationDate, boolean connected) {
        this.channelId = channelId;
        this.creationDate = creationDate;
        this.connected = connected;
    }

    public Long getChannelId() {
        return channelId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientChannel channel = (ClientChannel) o;

        if (!channelId.equals(channel.channelId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return channelId.hashCode();
    }
}
