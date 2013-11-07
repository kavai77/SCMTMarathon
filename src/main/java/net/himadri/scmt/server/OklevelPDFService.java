package net.himadri.scmt.server;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.itextpdf.text.DocumentException;
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
            String rajtszam = request.getParameter("rajtSzam");
            response.setContentType("application/pdf");
            if (rajtszam.equals("minta")) {
                printOklevel(oklevelPdfBlob, response);
            }
        } else {
            response.setCharacterEncoding("UTF-8");
            response.getWriter().println("Ehhez a versenyhez nincs oklevél feltöltve");
        }
    }

    private void printOklevel(OklevelPdfBlob oklevelPdfBlob, HttpServletResponse response) throws IOException {
        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        byte[] bytes = blobstoreService.fetchData(new BlobKey(oklevelPdfBlob.getUploadedPdfBlobKey()), 0, oklevelPdfBlob.getSize());
        try {
            PdfStamper pdfStamper = new PdfStamper(new PdfReader(bytes), response.getOutputStream());
            PdfContentByte canvas = pdfStamper.getOverContent(1);
            printSamplePage(canvas);
        }catch (DocumentException e) {
            throw new IOException(e);
        }
    }

}
