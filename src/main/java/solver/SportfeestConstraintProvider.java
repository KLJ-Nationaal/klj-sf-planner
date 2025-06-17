package solver;

import domain.Inschrijving;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.*;

import java.util.Comparator;

import static org.optaplanner.core.api.score.stream.Joiners.equal;
import static org.optaplanner.core.api.score.stream.Joiners.overlapping;

public class SportfeestConstraintProvider implements ConstraintProvider {

	@Override
	public Constraint[] defineConstraints(ConstraintFactory factory) {
		return new Constraint[]{
				// HARD CONSTRAINTS
				inschrijvingMoetTijdslotHebben(factory),
				geenDubbeleInschrijvingVanAfdelingZelfdeTijd(factory),
				geenDubbeleRingZelfdeTijd(factory),
				// SOFT CONSTRAINTS
				tijdTussenInschrijvingenVerschillendeRing(factory),
				inschrijvingenDisciplineZelfdeAfdelingMoetenAansluiten(factory),
				//TODO:
				minimaliseerUniformWissels(factory),
				inschrijvingenDieMoetenSamenvallen(factory),
				vermijdOngunstigeTijdslots(factory)
		};
	}

	// HARD CONSTRAINTS

	private Constraint inschrijvingMoetTijdslotHebben(ConstraintFactory factory) {
		return factory.forEachIncludingNullVars(Inschrijving.class)
				.filter(inschrijving -> inschrijving.getTijdslot() == null)
				.penalize(HardSoftScore.ofHard(16))
				.asConstraint("Inschrijving zonder tijdslot");
	}

	private Constraint geenDubbeleInschrijvingVanAfdelingZelfdeTijd(ConstraintFactory factory) {
		return factory.forEachUniquePair(Inschrijving.class,
						equal(Inschrijving::getAfdeling),
						overlapping(Inschrijving::getStartTijd, Inschrijving::getEindTijd))
				.filter((a, b) -> {
					boolean aj = a.getDiscipline().isJongens();
					boolean am = a.getDiscipline().isMeisjes();
					boolean bj = b.getDiscipline().isJongens();
					boolean bm = b.getDiscipline().isMeisjes();
					return !b.isVerbonden(a) && ((aj && bj) || (am && bm));
				})
				.penalize(HardSoftScore.ofHard(10))
				.asConstraint("Twee inschrijvingen van een AFDELING vallen op het zelfde moment");
	}

	private Constraint geenDubbeleRingZelfdeTijd(ConstraintFactory factory) {
		return factory.forEachUniquePair(Inschrijving.class,
						equal(Inschrijving::getRing),
						overlapping(Inschrijving::getStartTijd, Inschrijving::getEindTijd))
				.penalize(HardSoftScore.ofHard(8))
				.asConstraint("Geen twee afdelingen tegelijk in een RING");
	}

	// SOFT CONSTRAINTS

	private Constraint tijdTussenInschrijvingenVerschillendeRing(ConstraintFactory factory) {
		return factory.forEachUniquePair(Inschrijving.class,
						equal(Inschrijving::getAfdeling))
				.filter((a, b) -> {
					boolean aj = a.getDiscipline().isJongens();
					boolean am = a.getDiscipline().isMeisjes();
					boolean bj = b.getDiscipline().isJongens();
					boolean bm = b.getDiscipline().isMeisjes();
					return !a.getDiscipline().equals(b.getDiscipline()) && !b.isVerbonden(a) && ((aj && bj) || (am && bm));
				})
				.filter((a, b) -> Math.abs(b.getTijdslot().timeBetween(a.getTijdslot())) < 6)
				.penalize(HardSoftScore.ofSoft(5))
				.asConstraint("Te weinig tijd tussen inschrijvingen");
	}

	private Constraint inschrijvingenDisciplineZelfdeAfdelingMoetenAansluiten(ConstraintFactory factory) {
		return factory.forEach(Inschrijving.class)
				.groupBy(
						Inschrijving::getAfdeling,
						Inschrijving::getRing,
						Inschrijving::getDiscipline,
						ConstraintCollectors.toList())
				.filter((afdeling, ring, discipline, inschrijvingen) -> {
					inschrijvingen.sort(Comparator.comparing(Inschrijving::getStartTijd));
					int totaletijd = inschrijvingen.stream().mapToInt(i -> i.getTijdslot().getDuur()).sum();
					return inschrijvingen.get(0).getStartTijd() < inschrijvingen.get(inschrijvingen.size() - 1).getEindTijd() - totaletijd;
				})
				.penalize(HardSoftScore.ofSoft(30))
				.asConstraint("Inschrijvingen afdeling met zelfde discipline moeten aansluiten");
	}

	private Constraint minimaliseerUniformWissels(ConstraintFactory factory) {
		return factory.forEach(Inschrijving.class)
				.filter(Inschrijving::isJongens)
				.join(factory.forEach(Inschrijving.class)
								.filter(Inschrijving::isJongens),
						Joiners.equal(Inschrijving::getAfdeling))
				.filter((i1, i2) -> i1.getStartTijd() < i2.getStartTijd())
				.ifNotExists(Inschrijving.class,
						Joiners.equal((i1, i2) -> i1.getAfdeling(), Inschrijving::getAfdeling),
						Joiners.equal((i1, i2) -> i1.isJongens(), Inschrijving::isJongens),
						Joiners.filtering((i1, i2, i3) ->
								i3 != i1 && i3 != i2 &&
										i3.getStartTijd() > i1.getStartTijd() &&
										i3.getStartTijd() < i2.getStartTijd())
				)
				.filter((i1, i2) -> i1.getDiscipline().isVendeluniform() != i2.getDiscipline().isVendeluniform())
				.groupBy((i1, i2) -> i1.getAfdeling(), ConstraintCollectors.countBi())
				.filter((afdeling, wissels) -> wissels > 1)
				.penalize(HardSoftScore.ofSoft(1), (afdeling, wissels) -> (int) Math.pow(wissels, 1.8))
				.asConstraint("Minimaliseer uniformwissels");
	}

	private Constraint inschrijvingenDieMoetenSamenvallen(ConstraintFactory factory) {
		return factory.forEachUniquePair(Inschrijving.class,
						equal(Inschrijving::getAfdeling))
				.filter((a, b) -> (a.getRing() != b.getRing()) && a.isVerbonden(b) && a.getStartTijd() != b.getStartTijd())
				.penalize(HardSoftScore.ofSoft(70))
				.asConstraint("Inschrijvingen die op hetzelfde moment moeten beginnen");
	}

	private Constraint vermijdOngunstigeTijdslots(ConstraintFactory factory) {
		return factory.forEach(Inschrijving.class)
				.filter(i -> i.getTijdslot().isOngunstig())
				.penalize(HardSoftScore.ofSoft(1))
				.asConstraint("Ongunstig tijdslot");
	}
}
