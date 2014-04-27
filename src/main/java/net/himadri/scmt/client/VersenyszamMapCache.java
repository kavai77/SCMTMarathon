package net.himadri.scmt.client;

import net.himadri.scmt.client.entity.VersenySzam;
import net.himadri.scmt.client.serializable.MarathonActionListener;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.08. 19:36
 */
public class VersenyszamMapCache {
    private Map<Long, VersenySzam> idVersenySzamMap = new HashMap<Long, VersenySzam>();
    private long maxTime;
    private SCMTMarathon scmtMarathon;

    public VersenyszamMapCache(SCMTMarathon scmtMarathon) {
        this.scmtMarathon = scmtMarathon;
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

    public List<VersenySzam> getAllVersenySzamSorted() {
        ArrayList<VersenySzam> versenySzamok = new ArrayList<VersenySzam>(idVersenySzamMap.values());
        Collections.sort(versenySzamok, new Comparator<VersenySzam>() {
            @Override
            public int compare(VersenySzam o1, VersenySzam o2) {
                return Utils.getVersenySzamMegnevezes(scmtMarathon, o1).compareTo(Utils.getVersenySzamMegnevezes(scmtMarathon, o2));
            }
        });
        return versenySzamok;
    }
}
