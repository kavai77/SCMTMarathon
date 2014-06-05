package net.himadri.scmt.client;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.02.24. 10:07
 */
public class TimeCell extends AbstractCell<Long> {

    @Override
    public void render(Context context, Long elapsedTime, SafeHtmlBuilder safeHtmlBuilder) {
        buildElapsedTimeString(elapsedTime, safeHtmlBuilder);
    }

    public static void buildElapsedTimeString(Long elapsedTime, SafeHtmlBuilder safeHtmlBuilder) {
        if (elapsedTime == null) return;
        long seconds = elapsedTime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        if (hours > 0) {
            safeHtmlBuilder.append(hours).append(':');
        }

        long clockMinutes = minutes % 60;
        if (clockMinutes < 10) {
            safeHtmlBuilder.append(0);
        }
        safeHtmlBuilder.append(clockMinutes).append(':');

        long clockSeconds = seconds % 60;
        if (clockSeconds < 10) {
            safeHtmlBuilder.append(0);
        }
        safeHtmlBuilder.append(clockSeconds);
    }
}
