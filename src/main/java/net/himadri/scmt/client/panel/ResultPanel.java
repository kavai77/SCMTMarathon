package net.himadri.scmt.client.panel;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.*;
import net.himadri.scmt.client.SCMTMarathon;
import net.himadri.scmt.client.TavVersenySzam;
import net.himadri.scmt.client.Utils;
import net.himadri.scmt.client.entity.Tav;
import net.himadri.scmt.client.entity.VersenySzam;
import net.himadri.scmt.client.gwtextras.ImageButton;
import net.himadri.scmt.client.serializable.MarathonActionListener;
import net.himadri.scmt.client.token.TavVersenySzamToken;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.07.16. 21:44
 */
public class ResultPanel extends Composite
{
    private Button nyomtatasButton;
    private TextBox raceNumberFilterBox = new TextBox();
    private ResultTable resultPanel;
    private SCMTMarathon scmtMarathon;
    private ListBox versenySzamValaszto = new ListBox();

    public ResultPanel(SCMTMarathon scmtMarathon)
    {
        this.scmtMarathon = scmtMarathon;
        AbsolutePanel racePanel = new AbsolutePanel();

        racePanel.setSize("100%", "620px");
        versenySzamValaszto.setSize("190px", "27px");
        racePanel.add(versenySzamValaszto, 10, 10);
        versenySzamValaszto.addChangeHandler(new VersenySzamValasztoChangeHandler());
        raceNumberFilterBox.setSize("70px", "auto");
        raceNumberFilterBox.getElement().setPropertyString("placeholder", "rajtszám");
        racePanel.add(raceNumberFilterBox, 215, 10);
        raceNumberFilterBox.addKeyUpHandler(new KeyUpHandler()
            {
                @Override public void onKeyUp(KeyUpEvent keyUpEvent)
                {
                    String filter = raceNumberFilterBox.getText().trim();

                    if (filter.isEmpty())
                    {
                        new VersenySzamValasztoChangeHandler().onChange(null);
                    }
                    else
                    {
                        resultPanel.refilterRaceStatusRows(TavVersenySzam.createRaceNumber(filter));
                        nyomtatasButton.setVisible(false);
                        versenySzamValaszto.setSelectedIndex(versenySzamValaszto.getItemCount() - 1);
                    }
                }
            });
        nyomtatasButton = new ImageButton("fileprint.png", "Nyomatási kép", new ClickHandler()
                {
                    @Override public void onClick(ClickEvent clickEvent)
                    {
                        History.newItem(TavVersenySzamToken.encode(resultPanel.getFilter()));
                    }
                });
        racePanel.add(nyomtatasButton, 310, 10);
        nyomtatasButton.setVisible(false);
        resultPanel = new ResultTable(scmtMarathon, TavVersenySzam.createAllAcceptance());
        racePanel.add(resultPanel, 0, 60);
        scmtMarathon.getPollingService().getVersenySzamSync().addMarathonActionListener(new TabPanelActionListener<VersenySzam>());
        scmtMarathon.getPollingService().getTavSync().addMarathonActionListener(new TabPanelActionListener<Tav>());
        initWidget(racePanel);
    }

    private class TabPanelActionListener<T> implements MarathonActionListener<T>
    {
        @Override public void itemAdded(List<T> items)
        {
            refreshVersenySzamValaszto();
        }

        @Override public void itemRefreshed(List<T> items)
        {
            refreshVersenySzamValaszto();
        }

        private void refreshVersenySzamValaszto()
        {
            versenySzamValaszto.clear();
            versenySzamValaszto.addItem("Összes versenyző", TavVersenySzamToken.encode(TavVersenySzam.createAllAcceptance()));
            for (Tav tav : scmtMarathon.getTavMapCache().getAllTav())
            {
                versenySzamValaszto.addItem(tav.getMegnevezes() + " összes",
                    TavVersenySzamToken.encode(TavVersenySzam.createTav(tav.getId())));
            }

            for (VersenySzam versenySzam : scmtMarathon.getVersenyszamMapCache().getAllVersenySzamSorted())
            {
                versenySzamValaszto.addItem(Utils.getVersenySzamMegnevezes(scmtMarathon, versenySzam),
                    TavVersenySzamToken.encode(TavVersenySzam.createVersenyszamFilter(versenySzam.getId())));
            }

            versenySzamValaszto.addItem("Versenyszám szűrés", TavVersenySzamToken.encode(TavVersenySzam.createRaceNumber("")));
            resultPanel.refilterRaceStatusRows(TavVersenySzam.createAllAcceptance());
        }
    }

    private class VersenySzamValasztoChangeHandler implements ChangeHandler
    {
        @Override public void onChange(ChangeEvent changeEvent)
        {
            int selectedIndex = versenySzamValaszto.getSelectedIndex();
            String value = versenySzamValaszto.getValue(selectedIndex);
            TavVersenySzam tavVersenySzam = TavVersenySzamToken.decode(value);

            resultPanel.refilterRaceStatusRows(tavVersenySzam);
            nyomtatasButton.setVisible(tavVersenySzam.getMode().isPrintButtonVisible());
            raceNumberFilterBox.setText("");
        }
    }
}
