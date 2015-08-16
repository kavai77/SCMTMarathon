package net.himadri.scmt.client.gwtextras;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.05.30. 8:29
 */
public abstract class SortableColumn<T, C> extends Column<T, C>{
    public SortableColumn(ColumnSortEvent.ListHandler<T> listHandler, Cell<C> cCell, Comparator<T> comparator) {
        super(cCell);
        setSortable(true);
        listHandler.setComparator(this, comparator);
    }
}
