package net.himadri.scmt.client.panel;

import com.google.gwt.dom.client.FormElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
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
        AbsolutePanel absolutePanel = new AbsolutePanel();
        absolutePanel.setSize("100%", "200px");
        absolutePanel.add(createRedirectPanel("Oklevelek Távra", tavValasztoValaszto, PdfServiceType.TAV), 10, 10);
        absolutePanel.add(createRedirectPanel("Oklevelek Versenyszámra", versenySzamValaszto, PdfServiceType.VERSENYSZAM), 10, 50);
        absolutePanel.add(createRedirectPanel("Oklevelek Versenyzőnek Név alapján", versenyzoValaszto, PdfServiceType.VERSENYZO), 10, 90);
        raceNumberText.setSize("70px", "auto");
        raceNumberText.getElement().setPropertyString("placeholder", "rajtszám");
        absolutePanel.add(createRedirectPanel("Oklevelek Versenyzőnek Rajtszám alapján", raceNumberText, PdfServiceType.VERSENYZO), 10, 130);
        initWidget(absolutePanel);
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

    private Panel createRedirectPanel(String name, Widget idWidget, PdfServiceType pdfServiceType) {
        final FormPanel formPanel = new FormPanel();
        formPanel.setAction(PRE_PRINTED_PDFSERVICE);
        formPanel.setMethod(FormPanel.METHOD_GET);
        formPanel.getElement().<FormElement>cast().setTarget("_blank");
        FlowPanel flowPanel = new FlowPanel();
        flowPanel.add(new Hidden("type", pdfServiceType.name()));
        final Hidden versenyIdHidden = new Hidden("versenyId");
        flowPanel.add(versenyIdHidden);
        ((HasName)idWidget).setName("id");
        flowPanel.add(idWidget);
        SubmitButton submitButton = new SubmitButton(name);
        submitButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                versenyIdHidden.setValue(scmtMarathon.getVerseny().getId().toString());
            }
        });
        flowPanel.add(submitButton);

        formPanel.add(flowPanel);
        return formPanel;
    }
}
