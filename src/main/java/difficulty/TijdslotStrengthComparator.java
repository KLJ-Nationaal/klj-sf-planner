package difficulty;

import domain.Tijdslot;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.Comparator;

public class TijdslotStrengthComparator implements Comparator<Tijdslot> {
	public int compare(Tijdslot o1, Tijdslot o2) {
		if (o1 == null || o2 == null) return 0;
		return new CompareToBuilder()
				.append(o1.getStartTijd(), o2.getStartTijd())
				.append(o1.getDuur(), o2.getDuur())
				.toComparison();
	}
}
