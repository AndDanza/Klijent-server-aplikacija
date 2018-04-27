package org.foi.nwtis.anddanzan.web.zrna;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.Locale;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

/**
 * JSF Managed Bean za postavljanje jezika (lokalizacije) web aplikacije.
 *
 * @author Andrea
 */
@Named(value = "lokalizacija")
@SessionScoped
public class Lokalizacija implements Serializable {

    private String odabraniJezik;
    private Locale language;
    private HttpSession session;

    /**
     * Konstruktor čija je namjena instanciranje i kreiranje sesije za korisnika
     */
    public Lokalizacija() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        this.session = (HttpSession) facesContext.getExternalContext().getSession(true);
        this.language = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale();
    }

    /**
     * Metoda za dohvaćanje odabranog jezika
     *
     * @return <code>String</code> vrijednost dohvaćenog jezika
     */
    public String getOdabraniJezik() {
        odabraniJezik = FacesContext.getCurrentInstance().getViewRoot().getLocale().getLanguage();
        return odabraniJezik;
    }

    /**
     * Metoda za odabir jezika prema danom ulaznom parametru
     * @param jezik <code>String</code> vrijednost jezika koji je korisnik odabrao
     * @return prazan string
     */
    public Object odaberiJezik(String jezik) {
        Locale locale = new Locale(jezik);
        FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
        this.language = locale;
        return "";
    }

    /**
     *
     * @return
     */
    public String slanjePoruka() {
        return "slanjePoruka";
    }

    /**
     *
     * @return
     */
    public String pregledPoruka() {
        return "pregledPoruka";
    }

    /**
     *
     * @return
     */
    public String pregledDnevnika() {
        return "pregledDnevnika";
    }

    /**
     *
     * @return
     */
    public Locale getLanguage() {
        return language;
    }

    /**
     *
     * @param language
     */
    public void setLanguage(Locale language) {
        this.language = language;
    }
}
