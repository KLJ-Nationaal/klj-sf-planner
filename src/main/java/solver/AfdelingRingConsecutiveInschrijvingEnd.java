package solver;

import domain.Afdeling;
import domain.Discipline;
import domain.Ring;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

public class AfdelingRingConsecutiveInschrijvingEnd implements Comparable<AfdelingRingConsecutiveInschrijvingEnd>,
		Serializable {

	private Ring ring;
	private Afdeling afdeling;
	private Discipline discipline;
	private int eindTijd;

	public AfdelingRingConsecutiveInschrijvingEnd(Ring ring, Afdeling afdeling, Discipline discipline, int eindTijd) {
		this.ring = ring;
		this.afdeling = afdeling;
		this.discipline = discipline;
		this.eindTijd = eindTijd;
	}

	public Ring getRing() {	return ring; }
	public void setRing(Ring ring) { this.ring = ring; }

	public Afdeling getAfdeling() {	return afdeling; }
	public void setAfdeling(Afdeling afdeling) { this.afdeling = afdeling; }

	public Discipline getDiscipline() { return discipline; }
	public void setDiscipline(Discipline discipline) { this.discipline = discipline; }

	public int getEindTijd() { return eindTijd; }
	public void setEindTijd(int eindTijd) {	this.eindTijd = eindTijd; }

	@Override
	public int compareTo(AfdelingRingConsecutiveInschrijvingEnd o) {
		return new CompareToBuilder()
				.append(ring, o.ring)
				.append(afdeling, o.afdeling)
				.append(discipline, o.discipline)
				.append(eindTijd, o.eindTijd)
				.toComparison();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof AfdelingRingConsecutiveInschrijvingEnd) {
			AfdelingRingConsecutiveInschrijvingEnd other = (AfdelingRingConsecutiveInschrijvingEnd) o;
			return new EqualsBuilder()
					.append(ring, other.ring)
					.append(afdeling, other.afdeling)
					.append(discipline, other.discipline)
					.append(eindTijd, other.eindTijd)
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
				.append(eindTijd)
				.toHashCode();
	}

	@Override
	public String toString() {
		return ring.toString()  + " (" + discipline.toString() + ") " + afdeling.toString() + " tijd " + eindTijd + " - ...";
	}

}
