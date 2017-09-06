package net.himadri.scmt.client.panel;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import net.himadri.scmt.client.SCMTMarathon;
import net.himadri.scmt.client.entity.VersenySzam;
import net.himadri.scmt.client.entity.Versenyzo;
import net.himadri.scmt.client.serializable.MarathonActionListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class AbstractPrintPeopleRootPanel extends Composite {
    private VerticalPanel verticalPanel = new VerticalPanel();
    protected final SCMTMarathon scmtMarathon;
    private final String title;
    private final String historyToken;

    public AbstractPrintPeopleRootPanel(SCMTMarathon scmtMarathon, String title, String historyToken) {
        this.scmtMarathon = scmtMarathon;
        this.title = title;
        this.historyToken = historyToken;
        initWidget(verticalPanel);
        scmtMarathon.getPollingService().getVersenySzamSync().addMarathonActionListener(new RefreshSyncRequest<VersenySzam>());
        scmtMarathon.getPollingService().getVersenyzoSync().addMarathonActionListener(new RefreshSyncRequest<Versenyzo>());
    }

    public void showAllRunnersPanel() {
        verticalPanel.clear();
        int versenyzoLength = scmtMarathon.getVersenyzoMapCache().getAllVersenyzo().size();
        verticalPanel.add(new HTML("<h1>" + title + " (" + versenyzoLength + ")</h1>"));
        verticalPanel.add(createFlexTable());
    }

    private class RefreshSyncRequest<T> implements MarathonActionListener<T> {
        @Override
        public void itemAdded(List<T> items) {
            if (historyToken.equals(History.getToken())) {
                showAllRunnersPanel();
            }
        }

        @Override
        public void itemRefreshed(List<T> items) {
            itemAdded(items);
        }
    }

    private FlexTable createFlexTable() {
        FlexTable flexTable = new FlexTable();
        flexTable.setBorderWidth(1);
        flexTable.setCellPadding(5);
        flexTable.addStyleName("collapse");
        final String[] columnList = getColumnList();
        for (int i = 0; i < columnList.length; i++ ) {
            flexTable.setText(0, i, columnList[i]);
        }
        List<Versenyzo> allVersenyzo = new ArrayList<Versenyzo>(scmtMarathon.getVersenyzoMapCache().getAllVersenyzo());
        Collections.sort(allVersenyzo, getListComparator());
        for (int i = 0; i < allVersenyzo.size(); i++) {
            final String[] versenyzoData = getVersenyzoData(allVersenyzo.get(i));
            for (int col = 0; col < versenyzoData.length; col++) {
                flexTable.setText(i + 1, col, versenyzoData[col]);
            }
        }
        return flexTable;
    }

    protected abstract String[] getColumnList();

    protected abstract Comparator<Versenyzo> getListComparator();

    protected abstract String[] getVersenyzoData(Versenyzo versenyzo);
}
