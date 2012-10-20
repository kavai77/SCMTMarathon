package net.himadri.scmt.server;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import net.himadri.scmt.client.Utils;
import net.himadri.scmt.client.entity.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.06.01. 6:59
 */
public class PDFService extends HttpServlet {
    private static Objectify ofy = ObjectifyService.begin();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String versenySzamId = request.getParameter("versenySzam");
//        response.addHeader("Content-Disposition", "attachment; filename=raceresult.pdf");
        response.setContentType("application/pdf");
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
            document.open();
            PdfContentByte canvas = writer.getDirectContentUnder();
            if ("minta".equals(versenySzamId)) {
                printSamplePage(canvas);
            } else {
                printVersenySzam(canvas, document, versenySzamId);
            }
            document.close();
        } catch (DocumentException e) {
            throw new IOException(e);
        }
    }

    private void printVersenySzam(PdfContentByte canvas, Document document, String versenySzamIdStr) {
        long versenySzamId = Long.parseLong(versenySzamIdStr);
        List<PageProfile> pageProfiles = new MarathonServiceImpl().getAllPageProfiles();
        VersenySzam versenySzam = ofy.get(VersenySzam.class, versenySzamId);
        Tav tav = ofy.get(Tav.class, versenySzam.getTavId());
        List<Versenyzo> versenyzoQueryResultIterable = ofy.query(Versenyzo.class)
                .filter("versenySzamId", versenySzamId)
                .filter("versenyId", versenySzam.getVersenyId())
                .list();
        Map<String, Versenyzo> raceNumberVersenyzoMap = new HashMap<String, Versenyzo>();
        for (Versenyzo versenyzo: versenyzoQueryResultIterable) {
            raceNumberVersenyzoMap.put(versenyzo.getRaceNumber(), versenyzo);
        }

        QueryResultIterable<PersonLap> personLapQueryResultIterable = ofy.query(PersonLap.class)
                .filter("versenyId", versenySzam.getVersenyId())
                .filter("raceNumber in", raceNumberVersenyzoMap.keySet())
                .order("time")
                .fetch();
        Map<String, List<Long>> raceNumberPersonLapListMap = new HashMap<String, List<Long>>();
        for (PersonLap personLap: personLapQueryResultIterable) {
            List<Long> personLapList = raceNumberPersonLapListMap.get(personLap.getRaceNumber());
            if (personLapList == null) {
                personLapList = new ArrayList<Long>();
                raceNumberPersonLapListMap.put(personLap.getRaceNumber(), personLapList);
            }
            personLapList.add(personLap.getTime());
        }

        List<VersenyzoResult> versenyzoResults = new ArrayList<VersenyzoResult>();
        for (Map.Entry<String, List<Long>> raceNumberPersonLapEntry: raceNumberPersonLapListMap.entrySet()) {
            if (raceNumberPersonLapEntry.getValue().size() >= tav.getKorSzam()) {
                Versenyzo versenyzo = raceNumberVersenyzoMap.get(raceNumberPersonLapEntry.getKey());
                Long ido = raceNumberPersonLapEntry.getValue().get(tav.getKorSzam() - 1);
                versenyzoResults.add(new VersenyzoResult(versenyzo, ido));
            }
        }
        Collections.sort(versenyzoResults, new Comparator<VersenyzoResult>() {
            @Override
            public int compare(VersenyzoResult o1, VersenyzoResult o2) {
                return o1.ido.compareTo(o2.ido);
            }
        });

        for (int i = 0; i < versenyzoResults.size(); i++) {
            Versenyzo versenyzo = versenyzoResults.get(i).versenyzo;
            Map<PageProfileId, String> data = new HashMap<PageProfileId, String>();
            data.put(PageProfileId.NEV, versenyzo.getName());
            data.put(PageProfileId.EGYESULET, versenyzo.getEgyesulet());
            data.put(PageProfileId.VERSENYSZAM, Utils.getVersenySzamMegnevezes(tav, versenySzam));
            data.put(PageProfileId.IDO, Utils.getElapsedTimeString(versenyzoResults.get(i).ido));
            data.put(PageProfileId.HELYEZES, Utils.numberToRoman(i + 1) + ".");
            printSinglePage(canvas, pageProfiles, data);
            if (i < versenyzoResults.size() - 1) {
                document.newPage();
            }
        }
    }

    private class VersenyzoResult {
        private Versenyzo versenyzo;
        private Long ido;

        private VersenyzoResult(Versenyzo versenyzo, Long ido) {
            this.versenyzo = versenyzo;
            this.ido = ido;
        }
    }

    private void printSamplePage(PdfContentByte canvas) {
        List<PageProfile> pageProfiles = new MarathonServiceImpl().getAllPageProfiles();
        Map<PageProfileId, String> data = new HashMap<PageProfileId, String>();
        data.put(PageProfileId.NEV, "Kiss Gergely");
        data.put(PageProfileId.EGYESULET, "Futóbolondok");
        data.put(PageProfileId.VERSENYSZAM, "Maraton férfi 18-30 év");
        data.put(PageProfileId.IDO, "3:57:12");
        data.put(PageProfileId.HELYEZES, "XVII.");
        printSinglePage(canvas, pageProfiles, data);
    }

    private void printSinglePage(PdfContentByte canvas, List<PageProfile> pageProfiles, Map<PageProfileId, String> data) {
        for (PageProfile pageProfile: pageProfiles) {
            String entry = data.get(PageProfileId.valueOf(pageProfile.getId()));
            if (entry != null && (pageProfile.getxAxis() > 0 || pageProfile.getyAxis() > 0)) {
                ColumnText.showTextAligned(canvas, pageProfile.getAlignment(),
                    new Phrase(entry, new Font(Font.FontFamily.valueOf(pageProfile.getFontFamily()), pageProfile.getSize())),
                        convertCmToPixel(pageProfile.getxAxis()),
                        (int) PageSize.A4.getHeight() - convertCmToPixel(pageProfile.getyAxis()), 0);
            }
        }
    }

//    public static void main(String[] args) throws IOException, DocumentException {
//        Document document = new Document(PageSize.A4);
//        int a4Height = (int) PageSize.A4.getHeight();
//        // step 2
//        File pdfFile = new File("out.pdf");
//        PdfWriter writer
//                = PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
//        // step 3
//        document.open();
//        // step 4
//        // we set the compression to 0 so that we can read the PDF syntax
//        writer.setCompressionLevel(0);
//        // writes something to the direct content using a convenience method
//        PdfContentByte canvas = writer.getDirectContentUnder();
//        PDFService x = new PDFService();
//        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
//                new Phrase("Left align 5, 5"), x.convertCmToPixel(5), a4Height - x.convertCmToPixel(5), 0);
//        document.newPage();
//        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
//                new Phrase("Center align 10, 8"), x.convertCmToPixel(10), a4Height - x.convertCmToPixel(8), 0);
//        // step 5
//        document.close();
//
//        Desktop.getDesktop().open(pdfFile);
//    }

    private int convertCmToPixel(float cm) {
        return (int) ((cm / 2.54f) * 72);
    }

}
