package net.himadri.scmt.server.dto;

/**
 * Created by himadri on 2017. 06. 05..
 */
public class NevezesRequest {
    private long tav;
    private String nev;
    private String nem;
    private int ev;
    private String egyesulet;
    private String email;
    private String licenszSzam;
    private String poloMeret;
    private String recaptcha;

    public long getTav() {
        return tav;
    }

    public void setTav(long tav) {
        this.tav = tav;
    }

    public String getNev() {
        return nev;
    }

    public void setNev(String nev) {
        this.nev = nev;
    }

    public String getNem() {
        return nem;
    }

    public void setNem(String nem) {
        this.nem = nem;
    }

    public int getEv() {
        return ev;
    }

    public void setEv(int ev) {
        this.ev = ev;
    }

    public String getEgyesulet() {
        return egyesulet;
    }

    public void setEgyesulet(String egyesulet) {
        this.egyesulet = egyesulet;
    }

    public String getLicenszSzam() {
        return licenszSzam;
    }

    public void setLicenszSzam(String licenszSzam) {
        this.licenszSzam = licenszSzam;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPoloMeret() {
        return poloMeret;
    }

    public void setPoloMeret(String poloMeret) {
        this.poloMeret = poloMeret;
    }

    public String getRecaptcha() {
        return recaptcha;
    }

    public void setRecaptcha(String recaptcha) {
        this.recaptcha = recaptcha;
    }

    @Override
    public String toString() {
        return "NevezesRequest{" +
                "tav=" + tav +
                ", nev='" + nev + '\'' +
                ", nem='" + nem + '\'' +
                ", ev=" + ev +
                ", egyesulet='" + egyesulet + '\'' +
                ", email='" + email + '\'' +
                ", licenszSzam='" + licenszSzam + '\'' +
                ", poloMeret='" + poloMeret + '\'' +
                ", recaptcha='" + recaptcha + '\'' +
                '}';
    }
}
