package domain;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "Sportfeest")
@PlanningSolution
public class Sportfeest {
    private List<Afdeling> afdelingen;
    private List<Inschrijving> inschrijvingen;
    private List<Ring> ringen;
    private List<Tijdslot> tijdslots;
    private HardSoftScore score;

    @XmlElementWrapper(name = "Afdelingen")
    @XmlElement(name = "Afdeling")
    public List<Afdeling> getAfdelingen() {
        return afdelingen;
    }
    public void setAfdelingen(List<Afdeling> afdelingen) { this.afdelingen = afdelingen;
        /*this.inschrijvingen = new ArrayList<Inschrijving>();
        for (Afdeling afdeling : afdelingen){
            this.inschrijvingen.addAll(afdeling.getInschrijvingen());
        }*/
    }

    @ValueRangeProvider(id = "Inschrijving")
    @PlanningEntityCollectionProperty
    @XmlTransient
    public List<Inschrijving> getInschrijvingen() {
        return inschrijvingen;
    }
    public void setInschrijvingen(List<Inschrijving> inschrijvingen) {
        this.inschrijvingen = inschrijvingen;
    }

    @ValueRangeProvider(id = "Tijdslot")
    @ProblemFactCollectionProperty
    @XmlElementWrapper(name = "Tijdslots")
    @XmlElement(name = "Tijdslot")
    public List<Tijdslot> getTijdslots() {
        return tijdslots;
    }
    public void setTijdslots(List<Tijdslot> tijdslots) {
        this.tijdslots = tijdslots;
    }

    @XmlElementWrapper(name = "Ringen")
    @XmlElement(name = "Ring")
    public List<Ring> getRingen() {
        return ringen;
    }
    public void setRingen(List<Ring> ringen) {
        this.ringen = ringen;
    }

    @PlanningScore
    public HardSoftScore getScore() {
        return score;
    }
    public void setScore(HardSoftScore score) {
        this.score = score;
    }

    public void addInschrijving(Inschrijving inschr) {
        if(inschrijvingen == null) inschrijvingen = new ArrayList<Inschrijving>();
        inschrijvingen.add(inschr);
    }
}
