package net.himadri.scmt.client;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.06.18. 20:20
 */
public class TavVersenySzam
{
    public enum Mode
    {
        VERSENYSZAM(true),
        TAV(true),
        ALL(false),
        RACENUMBER(false);
        private boolean printButtonVisible;

        Mode(boolean printButtonVisible)
        {
            this.printButtonVisible = printButtonVisible;
        }

        public boolean isPrintButtonVisible()
        {
            return printButtonVisible;
        }
    }

    private Mode mode;
    private String raceNumber;
    private Long tavId;
    private Long versenySzamId;

    private TavVersenySzam()
    {
    }

    public static TavVersenySzam createAllAcceptance()
    {
        TavVersenySzam tavVersenySzam = new TavVersenySzam();

        tavVersenySzam.mode = Mode.ALL;

        return tavVersenySzam;
    }

    public static TavVersenySzam createRaceNumber(String raceNumber)
    {
        TavVersenySzam tavVersenySzam = new TavVersenySzam();

        tavVersenySzam.mode = Mode.RACENUMBER;
        tavVersenySzam.raceNumber = raceNumber;

        return tavVersenySzam;
    }

    public static TavVersenySzam createTav(Long tavId)
    {
        TavVersenySzam tavVersenySzam = new TavVersenySzam();

        tavVersenySzam.tavId = tavId;
        tavVersenySzam.mode = Mode.TAV;

        return tavVersenySzam;
    }

    public static TavVersenySzam createVersenyszamFilter(Long versenySzamId)
    {
        TavVersenySzam tavVersenySzam = new TavVersenySzam();

        tavVersenySzam.versenySzamId = versenySzamId;
        tavVersenySzam.mode = Mode.VERSENYSZAM;

        return tavVersenySzam;
    }

    public Object getEntityId()
    {
        switch (mode)
        {

            case VERSENYSZAM:
                return versenySzamId;

            case TAV:
                return tavId;

            case RACENUMBER:
                return raceNumber;

            case ALL:
                return null;

            default:
                throw new IllegalStateException();
        }
    }

    public Mode getMode()
    {
        return mode;
    }

    public String getRaceNumber()
    {
        return raceNumber;
    }

    public Long getTavId()
    {
        return tavId;
    }

    public Long getVersenySzamId()
    {
        return versenySzamId;
    }
}
