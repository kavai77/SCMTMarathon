package net.himadri.scmt.client.serializable;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.06. 16:15
 */
public interface MarathonActionListener<T> {
    void itemAdded(List<T> items);

    void itemRefreshed(List<T> items);
}
