package net.himadri.scmt.client.panel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import net.himadri.scmt.client.*;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.07.10. 22:17
 */
public class VersenyzoImportPanel extends Composite {
    public VersenyzoImportPanel(final SCMTMarathon scmtMarathon) {
        AbsolutePanel importPanel = new AbsolutePanel();
        importPanel.setSize("900px", "500px");
        final TextArea nevArea = new TextArea();
        nevArea.setSize("300px", "400px");
        importPanel.add(nevArea, 10, 10);
        final ScrollPanel scrollPanel = new ScrollPanel();
        scrollPanel.setSize("500px", "400px");
        importPanel.add(scrollPanel, 370, 10);

        Button elkuldButton = new Button("Mentés", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                VersenyzoCSVUploadServiceAsync uploadServiceAsync = GWT.create(VersenyzoCSVUploadService.class);
                uploadServiceAsync.importVersenyzok(scmtMarathon.getVerseny().getId(), nevArea.getText(),
                        new AsyncCallback<String>() {
                            @Override
                            public void onFailure(Throwable throwable) {
                                SCMTMarathon.commonFailureHandling(throwable);
                            }

                            @Override
                            public void onSuccess(String result) {
                                HTMLPanel htmlPanel = new HTMLPanel(result);
                                scrollPanel.setWidget(htmlPanel);
                            }
                        });
            }
        });
        importPanel.add(elkuldButton, 10, 470);
        initWidget(importPanel);

    }
}
