package net.himadri.scmt.client;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.06.18. 20:20
 */
public class TavVersenySzam {
    public enum Mode {
        VERSENYSZAM, TAV, ALL
    }

    private Long versenySzamId;
    private Long tavId;
    private Mode mode;

    private TavVersenySzam() {
    }

    public static TavVersenySzam createVersenyszamFilter(Long versenySzamId) {
        TavVersenySzam tavVersenySzam = new TavVersenySzam();
        tavVersenySzam.versenySzamId = versenySzamId;
        tavVersenySzam.mode = Mode.VERSENYSZAM;
        return tavVersenySzam;
    }

    public static TavVersenySzam createTav(Long tavId) {
        TavVersenySzam tavVersenySzam = new TavVersenySzam();
        tavVersenySzam.tavId = tavId;
        tavVersenySzam.mode = Mode.TAV;
        return tavVersenySzam;
    }

    public static TavVersenySzam createAllAcceptance() {
        TavVersenySzam tavVersenySzam = new TavVersenySzam();
        tavVersenySzam.mode = Mode.ALL;
        return tavVersenySzam;
    }

    public Long getVersenySzamId() {
        return versenySzamId;
    }

    public Long getTavId() {
        return tavId;
    }

    public Long getEntityId() {
        switch (mode) {
            case VERSENYSZAM:
                return versenySzamId;
            case TAV:
                return tavId;
            case ALL:
                return null;
            default:
                throw new IllegalStateException();
        }
    }

    public Mode getMode() {
        return mode;
    }
}
