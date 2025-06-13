package app;

import ch.qos.logback.classic.Logger;
import domain.Sportfeest;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.util.Pair;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.ScoreExplanation;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.solver.SolutionManager;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.slf4j.LoggerFactory;
import persistence.Instellingen;

import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SportfeestPlannerService extends Service<Sportfeest> {
	private final SolverConfig solverConfig = SolverConfig.createFromXmlResource("solverConfig.xml");
	private final Solver<Sportfeest> solver;
	private final SolutionManager<Sportfeest, HardSoftScore> solutionManager;
	private final static Logger logger = (Logger) LoggerFactory.getLogger(SportfeestPlannerService.class);

	private final ObjectProperty<Sportfeest> sportfeestProperty = new SimpleObjectProperty<>(this, "sportfeest");
	public ObjectProperty<Sportfeest> getSportfeestProperty() { return sportfeestProperty; }
	public void setSportfeest(Sportfeest sportfeest) { sportfeestProperty.set(sportfeest); }
	public Sportfeest getSportfeest() { return sportfeestProperty.get(); }

	private final ObjectProperty<Score<HardSoftScore>> scoreProperty = new SimpleObjectProperty<>(null, "score");
	public ObjectProperty<Score<HardSoftScore>> getScoreProperty() { return scoreProperty; }

	private final LongProperty timeMillisSpentProperty = new SimpleLongProperty(0, "time");
	public LongProperty getTimeMillisSpentProperty() { return timeMillisSpentProperty; }
	public long getTimeMillisSpent() { return timeMillisSpentProperty.get(); }

	public SportfeestPlannerService() {
		solverConfig.getPhaseConfigList().stream()
				.filter(pc -> pc.getTerminationConfig() != null)
				.forEach(pc -> pc.getTerminationConfig().setSecondsSpentLimit((long) Instellingen.Opties().SOLVERTIMELIMIT));
		SolverFactory<Sportfeest> solverFactory = SolverFactory.create(solverConfig);
		solutionManager = SolutionManager.create(solverFactory);
		solver = solverFactory.buildSolver();
		solver.addEventListener(event -> {
			sportfeestProperty.set(event.getNewBestSolution());
			scoreProperty.set(event.getNewBestScore());
			timeMillisSpentProperty.set(event.getTimeMillisSpent());
		});
	}

	public Pair<Long, Long> getTimeScheduled() {
		if (solverConfig.getTerminationConfig() == null || !solver.isSolving()) {
			return new Pair<>(0L, 0L);
		}
		long total = solverConfig.getPhaseConfigList().stream()
				.filter(pc -> pc.getTerminationConfig() != null)
				.filter(pc -> pc.getTerminationConfig().getSecondsSpentLimit() != null)
				.mapToLong(pc -> pc.getTerminationConfig().getSecondsSpentLimit() * 1000)
				.sum();
		//TODO: werkt niet !!!
		return new Pair<>(getTimeMillisSpent(), total);
	}

	public SolutionManager<Sportfeest, HardSoftScore> getSolutionManager() {
		return solutionManager;
	}

	@Override
	protected void succeeded() {
		super.succeeded();
		logger.info("Einde van de berekening");
		logger.info("Score: {}", sportfeestProperty.get().getScore().toString());
		if (!sportfeestProperty.get().getScore().isFeasible()) logger.warn("DEZE OPLOSSING IS NIET HAALBAAR!");

		try {
			ScoreExplanation<Sportfeest, HardSoftScore> explanation = solutionManager.explain(sportfeestProperty.get());

			for (ConstraintMatchTotal<?> cmt : explanation.getConstraintMatchTotalMap().values()) {
				Consumer<String> c = logger::info;
				if (((HardSoftScore) cmt.getScore()).hardScore() != 0) {
					c = logger::warn;
				}

				c.accept("  Voorwaarde: " + cmt.getConstraintName());
				c.accept("  Gewicht: " + cmt.getScore() + ", Aantal keer: " + cmt.getConstraintMatchCount());

				for (ConstraintMatch<?> cm : cmt.getConstraintMatchSet()) {
					c.accept("    " + cm.getIndictedObjectList().stream()
							.map(Object::toString)
							.collect(Collectors.joining(", ")));
				}
			}
		} catch (Exception e) {
			logger.warn("Fout bij het analyseren van de score: {}", e.getLocalizedMessage(), e);
		}

	}

	@Override
	protected Task<Sportfeest> createTask() {
		return new Task<Sportfeest>() {
			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return solver.terminateEarly() && super.cancel(mayInterruptIfRunning);
			}

			@Override
			public boolean isCancelled() { return solver.isTerminateEarly() && super.isCancelled(); }

			protected Sportfeest call() {
				sportfeestProperty.set(solver.solve(sportfeestProperty.get()));
				return sportfeestProperty.get();
			}
		};
	}
}
