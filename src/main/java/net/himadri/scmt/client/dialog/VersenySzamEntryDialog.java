package net.himadri.scmt.client.dialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import net.himadri.scmt.client.MarathonService;
import net.himadri.scmt.client.MarathonServiceAsync;
import net.himadri.scmt.client.SCMTMarathon;
import net.himadri.scmt.client.Utils;
import net.himadri.scmt.client.callback.CommonAsyncCallback;
import net.himadri.scmt.client.entity.Tav;
import net.himadri.scmt.client.entity.VersenySzam;
import net.himadri.scmt.client.gwtextras.ImageButton;
import net.himadri.scmt.client.serializable.MarathonActionListener;

import java.text.ParseException;
import java.util.List;

public class VersenySzamEntryDialog extends DialogBox {

    private MarathonServiceAsync marathonService = GWT.create(MarathonService.class);

    private Long currentId;
    private SCMTMarathon scmtMarathon;
    private Label lblVersenyszmMvelet;
    private ListBox tavComboBox;
    private RadioButton ferfiButton;
    private RadioButton noiButton;
    private IntegerBox evtolBox;
    private IntegerBox evigBox;

    public VersenySzamEntryDialog(SCMTMarathon scmtMarathon) {
        this.scmtMarathon = scmtMarathon;
        setHTML("Versenyszám");
        setAnimationEnabled(true);

        AbsolutePanel absolutePanel = new AbsolutePanel();
        setWidget(absolutePanel);
        absolutePanel.setSize("370px", "285px");

        lblVersenyszmMvelet = new Label("Versenyszám művelet");
        absolutePanel.add(lblVersenyszmMvelet, 10, 10);

        Label lblTv = new Label("Táv");
        absolutePanel.add(lblTv, 10, 45);

        tavComboBox = new ListBox();
        absolutePanel.add(tavComboBox, 10, 69);
        tavComboBox.setSize("346px", "22px");

        Label lblNem = new Label("Nem");
        absolutePanel.add(lblNem, 10, 113);

        ferfiButton = new RadioButton("nemRadio", "Férfi");
        absolutePanel.add(ferfiButton, 10, 137);

        noiButton = new RadioButton("nemRadio", "Női");
        absolutePanel.add(noiButton, 70, 137);

        Label lblKorosztly = new Label("Korosztály");
        absolutePanel.add(lblKorosztly, 10, 176);

        evtolBox = new IntegerBox();
        absolutePanel.add(evtolBox, 10, 200);
        evtolBox.setSize("39px", "16px");
        evtolBox.addKeyPressHandler(new EnterOkKeyPressHandler());

        Label lblvtl = new Label("évtől");
        absolutePanel.add(lblvtl, 59, 204);

        evigBox = new IntegerBox();
        absolutePanel.add(evigBox, 108, 200);
        evigBox.setSize("39px", "16px");
        evigBox.addKeyPressHandler(new EnterOkKeyPressHandler());

        Label lblvig = new Label("évig");
        absolutePanel.add(lblvig, 157, 204);
        lblvig.setSize("28px", "18px");

        Button btnOk = new ImageButton("button_ok.png", "OK", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                ok();
            }
        });
        absolutePanel.add(btnOk, 70, 250);
        btnOk.setSize("100px", "33px");

        Button btnMgsem = new ImageButton("button_cancel.png", "Mégsem", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                hide();
            }
        });
        absolutePanel.add(btnMgsem, 200, 250);
        btnMgsem.setSize("100px", "33px");

        scmtMarathon.getPollingService().getTavSync().addMarathonActionListener(new MarathonActionListener<Tav>() {
            @Override
            public void itemAdded(List<Tav> items) {
                for (Tav tav : items) {
                    tavComboBox.addItem(tav.getMegnevezes(), tav.getId().toString());
                }
            }

            @Override
            public void itemRefreshed(List<Tav> items) {
                tavComboBox.clear();
                tavComboBox.addItem("(válassz távot)", "0");
                itemAdded(items);
            }
        });
    }

    private void ok() {
        String validationResult = validateFields();
        if (validationResult != null) {
            Window.alert(validationResult);
            return;
        }
        Long tavId = Long.parseLong(tavComboBox.getValue(tavComboBox.getSelectedIndex()));
        Boolean ferfi = ferfiButton.getValue();
        Integer evtol = evtolBox.getValue();
        Integer evig = evigBox.getValue();

        // életkor átfedés ellenőrzése
        for (VersenySzam versenySzam: scmtMarathon.getVersenyszamMapCache().getAllVersenySzam()) {
            if (!versenySzam.getId().equals(currentId) &&
                tavId.equals(versenySzam.getTavId()) &&
                ferfi.equals(versenySzam.getFerfi()) &&
                    Utils.isKorAtfedes(versenySzam.getKorTol(), versenySzam.getKorIg(), evtol, evig)) {
                Window.alert("Átfedés van életkorban a következő már létező versenyszámmal: " +
                        Utils.getVersenySzamMegnevezes(scmtMarathon, versenySzam));
                return;
            }
        }

        if (currentId == null) {
            marathonService.addVersenySzam(scmtMarathon.getVerseny().getId(), tavId, ferfi, evtol, evig, new CommonAsyncCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    hide();
                }
            });
        } else {
            marathonService.modifyVersenySzam(currentId, tavId, ferfi, evtol, evig, new CommonAsyncCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    hide();
                }
            });
        }
    }

    public void showDialogForNew() {
        currentId = null;
        lblVersenyszmMvelet.setText("Új versenyszám:");
        tavComboBox.setSelectedIndex(0);
        ferfiButton.setValue(false);
        noiButton.setValue(false);
        evtolBox.setText(null);
        evigBox.setText(null);
        center();
        tavComboBox.setFocus(true);
    }

    public void showDialogForModify(Long currentId, VersenySzam versenySzam) {
        this.currentId = currentId;
        lblVersenyszmMvelet.setText("Versenyszám módosítása:");
        tavComboBox.setSelectedIndex(findTavIndex(versenySzam.getTavId()));
        Boolean ferfi = versenySzam.getFerfi();
        ferfiButton.setValue(ferfi != null && ferfi);
        noiButton.setValue(ferfi != null && !ferfi);
        evtolBox.setValue(versenySzam.getKorTol());
        evigBox.setValue(versenySzam.getKorIg());
        center();
        tavComboBox.setFocus(true);
    }

    private int findTavIndex(Long tavId) {
        if (tavId == null) return 0;
        for (int i = 1; i < tavComboBox.getItemCount(); i++) {
            if (tavComboBox.getValue(i).equals(tavId.toString())) {
                return i;
            }
        }
        return 0;
    }

    private String validateFields() {
        if (tavComboBox.getSelectedIndex() == 0) {
            return "Válaszd ki a távot!";
        }
        if (!ferfiButton.getValue() && !noiButton.getValue()) {
            return "Válaszd ki a nemet!";
        }
        try {
            Integer evtol = evtolBox.getValueOrThrow();
            if (evtol != null && evtol <= 0) {
                return "Az korosztály alsó értéke nem lehet nulla vagy kisebb. Viszont nem kötelező kitölteni, ami azt jelenti, nincs alsó korhatár";
            }
            Integer evig = evigBox.getValueOrThrow();
            if (evig != null && evig > 99) {
                return "Az korosztály felső értéke nem lehet 99-től több. Viszont nem kötelező kitölteni, ami azt jelenti, nincs felső korhatár";
            }
            if (evtol != null && evig != null && evtol > evig) {
                return "A korosztály alsó értéke nem lehet nagyobb felső értéktől";
            }
        } catch (ParseException e) {
            return "A korosztály értékei csak számok lehetnek";
        }

        return null;
    }

    private class EnterOkKeyPressHandler implements KeyPressHandler {
        @Override
        public void onKeyPress(KeyPressEvent keyPressEvent) {
            if (keyPressEvent.getCharCode() == KeyCodes.KEY_ENTER) {
                ok();
            }
        }
    }
}
