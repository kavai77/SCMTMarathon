package net.himadri.scmt.server;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.googlecode.objectify.Objectify;
import net.himadri.scmt.client.entity.OklevelPdfBlob;
import net.himadri.scmt.client.entity.Verseny;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class OklevelUploadHandler extends HttpServlet {
    private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        BlobInfo blobInfo = blobstoreService.getBlobInfos(req).get("pdfFile").get(0);
        String versenyId = req.getParameter("versenyId");
        Objectify ofy = ObjectifyUtils.beginObjectify();
        Verseny verseny = ofy.get(Verseny.class, Long.parseLong(versenyId));
        OklevelPdfBlob existingOklevelPdfBlob = ofy.query(OklevelPdfBlob.class).filter("versenyId", verseny.getId()).get();
        if (existingOklevelPdfBlob != null) {
            blobstoreService.delete(new BlobKey(existingOklevelPdfBlob.getUploadedPdfBlobKey()));
            ofy.delete(existingOklevelPdfBlob);
        }
        OklevelPdfBlob oklevelPdfBlob = new OklevelPdfBlob(verseny.getId(),
                blobInfo.getBlobKey().getKeyString(), blobInfo.getSize());
        ofy.put(oklevelPdfBlob);
    }
}
