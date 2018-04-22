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
    private String pocetni = null;
    private String krajnji = null;

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

            String upit = "SELECT COUNT(*) AS broj FROM `dnevnik`";
            ResultSet podaci = this.statement.executeQuery(upit);
            if (podaci.next()) {
                this.brojZapisaDnevnika = podaci.getInt("broj");
            }

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

    public void prikaziDnevnik(int odZapisa, int doZapisa) {
        try {
            zapisi = new ArrayList<>();
            String upit = "";
            if (this.pocetni == null && this.krajnji == null) {
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
    }

    public void pretraziDnevnik() {
    }

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

    public void prikaziPrethodne() {
        System.out.println("da");
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
