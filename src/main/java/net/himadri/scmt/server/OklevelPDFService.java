package net.himadri.scmt.server;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import net.himadri.scmt.client.entity.OklevelPdfBlob;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class OklevelPDFService extends AbstractPDFService {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Long versenyId = Long.parseLong(request.getParameter("versenyId"));
        OklevelPdfBlob oklevelPdfBlob = ofy.query(OklevelPdfBlob.class).filter("versenyId", versenyId).get();
        if (oklevelPdfBlob != null) {
            try {
                String raceNumber = request.getParameter("raceNumber");
                response.setContentType("application/pdf");
                BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
                byte[] bytes = blobstoreService.fetchData(new BlobKey(oklevelPdfBlob.getUploadedPdfBlobKey()), 0, oklevelPdfBlob.getSize());

                PdfReader pdfReader = new PdfReader(bytes);
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
