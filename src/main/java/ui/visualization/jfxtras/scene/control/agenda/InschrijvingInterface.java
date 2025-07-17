package ui.visualization.jfxtras.scene.control.agenda;

import domain.Afdeling;
import domain.Discipline;
import domain.Ring;
import domain.Tijdslot;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlIDREF;
import jakarta.xml.bind.annotation.XmlTransient;

import java.util.Comparator;
import java.util.List;

public interface InschrijvingInterface {
	@XmlTransient
	Integer getId();
	void setId(Integer id);

	@XmlAttribute(name = "id")
	@XmlID
	String getRef();
	void setRef(String s);

	@XmlIDREF
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

	// either sets the timeslot to null if it's a whole day, otherwise assign the first valid timeslot
	default void setWholeDay(Boolean b) {
		if (b) setTijdslot(null);
			// if it's not a whole day any longer, assign the first timeslot
		else setTijdslot((getTijdslot() == null ? getTijdslots().getFirst() : getTijdslot()));
	}

	default Boolean isDraggable() { return Boolean.TRUE; }

	/**
	 * This method is not used by the control, it can only be called when implemented by the user through the default Datetime methods on this interface
	 **/
	default int getStartTime() { return getStartTijd(); }
	default void setStartTime(int startTime) { setTijdslot(getClosestTijdslot(startTime)); }

	/**
	 * This method is not used by the control, it can only be called when implemented by the user through the default Datetime methods on this interface
	 **/
	default int getEndTime() { return getEindTijd(); }

	default Tijdslot getClosestTijdslot(int wantedStartTime) {
		if (getRing() == null) return getTijdslot();
		return getRing().getTijdslots().stream()
				.min(Comparator.comparingInt(ts -> Math.abs(ts.getStartTijd() - wantedStartTime)))
				.orElse(getTijdslot());
	}
}
