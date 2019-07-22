package app;

import ch.qos.logback.classic.Logger;
import domain.Sportfeest;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.slf4j.LoggerFactory;
import persistence.Marshalling;

import java.util.function.Consumer;
import java.util.stream.Collectors;


public class SportfeestPlannerApp {
	final static Logger logger = (Logger) LoggerFactory.getLogger(SportfeestPlannerApp.class);

	public static void main(String[] args) {
		SolverFactory<Sportfeest> solverFactory = SolverFactory.createFromXmlResource("solverConfig.xml");
		Solver<Sportfeest> solver = solverFactory.buildSolver();

		Sportfeest unsolvedSportfeest = Marshalling.unMarshall("data/ringverdeling.xlsx");

		Sportfeest solvedSportfeest = solver.solve(unsolvedSportfeest);

		logger.info("Einde van de berekening");
		logger.info("Score: " + solvedSportfeest.getScore().toString());
		if(!solvedSportfeest.getScore().isFeasible()) logger.warn("DEZE OPLOSSING IS NIET HAALBAAR!");

		ScoreDirector<Sportfeest> scoreDirector = solver.getScoreDirectorFactory().buildScoreDirector();
		scoreDirector.setWorkingSolution(solvedSportfeest);
		for(ConstraintMatchTotal cmt : scoreDirector.getConstraintMatchTotals()){
			Consumer<String> c = s -> logger.info(s);
			if( ((HardSoftScore)cmt.getScore()).getHardScore() != 0) c = s -> logger.warn(s);

			c.accept("  Voorwaarde: " + cmt.getConstraintName());
			c.accept("  Gewicht: " + cmt.getScore() + ", Aantal keer: " + cmt.getConstraintMatchCount());
			for(ConstraintMatch cm : cmt.getConstraintMatchSet()){
				c.accept("    " + cm.getJustificationList().stream()
						.map( n -> n.toString() )
						.collect( Collectors.joining( ", " ) ));
			}
		}

		Marshalling.marshall(solvedSportfeest);

	}
}
