package net.himadri.scmt.client;

import net.himadri.scmt.client.entity.VersenySzam;
import net.himadri.scmt.client.entity.Versenyzo;
import net.himadri.scmt.client.serializable.RaceStatusRow;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.06.18. 6:53
 */
public class TavVersenyszamFilter
{
    public static boolean isAccepted(TavVersenySzam tavVersenySzam, RaceStatusRow raceStatusRow)
    {
        VersenySzam versenyszam = raceStatusRow.getVersenySzam();

        switch (tavVersenySzam.getMode())
        {

            case ALL:
                return true;

            case VERSENYSZAM:
                return (versenyszam != null) && versenyszam.getId().equals(tavVersenySzam.getVersenySzamId());

            case TAV:
                return (versenyszam != null) && (versenyszam.getTavId() != null)
                      && versenyszam.getTavId().equals(tavVersenySzam.getTavId());

            case RACENUMBER:
                return raceStatusRow.getRaceNumber().startsWith(tavVersenySzam.getRaceNumber());

            default:
                throw new IllegalStateException();
        }
    }

    public static boolean isAccepted(TavVersenySzam tavVersenySzam, Versenyzo versenyzo, SCMTMarathon scmtMarathon)
    {
        switch (tavVersenySzam.getMode())
        {

            case ALL:
                return true;

            case VERSENYSZAM:
                return (versenyzo.getVersenySzamId() != null) && versenyzo.getVersenySzamId().equals(tavVersenySzam.getVersenySzamId());

            case TAV:
                if (versenyzo.getVersenySzamId() == null)
                {
                    return false;
                }

                Long tavId = scmtMarathon.getVersenyszamMapCache().getVersenySzam(versenyzo.getVersenySzamId()).getTavId();

                return (tavId != null) && tavId.equals(tavVersenySzam.getTavId());

            case RACENUMBER:
                return versenyzo.getRaceNumber().startsWith(tavVersenySzam.getRaceNumber());

            default:
                throw new IllegalStateException();
        }
    }
}
