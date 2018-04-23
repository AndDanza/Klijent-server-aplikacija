package org.foi.nwtis.anddanzan.web.kontrole;

import com.sun.mail.imap.IMAPInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Date;
import javax.mail.Message;
import javax.mail.MessagingException;

/**
 *
 * @author dkermek
 */
public class Poruka {

    public static enum VrstaPoruka {
        NWTiS_poruka,
        neNWTiS_poruka
    }

    private String id;
    private Date vrijemeSlanja;
    private Date vrijemePrijema;
    private String salje;
    private String predmet;
    private String privitak;
    private VrstaPoruka vrsta;

    public Poruka(String id, Date vrijemeSlanja, Date vrijemePrijema, String salje, String predmet, String privitak, VrstaPoruka vrsta) {
        this.id = id;
        this.vrijemeSlanja = vrijemeSlanja;
        this.vrijemePrijema = vrijemePrijema;
        this.salje = salje;
        this.predmet = predmet;
        this.privitak = privitak;
        this.vrsta = vrsta;
    }

    public String getId() {
        return id;
    }

    public Date getVrijemeSlanja() {
        return vrijemeSlanja;
    }

    public Date getVrijemePrijema() {
        return vrijemePrijema;
    }

    public String getPredmet() {
        return predmet;
    }

    public String getSalje() {
        return salje;
    }

    public VrstaPoruka getVrsta() {
        return vrsta;
    }

    public String getPrivitak() {
        return privitak;
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

}
