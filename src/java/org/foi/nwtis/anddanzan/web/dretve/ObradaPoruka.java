package org.foi.nwtis.anddanzan.web.dretve;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.mail.imap.IMAPInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Set;
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

    //podaci za bazu i spajanje
    BP_Konfiguracija konfiguracija;
    Connection connection;
    Statement statement;

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
                String url = konfiguracija.getServerDatabase() + konfiguracija.getUserDatabase();
                this.connection = DriverManager.getConnection(url, konfiguracija.getUserUsername(), konfiguracija.getUserPassword());
                this.statement = this.connection.createStatement();

                this.logObrade = new DatotekaRadaDretve();

                // Start the session
                Properties properties = System.getProperties();
                properties.put("mail.smtp.host", this.mailServer);
                properties.put("mail.imap.port", this.imapPort);
                this.session = Session.getInstance(properties, null);
                
                SlusacAplikacije.kontekst.setAttribute("mail_session", this.session);

                // Connect to the store
                store = session.getStore("imap");
                store.connect(this.mailServer, this.imapPort, this.korisnickoIme, this.lozinka);

                // Open the INBOX folder
                folder = store.getFolder("INBOX");
                folder.open(Folder.READ_ONLY);

                nwtisMapa = provjeraNwtisMape();

                //TODO ne dohvaćati sve poruke odjednom nego ih po grupama dohvatiti (numMessagesToRead)
                //definira koliko se poruka odjednom obrađuje this.numMessagesToRead;
                Message[] messages = null;
                messages = folder.getMessages();

                //TODO dohvatiti broj poruka koji se obrađuje samo u ovom ciklusu
                for (int i = 0; i < messages.length; i++) {
                    //System.out.println("poruka glasi: " + getMailContent(messages[i]) + messages[i].getContentType() + " - is json: " + messages[i].isMimeType("text/json"));
//                    messages[i].setFlag(Flags.Flag.DELETED, true);
                    sortirajMail(messages[i]);
                }

                folder.close(false);
                store.close();

                this.logObrade.pohraniPodatke(logDatoteka);
                System.out.println("Završila iteracija: " + (broj++));

                long trajanje = System.currentTimeMillis() - start;
                long sleepTime = this.milisecSpavanje - trajanje;

                Thread.sleep(sleepTime);
                this.logObrade = null;
                this.statement.close();
                this.connection.close();
            }
        }
        catch(MessagingException | InterruptedException | SQLException ex) {
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
            this.logObrade.setBrojObradenihPoruka(this.logObrade.getBrojObradenihPoruka()+1);

            if (privitak.contains(this.oznakaNwtisPoruke)) {
                System.out.println("Imate NWTiS poruku");

                obradiNwtisPoruku(message);

                Message[] msg = new Message[]{message};
                folder.copyMessages(msg, nwtisMapa);
                message.setFlag(Flags.Flag.DELETED, true);
                folder.expunge();
            }
            else {
                System.out.println("Imate neNWTiS poruku");
            }
        }catch(MessagingException ex) {
            this.logObrade.setBrojNeispravnihPoruka(this.logObrade.getBrojNeispravnihPoruka()+1);
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Metoda za obradu NWTiS poruke. Sadržaj poruke obrađuje se kao
     * <code>JsonObject</code>
     *
     * @param message
     */
    private void obradiNwtisPoruku(Message message) {
        String jsonString = getMailContent(message);
        
        try {         
            //dohvaćanje jsona unutar mail
            JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
            String komanda = jsonObject.get("komanda").getAsString();
            int idUredaja = jsonObject.get("id").getAsInt();

            String upit = "";
            if (komanda.equalsIgnoreCase("dodaj") && provjeriID(idUredaja) == -1) {
                String naziv = jsonObject.get("naziv").getAsString();
                String kreiranje = jsonObject.get("vrijeme").getAsString();
                upit = "INSERT INTO `uredaji`(`id`, `naziv`, `sadrzaj`, `vrijeme_kreiranja`) "
                        + "VALUES (" + idUredaja + ",'" + naziv + "','" + jsonString + "', '" + kreiranje + "')";
                this.statement.execute(upit);
                this.logObrade.setBrojDodanihIOT(this.logObrade.getBrojDodanihIOT()+1);
            }
            else if (komanda.equalsIgnoreCase("azuriraj") && provjeriID(idUredaja) != -1) {
                String azuriraniJsonString = azurirajPodatke(jsonString, idUredaja);
                upit = "UPDATE `uredaji` SET `sadrzaj` = '" + azuriraniJsonString + "' WHERE `id` = " + idUredaja;
                this.statement.execute(upit);
                this.logObrade.setBrojAzuriranihIOT(this.logObrade.getBrojAzuriranihIOT()+1);
            }
            zapisiUDnevnik(jsonString);

        }
        catch(SQLException ex) {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch(JsonSyntaxException ex){
            this.logObrade.setBrojNeispravnihPoruka(this.logObrade.getBrojNeispravnihPoruka()+1);
            zapisiUDnevnik(jsonString);
        }
    }

    /**
     * Metoda za ažuriranje sadržaja iot uređaja. Postupak ažuriranja vrši se
     * preko dvije varijable tipa <code>Properties</code>. Jedna s novim
     * podacima, a druga s podacima iz baze. U varijablu s podacima iz baze
     * upisuju se novi i mijenjaju postojeći te potom sve pomoću gson-a ide u
     * <code>String</code>
     *
     * @param stmt kreirani <code>Statement</code> za bazu
     * @param jsonString json sadržaj u varijabli tipa <code>String</code>
     * @param id identifikator uređaja
     * @return <code>String</code> vrijednost novog ažuriranog sadržajas
     */
    private String azurirajPodatke(String jsonString, int id) {
        Properties stariPodaci = null;

        try {
            String upit = "SELECT `sadrzaj` FROM `uredaji` WHERE `id` = " + id;
            ResultSet podaci = this.statement.executeQuery(upit);
            if (podaci.next()) {
                String sadrzaj = podaci.getString("sadrzaj");
                stariPodaci = new GsonBuilder().create().fromJson(sadrzaj, Properties.class);

                Properties noviPodaci = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().fromJson(jsonString, Properties.class);

                Set<String> keysKlijent = noviPodaci.stringPropertyNames();
                for (String keyK : keysKlijent) {
                    stariPodaci.setProperty(keyK, noviPodaci.getProperty(keyK));
                }
            }
            
            podaci.close();
        }
        catch(SQLException ex) {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new GsonBuilder().create().toJson(stariPodaci);
    }

    /**
     * Metoda za provjeru postoji li dani id i u bazi podataka te je li id manji
     * od 4 (u zadatku je raspon id-a uređaja dan od 1 do 4)
     *
     * @param stmt kreirani <code>Statement</code> za bazu podataka
     * @param naziv naziv uređaja zadan u jason-u
     * @param id identifikator uređaja (int)
     * @return -1 (uređaj s danim id-em ne postoji) u suprotnom vraća se id
     * uređaja
     */
    private int provjeriID(int id) {
        if (id < 5 && id > 0) {
            try {
                String upit = "SELECT `id` FROM `uredaji` WHERE `id` = " + id;
                ResultSet podaci = this.statement.executeQuery(upit);
                if (podaci.next()) {
                    return podaci.getInt("id");
                }
                
                podaci.close();
            }
            catch(SQLException ex) {
                System.out.println("Greška prilikom provjera ID-a u bazi podataka");
            }
        }
        return -1;
    }

    /**
     * Zapisivanje u dnevnik u bazi podataka
     *
     * @param sadrzaj sadržaj koji se zapisuje u log (nastale promjene)
     */
    private void zapisiUDnevnik(String sadrzaj) {
        try {
            String upit = "INSERT INTO `dnevnik`(`sadrzaj`) VALUES ('" + sadrzaj + "')";
            this.statement.execute(upit);
        }
        catch(SQLException ex) {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Metoda za pretvaranje <code>IMAPInputStream</code> maila u
     * <code>String</code>
     *
     * @param message mail
     * @return <code>String</code> vrijednost samog sadržaja maila
     */
    public static String getMailContent(Message message) {
        String read = "";

        try {
            IMAPInputStream imapStream = (IMAPInputStream) message.getContent();
            BufferedReader br = new BufferedReader(new InputStreamReader(imapStream, Charset.defaultCharset()));
            char cbuf[] = new char[2048];
            int len;
            StringBuilder sbuf = new StringBuilder();
            while ((len = br.read(cbuf, 0, cbuf.length)) != -1) {
                sbuf.append(cbuf, 0, len);
            }
            read = sbuf.toString();
        }
        catch(IOException | MessagingException ex) {
            read = "";
        }

        return read;
    }

    /**
     * Provjera postoji li zadana mapa za NWTiS poruke, ako ne kreira se nova i
     * vraća u obliku tipa <code>Folder</code>
     *
     * @return <code>null</code> ako je došlo do iznike ili objekt mape
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
