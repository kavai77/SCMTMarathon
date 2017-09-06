package net.himadri.scmt.client.panel;

import net.himadri.scmt.client.SCMTMarathon;
import net.himadri.scmt.client.Utils;
import net.himadri.scmt.client.entity.Versenyzo;

import java.util.Comparator;

import static net.himadri.scmt.client.Utils.defaultString;

public class PrintPreRegistrationRootPanel extends AbstractPrintPeopleRootPanel {
    public static final String HISTORY_TOKEN = "RegistrationList";

    private static final String[] COLUMN_LIST = new String[]{
            "Rajtszám", "Név", "Nem", "Szül.év", "Korcsoport", "Egyesület", "Fizetett Díj", "Licensz",
            "Pólóméret", "Email"};

    public PrintPreRegistrationRootPanel(SCMTMarathon scmtMarathon) {
        super(scmtMarathon, "Regisztrációs lista", HISTORY_TOKEN);
    }

    @Override
    protected String[] getColumnList() {
        return COLUMN_LIST;
    }

    @Override
    protected Comparator<Versenyzo> getListComparator() {
        return new Comparator<Versenyzo>() {
            @Override
            public int compare(Versenyzo o1, Versenyzo o2) {
                return o1.getName().toUpperCase().compareTo(o2.getName().toUpperCase());
            }
        };
    }

    @Override
    protected String[] getVersenyzoData(Versenyzo versenyzo) {
        return new String[]{
            versenyzo.getRaceNumber(),
            versenyzo.getName(),
            Utils.getFerfiMegnevezes(versenyzo.getFerfi()),
            defaultString(versenyzo.getSzuletesiEv()),
            Utils.getVersenySzamMegnevezes(scmtMarathon, scmtMarathon.getVersenyszamMapCache().getVersenySzam(versenyzo.getVersenySzamId())),
            defaultString(versenyzo.getEgyesulet()),
            defaultString(versenyzo.getFizetettDij()),
            defaultString(versenyzo.getLicenszSzam()),
            defaultString(versenyzo.getPoloMeret()),
            defaultString(versenyzo.getEmail()),
        };
    }
}
