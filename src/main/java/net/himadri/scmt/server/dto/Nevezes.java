package net.himadri.scmt.server.dto;

import java.util.Map;

/**
 * Created by himadri on 2017. 05. 24..
 */
public class Nevezes {
    private final String nev;
    private final boolean aktiv;
    private final Map<String, String> tavok;
    private final String emailText;

    public Nevezes(String nev, boolean aktiv, Map<String, String> tavok, String emailText) {
        this.nev = nev;
        this.aktiv = aktiv;
        this.tavok = tavok;
        this.emailText = emailText;
    }

    public String getNev() {
        return nev;
    }

    public boolean isAktiv() {
        return aktiv;
    }

    public Map<String, String> getTavok() {
        return tavok;
    }

    public String getEmailText() {
        return emailText;
    }
}
