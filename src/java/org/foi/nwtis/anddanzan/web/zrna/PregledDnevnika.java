package org.foi.nwtis.anddanzan.web.zrna;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.foi.nwtis.anddanzan.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.anddanzan.web.kontrole.Dnevnik;
import org.foi.nwtis.anddanzan.web.slusaci.SlusacAplikacije;

/**
 * JSF Managed Bean za učitavanje i pregledavanje zapisa u tablici dnevnik
 *
 * @author Andrea
 */
@Named(value = "pregledDnevnika")
@RequestScoped
public class PregledDnevnika {

    private HttpSession session;
    private BP_Konfiguracija konfiguracija;

    private int pomakCitanja = 0;
    private int brojZapisaDnevnika;
    private List<Dnevnik> zapisi;
    private String pocetni = "";
    private String krajnji = "";
    private List<String> pogreske = new ArrayList<>();

    private Connection connection;
    private Statement statement;

    /**
     * Creates a new instance of PregledDnevnika
     */
    public PregledDnevnika() {
        try {
            this.konfiguracija = (BP_Konfiguracija) SlusacAplikacije.kontekst.getAttribute("BP_Konfig");
            String url = konfiguracija.getServerDatabase() + konfiguracija.getUserDatabase();
            this.connection = DriverManager.getConnection(url, konfiguracija.getUserUsername(), konfiguracija.getUserPassword());
            this.statement = this.connection.createStatement();

            FacesContext facesContext = FacesContext.getCurrentInstance();
            this.session = (HttpSession) facesContext.getExternalContext().getSession(false);
            FacesContext.getCurrentInstance().getViewRoot().setLocale((Locale) session.getAttribute("locale"));

            this.pomakCitanja = this.konfiguracija.getNumLogItemsToShow();

            if (this.session.getAttribute("pocetni_datum") != null && this.session.getAttribute("krajnji_datum") != null) {
                this.pocetni = (String) this.session.getAttribute("pocetni_datum");
                this.krajnji = (String) this.session.getAttribute("krajnji_datum");
            }

            brojZapisa();

            int pocetak = 0;
            int kraj = 0;
            if (this.session.getAttribute("kreni_dnevnik") == null && this.session.getAttribute("stani_dnevnik") == null) {
                this.session.setAttribute("kreni_dnevnik", pocetak);
                this.session.setAttribute("stani_dnevnik", this.pomakCitanja);
            }

            pocetak = (int) this.session.getAttribute("kreni_dnevnik");
            kraj = (int) this.session.getAttribute("stani_dnevnik");

            prikaziDnevnik(pocetak, kraj);
        }
        catch(SQLException ex) {
            Logger.getLogger(PregledDnevnika.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Metoda za dohvaćanje zapisa iz tablice dnevnik
     *
     * @param odZapisa index početnog reda tablice
     * @param doZapisa index završnog reda tablice
     */
    public void prikaziDnevnik(int odZapisa, int doZapisa) {
        try {
            zapisi = new ArrayList<>();
            String upit = "";
            if (this.pocetni.isEmpty() && this.krajnji.isEmpty()) {
                upit = "SELECT `id`, `sadrzaj`, `vrijeme` FROM `dnevnik` ORDER BY `vrijeme` DESC LIMIT " + odZapisa + "," + doZapisa;
            }
            else {
                upit = "SELECT `id`, `sadrzaj`, `vrijeme` FROM `dnevnik` "
                        + "WHERE `vrijeme` BETWEEN '" + pocetni + "' AND '" + krajnji + "' ORDER BY `vrijeme` DESC LIMIT " + odZapisa + "," + doZapisa;
            }
            ResultSet podaci = this.statement.executeQuery(upit);
            while (podaci.next()) {
                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String date = podaci.getString("vrijeme");
                zapisi.add(new Dnevnik(Integer.valueOf(podaci.getString("id")), podaci.getString("sadrzaj"), formatter.parse(date)));
            }
        }
        catch(SQLException | ParseException ex) {
            Logger.getLogger(PregledDnevnika.class.getName()).log(Level.SEVERE, null, ex);
        }

        brojZapisa();
    }

    /**
     * Metoda za obradu klika pretraži na formi. Na temelju unesenih datuma
     * pretražuju se zapisi u tablic koji odgovaraju uvjetu
     */
    public void pretraziDnevnik() {
        if (!this.pocetni.isEmpty() && !this.krajnji.isEmpty()) {
            Locale currentLocale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
            ResourceBundle prijevod = ResourceBundle.getBundle("org.foi.nwtis.anddanzan.prijevod", currentLocale);

            if (!provjeriDatum(this.pocetni)) {
                this.pogreske.add(prijevod.getString("pregled.od_datuma") + " - " + prijevod.getString("pogreska.krivi_datum"));
            }
            if (!provjeriDatum(this.krajnji)) {
                this.pogreske.add(prijevod.getString("pregled.do_datuma") + " - " + prijevod.getString("pogreska.krivi_datum"));
            }

            this.session.setAttribute("pocetni_datum", this.pocetni);
            this.session.setAttribute("krajnji_datum", this.krajnji);

            prikaziDnevnik(0, this.pomakCitanja);
            brojZapisa();
        }

    }

    /**
     * Metoda za pražnjenje inputa forme te učitavanje inicijalnih podataka
     */
    public void ocistiPretragu() {
        this.pocetni = "";
        this.krajnji = "";
        this.session.removeAttribute("pocetni_datum");
        this.session.removeAttribute("krajnji_datum");

        this.session.setAttribute("kreni_dnevnik", 0);
        this.session.setAttribute("stani_dnevnik", this.pomakCitanja);
        prikaziDnevnik(0, this.pomakCitanja);

        brojZapisa();
    }

    /**
     * Metoda za obradu klika za sljedeću stranicu zapisa. Na klik korisnika na
     * temelju zadanog broj zapisa koje je potrebno prikazati pomiču se početni
     * i završni index retka u tablici
     */
    public void prikaziSljedece() {
        int pocetak = (int) this.session.getAttribute("kreni_dnevnik");
        int kraj = (int) this.session.getAttribute("stani_dnevnik");

        if (kraj < this.brojZapisaDnevnika) {
            pocetak += this.pomakCitanja;
            kraj += this.pomakCitanja;
        }

        if (kraj >= this.brojZapisaDnevnika) {
            kraj = this.brojZapisaDnevnika;
        }

        this.session.setAttribute("kreni_dnevnik", pocetak);
        this.session.setAttribute("stani_dnevnik", kraj);

        prikaziDnevnik(pocetak, kraj);
    }

    /**
     * Metoda za obradu klika za prethodnu stranicu zapisa. Na klik korisnika na
     * temelju zadanog broj zapisa koje je potrebno prikazati pomiču se početni
     * i završni index retka u tablici
     */
    public void prikaziPrethodne() {
        int pocetak = (int) this.session.getAttribute("kreni_dnevnik");
        int kraj = (int) this.session.getAttribute("stani_dnevnik");

        kraj = pocetak;
        pocetak -= this.pomakCitanja;

        if (pocetak <= 0) {
            pocetak = 0;
            kraj = this.pomakCitanja;
        }

        this.session.setAttribute("kreni_dnevnik", pocetak);
        this.session.setAttribute("stani_dnevnik", kraj);

        prikaziDnevnik(pocetak, kraj);
    }

    /**
     * Metoda za dohvaćanje broja zapisa u tablici podataka
     */
    private void brojZapisa() {
        try {
            String upit;
            if (this.pocetni.isEmpty() && this.krajnji.isEmpty()) {
                upit = "SELECT COUNT(*) AS broj FROM `dnevnik`";
            }
            else {
                upit = "SELECT COUNT(*) AS broj FROM `dnevnik` "
                        + "WHERE `vrijeme` BETWEEN '" + this.pocetni + "' AND '" + this.krajnji + "'";
            }
            ResultSet podaci = this.statement.executeQuery(upit);
            if (podaci.next()) {
                this.brojZapisaDnevnika = podaci.getInt("broj");
            }
        }
        catch(SQLException ex) {
            Logger.getLogger(PregledDnevnika.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Metoda za validaciju datuma unesenih u inpute forme
     * @param datum
     * @return 
     */
    private boolean provjeriDatum(String datum) {
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            formatter.parse(datum);
            return true;
        }
        catch(ParseException ex) {
            return false;
        }
    }

    public List<String> getPogreske() {
        return pogreske;
    }

    public void setPogreske(List<String> pogreske) {
        this.pogreske = pogreske;
    }

    public int getBrojZapisaDnevnika() {
        return brojZapisaDnevnika;
    }

    public void setBrojZapisaDnevnika(int brojZapisaDnevnika) {
        this.brojZapisaDnevnika = brojZapisaDnevnika;
    }

    public String getPocetni() {
        return pocetni;
    }

    public void setPocetni(String pocetni) {
        this.pocetni = pocetni;
    }

    public String getKrajnji() {
        return krajnji;
    }

    public void setKrajnji(String krajnji) {
        this.krajnji = krajnji;
    }

    public List<Dnevnik> getZapisi() {
        return zapisi;
    }

    public void setZapisi(List<Dnevnik> zapisi) {
        this.zapisi = zapisi;
    }

    public String slanjePoruka() {
        return "slanjePoruka";
    }

    public String pregledPoruka() {
        return "pregledPoruka";
    }

    public String promjeniJezik() {
        return "promjeniJezik";
    }
}
