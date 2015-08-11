package net.himadri.scmt.client.token;

import net.himadri.scmt.client.TavVersenySzam;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.06.18. 20:14
 */
public class TavVersenySzamToken
{
    public static final String RESULT_HISTORY_TOKEN = "result";
    public static final String VERSENYSZAM_ID_HISTORY_TOKEN = "versenyszamid";
    public static final String TAV_ID_HISTORY_TOKEN = "tavid";
    public static final String RACENUMBER_ID_HISTORY_TOKEN = "racenumber";

    public static boolean isHistoryMatches(String historyToken)
    {
        return (historyToken != null) && historyToken.startsWith(RESULT_HISTORY_TOKEN);
    }

    public static TavVersenySzam decode(String historyToken)
    {
        if (isHistoryMatches(historyToken))
        {
            String postfix = historyToken.substring(RESULT_HISTORY_TOKEN.length());

            if (postfix.isEmpty())
            {
                return TavVersenySzam.createAllAcceptance();
            }

            if (postfix.startsWith(VERSENYSZAM_ID_HISTORY_TOKEN))
            {
                return TavVersenySzam.createVersenyszamFilter(Long.parseLong(postfix.substring(VERSENYSZAM_ID_HISTORY_TOKEN.length())));
            }

            if (postfix.startsWith(TAV_ID_HISTORY_TOKEN))
            {
                return TavVersenySzam.createTav(Long.parseLong(postfix.substring(TAV_ID_HISTORY_TOKEN.length())));
            }

            if (postfix.startsWith(RACENUMBER_ID_HISTORY_TOKEN))
            {
                return TavVersenySzam.createRaceNumber(postfix.substring(TAV_ID_HISTORY_TOKEN.length()));
            }
        }

        throw new IllegalArgumentException();
    }

    public static String encode(TavVersenySzam tavVersenySzam)
    {
        if (tavVersenySzam.getMode() == TavVersenySzam.Mode.ALL)
        {
            return RESULT_HISTORY_TOKEN;
        }
        else
        {
            return RESULT_HISTORY_TOKEN + getIdKey(tavVersenySzam) + tavVersenySzam.getEntityId();
        }
    }

    private static String getIdKey(TavVersenySzam tavVersenySzam)
    {
        switch (tavVersenySzam.getMode())
        {

            case VERSENYSZAM:
                return VERSENYSZAM_ID_HISTORY_TOKEN;

            case TAV:
                return TAV_ID_HISTORY_TOKEN;

            case RACENUMBER:
                return RACENUMBER_ID_HISTORY_TOKEN;

            default:
                throw new IllegalStateException();
        }
    }
}
