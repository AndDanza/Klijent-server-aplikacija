package org.foi.nwtis.anddanzan.web.zrna;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
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
    private List<String> pogreske;

    BP_Konfiguracija konfiguracija = (BP_Konfiguracija) SlusacAplikacije.kontekst.getAttribute("BP_Konfig");
    private HttpSession session;

    /**
     * Creates a new instance of SlanjePoruka
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
    }

    /**
     * Metoda za provjeru unesenih podataka i slanje maila ukoliko su podaci ispravni
     *
     * @return prazan string
     */
    public String saljiPoruku() {
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
                //message.setText(this.privitak);
                message.setContent(this.privitak, "text/json");
                message.setFileName(this.odabranaDatoteka);
                message.setSentDate(new Date());

                Transport.send(message);
            }

        }
        catch(MessagingException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * Metoda za učitavanje sadržaja dane json datoteke
     *
     * @return vraća prazan string
     */
    public String preuzmiSadrzaj() {
        try {
            Path path = Paths.get(SlusacAplikacije.kontekst.getRealPath("/WEB-INF/" + this.odabranaDatoteka));
            this.privitak = new String(Files.readAllBytes(path));
        }
        catch(IOException ex) {
            this.privitak = "{}";
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
     * Meoda za provjeru podataka unesenih nu formu
     *
     * @return true ako su podaci uredu ili false ako je jedan od unesenih
     * podataka neispravan
     */
    public boolean provjeriUnesenePodatke() {
        this.pogreske = new ArrayList<>();
        Locale currentLocale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        ResourceBundle prijevod = ResourceBundle.getBundle("org.foi.nwtis.anddanzan.prijevod", currentLocale);

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

        try {
            new JsonParser().parse(this.privitak).getAsJsonObject();
            if (this.privitak.equals("{}")) {
                this.pogreske.add(prijevod.getString("pogreska.privitak"));
            }
        }
        catch(JsonSyntaxException ex) {
            this.pogreske.add(prijevod.getString("pogreska.privitak"));
        }

        if (this.pogreske.isEmpty()) {
            return true;
        }
        else {
            return false;
        }
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

    public List<String> getPogreske() {
        return pogreske;
    }

    public void setPogreske(List<String> pogreske) {
        this.pogreske = pogreske;
    }
}
