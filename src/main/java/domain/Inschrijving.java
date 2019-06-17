package domain;

import difficulty.InschrijvingDifficultyComparator;
import difficulty.TijdslotStrengthComparator;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "Inschrijving")
@PlanningEntity(difficultyComparatorClass = InschrijvingDifficultyComparator.class)
public class Inschrijving {
    private Integer id;
    private Afdeling afdeling;
    private Ring ring;
    private Tijdslot tijdslot;

    @XmlTransient
    //@PlanningId
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    @XmlElement(name = "Tijdslot")
    @PlanningVariable(valueRangeProviderRefs = {"Tijdslot"},
            strengthComparatorClass = TijdslotStrengthComparator.class,
            nullable=true)
    public Tijdslot getTijdslot() {
        return tijdslot;
    }
    public void setTijdslot(Tijdslot tijdslot) { this.tijdslot = tijdslot; }

    @XmlTransient
    public Afdeling getAfdeling() { return afdeling; }
    public void setAfdeling(Afdeling afdeling) { this.afdeling = afdeling; }

    @XmlElement(name = "Ring")
    public Ring getRing() { return ring; }
    public void setRing(Ring ring) { this.ring = ring; }

    public String getLabel() {
        return afdeling.getNaam() + " in " + ring.getLabel();
    }

    @Override
    public String toString() {
        return Integer.toString(id);
    }
}
