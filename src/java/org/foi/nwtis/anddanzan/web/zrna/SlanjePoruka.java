package org.foi.nwtis.anddanzan.web.zrna;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpSession;
import org.foi.nwtis.anddanzan.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.anddanzan.web.slusaci.SlusacAplikacije;

/**
 * JSF Managed Bean za slanje maila
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
    private List<String> pogreske = new ArrayList<>();
    ResourceBundle prijevod;

    BP_Konfiguracija konfiguracija = (BP_Konfiguracija) SlusacAplikacije.kontekst.getAttribute("BP_Konfig");
    private HttpSession session;

    /**
     * Konstruktor managed beana za inicijalizaciju. Učitavaju se parametri
     * potrebni za spajanje na bazu i početni podaci na formi.
     */
    public SlanjePoruka() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        this.session = (HttpSession) facesContext.getExternalContext().getSession(false);
        this.privitak = "{}";
        this.posluzitelj = konfiguracija.getMailServer();
        this.prima = konfiguracija.getMailUsernameThread();
        this.salje = konfiguracija.getUsernameEmail();
        this.predmet = konfiguracija.getSubjectEmail();

        this.popisDatoteka = dohvatiJsonDatoteke();

        Locale currentLocale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        this.prijevod = ResourceBundle.getBundle("org.foi.nwtis.anddanzan.prijevod", currentLocale);
    }

    /**
     * Metoda za provjeru unesenih podataka i slanje maila ukoliko su podaci
     * ispravni
     *
     * @return prazan string
     */
    public String saljiPoruku() {
        this.pogreske.removeAll(pogreske);
        try {
            Session session = (Session) SlusacAplikacije.kontekst.getAttribute("mail_session");

            if (provjeriUnesenePodatke()) {
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

                //TODO provjerit ako radi i bez ovog
                message.setFileName(this.odabranaDatoteka);

                File atachment = pripremiPrivitakZaSlanje();

                if (atachment != null) {
                    MimeBodyPart messageBodyPart = new MimeBodyPart();
                    MimeMultipart multipart = new MimeMultipart();
                    DataSource source = new FileDataSource(atachment);
                    messageBodyPart.setDataHandler(new DataHandler(source));
                    messageBodyPart.setFileName(this.odabranaDatoteka);
                    multipart.addBodyPart(messageBodyPart);
                    messageBodyPart.setHeader("Content-Type", "application/json");

                    message.setContent(multipart);
                    message.setSentDate(new Date());

                    Transport.send(message);
                }
                else {
                    this.pogreske.add(this.prijevod.getString("slanje.nije_poslano"));
                }
            }
        }
        catch(MessagingException e) {
            this.pogreske.add(this.prijevod.getString("slanje.nije_poslano"));
        }

        return "";
    }

    /**
     * Metoda za kreiranje privremene datoteke koja se šalje kao privitak u
     * mailu.
     *
     * @return objekt datoteke tipa <code>File</code> s podacima iz forme
     */
    private File pripremiPrivitakZaSlanje() {
        File temp = null;
        try {
            temp = File.createTempFile("privitak", ".json");
            BufferedWriter out = new BufferedWriter(new FileWriter(temp));
            out.write(this.privitak);
            out.close();
        }
        catch(IOException ex) {
            Logger.getLogger(SlanjePoruka.class.getName()).log(Level.SEVERE, null, ex);
        }

        return temp;
    }

    /**
     * Metoda za učitavanje sadržaja iz dane json datoteke
     *
     * @return vraća prazan string
     */
    public String preuzmiSadrzaj() {
        this.pogreske.removeAll(pogreske);
        if (this.odabranaDatoteka != null) {
            try {
                Path path = Paths.get(SlusacAplikacije.kontekst.getRealPath("/WEB-INF/" + this.odabranaDatoteka));
                this.privitak = new String(Files.readAllBytes(path));
            }
            catch(IOException ex) {
                this.privitak = "{}";
            }
        }
        else {
            this.pogreske.add(this.prijevod.getString("pogreska.datoteka"));
        }
        return "";
    }

    /**
     * Metoda za brisanje sadržaja maila
     *
     * @return vraća prazan string
     */
    public String obrisiPoruku() {
        this.privitak = "{}";
        return "";
    }

    /**
     * Metoda za dohvaćanje svih json datoteka unutar mape WEB-INF
     *
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
     * Meoda za provjeru podataka unesenih nu formu. Provjeravaju se uneseni
     * podaci na formi te valjanost json objekta.
     *
     * @return true ako su podaci uredu ili false ako je jedan od unesenih
     * podataka neispravan
     */
    public boolean provjeriUnesenePodatke() {
        this.pogreske.removeAll(pogreske);

        if (!this.prima.contains("@") || !this.prima.contains(".")) {
            this.pogreske.add(prijevod.getString("slanje.prima") + " - " + prijevod.getString("pogreska.mail"));
        }

        if (!this.salje.contains("@") || !this.salje.contains(".")) {
            this.pogreske.add(prijevod.getString("slanje.salje") + " - " + prijevod.getString("pogreska.mail"));
        }

        if (this.predmet.length() < 10) {
            this.pogreske.add(prijevod.getString("slanje.predmet") + " - " + prijevod.getString("pogreska.predmet"));
        }

        if (this.odabranaDatoteka == null) {
            this.pogreske.add(prijevod.getString("pogreska.datoteka"));
        }

        if (this.privitak.isEmpty()) {
            this.pogreske.add(prijevod.getString("pogreska.sadrzaj"));
        }
        else {
            try {
                if (!this.privitak.equals("{}")) {
                    new JsonParser().parse(this.privitak);
                }
            }
            catch(JsonSyntaxException ex) {
                this.pogreske.add(prijevod.getString("pogreska.sadrzaj"));
            }
        }

        if (this.pogreske.isEmpty()) {
            this.pogreske.add(prijevod.getString("slanje.poslano"));
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Dohvaćanje varijable poslužitelja
     * @return naziv poslužitelja
     */
    public String getPosluzitelj() {
        return posluzitelj;
    }

    /**
     * Postavljanje varijable poslužitelja
     * @param posluzitelj naziv poslužitelja za pohranu
     */
    public void setPosluzitelj(String posluzitelj) {
        this.posluzitelj = posluzitelj;
    }

    /**
     * Dohvaćanje adrese primatelja
     * @return adresa primatelja
     */
    public String getPrima() {
        return prima;
    }

    /**
     * Postavljanje varijable primatelja
     * @param prima adresa primatelja za pohranu
     */
    public void setPrima(String prima) {
        this.prima = prima;
    }

    /**
     * Dohvaćanje adrese primatelja
     * @return adresa primatelja poruke
     */
    public String getSalje() {
        return salje;
    }

    /**
     * Postavljanje adrese pošiljatelja
     * @param salje adresa pošiljatelja
     */
    public void setSalje(String salje) {
        this.salje = salje;
    }

    /**
     * Dohvaćanje predmeta poruka
     * @return predemet poruke
     */
    public String getPredmet() {
        return predmet;
    }

    /**
     * Postavljanje predmeta poruke
     * @param predmet novi predmet poruke
     */
    public void setPredmet(String predmet) {
        this.predmet = predmet;
    }

    /**
     * Dohvaćanje sadržaja privitka
     * @return sadržaj privitka
     */
    public String getPrivitak() {
        return privitak;
    }

    /**
     * Postavljanje sadržaja privitka
     * @param privitak novi sadržaj privetka
     */
    public void setPrivitak(String privitak) {
        this.privitak = privitak;
    }

    /**
     * Dohvaćanje popisa datoteka json
     * @return lists json datoteka
     */
    public List<String> getPopisDatoteka() {
        return popisDatoteka;
    }

    /**
     * Punjenje liste json datoteka
     * @param popisDatoteka nova lista datoteka
     */
    public void setPopisDatoteka(List<String> popisDatoteka) {
        this.popisDatoteka = popisDatoteka;
    }

    /**
     * Postavljanje odabrane datoteke
     * @param odabranaDatoteka nova odabrana datoteka
     */
    public void setOdabranaDatoteka(String odabranaDatoteka) {
        this.odabranaDatoteka = odabranaDatoteka;
    }

    /**
     * Dohvaćanje odabrane datoteke
     * @return naziv odabrane datoteke
     */
    public String getOdabranaDatoteka() {
        return odabranaDatoteka;
    }

    /**
     * Metoda za navigaciju
     * @return promjenaJezika
     */
    public String promjeniJezik() {
        return "promjeniJezik";
    }

    /**
     * Metoda za navigaciju
     * @return pregledPoruka
     */
    public String pregledPoruka() {
        return "pregledPoruka";
    }

    /**
     * Metoda za navigaciju
     * @return pregledDnevnika
     */
    public String pregledDnevnika() {
        return "pregledDnevnika";
    }

    /**
     * Dohvaćanje liste pogrešaka
     * @return lista pogrešaka
     */
    public List<String> getPogreske() {
        return pogreske;
    }

    /**
     * Postavljanje liste pogreški
     * @param pogreske nova lista pogreški
     */
    public void setPogreske(List<String> pogreske) {
        this.pogreske = pogreske;
    }
}
