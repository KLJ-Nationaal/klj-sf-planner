package domain;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import jakarta.xml.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.VersionInfo;

import java.util.*;

@XmlRootElement(name = "Sportfeest")
@PlanningSolution
public class Sportfeest {
	private static final Logger logger = LoggerFactory.getLogger(Sportfeest.class);
	private HashSet<Afdeling> afdelingen;
	private HashSet<Ring> ringen;
	private HashMap<String, Discipline> disciplines;
	private HashSet<Restrictie> restricties;
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

	@XmlElementWrapper(name = "Restricties")
	@XmlElement(name = "Restrictie")
	public HashSet<Restrictie> getRestricties() { return restricties; }
	public void setRestricties(HashSet<Restrictie> restricties) { this.restricties = restricties; }

	@PlanningScore
	public HardSoftScore getScore() { return score; }
	public void setScore(HardSoftScore score) { this.score = score; }

	public String getLocatie() { return locatie; }
	public void setLocatie(String locatie) { this.locatie = locatie; }

	public Date getDatum() { return datum; }
	public void setDatum(Date datum) { this.datum = datum; }

	@XmlAttribute(name = "ProgrammaVersie")
	public String getProgramVersion() { return VersionInfo.getVersion(); }

	public Sportfeest(HashSet<Afdeling> afdelingen, HashSet<Ring> ringen, HashMap<String, Discipline> disciplines, HashSet<Restrictie> restricties) {
		this.afdelingen = afdelingen;
		this.ringen = ringen;
		this.disciplines = disciplines;
		this.restricties = restricties;
	}

	public Sportfeest() {
		this(new HashSet<>(), new HashSet<>(), new HashMap<>(), new HashSet<>());
	}
}
