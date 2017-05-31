package net.himadri.scmt.server.dbcopy;

import com.googlecode.objectify.Objectify;
import net.himadri.scmt.client.entity.*;
import net.himadri.scmt.client.serializable.EntityCollector;
import net.himadri.scmt.server.ObjectifyUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.19. 22:19
 */
public class EntityCollectorServlet extends HttpServlet {
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Objectify obj = ObjectifyUtils.beginObjectify();
        EntityCollector entityCollector = new EntityCollector();
        entityCollector.setNevList(obj.query(Nev.class).list());
        entityCollector.setPageProfileList(obj.query(PageProfile.class).list());
        entityCollector.setPersonLapList(obj.query(PersonLap.class).list());
        entityCollector.setTavList(obj.query(Tav.class).list());
        entityCollector.setVersenyList(obj.query(Verseny.class).list());
        entityCollector.setVersenySzamList(obj.query(VersenySzam.class).list());
        entityCollector.setVersenyzoList(obj.query(Versenyzo.class).list());
        entityCollector.setConfigurationList(obj.query(Configuration.class).list());

        response.setContentType("application/octet-stream");
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(response.getOutputStream());
        objectOutputStream.writeObject(entityCollector);
    }
}
