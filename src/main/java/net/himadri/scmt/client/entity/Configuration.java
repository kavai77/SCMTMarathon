package net.himadri.scmt.client.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by himadri on 2017. 05. 29..
 */
@Entity
public class Configuration implements Serializable {
    @Id
    private String key;
    private String value;

    public Configuration() {
    }

    public Configuration(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
