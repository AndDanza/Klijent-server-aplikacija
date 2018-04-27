package org.foi.nwtis.anddanzan.web.kontrole;

/**
 *
 * @author dkermek
 */
public class Izbornik {
    private String labela;
    private String vrijednost;

    /**
     *
     * @param labela
     * @param vrijednost
     */
    public Izbornik(String labela, String vrijednost) {
        this.labela = labela;
        this.vrijednost = vrijednost;
    }

    /**
     *
     * @return
     */
    public String getLabela() {
        return labela;
    }

    /**
     *
     * @param labela
     */
    public void setLabela(String labela) {
        this.labela = labela;
    }

    /**
     *
     * @return
     */
    public String getVrijednost() {
        return vrijednost;
    }

    /**
     *
     * @param vrijednost
     */
    public void setVrijednost(String vrijednost) {
        this.vrijednost = vrijednost;
    }        
}
