package net.himadri.scmt.client.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
public class Nev implements Serializable {
    @Id
    private String nev;
    private boolean ferfi;

    @SuppressWarnings({"UnusedDeclaration"})
    public Nev() {
    }

    public Nev(String nev, boolean ferfi) {
        this.nev = nev;
        this.ferfi = ferfi;
    }

    public String getNev() {
        return nev;
    }

    public boolean isFerfi() {
        return ferfi;
    }
}
