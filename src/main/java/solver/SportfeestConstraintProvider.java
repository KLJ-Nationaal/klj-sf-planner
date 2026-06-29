package solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.*;
import domain.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ai.timefold.solver.core.api.score.stream.Joiners.*;

public class SportfeestConstraintProvider implements ConstraintProvider {
	public record SingleJustificationWithReason(Inschrijving inschrijving, String reason) implements ConstraintJustification {
		@Override
		public String toString() {
			return inschrijving + "  [" + reason + "]";
		}
	}

	public record SingleJustification(Inschrijving inschrijving) implements ConstraintJustification {
		@Override
		public String toString() {
			return inschrijving.toString();
		}
	}

	public record SingleCountJustification(Afdeling afdeling, Integer count) implements ConstraintJustification {
		@Override
		public String toString() {
			return afdeling.toString() + " (" + count + "x)";
		}
	}

	public record SingleCountsJustification(Object o, List<Integer> counts) implements ConstraintJustification {
		@Override
		public String toString() {
			return o.toString() + " (" + counts.stream().map(String::valueOf).collect(Collectors.joining(", ")) + ")";
		}
	}

	public record PairJustification(Inschrijving a, Inschrijving b) implements ConstraintJustification {
		@Override
		public String toString() {
			return a + "  +  " + b;
		}
	}

	public record DoubleJustification(Afdeling afdeling, Ring ring) implements ConstraintJustification {
		@Override
		public String toString() {
			return afdeling + " in " + ring;
		}
	}

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
				minimaliseerUniformWissels(factory),
				inschrijvingenDieMoetenSamenvallen(factory),
				vermijdOngunstigeTijdslots(factory),
				restricties(factory),
				touwtrekkenVerdeelTijdslots(factory),
				//touwtrekkenVerdeelRegios(factory),
				//touwtrekkenVerdeelDisciplines(factory),
		};
	}

	// ************************
	// *** HARD CONSTRAINTS ***
	// ************************

	Constraint inschrijvingMoetTijdslotHebben(ConstraintFactory factory) {
		return factory.forEachIncludingUnassigned(Inschrijving.class)
				.filter(inschrijving -> inschrijving.getTijdslot() == null)
				.penalize(HardSoftScore.ofHard(32))

				.justifyWith((a, score) -> new SingleJustification(a))
				.asConstraint("Inschrijving zonder tijdslot");
	}

	Constraint geenDubbeleInschrijvingVanAfdelingZelfdeTijd(ConstraintFactory factory) {
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
				.justifyWith((a, b, score) -> new PairJustification(a, b))
				.asConstraint("Twee inschrijvingen van een AFDELING vallen op het zelfde moment");
	}

	Constraint geenDubbeleRingZelfdeTijd(ConstraintFactory factory) {
		return factory.forEachUniquePair(Inschrijving.class,
						equal(Inschrijving::getRing),
						overlapping(Inschrijving::getStartTijd, Inschrijving::getEindTijd))
				.filter((a, b) -> !a.getDiscipline().getSport().equals(Sport.TOUWTREKKEN)
						&& !b.getDiscipline().getSport().equals(Sport.TOUWTREKKEN))
				.penalize(HardSoftScore.ofHard(8))
				.justifyWith((a, b, score) -> new PairJustification(a, b))
				.asConstraint("Geen twee afdelingen tegelijk in een RING");
	}

	// ************************
	// *** SOFT CONSTRAINTS ***
	// ************************

	Constraint tijdTussenInschrijvingenVerschillendeRing(ConstraintFactory factory) {
		return factory.forEachUniquePair(Inschrijving.class,
						equal(Inschrijving::getAfdeling))
				.filter((a, b) -> {
					boolean aj = a.getDiscipline().isJongens();
					boolean am = a.getDiscipline().isMeisjes();
					boolean bj = b.getDiscipline().isJongens();
					boolean bm = b.getDiscipline().isMeisjes();
					return !a.getDiscipline().equals(b.getDiscipline()) && !b.isVerbonden(a) && ((aj && bj) || (am && bm));
				})
				.filter((a, b) -> !a.getDiscipline().getSport().equals(Sport.TOUWTREKKEN)
						&& !b.getDiscipline().getSport().equals(Sport.TOUWTREKKEN))
				.filter((a, b) -> Math.abs(b.getTijdslot().timeBetween(a.getTijdslot())) < 6)
				.penalize(HardSoftScore.ofSoft(5))
				.justifyWith((a, b, score) -> new PairJustification(a, b))
				.asConstraint("Te weinig tijd tussen inschrijvingen");
	}

	Constraint inschrijvingenDisciplineZelfdeAfdelingMoetenAansluiten(ConstraintFactory factory) {
		return factory.forEach(Inschrijving.class)
				.filter(i -> !i.getDiscipline().getSport().equals(Sport.TOUWTREKKEN))
				.groupBy(
						Inschrijving::getAfdeling,
						Inschrijving::getRing,
						Inschrijving::getDiscipline,
						ConstraintCollectors.toList())
				.filter((afdeling, ring, discipline, inschrijvingen) -> {
					inschrijvingen.sort(Comparator.comparing(Inschrijving::getStartTijd));
					int totaletijd = inschrijvingen.stream().mapToInt(i -> i.getTijdslot().getDuur()).sum();
					return inschrijvingen.getFirst().getStartTijd() < inschrijvingen.getLast().getEindTijd() - totaletijd;
				})
				.penalize(HardSoftScore.ofSoft(30))
				.justifyWith((afdeling, ring, discipline, inschrijvingen, score) -> new DoubleJustification(afdeling, ring))
				.asConstraint("Inschrijvingen afdeling met zelfde discipline moeten aansluiten");
	}

	Constraint minimaliseerUniformWissels(ConstraintFactory factory) {
		return factory.forEach(Inschrijving.class)
				.filter(Inschrijving::isJongens)
				.filter(inschrijving -> !inschrijving.getDiscipline().getSport().equals(Sport.TOUWTREKKEN))
				.join(factory.forEach(Inschrijving.class)
								.filter(Inschrijving::isJongens)
								.filter(inschrijving -> !inschrijving.getDiscipline().getSport().equals(Sport.TOUWTREKKEN)),
						Joiners.equal(Inschrijving::getAfdeling))
				.filter((i1, i2) -> i1.getStartTijd() < i2.getStartTijd())
				.ifNotExists(Inschrijving.class,
						Joiners.equal((i1, i2) -> i1.getAfdeling(), Inschrijving::getAfdeling),
						Joiners.equal((i1, i2) -> i1.isJongens(), Inschrijving::isJongens),
						filtering((i1, i2, i3) ->
								i3 != i1 && i3 != i2 &&
										i3.getStartTijd() > i1.getStartTijd() &&
										i3.getStartTijd() < i2.getStartTijd())
				)
				.filter((i1, i2) -> i1.getDiscipline().isVendeluniform() != i2.getDiscipline().isVendeluniform())
				.groupBy((i1, i2) -> i1.getAfdeling(), ConstraintCollectors.countBi())
				.filter((afdeling, wissels) -> wissels > 1)
				.penalize(HardSoftScore.ofSoft(1), (afdeling, wissels) -> (int) Math.pow(wissels, 1.8))
				.justifyWith((afdeling, count, hardSoftScore) -> new SingleCountJustification(afdeling, count))
				.asConstraint("Minimaliseer uniformwissels");
	}

	Constraint inschrijvingenDieMoetenSamenvallen(ConstraintFactory factory) {
		return factory.forEachUniquePair(Inschrijving.class,
						equal(Inschrijving::getAfdeling))
				.filter((a, b) -> (a.getRing() != b.getRing()) && a.isVerbonden(b) && a.getStartTijd() != b.getStartTijd())
				.penalize(HardSoftScore.ofSoft(70))
				.justifyWith((a, b, hardSoftScore) -> new PairJustification(a, b))
				.asConstraint("Inschrijvingen die op hetzelfde moment moeten beginnen");
	}

	Constraint vermijdOngunstigeTijdslots(ConstraintFactory factory) {
		return factory.forEach(Inschrijving.class)
				.filter(i -> i.getTijdslot().isOngunstig())
				.penalize(HardSoftScore.ofSoft(1))
				.justifyWith((inschrijving, hardSoftScore) -> new SingleJustification(inschrijving))
				.asConstraint("Ongunstig tijdslot");
	}

	Constraint restricties(ConstraintFactory factory) {
		return factory.forEachUniquePair(Inschrijving.class,
						equal(Inschrijving::getAfdeling),
						overlapping(Inschrijving::getStartTijd, Inschrijving::getEindTijd))
				.filter((x, y) -> x.getVerbondenRestricties().contains(y))
				.penalize(HardSoftScore.ofSoft(5))
				.justifyWith((a, b, hardSoftScore) -> new PairJustification(a, b))
				.asConstraint("Uitzondering");
	}

	private Constraint touwtrekkenVerdeelTijdslots(ConstraintFactory factory) {
		var zeroSlots = factory
				.forEach(Tijdslot.class)
				.ifNotExists(Inschrijving.class,
						Joiners.equal(Tijdslot::getRing, Inschrijving::getRing),
						Joiners.equal(t -> t, Inschrijving::getTijdslot),
						Joiners.filtering((t, i) -> i.getDiscipline().getSport().equals(Sport.TOUWTREKKEN)))
				.map(Tijdslot::getRing, t -> t, t -> 0);

		var actualSlots = factory
				.forEach(Inschrijving.class)
				.filter(i -> i.getDiscipline().getSport().equals(Sport.TOUWTREKKEN))
				.groupBy(Inschrijving::getRing, Inschrijving::getTijdslot, ConstraintCollectors.count());

		return zeroSlots
				.concat(actualSlots)
				.groupBy(
						(ring, tijdslot, count) -> ring,
						// Bewaar tijdslot + count als paar zodat we kunnen sorteren
						ConstraintCollectors.toList((ring, tijdslot, count) -> Map.entry(tijdslot, count)))
				// Sorteer op tijdslot en extract daarna alleen de counts
				.map((ring, entries) -> ring,
						(ring, entries) -> entries.stream()
								.sorted(Comparator.comparing(Map.Entry::getKey))
								.map(Map.Entry::getValue)
								.toList())
				.filter((ring, counts) -> {
					int max = counts.stream().mapToInt(Integer::intValue).max().orElse(0);
					int min = counts.stream().mapToInt(Integer::intValue).min().orElse(0);
					return max - min > 1;
				})
				.penalize(HardSoftScore.ofSoft(8),
						(ring, counts) -> {
							int max = counts.stream().mapToInt(Integer::intValue).max().orElse(0);
							int min = counts.stream().mapToInt(Integer::intValue).min().orElse(0);
							return (max - min) * (max - min);
						})
				.justifyWith((ring, counts, score) -> new SingleCountsJustification(ring, counts))
				.asConstraint("Touwtrekken verdelen over de tijdslots");
	}

	private Constraint touwtrekkenVerdeelRegios(ConstraintFactory factory) {
		return factory.forEach(Inschrijving.class)
				.filter(inschrijving -> inschrijving.getDiscipline().getSport().equals(Sport.TOUWTREKKEN))
				.groupBy(
						i -> List.of(i.getRegio(), i.getRing(), i.getTijdslot()),
						ConstraintCollectors.count())
				.groupBy(
						(key, count) -> List.of(key.get(0), key.get(1)), // Regio + Ring
						ConstraintCollectors.toList((key, count) -> count))
				.filter((key, counts) -> {
					int max = counts.stream().mapToInt(Integer::intValue).max().orElse(0);
					int min = counts.stream().mapToInt(Integer::intValue).min().orElse(0);
					return max - min > 0;
				})
				.penalize(HardSoftScore.ONE_SOFT,
						(key, counts) -> {
							int max = counts.stream().mapToInt(Integer::intValue).max().orElse(0);
							int min = counts.stream().mapToInt(Integer::intValue).min().orElse(0);
							return (max - min) * (max - min);
						})
				//TODO: functie in justifyWith is niet de juiste (geen list)
				.justifyWith((objects, integers, hardSoftScore) -> new SingleCountsJustification(objects, integers))
				.asConstraint("Touwtrekken verdelen over de regio's");
	}

	private Constraint touwtrekkenVerdeelDisciplines(ConstraintFactory factory) {
		return factory.forEach(Inschrijving.class)
				.filter(inschrijving -> inschrijving.getDiscipline().getSport().equals(Sport.TOUWTREKKEN))
				.groupBy(
						i -> List.of(i.getDiscipline(), i.getRing(), i.getTijdslot()),
						ConstraintCollectors.count())
				.groupBy(
						(key, count) -> List.of(key.get(0), key.get(1)), // Discipline + Ring
						ConstraintCollectors.toList((key, count) -> count))
				.filter((key, counts) -> {
					int max = counts.stream().mapToInt(Integer::intValue).max().orElse(0);
					int min = counts.stream().mapToInt(Integer::intValue).min().orElse(0);
					return max - min > 0;
				})
				.penalize(HardSoftScore.ONE_SOFT,
						(key, counts) -> {
							int max = counts.stream().mapToInt(Integer::intValue).max().orElse(0);
							int min = counts.stream().mapToInt(Integer::intValue).min().orElse(0);
							return (max - min) * (max - min);
						})
				//TODO: functie in justifyWith is niet de juiste (geen list)
				.justifyWith((objects, integers, hardSoftScore) -> new SingleCountsJustification(objects, integers))
				.asConstraint("Tijdsblokken touwtrekken verdelen over de disciplines (leeftijd en meisjes-jongens)");
	}
}
