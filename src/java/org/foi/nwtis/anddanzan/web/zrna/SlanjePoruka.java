package org.foi.nwtis.anddanzan.web.zrna;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
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

    /**
     * Creates a new instance of SlanjePoruka
     */
    public SlanjePoruka() {
        //TODO učitat podatke iz konf
        this.privitak = "{}";
        this.posluzitelj = "127.0.0.1";
        this.prima = "servis@nwtis.nastava.foi.hr";
        this.salje = "admin@nwtis.nastava.foi.hr";
        this.predmet = "IOT";

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
            message.setText(this.privitak);

            Transport.send(message);

        }
        catch(MessagingException e) {
            e.printStackTrace();
        }

        return "";
    }

    public String preuzmiSadrzaj() {
        try {
            //TODO preuzmi sadržaj datoteke čij je naziv u varijabli odabrana datoteka
            this.privitak = new String(Files.readAllBytes(Paths.get(SlusacAplikacije.kontekst.getRealPath("/WEB-INF/"+this.odabranaDatoteka))));
            return "";
        }
        catch(IOException ex) {
            this.privitak = "{}";
            return "";
        }
    }

    public String obrisiPoruku() {
        return "";
    }

    private List<String> dohvatiJsonDatoteke() {
        List<String> datoteke = new ArrayList<>();
        
        File da = new File(SlusacAplikacije.kontekst.getRealPath("/WEB-INF"));
        File[] listOfFiles = da.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                if(file.getName().endsWith(".json"))
                    datoteke.add(file.getName());
            }
        }
        
        return datoteke;
    }
}
