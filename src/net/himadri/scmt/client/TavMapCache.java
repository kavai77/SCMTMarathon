package net.himadri.scmt.client;

import net.himadri.scmt.client.entity.Tav;
import net.himadri.scmt.client.serializable.MarathonActionListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.08. 19:36
 */
public class TavMapCache {
    private Map<Long, Tav> idTavMap = new HashMap<Long, Tav>();
    private long maxTime;

    public TavMapCache(SCMTMarathon scmtMarathon) {
        scmtMarathon.getPollingService().getTavSync().addMarathonActionListener(
                SyncSupport.Priority.HIGH, new MarathonActionListener<Tav>() {
            @Override
            public void itemAdded(List<Tav> items) {
                for (Tav tav : items) {
                    idTavMap.put(tav.getId(), tav);
                    if (maxTime < tav.getCreationTime()) {
                        maxTime = tav.getCreationTime();
                    }
                }
            }

            @Override
            public void itemRefreshed(List<Tav> items) {
                idTavMap.clear();
                maxTime = 0;
                itemAdded(items);
            }
        });
    }

    public Tav getTav(Long id) {
        return idTavMap.get(id);
    }

    public long getMaxTime() {
        return maxTime;
    }

    public Collection<Tav> getAllTav() {
        return idTavMap.values();
    }
}
