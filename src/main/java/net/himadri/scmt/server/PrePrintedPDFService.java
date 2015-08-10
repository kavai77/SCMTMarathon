package net.himadri.scmt.server;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import net.himadri.scmt.client.entity.PageProfile;
import net.himadri.scmt.client.entity.PageProfileId;
import net.himadri.scmt.client.entity.VersenySzam;
import net.himadri.scmt.client.entity.Versenyzo;
import net.himadri.scmt.client.serializable.PdfServiceType;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.06.01. 6:59
 */
public class PrePrintedPDFService extends AbstractPDFService {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PdfServiceType type = PdfServiceType.valueOf(request.getParameter("type"));
        String id = request.getParameter("id");
        response.setContentType("application/pdf");
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
            document.open();
            PdfContentByte canvas = writer.getDirectContentUnder();
            switch (type) {
                case MINTA:
                    printSamplePage(canvas);
                    break;
                case TAV:
                    printTav(canvas, document, Long.parseLong(id), false);
                    break;
                case DOBOGO:
                    printTav(canvas, document, Long.parseLong(id), true);
                    break;
                case VERSENYSZAM:
                    VersenySzam versenySzam = ofy.get(VersenySzam.class, Long.parseLong(id));
                    int count = ofy.query(Versenyzo.class)
                            .filter("versenySzamId", versenySzam.getId())
                            .filter("versenyId", versenySzam.getVersenyId()).count();
                    if (count > 0) {
                        printVersenySzam(canvas, document, versenySzam, false);
                    } else {
                        printEmptyPage(canvas, "Ebben a versenyszámban nem indult versenyzõ.");
                    }
                    break;
                case VERSENYZO:
                    Long versenyId = Long.valueOf(request.getParameter("versenyId"));
                    Versenyzo versenyzo = ofy.query(Versenyzo.class).filter("versenyId", versenyId).filter("raceNumber", id).get();
                    if (versenyzo != null) {
                        printVersenyzo(canvas, versenyzo);
                    } else {
                        printEmptyPage(canvas, "Nem található ilyen rajtszámmal versenyzõ");
                    }
                    break;
                    
            }

            document.close();
        } catch (DocumentException e) {
            throw new IOException(e);
        }
    }

//    public static void main(String[] args) throws IOException, DocumentException {
//        Document document = new Document(PageSize.A4);
//        File pdfFile = new File("out.pdf");
//        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
//        document.open();
//
//        PdfContentByte canvas = writer.getDirectContentUnder();
//        PrePrintedPDFService pdfService = new PrePrintedPDFService();
//        Map<PageProfileId, String> data = pdfService.createSampleText();
//        List<PageProfile> pageProfiles = Arrays.asList(
//                new PageProfile(PageProfileId.NEV, 5, 5, 0, BaseFont.COURIER, 14)
//        );
//        pdfService.printSinglePage(canvas, pageProfiles, data);
//        document.close();
//
//        Desktop.getDesktop().open(pdfFile);
//    }



}
