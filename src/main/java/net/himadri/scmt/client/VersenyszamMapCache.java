package net.himadri.scmt.client;

import net.himadri.scmt.client.entity.VersenySzam;
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
public class VersenyszamMapCache {
    private Map<Long, VersenySzam> idVersenySzamMap = new HashMap<Long, VersenySzam>();
    private long maxTime;

    public VersenyszamMapCache(SCMTMarathon scmtMarathon) {
        scmtMarathon.getPollingService().getVersenySzamSync().addMarathonActionListener(
                SyncSupport.Priority.HIGH, new MarathonActionListener<VersenySzam>() {
            @Override
            public void itemAdded(List<VersenySzam> items) {
                for (VersenySzam versenySzam : items) {
                    idVersenySzamMap.put(versenySzam.getId(), versenySzam);
                    if (maxTime < versenySzam.getCreationTime()) {
                        maxTime = versenySzam.getCreationTime();
                    }
                }
            }

            @Override
            public void itemRefreshed(List<VersenySzam> items) {
                idVersenySzamMap.clear();
                maxTime = 0;
                itemAdded(items);
            }
        });
    }

    public VersenySzam getVersenySzam(Long id) {
        return idVersenySzamMap.get(id);
    }

    public long getMaxTime() {
        return maxTime;
    }

    public Collection<VersenySzam> getAllVersenySzam() {
        return idVersenySzamMap.values();
    }
}
