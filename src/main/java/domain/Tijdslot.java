package domain;

import org.optaplanner.core.api.domain.lookup.PlanningId;

import javax.xml.bind.annotation.XmlRootElement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

@XmlRootElement(name = "Tijdslot")
public class Tijdslot implements Comparable<Tijdslot> {
	private int startTijd;
	private int eindTijd;
	private int duur;
	private Ring ring;
	private int id;

	public Tijdslot(int startTijd, int duur, Ring ring) {
		this.startTijd = startTijd;
		this.duur = duur;
		this.ring = ring;
		this.eindTijd = startTijd + duur;
		this.id = ring.getRingIndex() * 10000 + startTijd;
	}

	public Ring getRing() {
		return ring;
	}
	public int getStartTijd() {
		return startTijd;
	}
	public int getDuur() {
		return duur;
	}
	public int getEindTijd() {
		return eindTijd;
	}

	public String getStartTijdFormatted() {
		try {
			SimpleDateFormat df = new SimpleDateFormat("HH:mm");
			Date d = df.parse("08:00");
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
	public String toString() { return ring.toString() + " " + getStartTijdFormatted() + " " + hashCode(); }

	@Override
	public int hashCode() { return Objects.hash(ring, startTijd); }

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof Tijdslot) {
			Tijdslot other = (Tijdslot) o;
			return Objects.equals(getRing(), other.getRing()) &&
					getStartTijd() == other.getStartTijd();
		} else {
			return false;
		}
	}

	public boolean isOverlap(Tijdslot a) {
		if(a == null) return false;
		if(a.startTijd < eindTijd && a.eindTijd > startTijd) return true;
		else return false;
	}

	public boolean isIncluded(int a) {
		if(a >= startTijd && a < eindTijd) return true;
		else return false;
	}

	public int timeBetween(Tijdslot a) {
		if(a == null) return Integer.MAX_VALUE;
		if(startTijd < a.startTijd) return a.startTijd - eindTijd;
		else return startTijd - a.eindTijd;
	}

	@Override
	public int compareTo(Tijdslot o) {
		return Objects.compare(startTijd, o.startTijd, Integer::compareTo);
	}
}
