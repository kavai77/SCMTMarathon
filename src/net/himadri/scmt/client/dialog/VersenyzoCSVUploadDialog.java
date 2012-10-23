package net.himadri.scmt.client.dialog;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import net.himadri.scmt.client.SCMTMarathon;
import net.himadri.scmt.client.Utils;

/**
 * Created with IntelliJ IDEA.
 * User: ecsakva
 * Date: 2012.10.23.
 * Time: 9:30
 * To change this template use File | Settings | File Templates.
 */
public class VersenyzoCSVUploadDialog extends DialogBox {
    private SCMTMarathon scmtMarathon;
    private Hidden versenyIdHidden = new Hidden("versenyid", null);
    private FileUpload fileUpload = new FileUpload();

    public VersenyzoCSVUploadDialog(SCMTMarathon scmtMarathon) {
        super(false, true);
        this.scmtMarathon = scmtMarathon;
        setHTML("Versenyző CSV feltöltés");
        setAnimationEnabled(true);
        final FormPanel formPanel = new FormPanel("scmtmarathon/VersenyzoCSVUploadService");
        formPanel.setMethod(FormPanel.METHOD_POST);
        formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
        AbsolutePanel panel = new AbsolutePanel();
        panel.setSize("300px", "90px");
        formPanel.setWidget(panel);
        panel.add(versenyIdHidden);
        fileUpload.setName("uploadfile");
        panel.add(fileUpload, 10, 10);
        panel.add(new Button("Feltöltés", new ClickHandler() {
            public void onClick(ClickEvent event) {
                formPanel.submit();
            }
        }), 10, 50);
        panel.add(new Button("Mégsem", new ClickHandler() {
            public void onClick(ClickEvent event) {
                hide();
            }
        }), 100, 50);
        // Add an event handler to the form.
        formPanel.addSubmitHandler(new FormPanel.SubmitHandler() {
            public void onSubmit(FormPanel.SubmitEvent event) {
                if (Utils.isEmpty(fileUpload.getFilename())) {
                    Window.alert("Válassz ki egy fájlt a feltöltéshez!");
                    event.cancel();
                }
            }
        });
        formPanel.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
            public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
                hide();
            }
        });
        setWidget(formPanel);
    }

    public void showDialog() {
        versenyIdHidden.setValue(scmtMarathon.getVerseny().getId().toString());
        center();
    }
}
