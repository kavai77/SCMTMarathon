package net.himadri.scmt.client;

import net.himadri.scmt.client.entity.Versenyzo;
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
public class VersenyzoMapCache {
    private Map<String, Versenyzo> raceNumberVersenyzoMap = new HashMap<String, Versenyzo>();
    private long maxTime;

    public VersenyzoMapCache(SCMTMarathon scmtMarathon) {
        scmtMarathon.getPollingService().getVersenyzoSync().addMarathonActionListener(
                SyncSupport.Priority.HIGH, new MarathonActionListener<Versenyzo>() {
            @Override
            public void itemAdded(List<Versenyzo> items) {
                for (Versenyzo versenyzo : items) {
                    raceNumberVersenyzoMap.put(versenyzo.getRaceNumber(), versenyzo);
                    if (maxTime < versenyzo.getCreationTime()) {
                        maxTime = versenyzo.getCreationTime();
                    }
                }
            }

            @Override
            public void itemRefreshed(List<Versenyzo> items) {
                raceNumberVersenyzoMap.clear();
                maxTime = 0;
                itemAdded(items);
            }
        });
    }

    public Versenyzo getVersenyzo(String raceNumber) {
        return raceNumberVersenyzoMap.get(raceNumber);
    }

    public Collection<Versenyzo> getAllVersenyzo() {
        return raceNumberVersenyzoMap.values();
    }

    public long getMaxTime() {
        return maxTime;
    }
}
