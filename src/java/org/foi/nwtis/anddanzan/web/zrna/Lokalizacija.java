package org.foi.nwtis.anddanzan.web.zrna;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.Locale;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Andrea
 */
@Named(value = "lokalizacija")
@SessionScoped
public class Lokalizacija implements Serializable {

    private String odabraniJezik;
    private HttpSession session;

    public Lokalizacija() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        this.session = (HttpSession) facesContext.getExternalContext().getSession(true);        
    }

    public String getOdabraniJezik() {
        odabraniJezik = FacesContext.getCurrentInstance().getViewRoot().getLocale().getLanguage();
        return odabraniJezik;
    }
    
    public Object odaberiJezik(String jezik){
        Locale locale = new Locale(jezik);
        FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
        session.setAttribute("locale", locale);
        System.out.println("sesija id "+session.getId());
        return "";
    }    
    
    public String slanjePoruka(){
        return "slanjePoruka";
    }
    
    public String pregledPoruka(){
        return "pregledPoruka";
    }
    
    public String pregledDnevnika(){
        return "pregledDnevnika";
    }
}
