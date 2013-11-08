package net.himadri.scmt.server;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreInputStream;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import net.himadri.scmt.client.entity.OklevelPdfBlob;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OklevelPDFService extends AbstractPDFService {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Long versenyId = Long.parseLong(request.getParameter("versenyId"));
        OklevelPdfBlob oklevelPdfBlob = ofy.query(OklevelPdfBlob.class).filter("versenyId", versenyId).get();
        if (oklevelPdfBlob != null) {
            try {
                String raceNumber = request.getParameter("raceNumber");
                response.setContentType("application/pdf");
                BlobKey blobKey = new BlobKey(oklevelPdfBlob.getUploadedPdfBlobKey());

                PdfReader pdfReader = new PdfReader(new BlobstoreInputStream(blobKey));
                PdfStamper pdfStamper = new PdfStamper(pdfReader, response.getOutputStream());
                PdfContentByte canvas = pdfStamper.getOverContent(1);
                if (raceNumber.equals("minta")) {
                    printSamplePage(canvas);
                } else {
                    printVersenyzo(canvas, raceNumber, versenyId);
                }
                pdfStamper.close();
                pdfReader.close();
            }catch (Exception e) {
                Logger.getLogger(OklevelPDFService.class.getName()).log(Level.SEVERE, "Oklevel nyomtatás hiba", e);
                response.setContentType("text/html; charset=UTF-8");
                response.getWriter().println("A megadott rajtszám ismeretlen");
            }
        } else {
            response.setContentType("text/html; charset=UTF-8");
            response.getWriter().println("Ehhez a versenyhez nincs oklevél feltöltve");
        }
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
