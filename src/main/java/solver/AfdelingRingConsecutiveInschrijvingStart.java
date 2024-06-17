package solver;

import domain.Afdeling;
import domain.Discipline;
import domain.Ring;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

public class AfdelingRingConsecutiveInschrijvingStart implements Comparable<AfdelingRingConsecutiveInschrijvingStart>,
		Serializable {

	private Ring ring;
	private Afdeling afdeling;
	private Discipline discipline;
	private int startTijd;

	public AfdelingRingConsecutiveInschrijvingStart(Ring ring, Afdeling afdeling, Discipline discipline, int startTijd) {
		this.ring = ring;
		this.afdeling = afdeling;
		this.discipline = discipline;
		this.startTijd = startTijd;
	}
	public Ring getRing() {	return ring; }
	public void setRing(Ring ring) { this.ring = ring; }

	public Afdeling getAfdeling() {	return afdeling; }
	public void setAfdeling(Afdeling afdeling) { this.afdeling = afdeling; }

	public Discipline getDiscipline() { return discipline; }
	public void setDiscipline(Discipline discipline) { this.discipline = discipline; }

	public int getStartTijd() {	return startTijd; }
	public void setStartTijd(int startTijd) { this.startTijd = startTijd; }

	@Override
	public int compareTo(AfdelingRingConsecutiveInschrijvingStart o) {
		return new CompareToBuilder()
				.append(ring, o.ring)
				.append(afdeling, o.afdeling)
				.append(discipline, o.discipline)
				.append(startTijd, o.startTijd)
				.toComparison();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof AfdelingRingConsecutiveInschrijvingStart) {
			AfdelingRingConsecutiveInschrijvingStart other = (AfdelingRingConsecutiveInschrijvingStart) o;
			return new EqualsBuilder()
					.append(ring, other.ring)
					.append(afdeling, other.afdeling)
					.append(discipline, other.discipline)
					.append(startTijd, other.startTijd)
					.isEquals();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(ring)
				.append(afdeling)
				.append(discipline)
				.append(startTijd)
				.toHashCode();
	}

	@Override
	public String toString() {
		return ring.toString() + " (" + discipline.toString() + ") " + afdeling.toString() + " tijd " + startTijd + " - ...";
	}

}
