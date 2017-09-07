package net.himadri.scmt.client.gwtextras;

import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import net.himadri.scmt.client.LocaleCollator;

import java.util.Comparator;

import static net.himadri.scmt.client.Utils.defaultString;

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
                return LocaleCollator.getInstance().compare(defaultString(getValue(o1)), defaultString(getValue(o2)));
            }
        });
    }

    public SortableTextColumn(ColumnSortEvent.ListHandler<T> listHandler, Comparator<T> comparator) {
        setSortable(true);
        listHandler.setComparator(this, comparator);
    }
}
