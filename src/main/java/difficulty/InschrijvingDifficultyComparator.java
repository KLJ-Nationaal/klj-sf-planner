package difficulty;

import domain.Inschrijving;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.Comparator;

public class InschrijvingDifficultyComparator implements Comparator<Inschrijving> {
	@Override
	public int compare(Inschrijving o1, Inschrijving o2) {
		return new CompareToBuilder()
				.append(o1.getAfdeling().getInschrijvingen().size(), o2.getAfdeling().getInschrijvingen().size())
				.toComparison();
	}
}
