package net.himadri.scmt.client.panel;

import net.himadri.scmt.client.SCMTMarathon;
import net.himadri.scmt.client.Utils;
import net.himadri.scmt.client.entity.Versenyzo;

import java.util.Comparator;

import static net.himadri.scmt.client.Utils.defaultString;

public class PrintStartListRootPanel extends AbstractPrintPeopleRootPanel {
    public static final String HISTORY_TOKEN = "StartList";

    private static final String[] COLUMN_LIST = new String[]{
            "Rajtszám", "Név", "Szül.év", "Korcsoport", "Egyesület"};

    public PrintStartListRootPanel(SCMTMarathon scmtMarathon) {
        super(scmtMarathon, "Rajtlista", HISTORY_TOKEN);
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
                try {
                    Integer rajtszam1 = Integer.parseInt(o1.getRaceNumber());
                    Integer rajtszam2 = Integer.parseInt(o2.getRaceNumber());
                    return rajtszam1.compareTo(rajtszam2);
                } catch (NumberFormatException e) {
                    return o1.getRaceNumber().compareTo(o2.getRaceNumber());
                }
            }
        };
    }

    @Override
    protected String[] getVersenyzoData(Versenyzo versenyzo) {
        return new String[]{
            versenyzo.getRaceNumber(),
            versenyzo.getName(),
            defaultString(versenyzo.getSzuletesiEv()),
            Utils.getVersenySzamMegnevezes(scmtMarathon, scmtMarathon.getVersenyszamMapCache().getVersenySzam(versenyzo.getVersenySzamId())),
            defaultString(versenyzo.getEgyesulet())
        };
    }
}
