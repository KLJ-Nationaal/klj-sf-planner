package domain;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "Afdeling")
public class Afdeling {
    private String naam;
    private List<Inschrijving> inschrijvingen;

    @XmlElement(name = "Naam")
    public String getNaam() {
        return naam;
    }
    public void setNaam(String naam) {
        this.naam = naam;
    }

    @XmlElementWrapper(name = "Inschrijvingen")
    @XmlElement(name = "Inschrijving")
    public List<Inschrijving> getInschrijvingen() {
        return inschrijvingen;
    }
    public void setInschrijvingen(List<Inschrijving> inschrijvingen) {
        this.inschrijvingen = inschrijvingen;
    }
}
