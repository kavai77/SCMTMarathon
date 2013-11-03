package net.himadri.scmt.client;

import net.himadri.scmt.client.entity.Tav;
import net.himadri.scmt.client.entity.VersenySzam;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.05.23. 7:11
 */
public class Utils {
    public static String getKorosztalyMegnevezes(Integer korTol, Integer korIg) {
        if (korTol == null && korIg == null) {
            return "Életkor nélkül";
        } else if (korTol != null && korIg == null) {
            return korTol + " évtől";
        } else if (korTol == null) {
            return korIg + " évig";
        } else if (korTol.equals(korIg)) {
            return korTol + " év";
        } else {
            return korTol + "-" + korIg + " év";
        }
    }

    public static String getFerfiMegnevezes(Boolean ferfi) {
        return ferfi != null ? (ferfi ? "Férfi" : "Női") : null;
    }

    public static String getVersenySzamMegnevezes(SCMTMarathon scmtMarathon, Long versenySzamId) {
        VersenySzam versenySzam = scmtMarathon.getVersenyszamMapCache().getVersenySzam(versenySzamId);
        return getVersenySzamMegnevezes(scmtMarathon, versenySzam);
    }

    public static String getVersenySzamMegnevezes(SCMTMarathon scmtMarathon, VersenySzam versenySzam) {
        return getVersenySzamMegnevezes(scmtMarathon, versenySzam.getTavId(), versenySzam.getKorTol(), versenySzam.getKorIg(), versenySzam.getFerfi());
    }

    public static String getVersenySzamMegnevezes(Tav tav, VersenySzam versenySzam) {
        return getVersenySzamMegnevezes(tav, versenySzam.getKorTol(), versenySzam.getKorIg(), versenySzam.getFerfi());
    }

    public static String getVersenySzamMegnevezes(SCMTMarathon scmtMarathon, Long tavId, Integer korTol, Integer korIg, Boolean ferfi) {
        return getVersenySzamMegnevezes(tavId != null ? scmtMarathon.getTavMapCache().getTav(tavId) : null, korTol, korIg, ferfi);
    }

    public static String getVersenySzamMegnevezes(Tav tav, Integer korTol, Integer korIg, Boolean ferfi) {
        return (tav != null ? tav.getMegnevezes() + " " : "") +
                getKorosztalyMegnevezes(korTol, korIg) + " " +
                getFerfiMegnevezes(ferfi);
    }

    public static boolean isKorAtfedes(Integer korTol1, Integer korIg1, Integer korTol2, Integer korIg2) {
        if (korTol1 == null) korTol1 = Integer.MIN_VALUE;
        if (korTol2 == null) korTol2 = Integer.MIN_VALUE;
        if (korIg1 == null) korIg1 = Integer.MAX_VALUE;
        if (korIg2 == null) korIg2 = Integer.MAX_VALUE;

        boolean nincsFedes = (korTol1 < korTol2 && korIg1 < korTol2) || (korTol1 > korIg2 && korIg1 > korIg2);
        return !nincsFedes;
    }

    public static int compareInteger(Integer o1Integer, Integer o2Integer) {
        if (o1Integer == null) o1Integer = 0;
        if (o2Integer == null) o2Integer = 0;

        return o1Integer.compareTo(o2Integer);
    }

    public static int compareLong(Long o1Long, Long o2Long) {
        if (o1Long == null) o1Long = 0L;
        if (o2Long == null) o2Long = 0L;

        return o1Long.compareTo(o2Long);
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static int compareString(String o1String, String o2String) {
        if (o1String == null) o1String = "";
        if (o2String == null) o2String = "";

        return o1String.compareTo(o2String);
    }

    public static boolean equalsIgnoreCase(String[] a, String[] a2) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;

        int length = a.length;
        if (a2.length != length) {
            System.out.println("A méret nem stimmel: " + a2.length + ", " + length);
            return false;
        }

        for (int i=0; i<length; i++) {
            String o1 = a[i];
            String o2 = a2[i];
            if (!(o1==null ? o2==null : o1.equalsIgnoreCase(o2))) {
                System.out.println("A nem stimmel: " + Arrays.toString(o1.toCharArray()) + ", " + Arrays.toString(o2.toCharArray()));
                return false;
            }
        }

        return true;
    }

    public static String getElapsedTimeString(Long elapsedTime) {
        if (elapsedTime == null) return null;
        StringBuilder builder = new StringBuilder();
        long seconds = elapsedTime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        if (hours > 0) {
            builder.append(hours).append(':');
        }

        long clockMinutes = minutes % 60;
        if (clockMinutes < 10) {
            builder.append(0);
        }
        builder.append(clockMinutes).append(':');

        long clockSeconds = seconds % 60;
        if (clockSeconds < 10) {
            builder.append(0);
        }
        builder.append(clockSeconds);
        return builder.toString();
    }

    // Parallel arrays used in the conversion process.
    private static final String[] RCODE = {"M", "CM", "D", "CD", "C", "XC", "L",
            "XL", "X", "IX", "V", "IV", "I"};
    private static final int[]    BVAL  = {1000, 900, 500, 400,  100,   90,  50,
            40,   10,    9,   5,   4,    1};

    public static String numberToRoman(int number) {
        if (number <= 0 || number >= 4000) {
            throw new NumberFormatException("Value outside roman numeral range.");
        }
        StringBuilder roman = new StringBuilder();

        for (int i = 0; i < RCODE.length; i++) {
            while (number >= BVAL[i]) {
                number -= BVAL[i];
                roman.append(RCODE[i]);
            }
        }
        return roman.toString();
    }
}
