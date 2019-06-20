package domain;

public class Discipline {
    public String naam;
    public String ringNaam;
    public int duur;

    public boolean isMeisjes(){
        boolean ok = true;
        if(naam.toLowerCase().contains("gilden")) ok = false;
        if(naam.toLowerCase().contains("vendelen")) ok = false;
        if(naam.toLowerCase().contains("jongens")) ok = false;
        return ok;
    }

    public boolean isJongens(){
        boolean ok = true;
        if(naam.toLowerCase().contains("wimpelen")) ok = false;
        if(naam.toLowerCase().contains("meisjes")) ok = false;
        return ok;
    }
}
