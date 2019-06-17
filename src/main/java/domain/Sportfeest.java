package domain;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@XmlRootElement(name = "Sportfeest")
@PlanningSolution
public class Sportfeest {
    private HashSet<Afdeling> afdelingen;
    private List<Inschrijving> inschrijvingen;
    private HashSet<Ring> ringen;
    private List<Tijdslot> tijdslots;
    private HardSoftScore score;

    @XmlElementWrapper(name = "Afdelingen")
    @XmlElement(name = "Afdeling")
    public HashSet<Afdeling> getAfdelingen() {
        return afdelingen;
    }
    public void setAfdelingen(HashSet<Afdeling> afdelingen) { this.afdelingen = afdelingen; }

    @ValueRangeProvider(id = "Inschrijving")
    @PlanningEntityCollectionProperty
    @XmlElementWrapper(name = "Inschrijvingen")
    @XmlElement(name = "Inschrijving")
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
    public HashSet<Ring> getRingen() {
        return ringen;
    }
    public void setRingen(HashSet<Ring> ringen) {
        this.ringen = ringen;
    }

    @PlanningScore
    public HardSoftScore getScore() {
        return score;
    }
    public void setScore(HardSoftScore score) {
        this.score = score;
    }

    public Sportfeest(HashSet<Afdeling> afdelingen, List<Inschrijving> inschrijvingen, HashSet<Ring> ringen, List<Tijdslot> tijdslots) {
        this.afdelingen = afdelingen;
        this.inschrijvingen = inschrijvingen;
        this.ringen = ringen;
        this.tijdslots = tijdslots;
    }

    public Sportfeest(){
        this(new HashSet<>(), new ArrayList<>(), new HashSet<>(), new ArrayList<>());
    }
}
