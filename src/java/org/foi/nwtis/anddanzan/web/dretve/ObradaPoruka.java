package org.foi.nwtis.anddanzan.web.dretve;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import org.foi.nwtis.anddanzan.konfiguracije.bp.BP_Konfiguracija;

/**
 *
 * @author Andrea
 */
public class ObradaPoruka extends Thread {

    private String posluzitelj;
    private String korisnickoIme;
    private String lozinka;
    private int spavanje;
    private boolean radi;

    public ObradaPoruka(BP_Konfiguracija konfiguracijeBaze) {
    }

    @Override
    public void interrupt() {
        this.radi = false;
        super.interrupt(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public synchronized void start() {
        //TODO preuzeti podatke iz bpk prosljeđenog konstruktorom
        this.posluzitelj = "127.0.0.1";
        this.korisnickoIme = "servis@nwtis.nastava.foi.hr";
        this.lozinka = "123456";
        spavanje = 30 * 1000;
        super.start(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run() {
        int broj = 0;
        
        try {
            while (this.radi) {

                Session session;
                Store store;
                Folder folder;

                // Start the session
                Properties properties = System.getProperties();
                properties.put("mail.smtp.host", this.posluzitelj);
                session = Session.getInstance(properties, null);

                // Connect to the store
                store = session.getStore("imap");
                store.connect(this.posluzitelj, this.korisnickoIme, this.lozinka);

                // Open the INBOX folder
                folder = store.getFolder("INBOX");
                folder.open(Folder.READ_ONLY);

                Message[] messages = null;
                //TODO ne dohvaćati sve poruke odjednom nego ih po grupama dohvatiti (10 po 10 itd.)
                messages = folder.getMessages();
                
                for(int i = 0; i < messages.length; i++){
                    //TODO pretražiti tzv. nwtis poruke i obraditi ih 
                }
                
                folder.close(false);
                store.close();
                
                System.out.println("Završila iteracija: " + (broj++));
                
                
                //TODO spavanje u intervalu (kao kod serijalizatora u prvoj zadaći)
                Thread.sleep(this.spavanje);
            }
        } catch (MessagingException | InterruptedException ex) {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
