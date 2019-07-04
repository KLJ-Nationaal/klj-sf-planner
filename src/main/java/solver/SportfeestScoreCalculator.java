package solver;

import domain.Afdeling;
import domain.Inschrijving;
import domain.Ring;
import domain.Sportfeest;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SportfeestScoreCalculator implements EasyScoreCalculator<Sportfeest> {

    public Score calculateScore(Sportfeest sportfeest) {
        int hardScore = 0;
        int softScore = 0;


        for(Afdeling afdeling : sportfeest.getAfdelingen()) {

            for (int i = 0; i < afdeling.getInschrijvingen().size(); i++) {
                Inschrijving inschrijving = afdeling.getInschrijvingen().get(i);

                // REGEL: elke inschrijving moet een tijdslot toegewezen hebben
                if (inschrijving.getTijdslot() == null) {
                    hardScore = hardScore - 8;
                    continue;
                }

                // REGEL: geen twee inschrijvingen voor een afdeling op het zelfde moment
                if(inschrijving.getTijdslot() != null) {
                    boolean overlaps = afdeling.getInschrijvingen().stream()
                            .filter(inschr -> inschrijving.getTijdslot().isOverlap(inschr.getTijdslot()))
                            .anyMatch(inschr -> (
                                    inschrijving.getDiscipline().isJongens() == inschr.getDiscipline().isJongens() ||
                                    inschrijving.getDiscipline().isMeisjes() == inschr.getDiscipline().isMeisjes() ));
                    if (overlaps) {
                        hardScore = hardScore - 6;
                    }
                }
            }
        }

        for (Ring ring : sportfeest.getRingen()) {
            List<Inschrijving> sortedInschrijvingen = sportfeest.getAfdelingen().stream()
                    .map(Afdeling::getInschrijvingen)
                    .flatMap(Collection::stream)
                    .filter(inschr -> ring.equals(inschr.getRing()))
                    .filter(inschr -> inschr.getTijdslot() != null)
                    .sorted(Comparator.comparing(Inschrijving::getStartTijd))
                    .collect(Collectors.toList());
            Inschrijving vorigeInschrijving = null;
            for (int i = 0; i < sortedInschrijvingen.size(); i++) {
                Inschrijving inschrijving = sortedInschrijvingen.get(i);

                // REGEL: geen twee inschrijvingen voor een ring op het zelfde moment
                if(vorigeInschrijving != null) {
                    boolean isOverlap = inschrijving.getTijdslot().isOverlap(vorigeInschrijving.getTijdslot());
                    if (isOverlap ){
                        hardScore = hardScore - 4;
                    }
                }

                vorigeInschrijving = inschrijving;
            }
        }

        return HardSoftScore.of(hardScore, softScore);
    }
}
