package net.himadri.scmt.client.dialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import net.himadri.scmt.client.ImageButton;
import net.himadri.scmt.client.MarathonService;
import net.himadri.scmt.client.MarathonServiceAsync;
import net.himadri.scmt.client.SCMTMarathon;
import net.himadri.scmt.client.entity.Tav;

import java.text.ParseException;

public class TavEntryDialog extends DialogBox {
    private MarathonServiceAsync marathonService = GWT.create(MarathonService.class);

    private Long currentId;
    private SCMTMarathon scmtMarathon;
    private Label tavMuveletLabel;
    private TextBox megnevezesText;
	private IntegerBox korokSzamaText;
    private IntegerBox versenySzamtolText = new IntegerBox();
    private IntegerBox versenySzamigText = new IntegerBox();

	public TavEntryDialog(SCMTMarathon scmtMarathon) {
        this.scmtMarathon = scmtMarathon;
		setHTML("Táv");
		setAnimationEnabled(true);
		
		AbsolutePanel absolutePanel = new AbsolutePanel();
		setWidget(absolutePanel);
		absolutePanel.setSize("335px", "295px");
		
		tavMuveletLabel = new Label("Táv művelet");
		absolutePanel.add(tavMuveletLabel, 10, 10);
		
		Label lblTvMegnevezse = new Label("Táv megnevezése");
		absolutePanel.add(lblTvMegnevezse, 10, 40);

        KeyPressHandler enterOkKeyPressHandler = new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getCharCode() == KeyCodes.KEY_ENTER) {
                    ok();
                }
            }
        };
		
		megnevezesText = new TextBox();
		absolutePanel.add(megnevezesText, 10, 64);
		megnevezesText.setSize("301px", "18px");
        megnevezesText.addKeyPressHandler(enterOkKeyPressHandler);
		
		Label korokSzamaLabel = new Label("Körök száma");
		absolutePanel.add(korokSzamaLabel, 10, 111);
		
		korokSzamaText = new IntegerBox();
		absolutePanel.add(korokSzamaText, 10, 135);
		korokSzamaText.setSize("48px", "18px");
        korokSzamaText.addKeyPressHandler(enterOkKeyPressHandler);

        Label lblVersenyszm = new Label("Versenyszám kiosztás");
        absolutePanel.add(lblVersenyszm, 10, 177);

        absolutePanel.add(versenySzamtolText, 8, 201);
        versenySzamtolText.setSize("48px", "18px");
        versenySzamtolText.addKeyPressHandler(enterOkKeyPressHandler);

        Label label = new Label("-");
        absolutePanel.add(label, 72, 211);

        absolutePanel.add(versenySzamigText, 82, 201);
        versenySzamigText.setSize("48px", "18px");
        versenySzamigText.addKeyPressHandler(enterOkKeyPressHandler);
		
		Button btnOk = new ImageButton("button_ok.png", "OK", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                ok();
            }
        });
		absolutePanel.add(btnOk, 57, 252);
		btnOk.setSize("100px", "33px");
		
		Button btnMgsem = new ImageButton("button_cancel.png", "Mégsem", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                hide();
            }
        });
		absolutePanel.add(btnMgsem, 187, 252);
		btnMgsem.setSize("100px", "33px");
	}

    private void ok() {
        if (isValid()) {
            String megnevezes = megnevezesText.getText();
            Integer korokSzama = korokSzamaText.getValue();
            Integer versenySzamtol = versenySzamtolText.getValue();
            Integer versenySzamig = versenySzamigText.getValue();
            if (currentId == null) {
                marathonService.addTav(scmtMarathon.getVerseny().getId(), megnevezes, korokSzama, versenySzamtol, versenySzamig, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        SCMTMarathon.commonFailureHandling(throwable);
                    }

                    @Override
                    public void onSuccess(Void aVoid) {
                        hide();
                    }
                });
            } else {
                marathonService.modifyTav(currentId, megnevezes, korokSzama, versenySzamtol, versenySzamig, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        SCMTMarathon.commonFailureHandling(throwable);
                    }

                    @Override
                    public void onSuccess(Void aVoid) {
                        hide();
                    }
                });
            }
        }
    }

    public void showDialogForNew() {
        currentId = null;
        tavMuveletLabel.setText("Új táv felvitele:");
        megnevezesText.setText(null);
        korokSzamaText.setText(null);
        versenySzamtolText.setText(null);
        versenySzamigText.setText(null);
        center();
        megnevezesText.setFocus(true);
    }

    public void showDialogForModify(Long currentId, Tav tav) {
        this.currentId = currentId;
        tavMuveletLabel.setText("Táv módosítása:");
        megnevezesText.setText(tav.getMegnevezes());
        korokSzamaText.setValue(tav.getKorSzam());
        versenySzamtolText.setValue(tav.getVersenySzamtol());
        versenySzamigText.setValue(tav.getVersenySzamig());
        center();
        megnevezesText.setFocus(true);
    }

    private boolean isValid() {
        if (megnevezesText.getText().trim().isEmpty()) {
            Window.alert("Kötelező megadni a táv megnevezését!");
            return false;
        }
        try {
            Integer korokSzama = korokSzamaText.getValueOrThrow();
            if (korokSzama == null) {
                Window.alert("Kötelező megadni a körök számát!");
                return false;
            }
            if (korokSzama <= 0) {
                Window.alert("A körök számának pozitív egész számnak kell lennie!");
                return false;
            }
        } catch (ParseException e) {
            Window.alert("A körök számának egész számnak kell lennie!");
            return false;
        }
        try {
            Integer versenySzamtol = versenySzamtolText.getValueOrThrow();
            Integer versenySzamIg = versenySzamigText.getValueOrThrow();
            if (versenySzamtol != null && versenySzamIg == null || versenySzamtol == null && versenySzamIg != null) {
                Window.alert("A versenyszám kiosztásnál vagy mindkettő értéket kötelező megadni, vagy mindkettőt üresen kell hagyni!");
                return false;
            }
        } catch (ParseException e) {
            Window.alert("A versenyszám kiosztásnak egész számnak kell lennie!");
            return false;
        }
        return true;
    }
}
