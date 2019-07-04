package domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import persistence.Marshalling;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@XmlRootElement(name = "Ring")
public class Ring {
    @XmlElement(name = "Naam")
    public String naam;
    private int index;
    private List<Tijdslot> tijdslots;
    private HashSet<Discipline> disciplines;

    public Ring(String ringNaam, int ringIndex) {
        naam = ringNaam;
        index = ringIndex;
        tijdslots = new ArrayList<>();
        disciplines = new HashSet<>();
    }

    @XmlElement(name = "RingIndex")
    public int getRingIndex() {
        return index;
    }
    public void setRingIndex(int ringIndex) {
        index = ringIndex;
    }

    public List<Tijdslot> getTijdslots() {
        return tijdslots;
    }

    public HashSet<Discipline> getDisciplines() { return disciplines;}
    public void addDiscipline(Discipline discipline) {
        disciplines.add(discipline);
        //tijdslots voor ring maken als ze nog niet bestaan
        if(tijdslots.size() == 0) {
            for (int i = 0; i < Marshalling.TOTALETIJD; i = i + discipline.getDuur()) {  //TODO: property van maken
                Tijdslot tijdslot = new Tijdslot();
                tijdslot.setStartTijd(i);
                tijdslot.setDuur(discipline.getDuur());
                tijdslot.setRing(this);
                tijdslots.add(tijdslot);
            }
        }
    }

    public String getVerkorteNotatie() {
        return naam
                .replace("meisjes","")
                .replace("jongens","")
                .replace("gemengd","");
    }

    @Override
    public String toString() { return naam; }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getRingIndex())
                .toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof Ring) {
            Ring other = (Ring) o;
            return new EqualsBuilder()
                    .append(getRingIndex(), other.getRingIndex())
                    .isEquals();
        } else {
            return false;
        }
    }

}
