package net.himadri.scmt.server;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.06.01. 6:59
 */
public class PrePrintedPDFService extends AbstractPDFService {
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
