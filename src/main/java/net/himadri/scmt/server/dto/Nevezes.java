package net.himadri.scmt.server.dto;

import java.util.Map;

/**
 * Created by himadri on 2017. 05. 24..
 */
public class Nevezes {
    private final String id;
    private final String nev;
    private final boolean aktiv;
    private final boolean nyitva;
    private final Map<String, String> tavok;
    private final String emailText;
    private final boolean triatlonLicensz;
    private final String versenySzabalyzat;

    public Nevezes(String id, String nev, boolean aktiv, boolean nyitva, Map<String, String> tavok, String emailText, boolean triatlonLicensz, String versenySzabalyzat) {
        this.id = id;
        this.nev = nev;
        this.aktiv = aktiv;
        this.nyitva = nyitva;
        this.tavok = tavok;
        this.emailText = emailText;
        this.triatlonLicensz = triatlonLicensz;
        this.versenySzabalyzat = versenySzabalyzat;
    }

    public String getId() {
        return id;
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

    public String getVersenySzabalyzat() {
        return versenySzabalyzat;
    }
}
