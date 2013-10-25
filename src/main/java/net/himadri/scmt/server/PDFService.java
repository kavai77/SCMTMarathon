package net.himadri.scmt.server;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Query;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import net.himadri.scmt.client.Utils;
import net.himadri.scmt.client.entity.PageProfile;
import net.himadri.scmt.client.entity.PageProfileId;
import net.himadri.scmt.client.entity.PersonLap;
import net.himadri.scmt.client.entity.Tav;
import net.himadri.scmt.client.entity.VersenySzam;
import net.himadri.scmt.client.entity.Versenyzo;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.06.01. 6:59
 */
public class PDFService extends HttpServlet {
    private static Objectify ofy = ObjectifyUtils.beginObjectify();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String tavId = request.getParameter("tav");
//        response.addHeader("Content-Disposition", "attachment; filename=raceresult.pdf");
        response.setContentType("application/pdf");
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
            document.open();
            PdfContentByte canvas = writer.getDirectContentUnder();
            if ("minta".equals(tavId)) {
                printSamplePage(canvas);
            } else {
                printTav(canvas, document, Long.parseLong(tavId));
            }
            document.close();
        } catch (DocumentException e) {
            throw new IOException(e);
        }
    }

    private void printTav(PdfContentByte canvas, Document document, long tavId) throws IOException, DocumentException {
        Query<VersenySzam> versenySzamQuery = ofy.query(VersenySzam.class).filter("tavId", tavId);
        for (VersenySzam versenySzam: versenySzamQuery) {
            printVersenySzam(canvas, document, versenySzam);
        }
    }

    private void printVersenySzam(PdfContentByte canvas, Document document, VersenySzam versenySzam) throws IOException, DocumentException {
        List<PageProfile> pageProfiles = new MarathonServiceImpl().getAllPageProfiles();
        Tav tav = ofy.get(Tav.class, versenySzam.getTavId());
        Query<Versenyzo> versenyzoQuery = ofy.query(Versenyzo.class)
                .filter("versenySzamId", versenySzam.getId())
                .filter("versenyId", versenySzam.getVersenyId());
        Map<String, Versenyzo> raceNumberVersenyzoMap = new HashMap<String, Versenyzo>();
        for (Versenyzo versenyzo: versenyzoQuery) {
            raceNumberVersenyzoMap.put(versenyzo.getRaceNumber(), versenyzo);
        }

        Query<PersonLap> personLapQuery = ofy.query(PersonLap.class)
                .filter("versenyId", versenySzam.getVersenyId())
                .order("time");
        Map<String, List<Long>> raceNumberPersonLapListMap = new HashMap<String, List<Long>>();
        for (PersonLap personLap: personLapQuery) {
            List<Long> personLapList = raceNumberPersonLapListMap.get(personLap.getRaceNumber());
            if (personLapList == null) {
                personLapList = new ArrayList<Long>();
                raceNumberPersonLapListMap.put(personLap.getRaceNumber(), personLapList);
            }
            personLapList.add(personLap.getTime());
        }

        List<VersenyzoResult> versenyzoResults = new ArrayList<VersenyzoResult>();
        for (Map.Entry<String, List<Long>> raceNumberPersonLapEntry: raceNumberPersonLapListMap.entrySet()) {
            String raceNumber = raceNumberPersonLapEntry.getKey();
            List<Long> korIdok = raceNumberPersonLapEntry.getValue();
            if (raceNumberVersenyzoMap.containsKey(raceNumber) &&
                    korIdok.size() >= tav.getKorSzam()) {
                Versenyzo versenyzo = raceNumberVersenyzoMap.get(raceNumber);
                Long ido = korIdok.get(tav.getKorSzam() - 1);
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
            document.newPage();
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

    private void printSamplePage(PdfContentByte canvas) throws IOException, DocumentException {
        List<PageProfile> pageProfiles = new MarathonServiceImpl().getAllPageProfiles();
        Map<PageProfileId, String> data = createSampleText();
        printSinglePage(canvas, pageProfiles, data);
    }

    private Map<PageProfileId, String> createSampleText() {
        Map<PageProfileId, String> data = new HashMap<PageProfileId, String>();
        data.put(PageProfileId.NEV, "Árvíztűrő tükörfúrógép");
        data.put(PageProfileId.EGYESULET, "Futóbolondok");
        data.put(PageProfileId.VERSENYSZAM, "Maraton férfi 18-30 év");
        data.put(PageProfileId.IDO, "3:57:12");
        data.put(PageProfileId.HELYEZES, "XVII.");
        return data;
    }

    private void printSinglePage(PdfContentByte canvas, List<PageProfile> pageProfiles, Map<PageProfileId, String> data) throws IOException, DocumentException {
        for (PageProfile pageProfile: pageProfiles) {
            String entry = data.get(PageProfileId.valueOf(pageProfile.getId()));
            if (entry != null && (pageProfile.getxAxis() > 0 || pageProfile.getyAxis() > 0)) {
                String fontFamily = pageProfile.getFontFamily();
                if (Utils.isEmpty(fontFamily)) fontFamily = BaseFont.TIMES_ROMAN;
                int size = pageProfile.getSize();
                if (size == 0) size = 10;
                ColumnText.showTextAligned(canvas, pageProfile.getAlignment(),
                    new Phrase(entry, new Font(BaseFont.createFont(fontFamily, BaseFont.CP1250, BaseFont.EMBEDDED), size)),
                        convertCmToPixel(pageProfile.getxAxis()),
                        (int) PageSize.A4.getHeight() - convertCmToPixel(pageProfile.getyAxis()), 0);
            }
        }
    }

//    public static void main(String[] args) throws IOException, DocumentException {
//        Document document = new Document(PageSize.A4);
//        File pdfFile = new File("out.pdf");
//        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
//        document.open();
//
//        PdfContentByte canvas = writer.getDirectContentUnder();
//        PDFService pdfService = new PDFService();
//        Map<PageProfileId, String> data = pdfService.createSampleText();
//        List<PageProfile> pageProfiles = Arrays.asList(
//                new PageProfile(PageProfileId.NEV, 5, 5, 0, BaseFont.COURIER, 14)
//        );
//        pdfService.printSinglePage(canvas, pageProfiles, data);
//        document.close();
//
//        Desktop.getDesktop().open(pdfFile);
//    }

    private int convertCmToPixel(float cm) {
        return (int) ((cm / 2.54f) * 72);
    }

}
