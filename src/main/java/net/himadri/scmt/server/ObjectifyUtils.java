package net.himadri.scmt.server;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import net.himadri.scmt.client.entity.Nev;
import net.himadri.scmt.client.entity.PageProfile;
import net.himadri.scmt.client.entity.PersonLap;
import net.himadri.scmt.client.entity.Tav;
import net.himadri.scmt.client.entity.Verseny;
import net.himadri.scmt.client.entity.VersenySzam;
import net.himadri.scmt.client.entity.Versenyzo;

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
    }
    
    public static Objectify beginObjectify() {
        return ObjectifyService.begin();
    }
}
