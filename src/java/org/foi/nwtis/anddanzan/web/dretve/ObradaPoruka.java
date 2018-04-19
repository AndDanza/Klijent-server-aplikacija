package org.foi.nwtis.anddanzan.web.dretve;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import org.foi.nwtis.anddanzan.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.anddanzan.web.kontrole.DatotekaRadaDretve;

/**
 * Dretva koja je pokrenuta prilikom inicijalizacije samog konteksta.
 *
 * Namjena dretve je u pravilnim intervalima zadanim u konfiguracijskoj datoteci
 * pregledavati postoji li nova poruka u sandčiću korisnika.
 *
 * @author Andrea
 */
public class ObradaPoruka extends Thread {

    BP_Konfiguracija konfiguracija;
    
    public static DatotekaRadaDretve logObrade = null;
    
    //spajanje na mail server
    private String mailServer;
    private int imapPort;
    private String korisnickoIme;
    private String lozinka;
    
    //rad dretve (trajanje ciklusa i prekidanje)
    private int spavanje;
    private boolean radi;
    
    //obrada primljene poruke
    private int numMessagesToRead;
    private String oznakaNwtisPoruke;
    private String mapaNwtisPoruka;
    private String logDatoteka;

    public ObradaPoruka(BP_Konfiguracija konfiguracijeBaze) {
        this.konfiguracija = konfiguracijeBaze;
    }

    @Override
    public void interrupt() {
        this.radi = false;
        super.interrupt();
    }

    @Override
    public synchronized void start() {
        this.imapPort = konfiguracija.getImapPort();
        this.mailServer = konfiguracija.getMailServer();
        this.korisnickoIme = konfiguracija.getMailUsernameThread();
        this.lozinka = konfiguracija.getMailPasswordThread();
        this.spavanje = konfiguracija.getTimeSecThreadCycle() * 1000;
        this.numMessagesToRead = konfiguracija.getNumMessagesToRead();
        this.oznakaNwtisPoruke = konfiguracija.getAttachmentFilename();
        this.mapaNwtisPoruka = konfiguracija.getFolderNWTiS();
        this.logDatoteka = konfiguracija.getThreadCycleLog();
        
        super.start();
    }

    @Override
    public void run() {
        int broj = 0;
        
        try {
            while (this.radi) {
                
                logObrade = new DatotekaRadaDretve();

                Session session;
                Store store;
                Folder folder;

                // Start the session
                Properties properties = System.getProperties();
                properties.put("mail.smtp.host", this.mailServer);
                properties.put("mail.imap.port", this.imapPort);
                session = Session.getInstance(properties, null);

                // Connect to the store
                store = session.getStore("imap");
                store.connect(this.mailServer, this.imapPort, this.korisnickoIme, this.lozinka);

                // Open the INBOX folder
                folder = store.getFolder("INBOX");
                folder.open(Folder.READ_ONLY);

                Message[] messages = null;
                //TODO ne dohvaćati sve poruke odjednom nego ih po grupama dohvatiti (10 po 10 itd.)
                messages = folder.getMessages();

                for (int i = 0; i < messages.length; i++) {
                    //TODO pretražiti tzv. nwtis poruke i obraditi ih 
                }

                folder.close(false);
                store.close();

                System.out.println("Završila iteracija: " + (broj++));

                //TODO spavanje u intervalu (kao kod serijalizatora u prvoj zadaći)
                Thread.sleep(this.spavanje);
                
                logObrade.pohraniPodatke(logDatoteka);
            }
        }
        catch(MessagingException | InterruptedException ex) {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch(IOException ex) {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
