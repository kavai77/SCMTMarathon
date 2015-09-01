package net.himadri.scmt.client.dialog;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;
import net.himadri.scmt.client.MarathonService;
import net.himadri.scmt.client.MarathonServiceAsync;
import net.himadri.scmt.client.SCMTMarathon;
import net.himadri.scmt.client.Utils;
import net.himadri.scmt.client.callback.CommonAsyncCallback;
import net.himadri.scmt.client.entity.Tav;
import net.himadri.scmt.client.gwtextras.ImageButton;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class TavEntryDialog extends DialogBox {
    private MarathonServiceAsync marathonService = GWT.create(MarathonService.class);

    private Long currentId;
    private SCMTMarathon scmtMarathon;
    private Label tavMuveletLabel;
    private TextBox megnevezesText = new TextBox();
    private IntegerBox korokSzamaText = new IntegerBox();
    private IntegerBox versenySzamtolText = new IntegerBox();
    private IntegerBox versenySzamigText = new IntegerBox();
    private TextBox futamIdoText = new TextBox();
    private ListDataProvider<TavKorNev> tavKorListDataProvider = new ListDataProvider<>();

    public TavEntryDialog(SCMTMarathon scmtMarathon) {
        this.scmtMarathon = scmtMarathon;
        setHTML("Táv");
        setAnimationEnabled(true);
        
        AbsolutePanel absolutePanel = new AbsolutePanel();
        setWidget(absolutePanel);
        absolutePanel.setSize("335px", "458px");
        
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
        
        absolutePanel.add(megnevezesText, 10, 64);
        megnevezesText.setSize("301px", "18px");
        megnevezesText.addKeyPressHandler(enterOkKeyPressHandler);
        
        Label korokSzamaLabel = new Label("Körök száma");
        absolutePanel.add(korokSzamaLabel, 10, 111);
        
        absolutePanel.add(korokSzamaText, 10, 135);
        korokSzamaText.setSize("48px", "18px");
        korokSzamaText.addKeyPressHandler(enterOkKeyPressHandler);
        korokSzamaText.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent keyPressEvent) {
                new Timer() {
                    @Override
                    public void run() {
                        adjustTavKorList();
                    }
                }.schedule(1000);
            }
        });

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

        Label futamIdoLabel = new Label("Futam ideje");
        absolutePanel.add(futamIdoLabel, 10, 243);

        absolutePanel.add(futamIdoText, 10, 267);
        futamIdoText.setSize("70px", "18px");
        futamIdoText.addKeyPressHandler(enterOkKeyPressHandler);
        
        Button btnOk = new ImageButton("button_ok.png", "OK", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                ok();
            }
        });
        absolutePanel.add(btnOk, 57, 418);
        btnOk.setSize("100px", "33px");
        
        Button btnMgsem = new ImageButton("button_cancel.png", "Mégsem", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                hide();
            }
        });
        absolutePanel.add(btnMgsem, 187, 418);
        btnMgsem.setSize("100px", "33px");

        ScrollPanel scrollPanel = new ScrollPanel();
        absolutePanel.add(scrollPanel, 0, 74);
        scrollPanel.setSize("315px", "100px");

        CellTable<TavKorNev> tavKorTable = new CellTable<>();
        scrollPanel.setWidget(tavKorTable);
        tavKorTable.setSize("100%", "100%");

        absolutePanel.add(scrollPanel, 10, 310);
        tavKorListDataProvider.addDataDisplay(tavKorTable);
        tavKorTable.addColumn(new TextColumn<TavKorNev>() {
            @Override
            public String getValue(TavKorNev tavKorNev) {
                return Integer.toString(tavKorNev.korSzam + 1);
            }
        }, "Kör");
        Column<TavKorNev, Boolean>  enabledColumn = new Column<TavKorNev, Boolean>(new CheckboxCell()) {
            @Override
            public Boolean getValue(TavKorNev tavKorNev) {
                return tavKorNev.enabled;
            }
        };
        enabledColumn.setFieldUpdater(new FieldUpdater<TavKorNev, Boolean>() {
            @Override
            public void update(int i, TavKorNev tavKorNev, Boolean enabled) {
                tavKorNev.enabled = enabled;
            }
        });
        tavKorTable.addColumn(enabledColumn, "Nyomtat");
        Column<TavKorNev, String>  korNevColumn = new Column<TavKorNev, String>(new EditTextCell()) {
            @Override
            public String getValue(TavKorNev tavKorNev) {
                return tavKorNev.nev;
            }
        };
        korNevColumn.setFieldUpdater(new FieldUpdater<TavKorNev, String>() {
            @Override
            public void update(int i, TavKorNev tavKorNev, String nev) {
                tavKorNev.nev = nev;
            }
        });
        tavKorTable.addColumn(korNevColumn, "Kör név");
    }

    private void ok() {
        if (isValid()) {
            String megnevezes = megnevezesText.getText();
            Integer korokSzama = korokSzamaText.getValue();
            Integer versenySzamtol = versenySzamtolText.getValue();
            Integer versenySzamig = versenySzamigText.getValue();
            String time = futamIdoText.getText();
            long raceStartDiff;
            try {
                raceStartDiff = (time != null && !time.isEmpty()) ? Utils.parseTime(time) : 0;
            } catch (ParseException e) {
                throw new RuntimeException("Should never happen");
            }

            if (currentId == null) {
                marathonService.addTav(scmtMarathon.getVerseny().getId(), megnevezes, korokSzama, versenySzamtol, versenySzamig, raceStartDiff, convertTavKorNevToArray(tavKorListDataProvider.getList()), new CommonAsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        hide();
                    }
                });
            } else {
                marathonService.modifyTav(currentId, megnevezes, korokSzama, versenySzamtol, versenySzamig, raceStartDiff, convertTavKorNevToArray(tavKorListDataProvider.getList()), new CommonAsyncCallback<Void>() {
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
        futamIdoText.setText(null);
        tavKorListDataProvider.setList(new ArrayList<TavKorNev>());
        adjustTavKorList();
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
        futamIdoText.setText(tav.getRaceStartDiff() == 0 ? null : Utils.getElapsedTimeString(tav.getRaceStartDiff()));
        tavKorListDataProvider.setList(convertArrayToTavKorNev(tav.getKorNevArray()));
        adjustTavKorList();
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
        try {
            String time = futamIdoText.getText();
            if (time != null && !time.isEmpty()) {
                Utils.parseTime(time);
            }
        } catch (ParseException e) {
            Window.alert("Az időt ilyen formátumba írhatod be: 12:32 vagy 1:12:32");
            return false;
        }
        for (TavKorNev tavKorNev: tavKorListDataProvider.getList()) {
            if (tavKorNev.enabled && Utils.isEmpty(tavKorNev.nev)) {
                Window.alert("Ha egy kör nyomtatása engedélyezett, akkor kötelező megnevezés adása.");
                return false;
            }
        }
        return true;
    }

    private void adjustTavKorList() {
        Integer korSzam = korokSzamaText.getValue();
        if (korSzam == null) return;
        List<TavKorNev> newList = new ArrayList<>(korSzam);
        for (int i = 0; i < korSzam; i++) {
            newList.add(null);
        }
        for (TavKorNev tavKorNev: tavKorListDataProvider.getList()) {
            if (tavKorNev.korSzam < korSzam) {
                tavKorNev.nev = Utils.defaultString(tavKorNev.nev);
                newList.set(tavKorNev.korSzam, tavKorNev);
            }
        }
        for (int i = 0; i < korSzam; i++) {
            if (newList.get(i) == null) {
                newList.set(i, new TavKorNev(i, false, ""));
            }
        }
        tavKorListDataProvider.setList(newList);
    }

    private static class TavKorNev {
        int korSzam;
        boolean enabled;
        String nev;

        public TavKorNev(int korSzam, boolean enabled, String nev) {
            this.korSzam = korSzam;
            this.enabled = enabled;
            this.nev = nev;
        }

    }

    private static String[] convertTavKorNevToArray(List<TavKorNev> tavKorNevList) {
        String[] korNevArrayArray = new String[tavKorNevList.size()];
        for (int i = 0; i < tavKorNevList.size(); i++) {
            TavKorNev tavKorNev = tavKorNevList.get(i);
            korNevArrayArray[i] = tavKorNev.enabled ? tavKorNev.nev : null;
        }
        return korNevArrayArray;
    }

    private static List<TavKorNev> convertArrayToTavKorNev(String[] korNevArray) {
        ArrayList<TavKorNev> tavKorNevArrayList = new ArrayList<>(korNevArray.length);
        for (int i = 0; i < korNevArray.length; i++) {
            tavKorNevArrayList.add(new TavKorNev(i, korNevArray[i] != null, korNevArray[i]));
        }
        return tavKorNevArrayList;
    }
}
