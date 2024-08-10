package domain;

import difficulty.InschrijvingDifficultyComparator;
import difficulty.TijdslotStrengthComparator;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.entity.PlanningPin;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import ui.visualization.jfxtras.scene.control.agenda.InschrijvingInterface;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;
import java.util.List;
import java.util.Objects;

@PlanningEntity(difficultyComparatorClass = InschrijvingDifficultyComparator.class)
public class Inschrijving implements InschrijvingInterface {
	private Integer id;
	private Afdeling afdeling;
	private Ring ring;
	private Tijdslot tijdslot;
	private Discipline discipline;
	private int korps;
	private List<Ring> mogelijkeRingen;
	private Inschrijving verbondenInschrijving;
	private boolean gereserveerdBlok;

	@XmlTransient
	@PlanningId
	public Integer getId() { return id; }
	public void setId(Integer id) { this.id = id; }

	@XmlAttribute(name = "id")
	@XmlID
	public String getRef() { return "I"+ id; }
	public void setRef(String s) { this.id = Integer.parseInt(s.substring(1)); }

	@XmlIDREF
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

	@XmlIDREF
	public Ring getRing() { return ring; }
	public void setRing(Ring ring) { this.ring = ring; }

	@XmlIDREF
	public Discipline getDiscipline() { return discipline; }
	public void setDiscipline(Discipline discipline) { this.discipline = discipline; }

	@XmlIDREF
	public Inschrijving getVerbondenInschrijving() { return verbondenInschrijving; }
	public void setVerbondenInschrijving(Inschrijving inschrijving) { this.verbondenInschrijving = inschrijving; }

	public int getKorps() {	return korps; }
	public void setKorps(int korps) { this.korps = korps; }

	@ValueRangeProvider(id = "Tijdslot")
	public List<Tijdslot> getTijdslots(){ return ring.getTijdslots(); }

	public int getStartTijd(){
		if (tijdslot == null) return 0;
		return tijdslot.getStartTijd();
	}

	public int getEindTijd(){
		if (tijdslot == null) return 0;
		return tijdslot.getEindTijd();
	}

	@PlanningPin
	public boolean isGereserveerdBlok() { return gereserveerdBlok; }
	public void setGereserveerdBlok(boolean gereserveerdBlok) { this.gereserveerdBlok = gereserveerdBlok; }

	@XmlIDREF
	public void setMogelijkeRingen(List<Ring> ringen) {	this.mogelijkeRingen = ringen; }
	public List<Ring> getMogelijkeRingen(){	return this.mogelijkeRingen; }

	public boolean isVerbonden(Inschrijving o) {
		if (this == o) return false;
		if (verbondenInschrijving == null) return false;
		if (o.verbondenInschrijving == null) return false;
		return verbondenInschrijving.id.equals(o.id);
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
		return "[" + id + "] " + afdeling.getNaam() + (korps > 0 ? " " + korps : "")
				+ " in " + (ring == null ? "" : ring.getVerkorteNotatie());
	}

	public void afterUnmarshal(Unmarshaller u, Object parent) {
		setAfdeling((Afdeling) parent);
	}

}
