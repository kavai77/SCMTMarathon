package net.himadri.scmt.client.entity;

import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.07.10. 22:07
 */
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
