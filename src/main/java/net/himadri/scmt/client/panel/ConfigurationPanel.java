package net.himadri.scmt.client.panel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import net.himadri.scmt.client.MarathonService;
import net.himadri.scmt.client.MarathonServiceAsync;
import net.himadri.scmt.client.Utils;
import net.himadri.scmt.client.callback.CommonAsyncCallback;
import net.himadri.scmt.client.entity.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by himadri on 2017. 05. 29..
 */
public class ConfigurationPanel extends Composite {
    private final MarathonServiceAsync marathonService = GWT.create(MarathonService.class);
    private final VerticalPanel rootPanel;
    private final Button newConfButton;
    private final Button saveConfButton;
    private final List<TextBox[]> textBoxes = new ArrayList<>();

    public ConfigurationPanel() {
        rootPanel = new VerticalPanel();
        rootPanel.setSpacing(10);
        rootPanel.addStyleName("centerWithMargin");

        newConfButton = new Button("Új konfiguráció", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                addConfigRow(false, new Configuration());
            }
        });
        saveConfButton = new Button("Mentés", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                List<Configuration> configurations = new ArrayList<>(textBoxes.size());
                for (TextBox[] textBoxPair: textBoxes) {
                    String key = textBoxPair[0].getText();
                    String value = textBoxPair[1].getText();
                    if (!Utils.isEmpty(key)) {
                        configurations.add(new Configuration(key, value));
                    }
                }
                marathonService.saveConfigurations(configurations, new CommonAsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Window.alert("Sikeresen elmentve!");
                    }
                });
            }
        });

        initWidget(rootPanel);
    }

    public void showPanel() {
        rootPanel.clear();
        HorizontalPanel panel = new HorizontalPanel();
        panel.setSpacing(10);
        panel.add(newConfButton);
        panel.add(saveConfButton);
        rootPanel.add(panel);
        marathonService.getConfigurations(new CommonAsyncCallback<List<Configuration>>() {
            @Override
            public void onSuccess(List<Configuration> configurations) {
                for (Configuration conf: configurations) {
                    addConfigRow(true, conf);
                }
            }
        });
    }

    private void addConfigRow(boolean keyReadOnly, Configuration conf) {
        HorizontalPanel panel = new HorizontalPanel();
        panel.setSpacing(10);
        TextBox keyBox = new TextBox();
        keyBox.setText(conf.getKey());
        keyBox.setWidth("300px");
        keyBox.setReadOnly(keyReadOnly);
        panel.add(keyBox);
        TextBox valueBox = new TextBox();
        valueBox.setWidth("300px");
        valueBox.setText(conf.getValue());
        panel.add(valueBox);
        rootPanel.add(panel);
        textBoxes.add(new TextBox[]{keyBox, valueBox});
    }
}
