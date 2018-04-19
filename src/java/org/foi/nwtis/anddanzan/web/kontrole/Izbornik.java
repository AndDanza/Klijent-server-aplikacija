package org.foi.nwtis.anddanzan.web.kontrole;

/**
 *
 * @author dkermek
 */
public class Izbornik {
    private String labela;
    private String vrijednost;

    public Izbornik(String labela, String vrijednost) {
        this.labela = labela;
        this.vrijednost = vrijednost;
    }

    public String getLabela() {
        return labela;
    }

    public void setLabela(String labela) {
        this.labela = labela;
    }

    public String getVrijednost() {
        return vrijednost;
    }

    public void setVrijednost(String vrijednost) {
        this.vrijednost = vrijednost;
    }        
}
