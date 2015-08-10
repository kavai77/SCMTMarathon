package net.himadri.scmt.client.panel;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SelectionCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;
import com.itextpdf.text.pdf.BaseFont;
import net.himadri.scmt.client.MarathonService;
import net.himadri.scmt.client.MarathonServiceAsync;
import net.himadri.scmt.client.SCMTMarathon;
import net.himadri.scmt.client.Utils;
import net.himadri.scmt.client.callback.CommonAsyncCallback;
import net.himadri.scmt.client.callback.EmptyFailureHandlingAsyncCallback;
import net.himadri.scmt.client.entity.PageProfile;
import net.himadri.scmt.client.entity.PageProfileId;
import net.himadri.scmt.client.entity.Verseny;
import net.himadri.scmt.client.serializable.MarathonActionListener;
import net.himadri.scmt.client.serializable.PdfServiceType;

import java.util.*;

public class NyomtatoPanel extends Composite {

    private MarathonServiceAsync marathonService = GWT.create(MarathonService.class);
    private NumberFormat numberFormat = NumberFormat.getFormat("0.00");

    private static final List<String> fontFamilyOptions = Arrays.asList(
            BaseFont.TIMES_ROMAN, BaseFont.TIMES_BOLD, BaseFont.TIMES_ITALIC, BaseFont.TIMES_BOLDITALIC,
            BaseFont.HELVETICA, BaseFont.HELVETICA_BOLD, BaseFont.HELVETICA_OBLIQUE, BaseFont.HELVETICA_BOLDOBLIQUE,
            BaseFont.COURIER, BaseFont.COURIER_BOLD, BaseFont.COURIER_OBLIQUE, BaseFont.COURIER_BOLDOBLIQUE
    );

    public NyomtatoPanel(final SCMTMarathon scmtMarathon) {
        
        final AbsolutePanel absolutePanel = new AbsolutePanel();
        initWidget(absolutePanel);
        absolutePanel.setSize("900px", "360px");
        
        Label lblEmlklapnyomtatsBelltsai = new Label("Emléklapnyomtatás beállításai");
        absolutePanel.add(lblEmlklapnyomtatsBelltsai, 20, 10);
        
        final CellTable<PageProfile> cellTable = new CellTable<PageProfile>();
        final ListDataProvider<PageProfile> cellTableData = new ListDataProvider<PageProfile>();
        cellTableData.addDataDisplay(cellTable);

        marathonService.getAllPageProfiles(new CommonAsyncCallback<List<PageProfile>>() {
            @Override
            public void onSuccess(List<PageProfile> pageProfiles) {
                List<PageProfile> sortedProfiles = new ArrayList<PageProfile>(pageProfiles);
                Collections.sort(sortedProfiles, new Comparator<PageProfile>() {
                    @Override
                    public int compare(PageProfile o1, PageProfile o2) {
                        return Float.compare(o1.getyAxis(), o2.getyAxis());
                    }
                });
                cellTableData.setList(sortedProfiles);
            }
        });

        absolutePanel.add(cellTable, 20, 44);
        cellTable.setSize("459px", "214px");
        cellTable.addColumn(new TextColumn<PageProfile>() {
            @Override
            public String getValue(PageProfile pageProfile) {
                return PageProfileId.valueOf(pageProfile.getId()).getMegnevezes();
            }
        }, "Mező");

        final CheckboxCell checkboxCell = new CheckboxCell(false, false);
        Column<PageProfile, Boolean> printProfileColumn = new Column<PageProfile, Boolean>(checkboxCell) {
            @Override
            public Boolean getValue(PageProfile pageProfile) {
                return pageProfile.isPrintProfile();
            }
        };

        printProfileColumn.setFieldUpdater(new FieldUpdater<PageProfile, Boolean>() {
            @Override
            public void update(int i, final PageProfile pageProfile, Boolean printProfile) {
                pageProfile.setPrintProfile(printProfile);
                saveProfile(pageProfile);
            }

        });
        cellTable.addColumn(printProfileColumn, "Nyomtatás");

        final EditTextCell xTextInputCell = new EditTextCell();
        final Column<PageProfile, String> xColumn = new Column<PageProfile, String>(xTextInputCell) {
            @Override
            public String getValue(PageProfile pageProfile) {
                return numberFormat.format(pageProfile.getxAxis());
            }
        };
        xColumn.setFieldUpdater(new FieldUpdater<PageProfile, String>() {
            @Override
            public void update(int i, PageProfile pageProfile, String s) {
                try {
                    float xAxis = (float) numberFormat.parse(s);
                    if (xAxis < 0) throw new NumberFormatException();
                    pageProfile.setxAxis(xAxis);
                    saveProfile(pageProfile);
                } catch (NumberFormatException e) {
                    xTextInputCell.clearViewData(pageProfile);
                    cellTable.redraw();
                }
            }
        });
        xColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        cellTable.addColumn(xColumn, "Bal behúzás (cm)");

        final EditTextCell yTextInputCell = new EditTextCell();
        final Column<PageProfile, String> yColumn = new Column<PageProfile, String>(yTextInputCell) {
            @Override
            public String getValue(PageProfile pageProfile) {
                return numberFormat.format(pageProfile.getyAxis());
            }
        };
        yColumn.setFieldUpdater(new FieldUpdater<PageProfile, String>() {
            @Override
            public void update(int i, PageProfile pageProfile, String s) {
                try {
                    float yAxis = (float) numberFormat.parse(s);
                    if (yAxis < 0) throw new NumberFormatException();
                    pageProfile.setyAxis(yAxis);
                    saveProfile(pageProfile);
                } catch (NumberFormatException e) {
                    yTextInputCell.clearViewData(pageProfile);
                    cellTable.redraw();
                }
            }
        });
        cellTable.addColumn(yColumn, "Felső behúzás (cm)");


        SelectionCell fontFamilyCell = new SelectionCell(fontFamilyOptions);
        final Column<PageProfile, String> fontFamilyColumn = new Column<PageProfile, String>(fontFamilyCell) {
            @Override
            public String getValue(PageProfile pageProfile) {
                return pageProfile.getFontFamily();
            }
        };
        fontFamilyColumn.setFieldUpdater(new FieldUpdater<PageProfile, String>() {
            @Override
            public void update(int i, PageProfile pageProfile, String s) {
                pageProfile.setFontFamily(s);
                saveProfile(pageProfile);
            }
        });
        cellTable.addColumn(fontFamilyColumn, "Betűtípus");

        final EditTextCell sizeInputCell = new EditTextCell();
        final Column<PageProfile, String> sizeColumn = new Column<PageProfile, String>(sizeInputCell) {
            @Override
            public String getValue(PageProfile pageProfile) {
                return Integer.toString(pageProfile.getSize());
            }
        };
        sizeColumn.setFieldUpdater(new FieldUpdater<PageProfile, String>() {
            @Override
            public void update(int i, PageProfile pageProfile, String s) {
                try {
                    int size = Integer.parseInt(s);
                    if (size < 0) throw new NumberFormatException();
                    pageProfile.setSize(size);
                    saveProfile(pageProfile);
                } catch (NumberFormatException e) {
                    sizeInputCell.clearViewData(pageProfile);
                    cellTable.redraw();
                }
            }
        });
        cellTable.addColumn(sizeColumn, "Betűméret");

        final List<String> alignmentOptions = Arrays.asList("Balra igazítás", "Középre igazítás", "Jobbra igazítás");
        SelectionCell alignmentCell = new SelectionCell(alignmentOptions);
        final Column<PageProfile, String> igazitasColumn = new Column<PageProfile, String>(alignmentCell) {
            @Override
            public String getValue(PageProfile pageProfile) {
                return alignmentOptions.get(pageProfile.getAlignment());
            }
        };
        igazitasColumn.setFieldUpdater(new FieldUpdater<PageProfile, String>() {
            @Override
            public void update(int i, PageProfile pageProfile, String s) {
                pageProfile.setAlignment(alignmentOptions.indexOf(s));
                saveProfile(pageProfile);
            }
        });
        cellTable.addColumn(igazitasColumn, "Igazítás");

        Button probaNyomtatas = Utils.createRedirectButton(absolutePanel, "Próbanyomtatás", "/scmtmarathon/PrePrintedPDFService",
                new Hidden("type", PdfServiceType.MINTA.name()));
        probaNyomtatas.setSize("150px", "30px");
        absolutePanel.add(probaNyomtatas, 20, 282);

        scmtMarathon.getVersenySyncSupport().addMarathonActionListener(new MarathonActionListener<Verseny>() {
            @Override
            public void itemAdded(List<Verseny> items) {
                itemRefreshed(items);
            }

            @Override
            public void itemRefreshed(List<Verseny> items) {
                Verseny verseny = items.get(0);
//                Button oklevelButton = createRedirectButton(absolutePanel, "Oklevel", "/OklevelPdfServe.jsp",
                Button oklevelButton = Utils.createRedirectButton(absolutePanel, "Oklevel", "/scmtmarathon/serveemptyoklevelpdf",
                        new Hidden("versenyId", verseny.getId().toString()));
                oklevelButton.setSize("150px", "30px");
                absolutePanel.add(oklevelButton, 20, 320);

                Button probaOklevelButton = Utils.createRedirectButton(absolutePanel, "Oklevel mintával", "/public/OklevelPDFService",
                        new Hidden("versenyId", verseny.getId().toString()), new Hidden("raceNumber", "minta"));
                oklevelButton.setSize("150px", "30px");
                absolutePanel.add(probaOklevelButton, 190, 320);
            }
        });

        //cellTable.setColumnWidth(cellTable.getColumn(2), 50, Style.Unit.PX);
    }

    private void saveProfile(PageProfile pageProfile) {
        marathonService.savePageProfile(pageProfile, new EmptyFailureHandlingAsyncCallback<Void>());
    }
}
