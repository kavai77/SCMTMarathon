package net.himadri.scmt.server.dbcopy;

import com.google.appengine.api.utils.SystemProperty;
import com.googlecode.objectify.Objectify;
import net.himadri.scmt.client.entity.Nev;
import net.himadri.scmt.client.entity.PageProfile;
import net.himadri.scmt.client.entity.PersonLap;
import net.himadri.scmt.client.entity.Tav;
import net.himadri.scmt.client.entity.Verseny;
import net.himadri.scmt.client.entity.VersenySzam;
import net.himadri.scmt.client.entity.Versenyzo;
import net.himadri.scmt.client.serializable.EntityCollector;
import net.himadri.scmt.server.ObjectifyUtils;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.19. 22:19
 */
public class EntityFetcherServlet extends HttpServlet {
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (SystemProperty.environment.value() != SystemProperty.Environment.Value.Development) {
            throw new ServletException("Only supported in development mode!");
        }
        ServletFileUpload upload = new ServletFileUpload();
        Objectify ofy = ObjectifyUtils.beginObjectify();
        try {
            FileItemIterator iter = upload.getItemIterator(request);

            InputStream fileInput = null;
            while (iter.hasNext()) {
                FileItemStream next = iter.next();
                if (!next.isFormField()) {
                    fileInput = next.openStream();
                    break;
                }
            }
            
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInput);
            EntityCollector entityCollector = (EntityCollector) objectInputStream.readObject();
            PrintWriter printWriter = response.getWriter();
            replaceEntities(ofy, Nev.class, entityCollector.getNevList(), printWriter);            
            replaceEntities(ofy, PageProfile.class, entityCollector.getPageProfileList(), printWriter);
            replaceEntities(ofy, PersonLap.class, entityCollector.getPersonLapList(), printWriter);
            replaceEntities(ofy, Tav.class, entityCollector.getTavList(), printWriter);
            replaceEntities(ofy, Verseny.class, entityCollector.getVersenyList(), printWriter);
            replaceEntities(ofy, VersenySzam.class, entityCollector.getVersenySzamList(), printWriter);
            replaceEntities(ofy, Versenyzo.class, entityCollector.getVersenyzoList(), printWriter);
            
            
            
        } catch (ClassNotFoundException | FileUploadException e) {
            throw new IOException(e);
        } 
    }
    
    private <T> void replaceEntities(Objectify ofy, Class<T> clazz, List<T> enitites, PrintWriter printWriter) {
        ofy.delete(ofy.query(clazz));
        ofy.put(enitites);
        printWriter.println(clazz.getSimpleName() + " sikeresen beimportálva!");
    }
}
