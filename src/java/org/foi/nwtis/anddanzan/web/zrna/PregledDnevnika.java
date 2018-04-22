package org.foi.nwtis.anddanzan.web.zrna;

import java.util.Locale;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Andrea
 */
@Named(value = "pregledDnevnika")
@RequestScoped
public class PregledDnevnika {

    private HttpSession session;

    /**
     * Creates a new instance of PregledDnevnika
     */
    public PregledDnevnika() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        this.session = (HttpSession) facesContext.getExternalContext().getSession(false);
        FacesContext.getCurrentInstance().getViewRoot().setLocale((Locale)session.getAttribute("locale"));
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
