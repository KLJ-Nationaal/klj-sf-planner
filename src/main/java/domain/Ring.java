package domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "Ring")
public class Ring {
    @XmlElement(name = "Naam")
    public String naam;

    private int index;
    private List<Tijdslot> tijdslots;

    public Ring(String ringNaam, int ringIndex) {
        naam = ringNaam;
        index = ringIndex;
        tijdslots = new ArrayList<>();
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
    public void setTijdslots(List<Tijdslot> tijdslots) {
        this.tijdslots = tijdslots;
    }

    public String getLabel() {
        return naam;
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
