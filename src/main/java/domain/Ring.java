package domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "Ring")
public class Ring {  //Analogy Day
    @XmlElement(name = "DisciplineIndex")
    public int disciplineIndex;
    @XmlElement(name = "Naam")
    public String naam;

    private int ringIndex;
    private List<Tijdslot> tijdslots;

    @XmlElement(name = "RingIndex")
    public int getRingIndex() {
        return ringIndex;
    }
    public void setRingIndex(int ringIndex) {
        this.ringIndex = ringIndex;
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

    @Override
    public String toString() {
        return Integer.toString(ringIndex);
    }

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
