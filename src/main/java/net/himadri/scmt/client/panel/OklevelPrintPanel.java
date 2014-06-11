package net.himadri.scmt.client.panel;

import com.google.gwt.dom.client.FormElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HasName;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.StackLayoutPanel;
import com.google.gwt.user.client.ui.SubmitButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
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
        StackLayoutPanel layoutPanel = new StackLayoutPanel(Style.Unit.EM);
        layoutPanel.setSize("100%", "260px");
        layoutPanel.add(createRedirectPanel(tavValasztoValaszto, PdfServiceType.TAV), "Táv", 4);
        layoutPanel.add(createRedirectPanel(versenySzamValaszto, PdfServiceType.VERSENYSZAM), "Versenyszám", 4);
        layoutPanel.add(createRedirectPanel(versenyzoValaszto, PdfServiceType.VERSENYZO), "Versenyző Név alapján", 4);
        raceNumberText.setSize("70px", "auto");
        raceNumberText.getElement().setPropertyString("placeholder", "rajtszám");
        layoutPanel.add(createRedirectPanel(raceNumberText, PdfServiceType.VERSENYZO), "Versenyző Rajt alapján", 4);
        initWidget(layoutPanel);
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

    private Panel createRedirectPanel(Widget idWidget, PdfServiceType pdfServiceType) {
        final FormPanel formPanel = new FormPanel();
        formPanel.setAction(PRE_PRINTED_PDFSERVICE);
        formPanel.setMethod(FormPanel.METHOD_GET);
        formPanel.getElement().<FormElement>cast().setTarget("_blank");
        HorizontalPanel panel = new HorizontalPanel();
        panel.setSpacing(10);
        final Hidden versenyIdHidden = new Hidden("versenyId");
        ((HasName)idWidget).setName("id");
        panel.add(idWidget);
        SubmitButton submitButton = new SubmitButton("Nyomtat");
        submitButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                versenyIdHidden.setValue(scmtMarathon.getVerseny().getId().toString());
            }
        });
        panel.add(submitButton);
        panel.add(new Hidden("type", pdfServiceType.name()));
        panel.add(versenyIdHidden);
        formPanel.add(panel);
        return formPanel;
    }
}
