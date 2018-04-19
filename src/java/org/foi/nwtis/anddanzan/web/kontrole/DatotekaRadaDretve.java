package org.foi.nwtis.anddanzan.web.kontrole;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

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
    private long pocetak;
    private long zavrsetak;
    private long trajanje;
    private int brojObradenihPoruka;
    private int brojDodanihIOT;
    private int brojAzuriranihIOT;
    private int brojNeispravnihPoruka;

    public DatotekaRadaDretve() {
        DatotekaRadaDretve.brojObrade++;
        this.pocetak = System.currentTimeMillis();
    }

    public int getBrojObrade() {
        return DatotekaRadaDretve.brojObrade;
    }

    public long getPocetak() {
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

    public void pohraniPodatke(String datoteka) throws IOException {
        this.zavrsetak = System.currentTimeMillis();
        this.trajanje = this.zavrsetak - this.pocetak;

        FileOutputStream fos = new FileOutputStream(datoteka);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this);
        oos.close();
        fos.close();
    }
}
