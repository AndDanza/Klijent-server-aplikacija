package org.foi.nwtis.anddanzan.web.kontrole;

import com.sun.mail.imap.IMAPInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;

/**
 *
 * @author dkermek
 */
public class Poruka {

    /**
     *
     */
    public static enum VrstaPoruka {

        /**
         *
         */
        NWTiS_poruka,
        /**
         *
         */
        neNWTiS_poruka
    }

    private String id;
    private Date vrijemeSlanja;
    private Date vrijemePrijema;
    private String salje;
    private String predmet;
    private String privitak;
    private VrstaPoruka vrsta;

    /**
     *
     * @param id
     * @param vrijemeSlanja
     * @param vrijemePrijema
     * @param salje
     * @param predmet
     * @param privitak
     * @param vrsta
     */
    public Poruka(String id, Date vrijemeSlanja, Date vrijemePrijema, String salje, String predmet, String privitak, VrstaPoruka vrsta) {
        this.id = id;
        this.vrijemeSlanja = vrijemeSlanja;
        this.vrijemePrijema = vrijemePrijema;
        this.salje = salje;
        this.predmet = predmet;
        this.privitak = privitak;
        this.vrsta = vrsta;
    }

    /**
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @return
     */
    public Date getVrijemeSlanja() {
        return vrijemeSlanja;
    }

    /**
     *
     * @return
     */
    public Date getVrijemePrijema() {
        return vrijemePrijema;
    }

    /**
     *
     * @return
     */
    public String getPredmet() {
        return predmet;
    }

    /**
     *
     * @return
     */
    public String getSalje() {
        return salje;
    }

    /**
     *
     * @return
     */
    public VrstaPoruka getVrsta() {
        return vrsta;
    }

    /**
     *
     * @return
     */
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
            if (message.getContentType().contains("multipart")) {
                Multipart multiPart = (Multipart) message.getContent();

                if (multiPart.getCount() == 1) {
                    MimeBodyPart attachment = (MimeBodyPart) multiPart.getBodyPart(0);
                    if (Part.ATTACHMENT.equalsIgnoreCase(attachment.getDisposition())) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(attachment.getInputStream(), Charset.defaultCharset()));
                        char cbuf[] = new char[2048];
                        int len;
                        StringBuilder sbuf = new StringBuilder();
                        while ((len = br.read(cbuf, 0, cbuf.length)) != -1) {
                            sbuf.append(cbuf, 0, len);
                        }
                        read = sbuf.toString();
                    }
                }
            }
        }
        catch(MessagingException | IOException ex) {
            Logger.getLogger(Poruka.class.getName()).log(Level.SEVERE, null, ex);
        }

        return read;
    }

}
