package net.himadri.scmt.server;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import net.himadri.scmt.client.entity.*;

/**
 * Created with IntelliJ IDEA.
 * User: cskavai
 * Date: 10/21/13
 * Time: 12:04 PM
 */
public class ObjectifyUtils {
    static {
        ObjectifyService.register(PersonLap.class);
        ObjectifyService.register(VersenySzam.class);
        ObjectifyService.register(Tav.class);
        ObjectifyService.register(Versenyzo.class);
        ObjectifyService.register(Verseny.class);
        ObjectifyService.register(PageProfile.class);
        ObjectifyService.register(Nev.class);
        ObjectifyService.register(OklevelPdfBlob.class);
        ObjectifyService.register(PrintOklevelLog.class);
    }
    
    public static Objectify beginObjectify() {
        return ObjectifyService.begin();
    }
}
