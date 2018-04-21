package org.foi.nwtis.anddanzan.web.zrna;

import javax.inject.Named;
import javax.enterprise.context.RequestScoped;

/**
 *
 * @author Andrea
 */
@Named(value = "pregledDnevnika")
@RequestScoped
public class PregledDnevnika {

    /**
     * Creates a new instance of PregledDnevnika
     */
    public PregledDnevnika() {
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
