package org.foi.nwtis.anddanzan.web.zrna;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.mail.imap.IMAPInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.foi.nwtis.anddanzan.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.anddanzan.web.slusaci.SlusacAplikacije;

/**
 *
 * @author Andrea
 */
@Named(value = "slanjePoruka")
@RequestScoped
public class SlanjePoruka {

    private String posluzitelj;
    private String prima;
    private String salje;
    private String predmet;
    private String privitak;
    private List<String> popisDatoteka;
    private String odabranaDatoteka;

    BP_Konfiguracija konfiguracija = (BP_Konfiguracija) SlusacAplikacije.kontekst.getAttribute("BP_Konfig");

    /**
     * Creates a new instance of SlanjePoruka
     */
    public SlanjePoruka() {
        this.privitak = "{}";
        this.posluzitelj = konfiguracija.getMailServer();
        this.prima = konfiguracija.getMailUsernameThread();
        this.salje = konfiguracija.getUsernameEmail();
        this.predmet = konfiguracija.getSubjectEmail();

        this.popisDatoteka = dohvatiJsonDatoteke();
    }

    public String getPosluzitelj() {
        return posluzitelj;
    }

    public void setPosluzitelj(String posluzitelj) {
        this.posluzitelj = posluzitelj;
    }

    public String getPrima() {
        return prima;
    }

    public void setPrima(String prima) {
        this.prima = prima;
    }

    public String getSalje() {
        return salje;
    }

    public void setSalje(String salje) {
        this.salje = salje;
    }

    public String getPredmet() {
        return predmet;
    }

    public void setPredmet(String predmet) {
        this.predmet = predmet;
    }

    public String getPrivitak() {
        return privitak;
    }

    public void setPrivitak(String privitak) {
        this.privitak = privitak;
    }

    public List<String> getPopisDatoteka() {
        return popisDatoteka;
    }

    public void setPopisDatoteka(List<String> popisDatoteka) {
        this.popisDatoteka = popisDatoteka;
    }

    public void setOdabranaDatoteka(String odabranaDatoteka) {
        this.odabranaDatoteka = odabranaDatoteka;
    }

    public String getOdabranaDatoteka() {
        return odabranaDatoteka;
    }

    public String promjeniJezik() {
        return "promjeniJezik";
    }

    public String pregledPoruka() {
        return "pregledPoruka";
    }

    public String pregledDnevnika() {
        return "pregledDnevnika";
    }

    /**
     * Metoda za slanje maila
     * @return 
     */
    public String saljiPoruku() {
        try {
            // Create the JavaMail session
            java.util.Properties properties = System.getProperties();
            properties.put("mail.smtp.host", this.posluzitelj);

            Session session = Session.getInstance(properties, null);

            // Construct the message
            MimeMessage message = new MimeMessage(session);

            // Set the from address
            Address fromAddress = new InternetAddress(this.salje);
            message.setFrom(fromAddress);

            // Parse and set the recipient addresses
            Address[] toAddresses = InternetAddress.parse(this.prima);
            message.setRecipients(Message.RecipientType.TO, toAddresses);

            // Set the subject and text
            message.setSubject(this.predmet);
            //message.setText(this.privitak);
            message.setContent(this.privitak, "text/json");
            message.setFileName(this.odabranaDatoteka);

            Transport.send(message);

        }
        catch(MessagingException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * Metoda za učitavanje sadržaja dane json datoteke
     * @return 
     */
    public String preuzmiSadrzaj() {
        try {
            String json = new String(Files.readAllBytes(Paths.get(SlusacAplikacije.kontekst.getRealPath("/WEB-INF/" + this.odabranaDatoteka))));
            try {
                new JsonParser().parse(json).getAsJsonObject();
                this.privitak = json;
            }
            catch(JsonSyntaxException ex) {
                this.privitak = "{ Neispravan format JSON datoteke }";
            }

            return "";
        }
        catch(IOException ex) {
            this.privitak = "{}";
            return "";
        }
    }

    /**
     * Metoda za brisanje sadržaja maila
     * @return 
     */
    public String obrisiPoruku() {
        this.privitak = "{}";
        return "";
    }

    /**
     * Metoda za dohvaćanje svih json datoteka unutar mape WEB-INF
     * @return Lista tipa <code>String</code> s nazivima json datoteka
     */
    private List<String> dohvatiJsonDatoteke() {
        List<String> datoteke = new ArrayList<>();

        File da = new File(SlusacAplikacije.kontekst.getRealPath("/WEB-INF"));
        File[] listOfFiles = da.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                if (file.getName().endsWith(".json")) {
                    datoteke.add(file.getName());
                }
            }
        }

        return datoteke;
    }

    /**
     * Metoda za pretvaranje <code>IMAPInputStream</code> maila u <code>String</code>
     * @param message mail
     * @return <code>String</code> vrijednost samog sadržaja maila
     */
    private String getMailContent(Message message) {
        String read = "";

        try {
            IMAPInputStream imapStream = (IMAPInputStream) message.getContent();
            BufferedReader br = new BufferedReader(new InputStreamReader(imapStream, Charset.defaultCharset()));
            char cbuf[] = new char[2048];
            int len;
            StringBuilder sbuf = new StringBuilder();
            while ((len = br.read(cbuf, 0, cbuf.length)) != -1) {
                sbuf.append(cbuf, 0, len);
            }
            read = sbuf.toString();
        }
        catch(IOException | MessagingException ex) {
            read = "";
        }

        return read;
    }
}
