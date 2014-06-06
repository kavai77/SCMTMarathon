package net.himadri.scmt.client.panel;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import net.himadri.scmt.client.SCMTMarathon;
import net.himadri.scmt.client.TabChangeHandler;
import net.himadri.scmt.client.Utils;
import net.himadri.scmt.client.entity.Tav;
import net.himadri.scmt.client.entity.VersenySzam;
import net.himadri.scmt.client.entity.Versenyzo;
import net.himadri.scmt.client.serializable.PdfServiceType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Csaba Kávai
 */
public class OklevelPrintPanel extends Composite implements TabChangeHandler {
    public static final String PRE_PRINTED_PDFSERVICE = "/scmtmarathon/PrePrintedPDFService";
    public static final Logger LOGGER = Logger.getLogger(OklevelPrintPanel.class.getName());
    private SCMTMarathon scmtMarathon;
    private ListBox tavValasztoValaszto = new ListBox();
    private ListBox versenySzamValaszto = new ListBox();
    private ListBox versenyzoValaszto = new ListBox();
    private TextBox raceNumberText = new TextBox();

    public OklevelPrintPanel(SCMTMarathon scmtMarathon) {
        this.scmtMarathon = scmtMarathon;
        AbsolutePanel panel = new AbsolutePanel();
        panel.setSize("100%", "200px");
        panel.add(tavValasztoValaszto, 10, 10);
        panel.add(Utils.createRedirectButton(panel, "Oklevelek Távra", PRE_PRINTED_PDFSERVICE, new Hidden("type", PdfServiceType.TAV.name())), 300, 10);
        panel.add(versenySzamValaszto, 10, 50);
//        panel.add(Utils.createRedirectButton(panel, "Oklevelek Versenyszámra", PRE_PRINTED_PDFSERVICE, new Hidden("type", PdfServiceType.VERSENYSZAM.name()), createSelectedIdHidden(versenySzamValaszto)), 300, 50);
        panel.add(versenyzoValaszto, 10, 100);
//        panel.add(Utils.createRedirectButton(panel, "Oklevél Versenyzőnek Név alapján", PRE_PRINTED_PDFSERVICE, new Hidden("type", PdfServiceType.VERSENYZO.name()), createSelectedIdHidden(versenyzoValaszto)), 300, 100);
        panel.add(raceNumberText, 10, 150);
//        panel.add(Utils.createRedirectButton(panel, "Oklevél Versenyzőnek Rajtszám alapján", PRE_PRINTED_PDFSERVICE, new Hidden("type", PdfServiceType.VERSENYZO.name()), new Hidden("id", raceNumberText.getValue())), 300, 150);
        initWidget(panel);
    }

    @Override
    public void activated() {
        tavValasztoValaszto.clear();
        versenySzamValaszto.clear();
        versenyzoValaszto.clear();
        raceNumberText.setText(null);
        for (Tav tav: scmtMarathon.getTavMapCache().getAllTav()) {
            tavValasztoValaszto.addItem(tav.getMegnevezes(), tav.getId().toString());
        }
        for (VersenySzam versenySzam: scmtMarathon.getVersenyszamMapCache().getAllVersenySzamSorted()) {
            versenySzamValaszto.addItem(Utils.getVersenySzamMegnevezes(scmtMarathon, versenySzam), versenySzam.getId().toString());
        }
        List<Versenyzo> allVersenyzo = new ArrayList<Versenyzo>(scmtMarathon.getVersenyzoMapCache().getAllVersenyzo());
        Collections.sort(allVersenyzo, new Comparator<Versenyzo>() {
            @Override
            public int compare(Versenyzo o1, Versenyzo o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (Versenyzo versenyzo: allVersenyzo) {
            versenyzoValaszto.addItem(versenyzo.getName(), versenyzo.getRaceNumber());
        }
        
    }
    
    private Hidden createSelectedIdHidden(ListBox listBox) {
        return new Hidden("id", listBox.getValue(listBox.getSelectedIndex()));
    }
}
