package net.himadri.scmt.client.panel;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import net.himadri.scmt.client.EmptyFailureHandlingAsyncCallback;
import net.himadri.scmt.client.MarathonServiceAsync;
import net.himadri.scmt.client.Utils;
import net.himadri.scmt.client.serializable.RaceStatusRow;

import javax.validation.constraints.NotNull;
import java.util.Comparator;

/**
 * Created by Kávai on 2014.08.05..
 */
public class EllenorzottColumn extends Column<RaceStatusRow, Boolean> {

    public EllenorzottColumn(final MarathonServiceAsync marathonService, final CellTable<RaceStatusRow> statusTable, ColumnSortEvent.ListHandler<RaceStatusRow> listHandler) {
        super(new CheckboxCell(false, false));
        setFieldUpdater(new FieldUpdater<RaceStatusRow, Boolean>() {
            @Override
            public void update(int i, final RaceStatusRow raceStatusRow, Boolean ellenorzott) {
                if (raceStatusRow.getVersenyzo() != null) {
                    marathonService.versenyzoEredmenyEllenorzott(raceStatusRow.getVersenyzo().getId(), ellenorzott, new EmptyFailureHandlingAsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable throwable) {
                            super.onFailure(throwable);
                            undoChanges(raceStatusRow);
                        }
                    });
                } else {
                    undoChanges(raceStatusRow);
                }
            }

            private void undoChanges(RaceStatusRow raceStatusRow) {
                ((CheckboxCell) getCell()).clearViewData(raceStatusRow);
                statusTable.redraw();
            }
        });
        setSortable(true);
        listHandler.setComparator(this, new Comparator<RaceStatusRow>() {
            @Override
            public int compare(RaceStatusRow o1, RaceStatusRow o2) {
                return Utils.compareBoolean(getValue(o1), getValue(o2));
            }
        });
    }

    @Override
    @NotNull
    public Boolean getValue(RaceStatusRow raceStatusRow) {
        return raceStatusRow.getVersenyzo() != null && raceStatusRow.getVersenyzo().isEllenorzott();
    }
}
