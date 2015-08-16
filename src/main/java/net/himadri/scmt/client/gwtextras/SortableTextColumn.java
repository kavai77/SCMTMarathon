package net.himadri.scmt.client.gwtextras;

import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import net.himadri.scmt.client.Utils;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.05.30. 8:27
 */
public abstract class SortableTextColumn<T> extends TextColumn<T> {
    public SortableTextColumn(ColumnSortEvent.ListHandler<T> listHandler) {
        setSortable(true);
        listHandler.setComparator(this, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return Utils.compareString(getValue(o1), getValue(o2));
            }
        });
    }

    public SortableTextColumn(ColumnSortEvent.ListHandler<T> listHandler, Comparator<T> comparator) {
        setSortable(true);
        listHandler.setComparator(this, comparator);
    }
}
