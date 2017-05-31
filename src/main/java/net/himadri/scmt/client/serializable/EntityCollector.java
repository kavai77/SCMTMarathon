package net.himadri.scmt.client.serializable;

import net.himadri.scmt.client.entity.*;

import java.io.Serializable;
import java.util.List;

public class EntityCollector implements Serializable {
    private List<Nev> nevList;
    private List<PageProfile> pageProfileList;
    private List<PersonLap> personLapList;
    private List<Tav> tavList;
    private List<Verseny> versenyList;
    private List<VersenySzam> versenySzamList;
    private List<Versenyzo> versenyzoList;
    private List<Configuration> configurationList;

    public List<Nev> getNevList() {
        return nevList;
    }

    public void setNevList(List<Nev> nevList) {
        this.nevList = nevList;
    }

    public List<PageProfile> getPageProfileList() {
        return pageProfileList;
    }

    public void setPageProfileList(List<PageProfile> pageProfileList) {
        this.pageProfileList = pageProfileList;
    }

    public List<PersonLap> getPersonLapList() {
        return personLapList;
    }

    public void setPersonLapList(List<PersonLap> personLapList) {
        this.personLapList = personLapList;
    }

    public List<Tav> getTavList() {
        return tavList;
    }

    public void setTavList(List<Tav> tavList) {
        this.tavList = tavList;
    }

    public List<Verseny> getVersenyList() {
        return versenyList;
    }

    public void setVersenyList(List<Verseny> versenyList) {
        this.versenyList = versenyList;
    }

    public List<VersenySzam> getVersenySzamList() {
        return versenySzamList;
    }

    public void setVersenySzamList(List<VersenySzam> versenySzamList) {
        this.versenySzamList = versenySzamList;
    }

    public List<Versenyzo> getVersenyzoList() {
        return versenyzoList;
    }

    public void setVersenyzoList(List<Versenyzo> versenyzoList) {
        this.versenyzoList = versenyzoList;
    }

    public List<Configuration> getConfigurationList() {
        return configurationList;
    }

    public void setConfigurationList(List<Configuration> configurationList) {
        this.configurationList = configurationList;
    }
}
