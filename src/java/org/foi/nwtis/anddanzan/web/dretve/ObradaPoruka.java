package org.foi.nwtis.anddanzan.web.dretve;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import org.foi.nwtis.anddanzan.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.anddanzan.web.kontrole.DatotekaRadaDretve;
import org.foi.nwtis.anddanzan.web.slusaci.SlusacAplikacije;

/**
 * Dretva koja je pokrenuta prilikom inicijalizacije samog konteksta.
 *
 * Namjena dretve je u pravilnim intervalima zadanim u konfiguracijskoj datoteci
 * pregledavati postoji li nova poruka u sandčiću korisnika.
 *
 * @author Andrea
 */
public class ObradaPoruka extends Thread {

    //varijable sesije i mapa za mailove
    Session session;
    Store store;
    Folder folder;
    Folder nwtisMapa;

    BP_Konfiguracija konfiguracija;

    public static DatotekaRadaDretve logObrade = null;

    //spajanje na mail server
    private String mailServer;
    private int imapPort;
    private String korisnickoIme;
    private String lozinka;

    //rad dretve (trajanje ciklusa i prekidanje)
    private int milisecSpavanje;
    private boolean radi = true;

    //obrada primljene poruke
    private int numMessagesToRead;
    private String oznakaNwtisPoruke;
    private String mapaNwtisPoruka;
    private String logDatoteka;

    public ObradaPoruka() {
        this.konfiguracija = (BP_Konfiguracija) SlusacAplikacije.kontekst.getAttribute("BP_Konfig");
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
        this.milisecSpavanje = konfiguracija.getTimeSecThreadCycle() * 1000;
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
                long start = System.currentTimeMillis();

                logObrade = new DatotekaRadaDretve();

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

                nwtisMapa = provjeraNwtisMape();

                //TODO ne dohvaćati sve poruke odjednom nego ih po grupama dohvatiti (numMessagesToRead)
                Message[] messages = null;
                messages = folder.getMessages();

                //TODO dohvatiti broj poruka koji se obrađuje samo u ovom ciklusu
                for (int i = 0; i < messages.length; i++) {
                    //TODO pretražiti tzv. nwtis poruke i obraditi ih 
                    //System.out.println("poruka glasi: " + getMailContent(messages[i]) + messages[i].getContentType() + " - is json: " + messages[i].isMimeType("text/json"));
                    sortirajMail(messages[i]);
//                    if (!) {
//                        zapisiUDnevnik("neispravna");
//                    }
                }

                folder.close(false);
                store.close();

                logObrade.pohraniPodatke(logDatoteka);
                System.out.println("Završila iteracija: " + (broj++));

                long trajanje = System.currentTimeMillis() - start;
                long sleepTime = this.milisecSpavanje - trajanje;

                Thread.sleep(sleepTime);
                logObrade = null;
            }
        }
        catch(MessagingException | InterruptedException ex) {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Provjera je li dobivena poruka NWTiS ili neNWTiS te shodno tome
     * premještanje u zadanu mapu za NWTiS poruke
     *
     * @param message mail
     * @return true () ili false
     */
    private void sortirajMail(Message message) {
        try {
            String privitak = message.getFileName();

            if (privitak.contains(this.oznakaNwtisPoruke)) {
                System.out.println("Imate NWTiS poruku");
                Message[] msg = new Message[]{message};
                folder.copyMessages(msg, nwtisMapa);
                message.setFlag(Flags.Flag.DELETED, true);
                folder.expunge();
            }
            else {
                System.out.println("Imate neNWTiS poruku");
            }
        }
        catch(MessagingException ex) {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Zapisivanje u dnevnik u bazi podataka
     *
     * @param sadrzaj sadržaj koji se zapisuje u log (nastale promjene)
     */
    private void zapisiUDnevnik(String sadrzaj) {
        switch (sadrzaj) {
            case "neispravna":
                break;
        }
    }

    /**
     * Provjera postoji li zadana mapa za NWTiS poruke, ako ne kreira se nova i
     * vraća u obliku tipa <code>Folder</code>
     *
     * @return
     */
    private Folder provjeraNwtisMape() {
        try {
            Folder nwtisFolder = store.getFolder(this.mapaNwtisPoruka);
            if (!nwtisFolder.exists()) {
                nwtisFolder.create(Folder.HOLDS_MESSAGES);
            }
            nwtisFolder.open(Folder.READ_ONLY);
            return nwtisFolder;
        }
        catch(MessagingException ex) {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
