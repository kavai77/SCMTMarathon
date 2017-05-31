package net.himadri.scmt.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.Objectify;
import net.himadri.scmt.client.MarathonService;
import net.himadri.scmt.client.entity.Tav;
import net.himadri.scmt.client.entity.Verseny;
import net.himadri.scmt.client.entity.VersenySzam;
import net.himadri.scmt.client.entity.Versenyzo;
import net.himadri.scmt.server.dto.Nevezes;
import net.himadri.scmt.server.dto.RecaptchaResponse;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.logging.Logger;

public class NevezesServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(NevezesServlet.class.getName());

    private static final String RECAPTHA_SECRET_KEY = "RECAPTHA_SECRET";

    private final ObjectMapper mapper = new ObjectMapper();
    private final MarathonService marathonService = new MarathonServiceImpl();
    private static final Objectify ofy = ObjectifyUtils.beginObjectify();
    private static final long MILLIS_IN_DAY = 24 * 60 * 60 * 1000;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        final PrintWriter writer = resp.getWriter();
        final Verseny verseny = findCurrentVerseny();
        final Nevezes nevezes;
        if (verseny == null) {
            nevezes = new Nevezes(null, false, null);
        } else {
            final List<Tav> tavList = ofy.query(Tav.class).filter("versenyId", verseny.getId()).list();
            Collections.sort(tavList, new Comparator<Tav>() {
                @Override
                public int compare(Tav o1, Tav o2) {
                    int o1Kor = o1.getKorSzam() != null ? o1.getKorSzam() : 0;
                    int o2Kor = o2.getKorSzam() != null ? o2.getKorSzam() : 0;
                    return Integer.compare(o1Kor, o2Kor);
                }
            });
            final Map<String, String> tavMap = new LinkedHashMap<>();
            for (final Tav tav: tavList) {
                tavMap.put(tav.getId().toString(), tav.getMegnevezes());
            }
            nevezes = new Nevezes(verseny.getNev(), true, tavMap);
        }

        mapper.writeValue(resp.getWriter(), nevezes);
        writer.close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        verifyRecaptcha(req);
        Versenyzo versenyzo = saveVersenyzo(req);
        sendEmail(versenyzo);
    }

    private void sendEmail(Versenyzo versenyzo) throws ServletException, IOException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        Verseny verseny = ofy.get(Verseny.class, versenyzo.getVersenyId());

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("noreply@scmtmarathon.appspot.com", "Sri Chinmoy Marathon Team"));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(versenyzo.getEmail(), versenyzo.getName()));
            msg.setSubject(verseny.getNevezesEmailSubject());
            msg.setText(verseny.getNevezesEmailText());
            Transport.send(msg);
        } catch (AddressException e) {
            throw new ServletException("Rossz email cím!");
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new IOException("Email szolgáltató hiba", e);
        }
    }

    private Versenyzo saveVersenyzo(HttpServletRequest req) throws ServletException {
        Tav tav = parseTav(req);
        String nev = req.getParameter("nev");
        boolean isFerfi = parseNem(req);
        int szuletesiEv  = parseSzuletesiEv(req);
        String egyesulet = req.getParameter("egyesulet");
        String email = req.getParameter("email");
        String raceNumber = Integer.toString(getNextRaceNumber(tav));
        Long versenySzamId = getVersenySzamId(tav, szuletesiEv, isFerfi);
        Versenyzo versenyzo = new Versenyzo(raceNumber, nev, isFerfi, szuletesiEv, egyesulet, email, versenySzamId,
                tav.getVersenyId());
        ofy.put(versenyzo);
        return versenyzo;
    }

    private void verifyRecaptcha(HttpServletRequest req) throws IOException, ServletException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost("https://www.google.com/recaptcha/api/siteverify");
        List<NameValuePair> params = new ArrayList<>(3);
        params.add(new BasicNameValuePair("secret", marathonService.getConfiguration(RECAPTHA_SECRET_KEY)));
        params.add(new BasicNameValuePair("response", req.getParameter("recaptcha")));
        params.add(new BasicNameValuePair("remoteip", req.getRemoteHost()));
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();

        InputStream instream = entity.getContent();
        try {
            String responseJson = IOUtils.toString(instream, "UTF-8");
            RecaptchaResponse value = mapper.readerFor(RecaptchaResponse.class).readValue(responseJson);
            if (!value.isSuccess()) {
                throw new ServletException("Recaptcha validation failed: " + value);
            }
        } finally {
            instream.close();
        }

    }

    private Long getVersenySzamId(Tav tav, int szuletesiEv, boolean isFerfi) throws ServletException {
        QueryResultIterator<VersenySzam> versenySzamIterator = ofy.query(VersenySzam.class).filter("tavId",
                tav.getId()).iterator();
        int eletEv = Calendar.getInstance().get(Calendar.YEAR) - szuletesiEv;
        while (versenySzamIterator.hasNext()) {
            VersenySzam versenySzam = versenySzamIterator.next();
            if (versenySzam.getFerfi() == isFerfi &&
                    (versenySzam.getKorTol() == null || versenySzam.getKorTol() <= eletEv) &&
                    (versenySzam.getKorIg() == null || eletEv <= versenySzam.getKorIg())) {
                return versenySzam.getId();
            }
        }
        throw new ServletException("Could not find versenyszam: " + tav.getMegnevezes() + " " + szuletesiEv + " " + isFerfi);
    }

    private int getNextRaceNumber(Tav tav) throws ServletException {
        QueryResultIterator<VersenySzam> versenySzamIterator = ofy.query(VersenySzam.class).filter("tavId", tav.getId()).iterator();
        Set<Long> versenySzamIdSet = new HashSet<>();
        while (versenySzamIterator.hasNext()) {
            versenySzamIdSet.add(versenySzamIterator.next().getId());
        }
        QueryResultIterator<Versenyzo> versenyzoIterator = ofy.query(Versenyzo.class).filter("versenyId", tav.getVersenyId()).iterator();
        Set<Integer> raceNumberSet = new HashSet<>();
        while (versenyzoIterator.hasNext()) {
            Versenyzo versenyzo = versenyzoIterator.next();
            if (versenySzamIdSet.contains(versenyzo.getVersenySzamId())) {
                try {
                    raceNumberSet.add(Integer.parseInt(versenyzo.getRaceNumber()));
                } catch (NumberFormatException ignored) {
                    LOG.warning("Not a numeric race number: " + versenyzo.getRaceNumber());
                }
            }
        }
        for (int i = tav.getVersenySzamtol(); i <= tav.getVersenySzamig(); i++) {
            if (!raceNumberSet.contains(i)) {
                return i;
            }
        }
        throw new ServletException("Nevezés betelt");
    }

    private Tav parseTav(HttpServletRequest req) {
        long tavId = Long.parseLong(req.getParameter("tav"));
        return ofy.get(Tav.class, tavId);
    }

    private int parseSzuletesiEv(HttpServletRequest req) {
        int ev = Integer.parseInt(req.getParameter("ev"));
        if (ev < 1900 || ev > Calendar.getInstance().get(Calendar.YEAR)) {
            throw new IllegalArgumentException("Rossz év: " + ev);
        }
        return ev;
    }

    private boolean parseNem(HttpServletRequest req) {
        String nem = req.getParameter("nem");
        switch (nem.toLowerCase()) {
            case "ferfi":
                return true;
            case "no":
                return false;
            default:
                throw new IllegalArgumentException("Rossz nem: " + nem);
        }
    }

    private Verseny findCurrentVerseny() {
        QueryResultIterator<Verseny> iterator = ofy.query(Verseny.class).iterator();
        long timeInMillis = Calendar.getInstance(TimeZone.getTimeZone("Europe/Budapest")).getTimeInMillis();
        while (iterator.hasNext()) {
            Verseny currentVerseny = iterator.next();
            if (currentVerseny.getNevezesBegin() != null &&
                currentVerseny.getNevezesEnd() != null &&
                currentVerseny.getNevezesBegin() <= timeInMillis &&
                timeInMillis <= currentVerseny.getNevezesEnd() + MILLIS_IN_DAY) {
                return currentVerseny;
            }
        }
        return null;
    }
}
