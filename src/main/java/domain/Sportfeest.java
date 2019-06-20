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
    private HardSoftScore score;
    private String locatie;
    private String datum;

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

    //@ValueRangeProvider(id = "Tijdslot")
    @ProblemFactCollectionProperty
    @XmlElementWrapper(name = "Tijdslots")
    @XmlElement(name = "Tijdslot")
    public List<Tijdslot> getTijdslots() {
        List<Tijdslot> all = new ArrayList<>();
        for(Ring ring : ringen){
            all.addAll(ring.getTijdslots());
        }
        return all;
    }

    @XmlElementWrapper(name = "Ringen")
    @XmlElement(name = "Ring")
    public HashSet<Ring> getRingen() { return ringen; }
    public void setRingen(HashSet<Ring> ringen) { this.ringen = ringen; }

    @PlanningScore
    public HardSoftScore getScore() { return score; }
    public void setScore(HardSoftScore score) { this.score = score; }

    public String getLocatie() { return locatie; }
    public void setLocatie(String locatie) { this.locatie = locatie; }

    public String getDatum() { return datum; }
    public void setDatum(String datum) { this.datum = datum; }

    public Sportfeest(HashSet<Afdeling> afdelingen, List<Inschrijving> inschrijvingen, HashSet<Ring> ringen) {
        this.afdelingen = afdelingen;
        this.inschrijvingen = inschrijvingen;
        this.ringen = ringen;
    }

    public Sportfeest(){
        this(new HashSet<>(), new ArrayList<>(), new HashSet<>());
    }
}
