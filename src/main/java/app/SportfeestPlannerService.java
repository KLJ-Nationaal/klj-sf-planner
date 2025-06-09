package app;

import ch.qos.logback.classic.Logger;
import domain.Sportfeest;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;
import org.optaplanner.core.api.solver.event.SolverEventListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SportfeestPlannerService extends Service<Sportfeest> {
	private final SolverFactory<Sportfeest> solverFactory = SolverFactory.createFromXmlResource("solverConfig.xml");
	private final Solver<Sportfeest> solver = solverFactory.buildSolver();
	private final static Logger logger = (Logger) LoggerFactory.getLogger(SportfeestPlannerService.class);
	private final List<SolverEventListener<Sportfeest>> eventListeners = new ArrayList<>();
	private final ObjectProperty<Sportfeest> sportfeestProperty = new SimpleObjectProperty<>(this, "sportfeest");

	public SportfeestPlannerService() {
		solver.addEventListener((BestSolutionChangedEvent<Sportfeest> bestSolutionChangedEvent) -> eventListeners.forEach(
				solverEventListener -> solverEventListener.bestSolutionChanged(bestSolutionChangedEvent)
		));
	}

	public ObjectProperty<Sportfeest> getSportfeestProperty() { return sportfeestProperty; }
	public void setSportfeest(Sportfeest sportfeest) { sportfeestProperty.set(sportfeest); }
	public Sportfeest getSportfeest() { return sportfeestProperty.get(); }

	public Long getTimeScheduled() {
		if (solverFactory.getSolverConfig().getTerminationConfig() != null)
			//TODO: werkt niet !!!
			return solverFactory.getSolverConfig().getTerminationConfig().calculateTimeMillisSpentLimit();
		return Long.MAX_VALUE;
	}

	public ScoreDirector<Sportfeest> getScoreDirector() {
		ScoreDirector<Sportfeest> scoreDirector = solver.getScoreDirectorFactory().buildScoreDirector();
		scoreDirector.setWorkingSolution(sportfeestProperty.get());
		return scoreDirector;
	}

	public void addSolverEventListener(SolverEventListener<Sportfeest> solverEventListener) {
		eventListeners.add(solverEventListener);
	}

	@Override
	protected void succeeded() {
		super.succeeded();
		logger.info("Einde van de berekening");
		logger.info("Score: {}", sportfeestProperty.get().getScore().toString());
		if (!sportfeestProperty.get().getScore().isFeasible()) logger.warn("DEZE OPLOSSING IS NIET HAALBAAR!");

		ScoreDirector<Sportfeest> scoreDirector = solver.getScoreDirectorFactory().buildScoreDirector();
		scoreDirector.setWorkingSolution(sportfeestProperty.get());
		for (ConstraintMatchTotal cmt : scoreDirector.getConstraintMatchTotals()) {
			Consumer<String> c = logger::info;
			if (((HardSoftScore) cmt.getScore()).getHardScore() != 0) c = logger::warn;

			c.accept("  Voorwaarde: " + cmt.getConstraintName());
			c.accept("  Gewicht: " + cmt.getScore() + ", Aantal keer: " + cmt.getConstraintMatchCount());
			for (ConstraintMatch cm : cmt.getConstraintMatchSet()) {
				c.accept("    " + cm.getJustificationList().stream()
						.map(Object::toString)
						.collect(Collectors.joining(", ")));
			}
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

			@Override
			protected void cancelled() {
				super.cancelled();
				sportfeestProperty.set(solver.getBestSolution());
			}
			protected Sportfeest call() {
				sportfeestProperty.set(solver.solve(sportfeestProperty.get()));
				return sportfeestProperty.get();
			}

			public long getTimeMillisSpent() { return solver.getTimeMillisSpent(); }

			public Sportfeest getBestSolution() { return solver.getBestSolution(); }

			public Score<?> getBestScore() { return solver.getBestScore(); }
		};
	}
}
