package domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import persistence.Instellingen;
import persistence.IntegerAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

@XmlAccessorType(XmlAccessType.FIELD)
public class Tijdslot implements Comparable<Tijdslot> {
	private final int startTijd;
	private final int eindTijd;
	private final int duur;
	private boolean ongunstig;
	@XmlTransient
	private Ring ring;
	@XmlAttribute
	@XmlID
	@XmlJavaTypeAdapter(type = int.class, value = IntegerAdapter.class)
	private final int id;

	public Tijdslot(int startTijd, int duur, Ring ring) {
		this(startTijd, duur, ring, false);
	}

	public Tijdslot(int startTijd, int duur, Ring ring, boolean ongunstig) {
		this.startTijd = startTijd;
		this.duur = duur;
		this.ring = ring;
		this.eindTijd = startTijd + duur;
		this.id = ring.getRingIndex() * 10000 + startTijd;
		this.ongunstig = ongunstig;
	}

	// deze constructie is nog voor XML unmarshalling
	public Tijdslot() {
		this(0, 1, new Ring());
	}

	public Ring getRing() { return ring; }
	public int getStartTijd() { return startTijd; }
	public int getDuur() { return duur; }
	public int getEindTijd() { return eindTijd; }
	public void setOngunstig(boolean ongunstig) { this.ongunstig = ongunstig; }
	public boolean isOngunstig() { return ongunstig; }

	public String getStartTijdFormatted() {
		try {
			SimpleDateFormat df = new SimpleDateFormat("HH:mm");
			Date d = df.parse(Instellingen.Opties().STARTTIJD);
			Calendar cal = Calendar.getInstance();
			cal.setTime(d);
			cal.add(Calendar.MINUTE, startTijd);
			return df.format(cal.getTime());
		} catch (Exception e) {
			return String.valueOf(startTijd);
		}
	}

	@PlanningId
	public int getId() { return id; }

	@Override
	public String toString() { return ring.toString() + " om " + getStartTijdFormatted() + " [" + hashCode() + "]"; }

	@Override
	public int hashCode() { return Objects.hash(ring, startTijd); }

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof Tijdslot other) {
			return Objects.equals(getRing(), other.getRing()) &&
					getStartTijd() == other.getStartTijd();
		} else {
			return false;
		}
	}

	public boolean isOverlap(Tijdslot a) {
		if (a == null) return false;
		return a.startTijd < eindTijd && a.eindTijd > startTijd;
	}

	public boolean isIncluded(int a) {
		return a >= startTijd && a < eindTijd;
	}

	public int timeBetween(Tijdslot a) {
		if (a == null) return Integer.MAX_VALUE;
		if (startTijd < a.startTijd) return a.startTijd - eindTijd;
		else return startTijd - a.eindTijd;
	}

	@Override
	public int compareTo(Tijdslot o) {
		return Objects.compare(startTijd, o.startTijd, Integer::compareTo);
	}

	public void afterUnmarshal(Unmarshaller u, Object parent) { ring = (Ring) parent; }
}
