package domain;

import java.util.Objects;

public class Discipline {
    private String naam;
    private String ringNaam;
    private int duur;
    private boolean meisjes;
    private boolean jongens;

    public String getNaam() { return naam; }
    public void setNaam(String naam) {
        this.naam = naam;
        meisjes = true;
        jongens = true;
        if(naam.toLowerCase().contains("gilden")) meisjes = false;
        if(naam.toLowerCase().contains("vendelen")) meisjes = false;
        if(naam.toLowerCase().contains("jongens")) meisjes = false;
        if(naam.toLowerCase().contains("wimpelen")) jongens = false;
        if(naam.toLowerCase().contains("meisjes")) jongens = false;
    }

    public String getRingNaam() { return ringNaam; }
    public void setRingNaam(String ringNaam) { this.ringNaam = ringNaam; }

    public int getDuur() { return duur; }
    public void setDuur(int duur) { this.duur = duur; }

    public boolean isMeisjes(){ return meisjes; }
    public boolean isJongens(){ return jongens; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Discipline that = (Discipline) o;
        return naam.equals(that.naam);
    }

    @Override
    public int hashCode() {
        return Objects.hash(naam);
    }
}
