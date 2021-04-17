package ui.visualization.jfxtras.scene.control.agenda;

import difficulty.TijdslotStrengthComparator;
import domain.Afdeling;
import domain.Discipline;
import domain.Ring;
import domain.Tijdslot;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public interface InschrijvingInterface {
	@XmlTransient
	@PlanningId
	Integer getId();
	void setId(Integer id);

	@XmlAttribute(name = "id")
	@XmlID
	String getRef();
	void setRef(String s);

	@XmlIDREF
	@PlanningVariable(valueRangeProviderRefs = {"Tijdslot"},
			strengthComparatorClass = TijdslotStrengthComparator.class,
			nullable=true)
	Tijdslot getTijdslot();
	void setTijdslot(Tijdslot tijdslot);

	@XmlTransient
	Afdeling getAfdeling();
	void setAfdeling(Afdeling afdeling);

	@XmlIDREF
	Ring getRing();
	void setRing(Ring ring);

	@XmlIDREF
	Discipline getDiscipline();
	void setDiscipline(Discipline discipline);

	int getKorps();
	void setKorps(int korps);

	@ValueRangeProvider(id = "Tijdslot")
	List<Tijdslot> getTijdslots();

	int getStartTijd();

	int getEindTijd();

	@XmlIDREF
	void setMogelijkeRingen(List<Ring> ringen);
	List<Ring> getMogelijkeRingen();

	@Override
	boolean equals(Object o);

	@Override
	int hashCode();

	@Override
	String toString();


	default Boolean isWholeDay() { return getTijdslot() == null; }
	default void setWholeDay(Boolean b) {
		if(b) setTijdslot(null);
		else setTijdslot(getTijdslots().get(0));
	}

	default Boolean isDraggable() { return Boolean.TRUE; }

	/** This method is not used by the control, it can only be called when implemented by the user through the default Datetime methods on this interface **/
	default int getStartTime() { return getStartTijd();	}
	default void setStartTime(int startTime) {
		setTijdslot(getTijdslots().stream()
			.min(Comparator.comparingInt(i -> Math.abs(i.getStartTijd() - startTime)))
			.orElseGet((Supplier<? extends Tijdslot>) getTijdslot()));
	}
	/** This method is not used by the control, it can only be called when implemented by the user through the default Datetime methods on this interface **/
	default int getEndTime() { return getEindTijd(); }
}
