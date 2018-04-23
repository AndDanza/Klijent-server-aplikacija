package org.foi.nwtis.anddanzan.web.zrna;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.servlet.http.HttpSession;
import org.foi.nwtis.anddanzan.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.anddanzan.web.kontrole.Izbornik;
import org.foi.nwtis.anddanzan.web.kontrole.Poruka;
import org.foi.nwtis.anddanzan.web.slusaci.SlusacAplikacije;

/**
 * JSF Managed BEan za pregledavanje mailova u danim mapama
 *
 * @author Andrea
 */
@Named(value = "pregledPoruka")
@RequestScoped
public class PregledPoruka {

    private String posluzitelj;
    private final int imapPort;
    private String korisnickoIme;
    private String lozinka;
    private List<Izbornik> popisMapa;
    private String odabranaMapa;
    private int brojPorukaMape = 0;
    private List<Poruka> popisPoruka;
    private String folderNwtis;

    private final BP_Konfiguracija konfiguracija;
    private Session sesija;
    private Store store;
    private HttpSession session;

    private int pomakCitanja = 0;

    /**
     * Creates a new instance of PregledPoruka
     */
    public PregledPoruka() {
        this.konfiguracija = (BP_Konfiguracija) SlusacAplikacije.kontekst.getAttribute("BP_Konfig");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        this.session = (HttpSession) facesContext.getExternalContext().getSession(false);
        FacesContext.getCurrentInstance().getViewRoot().setLocale((Locale) this.session.getAttribute("locale"));

        this.imapPort = konfiguracija.getImapPort();
        this.posluzitelj = this.konfiguracija.getMailServer();
        this.korisnickoIme = this.konfiguracija.getMailUsernameThread();
        this.lozinka = this.konfiguracija.getMailPasswordThread();
        this.pomakCitanja = this.konfiguracija.getNumMessagesToShow();
        this.folderNwtis = this.konfiguracija.getFolderNWTiS();

        this.sesija = (Session) SlusacAplikacije.kontekst.getAttribute("mail_session");

        int pocetak = 1;
        int kraj = 1;
        if (this.session.getAttribute("kreni_mail") == null && this.session.getAttribute("stani_mail") == null) {
            this.session.setAttribute("kreni_mail", pocetak);
            this.session.setAttribute("stani_mail", this.pomakCitanja);
        }

        pocetak = (int) this.session.getAttribute("kreni_mail");
        kraj = (int) this.session.getAttribute("stani_mail");

        preuzmiMape();
        preuzmiPoruke(1, 1);
    }

    /**
     * Dohvaćanje mapa za pohranu maila za zadanog korisnika
     */
    private void preuzmiMape() {
        try {
            this.store = sesija.getStore("imap");
            this.store.connect(this.posluzitelj, this.imapPort, this.korisnickoIme, this.lozinka);

            this.popisMapa = new ArrayList<>();
            Folder folder = store.getFolder("INBOX");
            if (folder != null) {
                this.popisMapa.add(new Izbornik(folder.getName(), folder.getFullName()));
            }

            folder = store.getFolder(this.folderNwtis);
            if (folder != null) {
                this.popisMapa.add(new Izbornik(folder.getName(), folder.getFullName()));
            }

        }
        catch(MessagingException ex) {
            Logger.getLogger(PregledPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Dohvaćanje unaprijed određenog broja poruka u trenutnoj mapi
     *
     * @param start index početka raspona mailova
     * @param end index kraja raspona mailova
     */
    public void preuzmiPoruke(int start, int end) {
        try {
            Folder folder;
            if (this.odabranaMapa != null) {
                this.session.setAttribute("odabrana_mapa", this.odabranaMapa);
            }
            else {
                if (this.session.getAttribute("odabrana_mapa") == null) {
                    this.odabranaMapa = "INBOX";
                }
                else {
                    this.odabranaMapa = (String) this.session.getAttribute("odabrana_mapa");
                }
            }
            folder = store.getFolder(this.odabranaMapa);
            folder.open(Folder.READ_ONLY);

            this.brojPorukaMape = folder.getMessageCount();

            if (start == 1 && end == 1) {
                if (this.pomakCitanja > this.brojPorukaMape) {
                    end = this.brojPorukaMape;
                }
                else {
                    end = this.pomakCitanja;
                }
                this.session.setAttribute("kreni_mail", 1);
                this.session.setAttribute("stani_mail", end);
            }
            
            Message[] messages = folder.getMessages(start, end);
            
            this.popisPoruka = new ArrayList<>();
            for (int i = 0; i < this.pomakCitanja; i++) {
                popisPoruka.add(kreirajPoruku(messages[i], i));
            }
        }
        catch(MessagingException | ArrayIndexOutOfBoundsException ex) {
        }
    }

    private Poruka kreirajPoruku(Message poruka, int i) {
        try {
            String salje = poruka.getFrom()[0].toString();
            Date sent = poruka.getSentDate();
            Date sentOn = poruka.getSentDate();
            String subject = poruka.getSubject();
            String attachment = poruka.getFileName();

            Poruka mail = null;
            if (attachment.equals(this.konfiguracija.getAttachmentFilename())) {
                String content = Poruka.getMailContent(poruka);
                mail = new Poruka(Integer.toString(i), sent, sentOn, salje, subject, content, Poruka.VrstaPoruka.NWTiS_poruka);
            }
            else {
                mail = new Poruka(Integer.toString(i), sent, sentOn, salje, subject, "", Poruka.VrstaPoruka.neNWTiS_poruka);
            }

            return mail;
        }
        catch(MessagingException ex) {
            return null;
        }
    }

    /**
     * Metoda za obradu klika za sljedeću stranicu mailova. Na klik korisnika na
     * temelju zadanog broj prikaza mialova pomiču se početni i završni index
     * maila u mapi
     */
    public void prikaziSljedece() {
        int pocetak = (int) this.session.getAttribute("kreni_mail");
        int kraj = (int) this.session.getAttribute("stani_mail");

        if (kraj < this.brojPorukaMape) {
            pocetak += this.pomakCitanja;
            kraj += this.pomakCitanja;
        }

        if (kraj >= this.brojPorukaMape) {
            kraj = this.brojPorukaMape;
        }

        this.session.setAttribute("kreni_mail", pocetak);
        this.session.setAttribute("stani_mail", kraj);

        preuzmiPoruke(pocetak, kraj);
    }

    /**
     * Metoda za obradu klika za prethodnu stranicu mailova. Na klik korisnika
     * na temelju zadanog broj prikaza mialova pomiču se početni i završni index
     * maila u mapi
     */
    public void prikaziPrethodne() {
        int pocetak = (int) this.session.getAttribute("kreni_mail");
        int kraj = (int) this.session.getAttribute("stani_mail");

        if (pocetak <= 1) {
            pocetak = 1;
            if(this.pomakCitanja > this.brojPorukaMape)
                kraj = this.brojPorukaMape;
            else
                kraj = this.pomakCitanja;
        }
        else if(pocetak > this.pomakCitanja) {
            kraj = pocetak;
            pocetak -= this.pomakCitanja;
        }

        this.session.setAttribute("kreni_mail", pocetak);
        this.session.setAttribute("stani_mail", kraj);

        preuzmiPoruke(pocetak, kraj);
    }

    public int getBrojPorukaMape() {
        return brojPorukaMape;
    }

    public void setBrojPorukaMape(int brojPorukaMape) {
        this.brojPorukaMape = brojPorukaMape;
    }

    public String getPosluzitelj() {
        return posluzitelj;
    }

    public void setPosluzitelj(String posluzitelj) {
        this.posluzitelj = posluzitelj;
    }

    public String getKorisnickoIme() {
        return korisnickoIme;
    }

    public void setKorisnickoIme(String korisnickoIme) {
        this.korisnickoIme = korisnickoIme;
    }

    public String getLozinka() {
        return lozinka;
    }

    public void setLozinka(String lozinka) {
        this.lozinka = lozinka;
    }

    public List<Izbornik> getPopisMapa() {
        return popisMapa;
    }

    public void setPopisMapa(List<Izbornik> popisMapa) {
        this.popisMapa = popisMapa;
    }

    public String getOdabranaMapa() {
        return odabranaMapa;
    }

    public void setOdabranaMapa(String odabranaMapa) {
        this.odabranaMapa = odabranaMapa;
    }

    public List<Poruka> getPopisPoruka() {
        return popisPoruka;
    }

    public void setPopisPoruka(List<Poruka> popisPoruka) {
        this.popisPoruka = popisPoruka;
    }

    public String promjeniJezik() {
        return "promjeniJezik";
    }

    public String slanjePoruka() {
        return "slanjePoruka";
    }

    public String pregledDnevnika() {
        return "pregledDnevnika";
    }

}
