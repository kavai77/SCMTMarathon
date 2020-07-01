package net.himadri.scmt.client.panel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.datepicker.client.DatePicker;
import net.himadri.scmt.client.MarathonService;
import net.himadri.scmt.client.MarathonServiceAsync;
import net.himadri.scmt.client.SCMTMarathon;
import net.himadri.scmt.client.callback.CommonAsyncCallback;
import net.himadri.scmt.client.entity.Verseny;
import net.himadri.scmt.client.serializable.MarathonActionListener;

import java.util.Date;
import java.util.List;

/**
 * Created by himadri on 2017. 05. 24..
 */
public class NevezoPanel extends Composite {
    private final MarathonServiceAsync marathonService = GWT.create(MarathonService.class);

    public NevezoPanel(final SCMTMarathon scmtMarathon) {
        AbsolutePanel nevezoPanel = new AbsolutePanel();
        nevezoPanel.setSize("900px", "600px");
        nevezoPanel.add(new Label("Nevezés kezdete"), 10, 10);
        final DatePicker startDatePicker = new DatePicker();
        nevezoPanel.add(startDatePicker, 10, 30);
        startDatePicker.removeStyleFromDates("datePickerDayIsToday", new Date());
        nevezoPanel.add(new Label("Nevezés vége"), 270, 10);
        final DatePicker endDatePicker = new DatePicker();
        endDatePicker.removeStyleFromDates("datePickerDayIsToday", new Date());
        nevezoPanel.add(endDatePicker, 270, 30);

        nevezoPanel.add(new Label("Verseny napja"), 530, 10);
        final DatePicker raceDatePicker = new DatePicker();
        raceDatePicker.removeStyleFromDates("datePickerDayIsToday", new Date());
        nevezoPanel.add(raceDatePicker, 530, 30);

        nevezoPanel.add(new Label("Email tárgy"), 10, 230);
        final TextBox emailSubjectText = new TextBox();
        emailSubjectText.setWidth("500px");
        nevezoPanel.add(emailSubjectText, 10, 250);
        nevezoPanel.add(new Label("Email szöveg"), 10, 285);
        final TextArea emailBodyText = new TextArea();
        emailBodyText.setSize("500px", "130px");
        nevezoPanel.add(emailBodyText, 10, 305);
        nevezoPanel.add(new Label("Helyszíni nevezési díj"), 10, 460);
        final IntegerBox helysziniNevezes = new IntegerBox();
        helysziniNevezes.setWidth("50px");
        nevezoPanel.add(helysziniNevezes, 150, 460);
        nevezoPanel.add(new Label("Versenyszabályzat URL"), 10, 490);
        final TextBox versenySzabalyzat = new TextBox();
        versenySzabalyzat.setWidth("500px");
        versenySzabalyzat.setHeight("10px");
        nevezoPanel.add(versenySzabalyzat, 150, 490);
        final CheckBox triatlonLicensz = new CheckBox("Triatlon Licensz Kötelező");
        nevezoPanel.add(triatlonLicensz, 10, 520);
        final Anchor anchor = new Anchor("Link a nevezési felületre", "", "_blank");
        nevezoPanel.add(anchor, 10, 540);
        Button elkuldButton = new Button("Mentés", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                marathonService.setNevezesDatum(scmtMarathon.getVerseny().getId(),
                        startDatePicker.getValue().getTime(), endDatePicker.getValue().getTime(),
                        raceDatePicker.getValue().getTime(),
                        emailSubjectText.getText(), emailBodyText.getText(), helysziniNevezes.getValue(),
                        versenySzabalyzat.getText(), triatlonLicensz.getValue(),
                        new CommonAsyncCallback<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Window.alert("Sikeresen elmentve");
                            }
                        });
            }
        });
        nevezoPanel.add(elkuldButton, 10, 570);
        scmtMarathon.getVersenySyncSupport().addMarathonActionListener(
                new MarathonActionListener<Verseny>() {
            @Override
            public void itemAdded(List<Verseny> items) {
                itemRefreshed(items);
            }

            @Override
            public void itemRefreshed(List<Verseny> items) {
                Verseny verseny = scmtMarathon.getVerseny();
                Long nevezesBegin = verseny.getNevezesBegin();
                Long nevezesEnd = verseny.getNevezesEnd();
                Long raceDate = verseny.getRaceDate();
                if (nevezesBegin != null) {
                    Date date = new Date(nevezesBegin);
                    startDatePicker.setValue(date);
                    startDatePicker.setCurrentMonth(date);
                }
                if (nevezesEnd != null) {
                    Date date = new Date(nevezesEnd);
                    endDatePicker.setValue(date);
                    endDatePicker.setCurrentMonth(date);
                }
                if (raceDate != null) {
                    Date date = new Date(raceDate);
                    raceDatePicker.setValue(date);
                    raceDatePicker.setCurrentMonth(date);
                }
                emailSubjectText.setText(verseny.getNevezesEmailSubject());
                emailBodyText.setText(verseny.getNevezesEmailText());
                helysziniNevezes.setValue(verseny.getHelysziniNevezesOsszeg());
                versenySzabalyzat.setText(verseny.getVersenySzabalyzat());
                triatlonLicensz.setValue(verseny.getTriatlonLicensz());
                anchor.setHref("https://scmtmarathon.appspot.com/public/nevezes.html?id=" + verseny.getId());
            }
        });
        initWidget(nevezoPanel);
    }
}
