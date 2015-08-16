package net.himadri.scmt.client.gwtextras;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import net.himadri.scmt.client.Utils;
import net.himadri.scmt.client.serializable.RaceStatusRow;

import java.util.Comparator;

public class TimeCellColumn extends Column<RaceStatusRow, TimeCell.TimeCellData> {

    private int lapNb;

    public TimeCellColumn(int lapNb, ColumnSortEvent.ListHandler<RaceStatusRow> listHandler) {
        super(new TimeCell());
        this.lapNb = lapNb;
        setSortable(true);
        listHandler.setComparator(this, new Comparator<RaceStatusRow>() {
            @Override
            public int compare(RaceStatusRow o1, RaceStatusRow o2) {
                return Utils.compareLong(getValue(o1).actualTime, getValue(o2).actualTime);
            }
        });
    }

    @Override public TimeCell.TimeCellData getValue(RaceStatusRow raceStatusRow)
    {
        TimeCell.TimeCellData cellTime = new TimeCell.TimeCellData();

        cellTime.feladta = (raceStatusRow.getVersenyzo() != null) && raceStatusRow.getVersenyzo().isFeladta();
        if (lapNb < raceStatusRow.getLapTimes().size())
        {
            long diff = raceStatusRow.getTav() != null ? raceStatusRow.getTav().getRaceStartDiff() : 0;
            cellTime.actualTime = raceStatusRow.getLapTimes().get(lapNb) - diff;
            if (lapNb > 0)
            {
                cellTime.elapsedTime = cellTime.actualTime - raceStatusRow.getLapTimes().get(lapNb - 1);
            }
        }

        return cellTime;
    }
}
