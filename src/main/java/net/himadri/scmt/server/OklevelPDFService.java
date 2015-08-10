package net.himadri.scmt.server;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreInputStream;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import net.himadri.scmt.client.entity.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class OklevelPDFService extends AbstractPDFService {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        long versenyId = Long.parseLong(request.getParameter("versenyId"));
        String raceNumber = request.getParameter("raceNumber");
        if (raceNumber == null) throw new ServletException();
        OklevelPdfBlob oklevelPdfBlob = ofy.query(OklevelPdfBlob.class).filter("versenyId", versenyId).get();
        if (oklevelPdfBlob != null) {
            try {
                Versenyzo versenyzo = null;
                if (!raceNumber.equals("minta")) {
                    versenyzo = tryMultipleVersenyzo(raceNumber, versenyId);
                }
                response.setContentType("application/pdf");
                BlobKey blobKey = new BlobKey(oklevelPdfBlob.getUploadedPdfBlobKey());
                PdfReader pdfReader = new PdfReader(new BlobstoreInputStream(blobKey));
                PdfStamper pdfStamper = new PdfStamper(pdfReader, response.getOutputStream());
                PdfContentByte canvas = pdfStamper.getOverContent(1);
                if (raceNumber.equals("minta")) {
                    printSamplePage(canvas);
                } else {
                    printVersenyzo(canvas, versenyzo);
                    ofy.put(new PrintOklevelLog(new Date(), request.getRemoteAddr(), versenyzo));
                }
                pdfStamper.close();
                pdfReader.close();
            } catch (NotExistingRunnerException e) {
                resposeSimpleText(response, "A megadott rajtszám ismeretlen: " + raceNumber);
            } catch (DocumentException e) {
                throw new ServletException(e);
            }
        } else {
            resposeSimpleText(response, "Ehhez a versenyhez nincs oklevél feltöltve");
        }
    }

    private void resposeSimpleText(HttpServletResponse response, String message) throws IOException {
        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().println(message);
    }

    private Versenyzo tryMultipleVersenyzo(String raceNumber, Long versenyId) throws NotExistingRunnerException {
        try {
            String raceNumberTry1 = Long.toString(Long.parseLong(raceNumber));
            Versenyzo versenyzoTry1 = queryVersenyzo(raceNumberTry1, versenyId);
            if (versenyzoTry1 != null) {
                return versenyzoTry1;
            } else if (raceNumberTry1.length() == 2) {
                String raceNumberTry2 = "0" + raceNumberTry1;
                Versenyzo versenyzoTry2 = queryVersenyzo(raceNumberTry2, versenyId);
                if (versenyzoTry2 != null) {
                    return versenyzoTry2;
                }
            }
        } catch (NumberFormatException e) {
        }
        throw new NotExistingRunnerException(raceNumber, versenyId);
    }

    private Versenyzo queryVersenyzo(String raceNumber, Long versenyId) {
        return ofy.query(Versenyzo.class).filter("raceNumber", raceNumber).filter("versenyId", versenyId).get();
    }


//    public static void main(String[] args) throws Exception {
//        try (FileOutputStream fileOutputStream = new FileOutputStream("oklevel.pdf")) {
//            Document document = new Document(PageSize.A4);
//            PdfWriter writer = PdfWriter.getInstance(document, fileOutputStream);
//            document.open();
//            PdfContentByte canvas = writer.getDirectContentUnder();
//            OklevelPDFService pdfService = new OklevelPDFService();
//            Map<PageProfileId, String> data = pdfService.createSampleText();
//            List<PageProfile> pageProfiles = Arrays.asList(
//                    new PageProfile(PageProfileId.EGYESULET.name(), 0, "Times-Roman", 25, 9.2f, 16.1f, true),
//                    new PageProfile(PageProfileId.HELYEZES.name(), 0, "Helvetica-Bold", 35, 11.2f, 21.8f, true),
//                    new PageProfile(PageProfileId.IDO.name(), 0, "Helvetica-Bold", 25, 5.5f, 19.5f, true),
//                    new PageProfile(PageProfileId.NEV.name(), 0, "Times-Bold", 35, 5.5f, 14.33f, true),
//                    new PageProfile(PageProfileId.VERSENYSZAM.name(), 0, "Times-Roman", 25, 9f, 17.8f, true)
//            );
//            pdfService.printSinglePage(canvas, pageProfiles, data);
//            document.close();
//        }
//    }

}
