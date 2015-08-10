package net.himadri.scmt.server;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Query;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import net.himadri.scmt.client.Utils;
import net.himadri.scmt.client.entity.PageProfile;
import net.himadri.scmt.client.entity.PageProfileId;
import net.himadri.scmt.client.entity.PersonLap;
import net.himadri.scmt.client.entity.Tav;
import net.himadri.scmt.client.entity.VersenySzam;
import net.himadri.scmt.client.entity.Versenyzo;

import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbstractPDFService extends HttpServlet {
    protected static Objectify ofy = ObjectifyUtils.beginObjectify();

    private class VersenyzoResult {
        private Versenyzo versenyzo;
        private Long ido;

        private VersenyzoResult(Versenyzo versenyzo, Long ido) {
            this.versenyzo = versenyzo;
            this.ido = ido;
        }
    }

    protected void printVersenyzo(PdfContentByte canvas, Versenyzo versenyzo) throws IOException, DocumentException {
        List<PageProfile> pageProfiles = new MarathonServiceImpl().getAllPageProfiles();
        VersenySzam versenySzam = ofy.get(VersenySzam.class, versenyzo.getVersenySzamId());
        Tav tav = ofy.get(Tav.class, versenySzam.getTavId());
        List<VersenyzoResult> versenySzamResult = createVersenySzamResult(versenySzam, tav);
        int index = findVersenyzoInResults(versenySzamResult, versenyzo);
        Map<PageProfileId, String> pageData = createPageData(versenySzam, tav, versenySzamResult, index);
        printSinglePage(canvas, pageProfiles, pageData);
    }

    protected void printVersenySzam(PdfContentByte canvas, Document document, VersenySzam versenySzam, boolean csakDobogo) throws IOException, DocumentException {
        List<PageProfile> pageProfiles = new MarathonServiceImpl().getAllPageProfiles();
        Tav tav = ofy.get(Tav.class, versenySzam.getTavId());
        List<VersenyzoResult> versenyzoResults = createVersenySzamResult(versenySzam, tav);
        int size = versenyzoResults.size();
        if (csakDobogo) size = Math.min(size, 3);
        for (int i = 0; i < size; i++) {
            Map<PageProfileId, String> data = createPageData(versenySzam, tav, versenyzoResults, i);
            printSinglePage(canvas, pageProfiles, data);
            document.newPage();
        }
    }

    protected void printTav(PdfContentByte canvas, Document document, long tavId, boolean csakDobogo) throws IOException, DocumentException {
        Query<VersenySzam> versenySzamQuery = ofy.query(VersenySzam.class).filter("tavId", tavId);
        for (VersenySzam versenySzam: versenySzamQuery) {
            printVersenySzam(canvas, document, versenySzam, csakDobogo);
        }
    }

    protected void printSamplePage(PdfContentByte canvas) throws IOException, DocumentException {
        List<PageProfile> pageProfiles = new MarathonServiceImpl().getAllPageProfiles();
        Map<PageProfileId, String> data = createSampleText();
        printSinglePage(canvas, pageProfiles, data);
    }

    protected Map<PageProfileId, String> createSampleText() {
        Map<PageProfileId, String> data = new HashMap<PageProfileId, String>();
        data.put(PageProfileId.NEV, "Árvíztűrő tükörfúrógép");
        data.put(PageProfileId.EGYESULET, "Futóbolondok");
        data.put(PageProfileId.VERSENYSZAM, "Maraton férfi 18-30 év");
        data.put(PageProfileId.IDO, "3:57:12");
        data.put(PageProfileId.HELYEZES, "XVII.");
        return data;
    }

    protected void printSinglePage(PdfContentByte canvas, List<PageProfile> pageProfiles, Map<PageProfileId, String> data) throws IOException, DocumentException {
        for (PageProfile pageProfile: pageProfiles) {
            String entry = data.get(PageProfileId.valueOf(pageProfile.getId()));
            if (pageProfile.isPrintProfile() && entry != null) {
                String fontFamily = pageProfile.getFontFamily();
                if (Utils.isEmpty(fontFamily)) fontFamily = BaseFont.TIMES_ROMAN;
                int size = pageProfile.getSize();
                if (size == 0) size = 10;
                ColumnText.showTextAligned(canvas, pageProfile.getAlignment(),
                        new Phrase(entry, FontFactory.getFont(fontFamily, BaseFont.CP1250, BaseFont.EMBEDDED, size)),
                        convertCmToPixel(pageProfile.getxAxis()),
                        (int) PageSize.A4.getHeight() - convertCmToPixel(pageProfile.getyAxis()), 0);
            }
        }
    }

    protected void printEmptyPage(PdfContentByte canvas, String message) throws IOException, DocumentException {
        printSinglePage(canvas,
                Collections.singletonList(new PageProfile(PageProfileId.NEV.name(), 0, "Times-Roman", 15, 1, 1, true)),
                Collections.singletonMap(PageProfileId.NEV, message) );
    }

    private int findVersenyzoInResults(List<VersenyzoResult> versenySzamResult, Versenyzo versenyzo) {
        for (int i = 0; i < versenySzamResult.size(); i++) {
            if (versenySzamResult.get(i).versenyzo.getId().equals(versenyzo.getId())) {
                return i;
            }
        }
        throw new IllegalStateException("Could not found versenyzo in versenyszamresults: " + versenyzo.getRaceNumber());
    }

    private Map<PageProfileId, String> createPageData(VersenySzam versenySzam, Tav tav, List<VersenyzoResult> versenyzoResults, int index) {
        Versenyzo versenyzo = versenyzoResults.get(index).versenyzo;
        Map<PageProfileId, String> data = new HashMap<PageProfileId, String>();
        data.put(PageProfileId.NEV, versenyzo.getName());
        data.put(PageProfileId.EGYESULET, versenyzo.getEgyesulet());
        data.put(PageProfileId.VERSENYSZAM, Utils.getVersenySzamMegnevezes(tav, versenySzam));
        data.put(PageProfileId.IDO, Utils.getElapsedTimeString(versenyzoResults.get(index).ido));
        data.put(PageProfileId.HELYEZES, Utils.numberToRoman(index + 1) + ".");
        return data;
    }

    private List<VersenyzoResult> createVersenySzamResult(VersenySzam versenySzam, Tav tav) {
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
                Long ido = korIdok.get(tav.getKorSzam() - 1) - tav.getRaceStartDiff();
                versenyzoResults.add(new VersenyzoResult(versenyzo, ido));
            }
        }
        Collections.sort(versenyzoResults, new Comparator<VersenyzoResult>() {
            @Override
            public int compare(VersenyzoResult o1, VersenyzoResult o2) {
                return o1.ido.compareTo(o2.ido);
            }
        });
        return versenyzoResults;
    }

    private int convertCmToPixel(float cm) {
        return (int) ((cm / 2.54f) * 72);
    }

}
