package org.foi.nwtis.anddanzan.web.zrna;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import org.foi.nwtis.anddanzan.web.kontrole.Izbornik;
import org.foi.nwtis.anddanzan.web.kontrole.Poruka;

/**
 *
 * @author Andrea
 */
@Named(value = "pregledPoruka")
@RequestScoped
public class PregledPoruka {

    private String posluzitelj;
    private String korisnickoIme;
    private String lozinka;
    private List<Izbornik> popisMapa;
    private String odabranaMapa;
    private List<Poruka> popisPoruka;

    /**
     * Creates a new instance of PregledPoruka
     */
    public PregledPoruka() {
        //TODO preuzmi podatke iz konfiguracije
        posluzitelj = "127.0.0.1";
        korisnickoIme = "servis@nwtis.nastava.foi.hr";
        lozinka = "123456";

        preuzmiMape();
        preuzmiPoruke();
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

    private void preuzmiMape() {
        popisMapa = new ArrayList<>();
        popisMapa.add(new Izbornik("INBOX", "INBOX"));
        popisMapa.add(new Izbornik("NWTiS anddanzan", "NWTiS anddanzan"));
    }

    private void preuzmiPoruke() {
        popisPoruka = new ArrayList<>();
        for(int i = 0; i < 100; i++)
            popisPoruka.add(new Poruka(Integer.toString(i), new Date(), new Date(), "anddanzan@foi.hr", "Poruka"+i, "Poruka"+i+"sadrži json", Poruka.VrstaPoruka.NWTiS_poruka));
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
