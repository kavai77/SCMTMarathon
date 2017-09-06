package net.himadri.scmt.server;

import au.com.bytecode.opencsv.CSVReader;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Query;
import net.himadri.scmt.client.MarathonService;
import net.himadri.scmt.client.Utils;
import net.himadri.scmt.client.VersenyzoCSVUploadService;
import net.himadri.scmt.client.entity.Tav;
import net.himadri.scmt.client.entity.VersenySzam;
import net.himadri.scmt.client.entity.Versenyzo;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersenyzoCSVUploadServiceImpl extends RemoteServiceServlet implements VersenyzoCSVUploadService {

    private static Objectify ofy = ObjectifyUtils.beginObjectify();
    public static final String[] HEADER = {"Rajtszám", "Név", "Született", "Egyesület", "Email"};
    public static final Pattern SZULETES_PATTERN = Pattern.compile("(\\d\\d\\d\\d).*");
    public final MarathonService marathonService = new MarathonServiceImpl();

    @Override
    public String getHeader() {
        return Arrays.toString(HEADER);
    }

    @Override
    public String importVersenyzok(long versenyId, String fileContent) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            CSVReader csvReader = new CSVReader(new StringReader(fileContent), ';');
            String [] firstLine = csvReader.readNext();
            if (!Utils.equalsIgnoreCase(firstLine, HEADER)) {
                return String.format("A fejléc nem megfelelő: (%d) %s<br>Az elvárt fejléc: (%d) %s",
                        firstLine.length, Arrays.toString(firstLine), HEADER.length, Arrays.toString(HEADER));
            }
            String [] nextLine;
            List<HibasSor> hibasak = new ArrayList<HibasSor>();
            int sikeres = 0;
            int sorSzam = 2;
            while ((nextLine = csvReader.readNext()) != null) {
                try {
                    String raceNumber = getRaceNumber(nextLine, versenyId);
                    String name = getName(nextLine);
                    boolean ferfi = getFerfi(name);
                    int szuletesiEv = getSzuletesiEv(nextLine);
                    Versenyzo versenyzo = new Versenyzo(raceNumber, name, ferfi, getSzuletesiEv(nextLine), getEgyesulet(nextLine), getEmail(nextLine),
                            getVersenySzamId(versenyId, getTavId(raceNumber, versenyId), ferfi, szuletesiEv), versenyId, null, null,null);
                    ofy.put(versenyzo);
                    sikeres++;
                } catch (HibasSorException e) {
                    hibasak.add(new HibasSor(e.getMessage(), sorSzam, nextLine));
                } catch (RuntimeException e) {
                    hibasak.add(new HibasSor("Ismeretlen hiba: " + e.toString(), sorSzam, nextLine));
                }
                sorSzam++;
            }
            if (hibasak.isEmpty()) {
                stringBuilder.append("Az összes versenyzőt sikeresen beimportáltuk!");
            } else {
                stringBuilder.append("<p>Az alábbi hibák fordultak elő</p>");
                stringBuilder.append("<table border=1 cellspacing=0><tr><th>Megnevezés<th>Szám<th>Sor</tr>");
                for (HibasSor hibasSor: hibasak) {
                    stringBuilder.append("<tr><td>").append(hibasSor.hibaOka);
                    stringBuilder.append("<td>").append(hibasSor.sorszam);
                    stringBuilder.append("<td>").append(Arrays.toString(hibasSor.line));
                }
                stringBuilder.append("</table>Sikeresen beimportált versenyzők száma: ").append(sikeres);
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            throw new IllegalStateException("This should never happen!");
        }
    }

    private String getRaceNumber(String[] line, long versenyId) throws HibasSorException {
        if (Utils.isEmpty(line[0])) {
            throw new HibasSorException("A rajtszám hiányzik");
        }
        int count = ofy.query(Versenyzo.class)
                .filter("versenyId", versenyId)
                .filter("raceNumber", line[0]).count();
        if (count > 0) {
            throw new HibasSorException("Ezzel a rajtszámmal már ven versenyző");
        }
        return line[0];
    }

    private String getName(String[] line) throws HibasSorException {
        if (Utils.isEmpty(line[1])) {
            throw new HibasSorException("A név hiányzik");
        }
        return line[1];
    }

    private int getSzuletesiEv(String[] line) throws HibasSorException {
        if (Utils.isEmpty(line[2])) {
            throw new HibasSorException("A születési dátum hiányzik");
        }
        try {
            Matcher matcher = SZULETES_PATTERN.matcher(line[2]);
            if (!matcher.matches()) {
                throw new NumberFormatException();
            }
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException e) {
            throw new HibasSorException("A születési dátum nem megfelelő. Az első négy betű az évszám kell, hogy legyen");
        }
    }

    private String getEgyesulet(String[] line) throws HibasSorException {
        return line[3];
    }

    private String getEmail(String[] line) throws HibasSorException {
        String email = line[4];
        if (!Utils.isEmpty(email) && !email.contains("@")) {
            throw new HibasSorException("Az email cím helytelen.");
        }
        return email;
    }

    private boolean getFerfi(String name) throws HibasSorException {
        String[] splitnev = name.split(" ");
        Boolean ferfi = marathonService.isFerfiNev(splitnev[splitnev.length - 1]);
        if (ferfi == null) {
            throw new HibasSorException("A nemet nem lehetett meghatrozni");
        }
        return ferfi;
    }

    private long getTavId(String versenySzamStr, long versenyId) throws HibasSorException {
        try {
            int versenySzam = Integer.parseInt(versenySzamStr);
            for (Tav tav: ofy.query(Tav.class).filter("versenyId", versenyId)) {
                if (tav.getVersenySzamtol() != null && tav.getVersenySzamig() != null &&
                        tav.getVersenySzamtol() <= versenySzam && versenySzam <= tav.getVersenySzamig()) {
                    return tav.getId();
                }
            }
        } catch (NumberFormatException e) {
            throw new HibasSorException("A rajtszám nem egész szám");
        }
        throw new HibasSorException("Ehhez a rajtszámhoz nem tartozik egyetlen táv sem");
    }

    private long getVersenySzamId(long versenyId, long tavId, boolean ferfi, int szuletesiEv) throws HibasSorException {
        Query<VersenySzam> versenySzamQuery = ofy.query(VersenySzam.class)
                .filter("versenyId", versenyId)
                .filter("tavId", tavId)
                .filter("ferfi", ferfi);
        int kor = Calendar.getInstance().get(Calendar.YEAR) - szuletesiEv;
        for (VersenySzam versenySzam: versenySzamQuery) {
            if ((versenySzam.getKorTol() == null || versenySzam.getKorTol() <= kor) &&
                (versenySzam.getKorIg() == null || kor <= versenySzam.getKorIg())) {
                return versenySzam.getId();
            }
        }
        throw new HibasSorException("Nem található versenyszám.");
    }

    private class HibasSor {
        String hibaOka;
        int sorszam;
        String[] line;

        private HibasSor(String hibaOka, int sorszam, String[] line) {
            this.hibaOka = hibaOka;
            this.sorszam = sorszam;
            this.line = line;
        }
    }

    private class HibasSorException extends Exception {
        private HibasSorException(String message) {
            super(message);
        }
    }
}
