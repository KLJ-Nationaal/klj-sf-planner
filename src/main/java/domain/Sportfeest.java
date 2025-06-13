package domain;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.*;

@XmlRootElement(name = "Sportfeest")
@PlanningSolution
public class Sportfeest {
	private HashSet<Afdeling> afdelingen;
	private HashSet<Ring> ringen;
	private HashMap<String, Discipline> disciplines;
	private HardSoftScore score;
	private String locatie;
	private Date datum;

	@XmlElementWrapper(name = "Afdelingen")
	@XmlElement(name = "Afdeling")
	public HashSet<Afdeling> getAfdelingen() { return afdelingen; }
	public void setAfdelingen(HashSet<Afdeling> afdelingen) { this.afdelingen = afdelingen; }

	@PlanningEntityCollectionProperty
	@XmlTransient
	public List<Inschrijving> getInschrijvingen() {
		List<Inschrijving> all = new ArrayList<>();
		for (Afdeling afdeling : afdelingen) {
			all.addAll(afdeling.getInschrijvingen());
		}
		return all;
	}

	@ProblemFactCollectionProperty
	@XmlTransient
	public List<Tijdslot> getTijdslots() {
		List<Tijdslot> all = new ArrayList<>();
		for (Ring ring : ringen) {
			all.addAll(ring.getTijdslots());
		}
		return all;
	}

	@XmlElementWrapper(name = "Disciplines")
	@XmlElement(name = "Discipline")
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

	public Date getDatum() { return datum; }
	public void setDatum(Date datum) { this.datum = datum; }

	public Sportfeest(HashSet<Afdeling> afdelingen, HashSet<Ring> ringen, HashMap<String, Discipline> disciplines) {
		this.afdelingen = afdelingen;
		this.ringen = ringen;
		this.disciplines = disciplines;
	}

	public Sportfeest() {
		this(new HashSet<>(), new HashSet<>(), new HashMap<>());
	}
}
