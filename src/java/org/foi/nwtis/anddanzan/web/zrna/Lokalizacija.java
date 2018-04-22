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
    private HttpSession session;

    /**
     * Konstruktor čija je namjena instanciranje i kreiranje sesije za korisnika
     */
    public Lokalizacija() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        this.session = (HttpSession) facesContext.getExternalContext().getSession(true);
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
        session.setAttribute("locale", locale);
        System.out.println("sesija id " + session.getId());
        return "";
    }

    public String slanjePoruka() {
        return "slanjePoruka";
    }

    public String pregledPoruka() {
        return "pregledPoruka";
    }

    public String pregledDnevnika() {
        return "pregledDnevnika";
    }
}
