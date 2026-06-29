package app;

import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ch.qos.logback.classic.Logger;
import domain.Sport;
import domain.Sportfeest;
import domain.Tijdslot;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.util.Pair;
import org.slf4j.LoggerFactory;
import persistence.Instellingen;
import solver.ConstraintAnalyzer;

import java.util.Random;

public class SportfeestPlannerService extends Service<Sportfeest> {
	private final SolverConfig solverConfig = SolverConfig.createFromXmlResource("solverConfig.xml");
	private final Solver<Sportfeest> solver;
	private final SolutionManager<Sportfeest, HardSoftScore> solutionManager;
	private final SolverFactory<Sportfeest> solverFactory;
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
		solverFactory = SolverFactory.create(solverConfig);
		solutionManager = SolutionManager.create(solverFactory);
		solver = solverFactory.buildSolver();
		solver.addEventListener(event -> {
			sportfeestProperty.set(event.getNewBestSolution());
			scoreProperty.set(event.getNewBestScore());
			timeMillisSpentProperty.set(event.getTimeMillisSpent());
			logger.info("{} Betere oplossing gevonden, verlopen tijd: {}s, beste score: {}", event.getProducerId().toString(),
					Math.round((float) event.getTimeMillisSpent() / 100) / 10f, event.getNewBestScore().toString());
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
	public SolverFactory<Sportfeest> getSolverFactory() { return solverFactory; }

	public void printResults() {
		logger.info("Score: {}", sportfeestProperty.get().getScore().toString());
		if (!sportfeestProperty.get().getScore().isFeasible()) logger.warn("DEZE OPLOSSING IS NIET HAALBAAR!");

		try {
			var analyzer = new ConstraintAnalyzer(solverFactory);
			analyzer.analyze(sportfeestProperty.get()).printSummary();
		} catch (Exception e) {
			logger.warn("Fout bij het analyseren van de score: {}", e.getLocalizedMessage(), e);
		}
	}

	@Override
	protected void succeeded() {
		super.succeeded();
		logger.info("Berekening voltooid");
		printResults();
	}

	@Override
	protected void cancelled() {
		super.cancelled();
		logger.info("Berekening gestopt");
		printResults();
	}
	@Override
	protected Task<Sportfeest> createTask() {
		return new Task<>() {
			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return solver.terminateEarly() && super.cancel(mayInterruptIfRunning);
			}

			@Override
			public boolean isCancelled() { return solver.isTerminateEarly() && super.isCancelled(); }

			protected Sportfeest call() {
				//TODO: temp workaround
				Random rand = new Random();
				sportfeestProperty.get().getInschrijvingen().stream()
						.filter(inschrijving -> inschrijving.getDiscipline().getSport().equals(Sport.TOUWTREKKEN))
						.filter(inschrijving -> inschrijving.getTijdslot() == null)
						.forEach(inschrijving -> {
							Tijdslot s = inschrijving.getTijdslots().get(rand.nextInt(0, inschrijving.getTijdslots().size()));
							logger.debug("{}: {}", inschrijving, s);
							inschrijving.setTijdslot(s);
						});
				final Sportfeest newSf = solver.solve(sportfeestProperty.get());
				sportfeestProperty.set(newSf);
				return newSf;
			}
		};
	}
}
