package net.himadri.scmt.client.token;

public class VersenyToken {
    public static final String VERSENY_TOKEN = "verseny";

    public static boolean isHistoryMatches(String historyToken)
    {
        return (historyToken != null) && historyToken.startsWith(VERSENY_TOKEN);
    }

    public static Long decode(String historyToken) {
        if (isHistoryMatches(historyToken)) {
            return Long.parseLong(historyToken.substring(VERSENY_TOKEN.length()));
        }
        throw new IllegalArgumentException();
    }

    public static String encode(Long versenyId) {
        return VERSENY_TOKEN + versenyId.toString();
    }
}
