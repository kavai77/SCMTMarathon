package net.himadri.scmt.client.panel;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import net.himadri.scmt.client.Utils;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.02.24. 10:07
 */
public class TimeCell extends AbstractCell<ResultTable.TimeCellData> {

    @Override
    public void render(Context context, ResultTable.TimeCellData cellTime, SafeHtmlBuilder safeHtmlBuilder) {
        if (cellTime.actualTime != null) {
            safeHtmlBuilder.appendEscaped(Utils.getElapsedTimeString(cellTime.actualTime));
            if (cellTime.elapsedTime != null) {
                safeHtmlBuilder.appendHtmlConstant("<br>");
                safeHtmlBuilder.appendEscaped("(");
                safeHtmlBuilder.appendEscaped(Utils.getElapsedTimeString(cellTime.elapsedTime));
                safeHtmlBuilder.appendEscaped(")");
            }
        } else if (cellTime.feladta) {
            safeHtmlBuilder.appendEscaped("Feladta");
        }
    }
}
