package net.himadri.scmt.server;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreInputStream;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import net.himadri.scmt.client.entity.OklevelPdfBlob;
import net.himadri.scmt.client.entity.PrintOklevelLog;
import net.himadri.scmt.client.entity.Versenyzo;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

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
                Logger.getLogger(OklevelPDFService.class.getName()).log(Level.SEVERE, "Oklevel nyomtatás hiba", e);
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
//
//        PdfReader reader = new PdfReader("/home/himadri/Documents/CsabaKavaiCV-en.pdf");
//        PdfStamper pdfStamper = new PdfStamper(reader, new FileOutputStream("oklevel.pdf"));
//        PdfContentByte canvas = pdfStamper.getOverContent(1);
//        OklevelPDFService pdfService = new OklevelPDFService();
//        Map<PageProfileId, String> data = pdfService.createSampleText();
//        List<PageProfile> pageProfiles = Arrays.asList(
//                new PageProfile(PageProfileId.NEV, 5, 5, 0, BaseFont.COURIER, 14)
//        );
//        pdfService.printSinglePage(canvas, pageProfiles, data);
//        pdfStamper.close();
//        reader.close();
//    }

}
