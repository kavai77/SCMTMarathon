package net.himadri.scmt.client;

import net.himadri.scmt.client.entity.VersenySzam;
import net.himadri.scmt.client.entity.Versenyzo;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.06.18. 6:53
 */
public class TavVersenyszamFilter {
    public static boolean isAccepted(TavVersenySzam tavVersenySzam, Versenyzo versenyzo, SCMTMarathon scmtMarathon) {
        switch (tavVersenySzam.getMode()) {
            case ALL:
                return true;
            case VERSENYSZAM:
                return versenyzo.getVersenySzamId() != null && versenyzo.getVersenySzamId().equals(tavVersenySzam.getVersenySzamId());
            case TAV:
                if (versenyzo.getVersenySzamId() == null) return false;
                Long tavId = scmtMarathon.getVersenyszamMapCache().getVersenySzam(versenyzo.getVersenySzamId()).getTavId();
                return tavId != null && tavId.equals(tavVersenySzam.getTavId());
            default:
                throw new IllegalStateException();
        }
    }

    public static boolean isAccepted(TavVersenySzam tavVersenySzam, VersenySzam versenyszam) {
        switch (tavVersenySzam.getMode()) {
            case ALL:
                return true;
            case VERSENYSZAM:
                return versenyszam != null && versenyszam.getId().equals(tavVersenySzam.getVersenySzamId());
            case TAV:
                return versenyszam != null && versenyszam.getTavId() != null &&
                        versenyszam.getTavId().equals(tavVersenySzam.getTavId());
            default:
                throw new IllegalStateException();
        }
    }


}
