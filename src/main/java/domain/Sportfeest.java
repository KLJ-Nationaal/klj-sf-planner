package domain;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@XmlRootElement(name = "Sportfeest")
@PlanningSolution
public class Sportfeest {
    private HashSet<Afdeling> afdelingen;
    private HashSet<Ring> ringen;
    private HashMap<String, Discipline> disciplines;
    private HardSoftScore score;
    private String locatie;
    private String datum;

    @XmlElementWrapper(name = "Afdelingen")
    @XmlElement(name = "Afdeling")
    public HashSet<Afdeling> getAfdelingen() { return afdelingen; }
    public void setAfdelingen(HashSet<Afdeling> afdelingen) { this.afdelingen = afdelingen; }

    @PlanningEntityCollectionProperty
    @XmlElementWrapper(name = "Inschrijvingen")
    @XmlElement(name = "Inschrijving")
    public List<Inschrijving> getInschrijvingen() {
        List<Inschrijving> all = new ArrayList<>();
        for(Afdeling afdeling : afdelingen){
            all.addAll(afdeling.getInschrijvingen());
        }
        return all;
    }

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

    public HashMap<String, Discipline> getDisciplines() { return disciplines; }
    public void setDisciplines(HashMap<String, Discipline> disciplines) { this.disciplines = disciplines; }

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

    public Sportfeest(HashSet<Afdeling> afdelingen, HashSet<Ring> ringen, HashMap<String, Discipline> disciplines) {
        this.afdelingen = afdelingen;
        this.ringen = ringen;
        this.disciplines = disciplines;
    }

    public Sportfeest(){
        this(new HashSet<>(), new HashSet<>(), new HashMap<>());
    }
}
