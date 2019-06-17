package domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.optaplanner.core.api.domain.lookup.PlanningId;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@XmlRootElement(name = "Tijdslot")
public class Tijdslot {
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

    public String getStartTijdFormatted() {
        try {
            SimpleDateFormat df = new SimpleDateFormat("HH:mm");
            Date d = df.parse("08:00");
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            cal.add(Calendar.MINUTE, startTijd);
            return df.format(cal.getTime());
        } catch (Exception e) {
            return String.valueOf(startTijd);
        }
    }

    public String getLabel() {
        return ring.getLabel() + " " + getStartTijdFormatted();
    }

    @Override
    public String toString() { return ring.toString() + " " + getStartTijdFormatted(); }

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
    }
}
