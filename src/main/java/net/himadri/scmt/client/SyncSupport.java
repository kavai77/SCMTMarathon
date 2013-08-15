package net.himadri.scmt.client;

import net.himadri.scmt.client.serializable.MarathonActionListener;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.22. 7:04
 */
public class SyncSupport<T> {
    private int syncId;

    public enum Priority {
        HIGH, MEDIUM, NORMAL
    }

    private static final Logger logger = Logger.getLogger(SyncSupport.class.getName());

    private Map<Priority, List<MarathonActionListener<T>>> marathonActionListeners = new EnumMap<Priority, List<MarathonActionListener<T>>>(Priority.class);

    public void addMarathonActionListener(Priority priority, MarathonActionListener<T> actionListener) {
        List<MarathonActionListener<T>> marathonActionListenerList = marathonActionListeners.get(priority);
        if (marathonActionListenerList == null) {
            marathonActionListenerList = new ArrayList<MarathonActionListener<T>>();
            marathonActionListeners.put(priority, marathonActionListenerList);
        }
        marathonActionListenerList.add(actionListener);
    }

    public void addMarathonActionListener(MarathonActionListener<T> actionListener) {
        addMarathonActionListener(Priority.NORMAL, actionListener);
    }

    public void notifyItemsAdded(List<T> items) {
        for (Priority priority : Priority.values()) {
            List<MarathonActionListener<T>> marathonActionListenerList = getMarathonActionListenerList(priority);
            for (MarathonActionListener<T> actionListener : marathonActionListenerList) {
                try {
                    actionListener.itemAdded(items);
                } catch (RuntimeException e) {
                    logger.log(Level.SEVERE, "ActionListener exception", e);
                }
            }
        }
    }

    public void notifyRefreshed(List<T> items) {
        for (Priority priority : Priority.values()) {
            List<MarathonActionListener<T>> marathonActionListenerList = getMarathonActionListenerList(priority);
            for (MarathonActionListener<T> actionListener : marathonActionListenerList) {
                try {
                    actionListener.itemRefreshed(items);
                } catch (RuntimeException e) {
                    logger.log(Level.SEVERE, "ActionListener exception", e);
                }
            }
        }
    }

    private List<MarathonActionListener<T>> getMarathonActionListenerList(Priority priority) {
        if (marathonActionListeners.containsKey(priority)) {
            return marathonActionListeners.get(priority);
        } else {
            return Collections.emptyList();
        }

    }

    public int getSyncId() {
        return syncId;
    }

    public void setSyncId(int syncId) {
        this.syncId = syncId;
    }
}