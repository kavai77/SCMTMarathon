package net.himadri.scmt.server;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.googlecode.objectify.Objectify;
import net.himadri.scmt.client.entity.OklevelPdfBlob;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ServeEmptyOklevelPdf extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Long versenyId = Long.parseLong(request.getParameter("versenyId"));
        Objectify ofy = ObjectifyUtils.beginObjectify();
        OklevelPdfBlob oklevelPdfBlob = ofy.query(OklevelPdfBlob.class).filter("versenyId", versenyId).get();
        if (oklevelPdfBlob != null) {
            BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
            BlobKey blobKey = new BlobKey(oklevelPdfBlob.getUploadedPdfBlobKey());
            response.setContentType("application/pdf");
            blobstoreService.serve(blobKey, response);
        } else {
            response.getWriter().println("Nincs oklevél feltöltve ehhez a versenyhez");
        }
    }
}
