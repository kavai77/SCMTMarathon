package net.himadri.scmt.client.dialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import net.himadri.scmt.client.*;
import net.himadri.scmt.client.entity.Tav;
import net.himadri.scmt.client.entity.VersenySzam;
import net.himadri.scmt.client.entity.Versenyzo;
import net.himadri.scmt.client.exception.AlreadyExistingEntityException;
import net.himadri.scmt.client.exception.NotExistingEntityException;
import net.himadri.scmt.client.serializable.MarathonActionListener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class VersenyzoEntryDialog extends DialogBox {
    private MarathonServiceAsync service = GWT.create(MarathonService.class);

    private MultiWordSuggestOracle egyesuletOracle = new MultiWordSuggestOracle();

    private TextBox rajtszamText = new TextBox();
    private TextBox nevTextBox = new TextBox();
    private SuggestBox egyesuletText = new SuggestBox(egyesuletOracle);
    private TextBox emailTextBox = new TextBox();
    private IntegerBox szuletesiEvText = new IntegerBox();
    private RadioButton ferfiRadio = new RadioButton("nemRadio", "Férfi");
    private RadioButton noRadio = new RadioButton("nemRadio", "Nő");
    private CheckBox versenyszamSzuresKikapcs = new CheckBox("Versenyszámok szűrésének kikapcsolása");

    private ListBox versenySzamComboBox = new ListBox();
    private Versenyzo versenyzo;
    private SCMTMarathon scmtMarathon;

    public VersenyzoEntryDialog(final SCMTMarathon scmtMarathon) {
        this.scmtMarathon = scmtMarathon;
        setHTML("Versenyző");
        setAnimationEnabled(true);

        BlurHandler filterVersenySzamBlurHandler = new BlurHandler() {
            @Override
            public void onBlur(BlurEvent blurEvent) {
                filterVersenySzam();
            }
        };

        AbsolutePanel absolutePanel = new AbsolutePanel();
        setWidget(absolutePanel);
        absolutePanel.setSize("275px", "562px");

        Label lblRajtszm = new Label("Rajtszám *");
        absolutePanel.add(lblRajtszm, 10, 10);

        absolutePanel.add(rajtszamText, 10, 34);
        rajtszamText.setSize("96px", "18px");
        rajtszamText.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                Versenyzo existingVersenyzo = scmtMarathon.getVersenyzoMapCache().getVersenyzo(rajtszamText.getText());
                if (existingVersenyzo == null) {
                    filterVersenySzam();
                } else {
                    if (Window.confirm("Ezzel a rajtszámmal már létezik versenyző:\n" + existingVersenyzo.getName() +
                            "\nSzületési év: " + existingVersenyzo.getSzuletesiEv() +
                            "\nEgyesület: " + existingVersenyzo.getEgyesulet() +
                            "\nEmail: " + existingVersenyzo.getEmail() +
                            "\nVersenyszám: " + Utils.getVersenySzamMegnevezes(scmtMarathon, existingVersenyzo.getVersenySzamId()) +
                            "\nMódosítsuk?")) {
                        showDialog(existingVersenyzo);
                    } else {
                        hide();
                    }
                }
            }
        });

        Label lblNv = new Label("Név *");
        absolutePanel.add(lblNv, 10, 74);

        absolutePanel.add(nevTextBox, 10, 98);
        nevTextBox.setSize("240px", "18px");
        nevTextBox.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent blurEvent) {
                String nev = nevTextBox.getText().trim();
                if (!nev.isEmpty()) {
                    String[] nevek = nev.split(" ");
                    String keresztNev = nevek[nevek.length - 1];
                    service.isFerfiNev(keresztNev, new AsyncCallback<Boolean>() {
                        @Override
                        public void onFailure(Throwable throwable) {
                            SCMTMarathon.commonFailureHandling(throwable);
                        }

                        @Override
                        public void onSuccess(Boolean ferfi) {
                            if (Boolean.TRUE.equals(ferfi)) {
                                ferfiRadio.setValue(true, true);
                            } else if (Boolean.FALSE.equals(ferfi)) {
                                noRadio.setValue(true, true);
                            }
                            if (ferfi != null) {
                                filterVersenySzam();
                                szuletesiEvText.setFocus(true);
                            }
                        }
                    });
                }
            }
        });

        Label lblNem = new Label("Nem *");
        absolutePanel.add(lblNem, 10, 153);

        ClickHandler nemRadioClickHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                filterVersenySzam();
            }
        };

        absolutePanel.add(ferfiRadio, 52, 153);
        ferfiRadio.addClickHandler(nemRadioClickHandler);

        absolutePanel.add(noRadio, 109, 153);
        noRadio.addClickHandler(nemRadioClickHandler);

        Label lblletkoraVerseny = new Label("Születési év");
        absolutePanel.add(lblletkoraVerseny, 10, 192);

        absolutePanel.add(szuletesiEvText, 10, 216);
        szuletesiEvText.setSize("100px", "18px");
        szuletesiEvText.addBlurHandler(filterVersenySzamBlurHandler);

        Label lblEgyeslet = new Label("Egyesület");
        absolutePanel.add(lblEgyeslet, 10, 259);

        absolutePanel.add(egyesuletText, 10, 283);
        egyesuletText.setSize("240px", "18px");

        Label lblEmail = new Label("Email");
        absolutePanel.add(lblEmail, 10, 335);

        absolutePanel.add(emailTextBox, 10, 362);
        emailTextBox.setSize("240px", "18px");

        Label lblVersenyszm = new Label("Versenyszám *");
        absolutePanel.add(lblVersenyszm, 10, 411);

        absolutePanel.add(versenySzamComboBox, 10, 440);
        versenySzamComboBox.setSize("250px", "22px");

        versenyszamSzuresKikapcs.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> booleanValueChangeEvent) {
                filterVersenySzam();
            }
        });
        absolutePanel.add(versenyszamSzuresKikapcs, 10, 472);

        Button btnOk = new ImageButton("button_ok.png", "OK");
        absolutePanel.add(btnOk, 27, 519);
        btnOk.setSize("100px", "33px");

        Button btnMgsem = new ImageButton("button_cancel.png", "Mégsem");
        absolutePanel.add(btnMgsem, 148, 519);
        btnMgsem.setSize("100px", "33px");
        
        btnOk.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                ok();
            }
        });
        btnMgsem.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                hide();
            }
        });

        KeyPressHandler enterHandler = new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent keyPressEvent) {
                if (keyPressEvent.getCharCode() == KeyCodes.KEY_ENTER) {
                    ok();
                }
            }
        };
        rajtszamText.addKeyPressHandler(enterHandler);
        nevTextBox.addKeyPressHandler(enterHandler);
        szuletesiEvText.addKeyPressHandler(enterHandler);
        versenySzamComboBox.addKeyPressHandler(enterHandler);
        scmtMarathon.getPollingService().getVersenyzoSync().addMarathonActionListener(new VersenyzoListener());
    }

    private void filterVersenySzam() {
        List<VersenySzam> filterVersenySzamok = new ArrayList<VersenySzam>
                (scmtMarathon.getVersenyszamMapCache().getAllVersenySzam());
        if (!versenyszamSzuresKikapcs.getValue()) {
            // rajtszam filter
            try {
                int rajtszam = Integer.parseInt(rajtszamText.getText());
                for (Iterator<VersenySzam> versenySzamIterator = filterVersenySzamok.iterator(); versenySzamIterator.hasNext();) {
                    VersenySzam versenySzam = versenySzamIterator.next();
                    if (versenySzam.getTavId() != null) {
                        Tav tav = scmtMarathon.getTavMapCache().getTav(versenySzam.getTavId());
                        if (tav.getVersenySzamtol() != null && tav.getVersenySzamtol() > rajtszam ||
                            tav.getVersenySzamig() != null && tav.getVersenySzamig() < rajtszam) {
                            versenySzamIterator.remove();
                        }
                    }
                }
            } catch (NumberFormatException ignored) {
            }

            // életkor filter
            Integer szuletesiEv = szuletesiEvText.getValue();
            if (szuletesiEv != null) {
                int eletkor = getCurrentYear() - szuletesiEv;
                for (Iterator<VersenySzam> versenySzamIterator = filterVersenySzamok.iterator(); versenySzamIterator.hasNext();) {
                    VersenySzam versenySzam = versenySzamIterator.next();
                    if (versenySzam.getKorTol() != null && versenySzam.getKorTol() > eletkor ||
                        versenySzam.getKorIg() != null && versenySzam.getKorIg() < eletkor) {
                        versenySzamIterator.remove();
                    }
                }
            }

            // nem filter
            if (isFerfi() != null) {
                for (Iterator<VersenySzam> versenySzamIterator = filterVersenySzamok.iterator(); versenySzamIterator.hasNext();) {
                    VersenySzam versenySzam = versenySzamIterator.next();
                    if (noRadio.getValue().equals(versenySzam.getFerfi())) {
                        versenySzamIterator.remove();
                    }
                }
            }
        }

        long selectedVersenySzam = getSelectedVersenySzamId();
        versenySzamComboBox.clear();
        versenySzamComboBox.setEnabled(!filterVersenySzamok.isEmpty());
        if (filterVersenySzamok.isEmpty()) {
            versenySzamComboBox.addItem("(nincs megfelelő versenyszám)", "0");
        }
        if (filterVersenySzamok.size() > 1) {
            versenySzamComboBox.addItem("(válassz versenyszámot)", "0");
        }
        for (VersenySzam versenySzam: filterVersenySzamok) {
            versenySzamComboBox.addItem(Utils.getVersenySzamMegnevezes(scmtMarathon, versenySzam), Long.toString(versenySzam.getId()));
        }
        versenySzamComboBox.setSelectedIndex(getVersenySzamComboIndex(selectedVersenySzam));

    }

    @SuppressWarnings("deprecation")
    private int getCurrentYear() {
        return new Date().getYear() + 1900;
    }

    private void ok() {
        String validationResult = validateFields();
        if (validationResult != null) {
            Window.alert(validationResult);
            return;
        }
        applyValues();
        if (versenyzo.getId() == null) {
            service.addVersenyzo(versenyzo.getRaceNumber(),
                    versenyzo.getName(), versenyzo.getFerfi(), versenyzo.getSzuletesiEv(),
                    versenyzo.getEgyesulet(), versenyzo.getEmail(), versenyzo.getVersenySzamId(),
                    versenyzo.getVersenyId(), new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable throwable) {
                    if (throwable instanceof AlreadyExistingEntityException) {
                        Window.alert("Ezzel a rajtszámmal már létezik versenyző. Nem vihető fel újra.");
                    } else {
                        SCMTMarathon.commonFailureHandling(throwable);
                    }
                }

                @Override
                public void onSuccess(Void aVoid) {
                    hide();
                }
            });
        } else {
            service.modifyVersenyzo(versenyzo, new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable throwable) {
                    if (throwable instanceof NotExistingEntityException) {
                        Window.alert("Nem sikerült a módosítás, mivel ezt a versenyzőt időközben már valaki törölt vagy módosította a rajtszámát.");
                    } else if (throwable instanceof AlreadyExistingEntityException) {
                        Window.alert("Ezzel a rajtszámmal már létezik versenyző. Nem vihető fel újra.");
                    } else {
                        SCMTMarathon.commonFailureHandling(throwable);
                    }
                }

                @Override
                public void onSuccess(Void aVoid) {
                    hide();
                }
            });
        }
    }

    public void showDialog(Versenyzo versenyzo) {
        this.versenyzo = versenyzo;
        versenyszamSzuresKikapcs.setValue(false, false);
        if (versenyzo == null) {
            this.versenyzo = new Versenyzo();
            rajtszamText.setText(null);
            nevTextBox.setText(null);
            ferfiRadio.setValue(false);
            noRadio.setValue(false);
            szuletesiEvText.setText(null);
            egyesuletText.setText(null);
            emailTextBox.setText(null);
            filterVersenySzam();
            versenySzamComboBox.setSelectedIndex(0);
        } else {
            rajtszamText.setText(versenyzo.getRaceNumber());
            nevTextBox.setText(versenyzo.getName());
            ferfiRadio.setValue(versenyzo.getFerfi() != null && versenyzo.getFerfi());
            noRadio.setValue(versenyzo.getFerfi() != null && !versenyzo.getFerfi());
            szuletesiEvText.setText(versenyzo.getSzuletesiEv() != null ? versenyzo.getSzuletesiEv().toString() : null);
            egyesuletText.setText(versenyzo.getEgyesulet());
            emailTextBox.setText(versenyzo.getEmail());
            filterVersenySzam();
            versenySzamComboBox.setSelectedIndex(getVersenySzamComboIndex(versenyzo.getVersenySzamId()));
        }
        center();
        rajtszamText.setFocus(true);
    }

    private int getVersenySzamComboIndex(Long value) {
        String strValue = value.toString();
        for (int i = 1; i < versenySzamComboBox.getItemCount(); i++) {
            if (strValue.equals(versenySzamComboBox.getValue(i))) {
                return i;
            }
        }
        return 0;
    }

    private String validateFields() {
        if (rajtszamText.getText().trim().isEmpty()) {
            return "A rajtszám megadása kötelező!";
        }
        if (nevTextBox.getText().trim().isEmpty()) {
            return "A név megadása kötelező!";
        }
        if (isFerfi() == null) {
            return "A nem megadása kötelező!";
        }
        if (szuletesiEvText.getText().trim().isEmpty()) {
            return "Az születési év megadása kötelező!";
        }
        try {
            int szuletesiEvText = this.szuletesiEvText.getValueOrThrow();
            if (szuletesiEvText < 1900 || szuletesiEvText > getCurrentYear()) {
                return "A születési év rosszul van megadva!";
            }
        } catch (ParseException e) {
            return "A születési évnek számot kell megadni!";
        }
        if (getSelectedVersenySzamId() == 0) {
            return "A versenyszám megadása kötelező!";
        }
        if (!emailTextBox.getText().trim().isEmpty() && !emailTextBox.getText().contains("@")) {
            return "Helytelen email cím!";
        }
        return null;
    }

    private void applyValues() {
        versenyzo.setRaceNumber(rajtszamText.getText().trim());
        versenyzo.setName(nevTextBox.getText().trim());
        versenyzo.setFerfi(isFerfi());
        versenyzo.setSzuletesiEv(szuletesiEvText.getValue());
        versenyzo.setEgyesulet(egyesuletText.getText().trim());
        versenyzo.setEmail(emailTextBox.getText().trim());
        versenyzo.setVersenySzamId(getSelectedVersenySzamId());
        versenyzo.setVersenyId(scmtMarathon.getVerseny().getId());
    }

    private long getSelectedVersenySzamId() {
        if (versenySzamComboBox.getSelectedIndex() == -1) {
            return 0;
        }
        return Long.parseLong(versenySzamComboBox.getValue(versenySzamComboBox.getSelectedIndex()));
    }

    private Boolean isFerfi() {
        return ferfiRadio.getValue() || noRadio.getValue() ? ferfiRadio.getValue() : null;
    }

    private class VersenyzoListener implements MarathonActionListener<Versenyzo> {
        @Override
        public void itemAdded(List<Versenyzo> items) {
            for (Versenyzo versenyzo: items) {
                egyesuletOracle.add(versenyzo.getEgyesulet());
            }
        }

        @Override
        public void itemRefreshed(List<Versenyzo> items) {
            egyesuletOracle.clear();
            itemAdded(items);
        }
    }
}
