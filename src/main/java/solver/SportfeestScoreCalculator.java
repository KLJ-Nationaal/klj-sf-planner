package solver;

import domain.*;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;

public class SportfeestScoreCalculator implements EasyScoreCalculator<Sportfeest> {

    public Score calculateScore(Sportfeest sportfeest) {
        int hardScore = 0;
        int softScore = 0;

        /*for(Afdeling afdeling : sportfeest.getAfdelingen()){
            for(Inschrijving inschrijving : afdeling.getInschrijvingen()){
                if(inschrijving.getTijdslot() == null) hardScore = hardScore - 5;
            }
        }*/
        for(Inschrijving inschrijving : sportfeest.getInschrijvingen()){
            if(inschrijving.getTijdslot() == null) hardScore = hardScore - 5;
        }

        return HardSoftScore.of(hardScore, softScore);
    }
}
