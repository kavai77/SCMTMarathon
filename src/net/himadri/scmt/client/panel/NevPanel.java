package net.himadri.scmt.client.panel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import net.himadri.scmt.client.MarathonService;
import net.himadri.scmt.client.MarathonServiceAsync;
import net.himadri.scmt.client.SCMTMarathon;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.07.10. 22:17
 */
public class NevPanel extends Composite {
    public NevPanel() {
        AbsolutePanel nevPanel = new AbsolutePanel();
        nevPanel.setSize("700px", "500px");
        final ListBox nemValaszto = new ListBox();
        nemValaszto.addItem("Férfi");
        nemValaszto.addItem("Nő");
        nevPanel.add(nemValaszto, 10, 10);
        final TextArea nevArea = new TextArea();
        nevArea.setSize("300px", "400px");
        nevPanel.add(nevArea, 10, 50);

        Button elkuldButton = new Button("Mentés", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                boolean ferfi = nemValaszto.getSelectedIndex() == 0;
                final String[] nevek = nevArea.getText().split("\n");
                MarathonServiceAsync marathonService = GWT.create(MarathonService.class);
                marathonService.saveNev(nevek, ferfi, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        SCMTMarathon.commonFailureHandling(throwable);
                    }

                    @Override
                    public void onSuccess(Void aVoid) {
                        Window.alert("Sikeresen elmentve " + nevek.length + " név.");
                        nevArea.setText(null);
                    }
                });
            }
        });
        nevPanel.add(elkuldButton, 10, 470);
        initWidget(nevPanel);

    }
}
