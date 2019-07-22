package domain;

import difficulty.InschrijvingDifficultyComparator;
import difficulty.TijdslotStrengthComparator;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.List;
import java.util.Objects;

@XmlRootElement(name = "Inschrijving")
@PlanningEntity(difficultyComparatorClass = InschrijvingDifficultyComparator.class)
public class Inschrijving {
	private Integer id;
	private Afdeling afdeling;
	private Ring ring;
	private Tijdslot tijdslot;
	private Discipline discipline;
	private int korps;

	@XmlTransient
	@PlanningId
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

	@XmlElement(name = "Discipline")
	public Discipline getDiscipline() { return discipline; }
	public void setDiscipline(Discipline discipline) { this.discipline = discipline; }

	public int getKorps() {	return korps; }
	public void setKorps(int korps) { this.korps = korps; }

	@ValueRangeProvider(id = "Tijdslot")
	public List<Tijdslot> getTijdslots(){ return ring.getTijdslots(); }

	public int getStartTijd(){
		if (tijdslot == null) return 0;
		return tijdslot.getStartTijd();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Inschrijving that = (Inschrijving) o;
		return id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "[" + id + "] " + afdeling.getNaam() + " in " + ring.getVerkorteNotatie();
	}

}
