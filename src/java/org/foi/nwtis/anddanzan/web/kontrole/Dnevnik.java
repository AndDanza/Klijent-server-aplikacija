package org.foi.nwtis.anddanzan.web.kontrole;

import java.util.Date;

/**
 *
 * @author dkermek
 */
public class Dnevnik {

    private int id;
    private String sadrzaj;
    private Date vrijemeZapisa;

    /**
     *
     * @param id
     * @param sadrzaj
     * @param vrijemeZapisa
     */
    public Dnevnik(int id, String sadrzaj, Date vrijemeZapisa) {
        this.id = id;
        this.sadrzaj = sadrzaj;
        this.vrijemeZapisa = vrijemeZapisa;
    }

    /**
     *
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     *
     * @return
     */
    public String getSadrzaj() {
        return sadrzaj;
    }

    /**
     *
     * @return
     */
    public Date getVrijemeZapisa() {
        return vrijemeZapisa;
    }
}
