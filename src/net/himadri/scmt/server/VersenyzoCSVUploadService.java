package net.himadri.scmt.server;

import au.com.bytecode.opencsv.CSVReader;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Query;
import net.himadri.scmt.client.MarathonService;
import net.himadri.scmt.client.Utils;
import net.himadri.scmt.client.entity.Tav;
import net.himadri.scmt.client.entity.VersenySzam;
import net.himadri.scmt.client.entity.Versenyzo;
import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class VersenyzoCSVUploadService extends HttpServlet {

    private static Objectify ofy = ObjectifyService.begin();
    public static final String[] HEADER = {"Rajtszám", "Név", "Született", "Egyesület"};
    public static final DateFormat SZULETES = new SimpleDateFormat("yyyy.MM.dd.");
    public final MarathonService marathonService = new MarathonServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            ServletFileUpload upload = new ServletFileUpload();
            upload.setFileSizeMax(1000000);
            long versenyId = 0;
            String fileContent = null;
            FileItemIterator iter = upload.getItemIterator(request);
            while (iter.hasNext()) {
                FileItemStream item = iter.next();
                String name = item.getFieldName();
                InputStream stream = item.openStream();
                if (item.isFormField()) {
                    String value = Streams.asString(stream);
                    if ("versenyid".equals(name)) versenyId = Long.parseLong(value);
                } else {
                    fileContent = Streams.asString(stream, "UTF-8");
                }
            }
            if (versenyId == 0) {
                throw new IllegalStateException("The form field versenyid is missing");
            }
            if (fileContent == null) {
                throw new IllegalStateException("The uploaded file is missing");
            }

            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            PrintWriter printWriter = response.getWriter();
            handleFileItem(versenyId, fileContent, printWriter);
            printWriter.close();
        } catch (FileUploadException e) {
            throw new IllegalStateException("This service only accept file uploads", e);
        }

    }

    private void handleFileItem(long versenyId, String fileContent, PrintWriter printWriter) throws IOException {
        CSVReader csvReader = new CSVReader(new StringReader(fileContent), ';');
        String [] firstLine = csvReader.readNext();
        if (!Utils.equalsIgnoreCase(firstLine, HEADER)) {
            printWriter.format("A fejléc nem megfelelő: (%d) %s<br>Az elvárt fejléc: (%d) %s",
                    firstLine.length, Arrays.toString(firstLine), HEADER.length, Arrays.toString(HEADER));
            return;
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
                Versenyzo versenyzo = new Versenyzo(raceNumber, name, ferfi, getSzuletesiEv(nextLine), getEgyesulet(nextLine),
                        getVersenySzamId(versenyId, getTavId(raceNumber, versenyId), ferfi, szuletesiEv), versenyId);
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
            printWriter.print("Az összes versenyzőt sikeresen beimportáltuk!");
        } else {
            printWriter.println("<p>Az alábbi hibák fordultak elő</p>");
            printWriter.println("<table><tr><th>Megnevezés<th>Sorszám<th>Sor</tr>");
            for (HibasSor hibasSor: hibasak) {
                printWriter.println("<tr><td>" + hibasSor.hibaOka);
                printWriter.println("<td>" + hibasSor.sorszam);
                printWriter.println("<td>" + Arrays.toString(hibasSor.line));
            }
            printWriter.println("</table>Sikeresen beimportált versenyzők száma: " + sikeres);
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
            Calendar szuletes = Calendar.getInstance();
            Date parse = SZULETES.parse(line[2]);
            szuletes.setTime(parse);
            return szuletes.get(Calendar.YEAR);
        } catch (ParseException e) {
            throw new HibasSorException("A születési dátum nem megfelelő. A helyes forma: 1999.12.31.");
        }
    }

    private String getEgyesulet(String[] line) throws HibasSorException {
        return line[3];
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
