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
import java.util.Date;
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
    private String odDatuma = "";
    private String doDatuma = "";
    private List<String> pogreske = new ArrayList<>();

    private Connection connection;
    private Statement statement;
    
    private boolean render_prev = true;
    private boolean render_next = true;

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
            this.pomakCitanja = this.konfiguracija.getNumLogItemsToShow();

            if (this.session.getAttribute("pocetni_datum") != null && this.session.getAttribute("krajnji_datum") != null) {
                this.pocetni = (String) this.session.getAttribute("pocetni_datum");
                this.krajnji = (String) this.session.getAttribute("krajnji_datum");
            }
            if (this.session.getAttribute("uneseni_pocetni") != null && this.session.getAttribute("uneseni_krajnji") != null) {
                this.odDatuma = (String) this.session.getAttribute("uneseni_pocetni");
                this.doDatuma = (String) this.session.getAttribute("uneseni_krajnji");
            }

            brojZapisa();

            if (this.session.getAttribute("stranica_dnevnik") == null) {
                this.session.setAttribute("stranica_dnevnik", 0);
                prikaziDnevnik(0);
            }

        }
        catch(SQLException ex) {
            Logger.getLogger(PregledDnevnika.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Metoda za dohvaćanje zapisa iz tablice dnevnik
     *
     * @param brojStranice trenutna stranica (množi se s pomakom čitanja kako bi
     * se odredio početni red u tablici)
     */
    public void prikaziDnevnik(int brojStranice) {
        try {
            zapisi = new ArrayList<>();
            
            brojZapisa();
            brojStranice = brojStranice * this.pomakCitanja;
            
//            if (this.brojPorukaMape < this.pomakCitanja) {
//                this.render_prev = false;
//                this.render_next = false;
//            }
//            else if (start == (this.brojPorukaMape - this.pomakCitanja + 1)) {
//                this.render_prev = false;
//            }
        
            System.out.println("br stran "+brojStranice);
            System.out.println(brojStranice+this.pomakCitanja >= this.brojZapisaDnevnika);
            if (brojStranice == 0) {
                this.render_prev = false;
            }
            if (brojStranice+this.pomakCitanja >= this.brojZapisaDnevnika){
                this.render_next = false;
            }

            String upit = "";
            if (this.pocetni.isEmpty() && this.krajnji.isEmpty()) {
                upit = "SELECT `id`, `sadrzaj`, `vrijeme` FROM `dnevnik` "
                        + "ORDER BY `vrijeme` DESC LIMIT " + brojStranice + "," + this.pomakCitanja;
            }
            else {
                upit = "SELECT `id`, `sadrzaj`, `vrijeme` FROM `dnevnik` "
                        + "WHERE `vrijeme` BETWEEN '" + this.pocetni + "' AND '" + this.krajnji + "' "
                        + "ORDER BY `vrijeme` DESC LIMIT " + brojStranice + "," + this.pomakCitanja;
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

    /**
     * Metoda za obradu klika pretraži na formi. Na temelju unesenih datuma
     * pretražuju se zapisi u tablic koji odgovaraju uvjetu
     */
    public void pretraziDnevnik() {
        Locale currentLocale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        ResourceBundle prijevod = ResourceBundle.getBundle("org.foi.nwtis.anddanzan.prijevod", currentLocale);

        if (!this.odDatuma.isEmpty() && !this.doDatuma.isEmpty()) {
            this.session.setAttribute("uneseni_pocetni", this.odDatuma);
            this.session.setAttribute("uneseni_krajnji", this.doDatuma);
        }

        this.pocetni = provjeriDatum(this.odDatuma);
        if (this.pocetni.equals("ERROR")) {
            this.pogreske.add(prijevod.getString("pregled.od_datuma") + " - " + prijevod.getString("pogreska.krivi_datum"));
        }

        this.krajnji = provjeriDatum(this.doDatuma);
        if (this.krajnji.equals("ERROR")) {
            this.pogreske.add(prijevod.getString("pregled.do_datuma") + " - " + prijevod.getString("pogreska.krivi_datum"));
        }

        this.session.setAttribute("pocetni_datum", this.pocetni);
        this.session.setAttribute("krajnji_datum", this.krajnji);

        prikaziDnevnik(0);
        brojZapisa();

    }

    /**
     * Metoda za pražnjenje inputa forme te učitavanje inicijalnih podataka
     */
    public void ocistiPretragu() {
        this.pocetni = this.krajnji = "";
        this.odDatuma = this.doDatuma = "";

        this.session.removeAttribute("pocetni_datum");
        this.session.removeAttribute("krajnji_datum");
        this.session.removeAttribute("stranica_dnevnik");

        this.session.removeAttribute("uneseni_pocetni");
        this.session.removeAttribute("uneseni_krajnji");

        this.session.setAttribute("stranica_dnevnik", 0);

        prikaziDnevnik(0);
        brojZapisa();
    }

    /**
     * Metoda za obradu klika za sljedeću stranicu zapisa. Na klik korisnika na
     * temelju zadanog broj zapisa koje je potrebno prikazati pomiču se početni
     * i završni index retka u tablici
     */
    public void prikaziSljedece() {
        int stranica = (int) this.session.getAttribute("stranica_dnevnik");

        if ((stranica * this.pomakCitanja) < (this.brojZapisaDnevnika - this.pomakCitanja)) {
            stranica++;
            this.session.setAttribute("stranica_dnevnik", stranica);
        }

        prikaziDnevnik(stranica);
    }

    /**
     * Metoda za obradu klika za prethodnu stranicu zapisa. Na klik korisnika na
     * temelju zadanog broj zapisa koje je potrebno prikazati pomiču se početni
     * i završni index retka u tablici
     */
    public void prikaziPrethodne() {
        int stranica = (int) this.session.getAttribute("stranica_dnevnik");

        if (stranica > 0) {
            stranica--;
            this.session.setAttribute("stranica_dnevnik", stranica);
        }

        prikaziDnevnik(stranica);
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
     *
     * @param datum
     * @return
     */
    private String provjeriDatum(String datum) {
        try {
            DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            Date date = formatter.parse(datum);
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return formatter.format(date);

        }
        catch(ParseException ex) {
            return "ERROR";
        }
    }

    public boolean isRender_prev() {
        return render_prev;
    }

    public void setRender_prev(boolean render_prev) {
        this.render_prev = render_prev;
    }

    public boolean isRender_next() {
        return render_next;
    }

    public void setRender_next(boolean render_next) {
        this.render_next = render_next;
    }

    public String getOdDatuma() {
        return odDatuma;
    }

    public void setOdDatuma(String odDatuma) {
        this.odDatuma = odDatuma;
    }

    public String getDoDatuma() {
        return doDatuma;
    }

    public void setDoDatuma(String doDatuma) {
        this.doDatuma = doDatuma;
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
