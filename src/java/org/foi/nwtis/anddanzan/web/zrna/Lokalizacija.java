package org.foi.nwtis.anddanzan.web.zrna;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.Locale;
import javax.faces.context.FacesContext;

/**
 *
 * @author Andrea
 */
@Named(value = "lokalizacija")
@SessionScoped
public class Lokalizacija implements Serializable {

    private String odabraniJezik;

    /**
     * Creates a new instance of Lokalizacija
     */
    public Lokalizacija() {
    }

    public String getOdabraniJezik() {
        this.odabraniJezik = FacesContext.getCurrentInstance().getViewRoot().getLocale().getLanguage();
        return this.odabraniJezik;
    }

    public Object odaberiJezik(String jezik) {
        Locale locale = new Locale(jezik);
        FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
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
