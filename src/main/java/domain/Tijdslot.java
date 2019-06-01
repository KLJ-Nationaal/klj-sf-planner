package domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.optaplanner.core.api.domain.lookup.PlanningId;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "Tijdslot")
public class Tijdslot {  //Analogy Timeslot
    private int startTijd;
    private int eindTijd;
    private int duur;
    private Ring ring;

    //@XmlTransient
    //@PlanningId
    //public Integer getId() { return startTijd + ring.getRingIndex() * 100; }

    @XmlElement(name = "Ring")
    public Ring getRing() {
        return ring;
    }
    public void setRing(Ring ring) {
        this.ring = ring;
    }

    @XmlElement(name = "StartTijd")
    public int getStartTijd() {
        return startTijd;
    }
    public void setStartTijd(int startTijd) {
        this.startTijd = startTijd;
    }

    @XmlElement(name = "Duur")
    public int getDuur() {
        return duur;
    }
    public void setDuur(int duur) {
        this.duur = duur;
        this.eindTijd = startTijd + duur;
    }

    public int getEindTijd() {
        return eindTijd;
    }

    //Legacy
    public int getTijdslotIndex() {
        return startTijd;
    }
    public void setTijdslotIndex(int tijdslotIndex) {
        this.startTijd = tijdslotIndex;
    }

    //TODO: verwijderen
    private static final String[] TIMES = {"08:00", "08:03", "08:06", "08:09", "08:12", "08:15", "08:18", "08:21", "08:24", "08:30"};

    public String getLabel() {
        String time = TIMES[startTijd % TIMES.length];
        if (startTijd > TIMES.length) {
            return "Tijdslot " + startTijd;
        }
        return ring.getLabel() + " " + time;
    }
/*
    @Override
    public String toString() { return Integer.toString(startTijd); }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getRing())
                .append(getStartTijd())
                .toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof Tijdslot) {
            Tijdslot other = (Tijdslot) o;
            return new EqualsBuilder()
                    .append(getRing(), other.getRing())
                    .append(getStartTijd(), other.getStartTijd())
                    .isEquals();
        } else {
            return false;
        }
    }*/
}
