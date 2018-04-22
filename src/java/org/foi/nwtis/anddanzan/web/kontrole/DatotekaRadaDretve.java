package org.foi.nwtis.anddanzan.web.kontrole;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import org.nwtis.anddanzan.konfiguracije.Konfiguracija;
import org.nwtis.anddanzan.konfiguracije.KonfiguracijaApstraktna;
import org.nwtis.anddanzan.konfiguracije.NeispravnaKonfiguracija;
import org.nwtis.anddanzan.konfiguracije.NemaKonfiguracije;

/**
 * Dretva na kraju svakog ciklusa dodaje podatke o radu u datoteku
 *
 * Klasa definira strukturu datoteke koju kreira dretva
 *
 * @author Andrea
 */
public class DatotekaRadaDretve implements Serializable {

    //broj iteracije
    static private int brojObrade = 0;
    private String pocetak;
    private String zavrsetak;
    private long trajanje;
    private int brojObradenihPoruka;
    private int brojDodanihIOT;
    private int brojAzuriranihIOT;
    private int brojNeispravnihPoruka;

    /**
     * Konstruktor klase. Svakom inicijalizacijom povećava se statični brojač i
     * postavlja se vrijeme početka
     */
    public DatotekaRadaDretve() {
        DatotekaRadaDretve.brojObrade++;

        inicijalizacijaPodataka();
    }

    public int getBrojObrade() {
        return DatotekaRadaDretve.brojObrade;
    }

    public String getPocetak() {
        return pocetak;
    }

    public int getBrojObradenihPoruka() {
        return brojObradenihPoruka;
    }

    public void setBrojObradenihPoruka(int brojObradenihPoruka) {
        this.brojObradenihPoruka = brojObradenihPoruka;
    }

    public int getBrojDodanihIOT() {
        return brojDodanihIOT;
    }

    public void setBrojDodanihIOT(int brojDodanihIOT) {
        this.brojDodanihIOT = brojDodanihIOT;
    }

    public int getBrojAzuriranihIOT() {
        return brojAzuriranihIOT;
    }

    public void setBrojAzuriranihIOT(int brojAzuriranihIOT) {
        this.brojAzuriranihIOT = brojAzuriranihIOT;
    }

    public int getBrojNeispravnihPoruka() {
        return brojNeispravnihPoruka;
    }

    public void setBrojNeispravnihPoruka(int brojNeispravnihPoruka) {
        this.brojNeispravnihPoruka = brojNeispravnihPoruka;
    }

    /**
     * Metoda za pohranu podataka o radu dretve. Pohrana se vrši pomoću
     * serijalizacije (File i Object OutputStream-ovima)
     *
     * @param datoteka datoteka u koju se pohranjuju podaci o radu dretve
     * @throws IOException
     */
    public void pohraniPodatke(String datoteka) {
        try {
            Date date = new Date(System.currentTimeMillis());
            DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss:SSS zzz");
            this.zavrsetak = formatter.format(date);

            this.trajanje = formatter.parse(this.zavrsetak).getTime() - formatter.parse(this.pocetak).getTime();

            Properties prop = serijalizirajPodatke();

            Konfiguracija pohranaEvidencije = KonfiguracijaApstraktna.kreirajKonfiguraciju(datoteka);
            pohranaEvidencije.kopirajKonfiguraciju(prop);
            pohranaEvidencije.spremiKonfiguraciju();
        }
        catch(NeispravnaKonfiguracija | ParseException | NemaKonfiguracije ex) {
            System.out.println("Greška prilikom dohvaćanja i pohrane evidencije rada dretve");
        }
    }

    /**
     * Metoda za inicijalizaciju varijabli klase
     */
    private void inicijalizacijaPodataka() {
        this.zavrsetak = "";
        this.trajanje = 0;
        this.brojObradenihPoruka = 0;
        this.brojDodanihIOT = 0;
        this.brojAzuriranihIOT = 0;
        this.brojNeispravnihPoruka = 0;

        Date date = new Date(System.currentTimeMillis());
        DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss:SSS zzz");

        this.pocetak = formatter.format(date);
    }

    /**
     * Podaci iz objekta pohranjuju se u objekt tipa <code>Properties</code>
     *
     * @return objekt tipa <code>Properties</code>
     */
    private Properties serijalizirajPodatke() {
        Properties podaci = new Properties();

        podaci.put("Obrada.poruka.broj", String.valueOf(this.brojObrade));
        podaci.put("Obrada.zapocela.u", String.valueOf(this.pocetak));
        podaci.put("Obrada.zavrsila.u", String.valueOf(this.zavrsetak));
        podaci.put("Trajanje.obrade.u.ms", String.valueOf(this.trajanje));
        podaci.put("Broj.poruka", String.valueOf(this.brojObradenihPoruka));
        podaci.put("Broj.dodanih.IOT", String.valueOf(this.brojDodanihIOT));
        podaci.put("Broj.azuriranih.IOT", String.valueOf(this.brojAzuriranihIOT));
        podaci.put("Broj.neispravnih.poruka", String.valueOf(this.brojNeispravnihPoruka));

        return podaci;
    }
}
