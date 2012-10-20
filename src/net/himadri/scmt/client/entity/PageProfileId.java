package net.himadri.scmt.client.entity;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.06.02. 14:39
 */
public enum PageProfileId {
    NEV("Név"),
    EGYESULET("Egyesület"),
    VERSENYSZAM("Versenyszám"),
    IDO("Idő"),
    HELYEZES("Helyezés");

    private String megnevezes;

    PageProfileId(String megnevezes) {
        this.megnevezes = megnevezes;
    }

    public String getMegnevezes() {
        return megnevezes;
    }
}
