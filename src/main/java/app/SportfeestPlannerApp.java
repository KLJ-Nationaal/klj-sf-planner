package app;

import ai.timefold.solver.core.api.score.ScoreExplanation;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.constraint.ConstraintMatch;
import ai.timefold.solver.core.api.score.constraint.ConstraintMatchTotal;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ch.qos.logback.classic.Logger;
import domain.Sportfeest;
import org.slf4j.LoggerFactory;
import persistence.Marshalling;

import java.util.function.Consumer;
import java.util.stream.Collectors;


public class SportfeestPlannerApp {
	private final static Logger logger = (Logger) LoggerFactory.getLogger(SportfeestPlannerApp.class);

	public static void main(String[] args) {
		SolverFactory<Sportfeest> solverFactory = SolverFactory.createFromXmlResource("solverConfig.xml");
		Solver<Sportfeest> solver = solverFactory.buildSolver();

		Sportfeest unsolvedSportfeest = Marshalling.unmarshallXml("data/adegem-140724-pre.xml");

		Sportfeest solvedSportfeest = solver.solve(unsolvedSportfeest);

		logger.info("Einde van de berekening");
		logger.info("Score: {}", solvedSportfeest.getScore().toString());
		if (!solvedSportfeest.getScore().isFeasible()) logger.warn("DEZE OPLOSSING IS NIET HAALBAAR!");

		try {
			SolutionManager<Sportfeest, HardSoftScore> solutionManager = SolutionManager.create(solverFactory);
			ScoreExplanation<Sportfeest, HardSoftScore> explanation = solutionManager.explain(solvedSportfeest);
			for (ConstraintMatchTotal<?> cmt : explanation.getConstraintMatchTotalMap().values()) {
				Consumer<String> c = logger::info;
				if (((HardSoftScore) cmt.getScore()).hardScore() != 0) {
					c = logger::warn;
				}

				c.accept("  Voorwaarde: " + cmt.getConstraintRef().constraintName());
				c.accept("  Gewicht: " + cmt.getScore() + ", Aantal keer: " + cmt.getConstraintMatchCount());

				for (ConstraintMatch<?> cm : cmt.getConstraintMatchSet()) {
					c.accept("    " + cm.getIndictedObjectList().stream()
							.map(Object::toString)
							.collect(Collectors.joining(", ")));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			Marshalling.marshall(solvedSportfeest, "data/uurschema");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
