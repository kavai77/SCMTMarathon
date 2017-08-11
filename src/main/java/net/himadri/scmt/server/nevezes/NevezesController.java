package net.himadri.scmt.server.nevezes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.googlecode.objectify.Objectify;
import net.himadri.scmt.client.MarathonService;
import net.himadri.scmt.client.Utils;
import net.himadri.scmt.client.entity.Tav;
import net.himadri.scmt.client.entity.Verseny;
import net.himadri.scmt.client.entity.VersenySzam;
import net.himadri.scmt.client.entity.Versenyzo;
import net.himadri.scmt.server.UserServiceImpl;
import net.himadri.scmt.server.dto.ListOfAthletes;
import net.himadri.scmt.server.dto.Nevezes;
import net.himadri.scmt.server.dto.NevezesRequest;
import net.himadri.scmt.server.dto.RecaptchaResponse;
import net.himadri.scmt.server.exception.NevezesException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.appengine.api.search.checkers.Preconditions.checkArgument;

@RestController
@RequestMapping(value = "/nevezes")
public class NevezesController {
    private static final Logger LOG = Logger.getLogger(NevezesController.class.getName());

    private static final String RECAPTHA_SECRET_KEY = "RECAPTHA_SECRET";
    private static final long MILLIS_IN_DAY = 24 * 60 * 60 * 1000;
    private static final String GOOGLE_RECAPTCHA_SITEVERIFY = "https://www.google.com/recaptcha/api/siteverify";
    private static final int RECAPCTHA_EXPIRATION_SECONDS = 300;
    private static InternetAddress EMAIL_FROM_ADDRESS;
    private static InternetAddress EMAIL_REPLY_TO_ADDRESS;
    static {
        try {
            EMAIL_FROM_ADDRESS = new InternetAddress("noreply@scmtmarathon.appspotmail.com", "Sri Chinmoy Marathon Team");
            EMAIL_REPLY_TO_ADDRESS = new InternetAddress("info@srichinmoyversenyek.hu");
        } catch (UnsupportedEncodingException | AddressException e) {
            throw new RuntimeException(e);
        }
    }

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private Objectify ofy;

    @Autowired
    private MarathonService marathonService;

    private static MemcacheService recaptchaMemcacheService = MemcacheServiceFactory.getMemcacheService("recaptcha");

    @RequestMapping(value = "/get", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Nevezes getNevezes() throws IOException {
        final Verseny verseny = findCurrentVerseny();
        if (verseny == null) {
            return new Nevezes(null, false, null, null);
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
            return new Nevezes(verseny.getNev(), true, tavMap, verseny.getNevezesEmailText());
        }
    }

    @RequestMapping(value = "/store", method = RequestMethod.POST)
    public void storeNevezes(@RequestBody NevezesRequest nevezesRequest, HttpServletRequest req) throws IOException, NevezesException {
        verifyRecaptcha(nevezesRequest, req);
        Versenyzo versenyzo = saveVersenyzo(nevezesRequest);
        sendEmail(versenyzo);
        emailSuperUser("Sikeres nevezés", versenyzo.toString());
    }

    @RequestMapping(value = "/getListOfAthletes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ListOfAthletes getListOfAthletes() throws IOException {
        final Verseny verseny = findCurrentVerseny();
        if (verseny == null) {
            return new ListOfAthletes(null, false, null);
        } else {
            Map<Long, String> versenySzamMap = getVersenySzamMap(verseny);
            List<Versenyzo> versenyzoList = ofy.query(Versenyzo.class)
                    .filter("versenyId", verseny.getId())
                    .order("name")
                    .list();
            List<ListOfAthletes.Athlete> athleteList = new ArrayList<>(versenyzoList.size());
            for (Versenyzo versenyzo: versenyzoList) {
                ListOfAthletes.Athlete athlete = new ListOfAthletes.Athlete(versenyzo.getRaceNumber(), versenyzo.getName(),
                        versenyzo.getEgyesulet(), versenySzamMap.get(versenyzo.getVersenySzamId()));
                athleteList.add(athlete);
            }
            return new ListOfAthletes(verseny.getNev(), true, athleteList);
        }
    }

    private Map<Long, String> getVersenySzamMap(Verseny verseny) {
        List<VersenySzam> versenySzamList = ofy.query(VersenySzam.class).filter("versenyId", verseny.getId()).list();
        Map<Long, Tav> tavMap = new HashMap<>();
        Map<Long, String> versenySzamMap = new HashMap<>();
        for (VersenySzam versenySzam: versenySzamList) {
            Tav tav = tavMap.get(versenySzam.getTavId());
            if (tav == null) {
                tav = ofy.get(Tav.class, versenySzam.getTavId());
                tavMap.put(tav.getId(), tav);
            }
            versenySzamMap.put(versenySzam.getId(), Utils.getVersenySzamMegnevezes(tav, versenySzam));
        }
        return versenySzamMap;
    }

    @ExceptionHandler(NevezesException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleNevezesException(Exception e) {
        LOG.log(Level.WARNING, e.getMessage());
        alertSuperUser(e);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public void handleException(Exception e) {
        LOG.log(Level.SEVERE, e.getMessage());
        alertSuperUser(e);
    }

    private void sendEmail(Versenyzo versenyzo) throws IOException, NevezesException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        Verseny verseny = ofy.get(Verseny.class, versenyzo.getVersenyId());

        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(EMAIL_FROM_ADDRESS);
            msg.setReplyTo(new Address[]{EMAIL_REPLY_TO_ADDRESS});
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(versenyzo.getEmail(), versenyzo.getName()));
            msg.setSubject(verseny.getNevezesEmailSubject(), "UTF-8");
            msg.setText(verseny.getNevezesEmailText(), "UTF-8");
            Transport.send(msg);
        } catch (AddressException e) {
            throw new NevezesException("Rossz email cím! " + versenyzo.toString());
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new IOException("Email szolgáltató hiba", e);
        }
    }

    private Versenyzo saveVersenyzo(NevezesRequest nevezes) throws NevezesException, IOException {
        Tav tav = ofy.get(Tav.class, nevezes.getTav());
        boolean isFerfi = parseNem(nevezes.getNem());
        String raceNumber = Integer.toString(getNextRaceNumber(tav));
        Long versenySzamId = getVersenySzamId(tav, nevezes.getEv(), isFerfi);
        checkArgument(nevezes.getEv() >= 1900 && nevezes.getEv() < Calendar.getInstance().get(Calendar.YEAR),
                "Rossz év: " + nevezes.getEv());
        Versenyzo versenyzo = new Versenyzo(raceNumber, nevezes.getNev(), isFerfi, nevezes.getEv(),
                nevezes.getEgyesulet(), nevezes.getEmail(), versenySzamId, tav.getVersenyId());
        versenyzo.setPoloMeret(nevezes.getPoloMeret());
        versenyzo.setLicenszSzam(nevezes.getLicenszSzam());
        ofy.put(versenyzo);
        return versenyzo;
    }

    private void verifyRecaptcha(NevezesRequest nevezesRequest, HttpServletRequest req) throws NevezesException, IOException {
        String recaptcha = nevezesRequest.getRecaptcha();
        if (recaptchaMemcacheService.contains(recaptcha)) {
            if (!(boolean) recaptchaMemcacheService.get(recaptcha)) {
                throw new NevezesException("Recaptcha ellenőrzés hibát adott a cache-bol: " + nevezesRequest.toString());
            }
            return;
        }

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(GOOGLE_RECAPTCHA_SITEVERIFY);
        List<NameValuePair> params = new ArrayList<>(3);
        params.add(new BasicNameValuePair("secret", marathonService.getConfiguration(RECAPTHA_SECRET_KEY)));
        params.add(new BasicNameValuePair("response", recaptcha));
        params.add(new BasicNameValuePair("remoteip", req.getRemoteHost()));
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();

        InputStream instream = entity.getContent();
        try {
            String responseJson = IOUtils.toString(instream, "UTF-8");
            RecaptchaResponse value = mapper.readerFor(RecaptchaResponse.class).readValue(responseJson);
            recaptchaMemcacheService.put(recaptcha, value.isSuccess(), Expiration.byDeltaSeconds(RECAPCTHA_EXPIRATION_SECONDS));
            if (!value.isSuccess()) {
                throw new NevezesException("Recaptcha ellenőrzés hibát adott: " + value + " " + nevezesRequest.toString());
            }
        } finally {
            instream.close();
        }

    }

    private Long getVersenySzamId(Tav tav, int szuletesiEv, boolean isFerfi) throws IOException {
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
        throw new IOException("Nem található versenyszám: " + tav.getMegnevezes() + " " + szuletesiEv + " " + isFerfi);
    }

    private int getNextRaceNumber(Tav tav) throws NevezesException {
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
        throw new NevezesException("Nevezés betelt. " + tav.toString());
    }

    private boolean parseNem(String nem) {
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

    private void alertSuperUser(Exception exception) {
        emailSuperUser("SCMT Nevezés Hiba", getStackTraceAsString(exception));
    }

    private void emailSuperUser(String subject, String text) {
        String superUserEmail = marathonService.getConfiguration(UserServiceImpl.SUPER_USER_KEY);
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(EMAIL_FROM_ADDRESS);
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(superUserEmail));
            msg.setSubject(subject, "UTF-8");
            msg.setText(text, "UTF-8");
            Transport.send(msg);
        } catch (MessagingException e) {
            LOG.log(Level.SEVERE, "Alert küldés hiba", e);
        }
    }

    private String getStackTraceAsString(Exception exception) {
        StringWriter buffer  = new StringWriter();
        exception.printStackTrace(new PrintWriter(buffer));
        return buffer.toString();
    }
}
