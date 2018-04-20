package org.foi.nwtis.anddanzan.web.slusaci;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.foi.nwtis.anddanzan.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.anddanzan.web.dretve.ObradaPoruka;
import org.nwtis.anddanzan.konfiguracije.NeispravnaKonfiguracija;
import org.nwtis.anddanzan.konfiguracije.NemaKonfiguracije;

/**
 * Web application lifecycle listener.
 *
 * @author Andrea
 */
@WebListener
public class SlusacAplikacije implements ServletContextListener {

    ObradaPoruka obrada;
    public static ServletContext kontekst;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        kontekst = sce.getServletContext();

        String datoteka = kontekst.getInitParameter("konfiguracija");
        String putanja = kontekst.getRealPath("/WEB-INF") + java.io.File.separator;
        String puniNaziv = putanja + datoteka;

        try {
            BP_Konfiguracija bpk = new BP_Konfiguracija(puniNaziv);
            kontekst.setAttribute("BP_Konfig", bpk);    //atributi na razini aplikacije

            obrada = new ObradaPoruka();
            obrada.start();
        }
        catch(NemaKonfiguracije | NeispravnaKonfiguracija ex) {
            Logger.getLogger(SlusacAplikacije.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        kontekst.removeAttribute("BP_Konfig");
        kontekst = null;

        obrada.interrupt();
    }
}
