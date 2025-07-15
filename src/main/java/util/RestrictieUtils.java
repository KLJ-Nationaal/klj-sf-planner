package util;

import domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class RestrictieUtils {
	private static final Logger logger = LoggerFactory.getLogger(RestrictieUtils.class);

	public static void addRestricties(Sportfeest sf, List<Restrictie> restricties) {
		// Restricties in map opslaan, ter referentie
		sf.getRestricties().addAll(restricties);
		// Restricties invullen in inschrijvingen
		restricties.forEach(ro -> {
			// Afdeling opzoeken
			Optional<Afdeling> afdeling = sf.getAfdelingen().stream()
					.filter(afd -> afd.getNaam().equalsIgnoreCase(ro.getAfdeling()))
					.findAny();
			if (afdeling.isEmpty()) {
				logger.warn("Afdeling {} niet gevonden bij het toevoegen van restricties", ro.getAfdeling());
				return;
			}

			List<Inschrijving> listA = getInschrijvingenForRestrictieObject(afdeling.get(), ro.getA());
			List<Inschrijving> listB = getInschrijvingenForRestrictieObject(afdeling.get(), ro.getB());

			if (listA.isEmpty()) logger.warn("Geen inschrijvingen gevonden voor A van uitzondering {}", ro);
			if (listB.isEmpty()) logger.warn("Geen inschrijvingen gevonden voor B van uitzondering {}", ro);

			listA.forEach(inschrijving -> inschrijving.getVerbondenRestricties().addAll(listB));
			listB.forEach(inschrijving -> inschrijving.getVerbondenRestricties().addAll(listA));

			logger.info("Uitzondering {} toegevoegd, {} x A en {} x B", ro, listA.size(), listB.size());
		});
	}

	private static List<Inschrijving> getInschrijvingenForRestrictieObject(Afdeling afdeling, RestrictieOptie ro) {
		// Alle inschrijvingen van deze afdeling
		Stream<Inschrijving> setA = afdeling.getInschrijvingen().stream();
		// Filter de sport of discipline er uit
		if (ro.getObject() instanceof Sport s) {
			setA = setA.filter(inschrijving -> inschrijving.getDiscipline().getSport().equals(s));
		} else {
			setA = setA.filter(inschrijving -> inschrijving.getDiscipline().getNaam().equals(ro.getObject().getNaam()));
		}
		// Indien NIET alle korpsen, dus 1 korps is voldoende
		// Inschrijvingen met maar één korps hebben korps = 0, met meerdere korpsen begint het vanaf korps = 1
		if (!ro.getAlleKorpsen())
			setA = setA.filter(inschrijving -> inschrijving.getKorps() < 2);
		return setA.toList();
	}

	public static boolean compareRestricties(Sportfeest sf, List<Restrictie> restricties) {
		boolean identical = true;
		// controleer dat alle restricties in onze map overeenkomen met de algemene
		for (Restrictie restrictie : sf.getRestricties()) {
			// probeer restrictie te verwijderen uit de lijst
			if (restricties.remove(restrictie)) {
				logger.debug("Restrictie {} gevonden als algemene restrictie", restrictie);
			} else {
				logger.warn("Restrictie {} niet gevonden als algemene restrictie", restrictie);
				identical = false;
			}
		}
		// overschot zijn algemene restricties die niet in onze map zitten
		for (Restrictie restrictie : restricties) {
			logger.warn("Algemene restrictie {} niet gevonden in sportfeestmap", restrictie);
			identical = false;
		}
		return identical;
	}

	public static void replaceRestricties(Sportfeest sf, List<Restrictie> restricties) {
		if (sf == null) return;
		// eerst alles leegmaken
		sf.getRestricties().clear();
		sf.getInschrijvingen().forEach(i -> i.getVerbondenRestricties().clear());
		// nieuwe toevoegen
		RestrictieUtils.addRestricties(sf, restricties);
	}
}
