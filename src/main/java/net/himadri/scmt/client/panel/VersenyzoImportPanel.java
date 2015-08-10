package net.himadri.scmt.client.panel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import net.himadri.scmt.client.SCMTMarathon;
import net.himadri.scmt.client.VersenyzoCSVUploadService;
import net.himadri.scmt.client.VersenyzoCSVUploadServiceAsync;
import net.himadri.scmt.client.callback.CommonAsyncCallback;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.07.10. 22:17
 */
public class VersenyzoImportPanel extends Composite {


    public VersenyzoImportPanel(final SCMTMarathon scmtMarathon) {
        final VersenyzoCSVUploadServiceAsync uploadServiceAsync = GWT.create(VersenyzoCSVUploadService.class);
        AbsolutePanel importPanel = new AbsolutePanel();
        importPanel.setSize("900px", "500px");
        final Label csvFormatLabel = new Label("CSV formátum: ");
        importPanel.add(csvFormatLabel, 10, 10);
        final Label resultLabel = new Label("Eredmény");
        resultLabel.setVisible(false);
        importPanel.add(resultLabel, 370, 10);
        final TextArea nevArea = new TextArea();
        nevArea.setSize("300px", "400px");
        importPanel.add(nevArea, 10, 50);
        final ScrollPanel scrollPanel = new ScrollPanel();
        scrollPanel.setSize("500px", "400px");
        importPanel.add(scrollPanel, 370, 50);

        Button elkuldButton = new Button("Mentés", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                uploadServiceAsync.importVersenyzok(scmtMarathon.getVerseny().getId(), nevArea.getText(),
                        new CommonAsyncCallback<String>() {
                            @Override
                            public void onSuccess(String result) {
                                HTMLPanel htmlPanel = new HTMLPanel(result);
                                scrollPanel.setWidget(htmlPanel);
                                resultLabel.setVisible(true);
                            }
                        });
            }
        });
        importPanel.add(elkuldButton, 10, 470);

        uploadServiceAsync.getHeader(new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                csvFormatLabel.setText("CSV betöltés");
            }

            @Override
            public void onSuccess(String result) {
                csvFormatLabel.setText("CSV formátum: " + result);
            }
        });

        initWidget(importPanel);
    }
}
