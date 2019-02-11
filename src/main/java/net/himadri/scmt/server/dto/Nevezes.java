package net.himadri.scmt.server.dto;

import java.util.Map;

/**
 * Created by himadri on 2017. 05. 24..
 */
public class Nevezes {
    private final String nev;
    private final boolean aktiv;
    private final boolean nyitva;
    private final Map<String, String> tavok;
    private final String emailText;
    private final boolean triatlonLicensz;

    public Nevezes(String nev, boolean aktiv, boolean nyitva, Map<String, String> tavok, String emailText, boolean triatlonLicensz) {
        this.nev = nev;
        this.aktiv = aktiv;
        this.nyitva = nyitva;
        this.tavok = tavok;
        this.emailText = emailText;
        this.triatlonLicensz = triatlonLicensz;
    }

    public String getNev() {
        return nev;
    }

    public boolean isAktiv() {
        return aktiv;
    }

    public boolean isNyitva() {
        return nyitva;
    }

    public Map<String, String> getTavok() {
        return tavok;
    }

    public String getEmailText() {
        return emailText;
    }

    public boolean isTriatlonLicensz() {
        return triatlonLicensz;
    }
}
