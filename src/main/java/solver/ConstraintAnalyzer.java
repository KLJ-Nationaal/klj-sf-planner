package solver;

import ai.timefold.solver.core.api.score.analysis.ConstraintAnalysis;
import ai.timefold.solver.core.api.score.analysis.ScoreAnalysis;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.ScoreAnalysisFetchPolicy;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ch.qos.logback.classic.Logger;
import domain.Sportfeest;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Free replacement for the now paid ScoreExplanation.
 * <p>
 * Usage:
 * var analyzer = new ConstraintAnalyzer(solverFactory);
 * analyzer.analyze(solution).printSummary();
 * <p>
 * NOTE on justification objects:
 * By default Timefold returns an internal object whose toString() may not be very readable.
 * To get useful output, add .justifyWith(...) to your constraints
 */
public class ConstraintAnalyzer {

	// -------------------------------------------------------------------------
	// Result records
	// -------------------------------------------------------------------------

	public record MatchResult(HardSoftScore score, Object justification) {
		@Override
		public String toString() {
			return score.toShortString() + " - " + justification;
		}
	}

	public record ConstraintResult(String name, HardSoftScore totalScore, int matchCount, List<MatchResult> matches) {
		public boolean hasMatches() {
			return matchCount > 0;
		}
	}

	public record AnalysisResult(HardSoftScore totalScore, List<ConstraintResult> constraints) {
		// Only constraints that actually fired (matchCount > 0).
		public List<ConstraintResult> activeConstraints() {
			return constraints.stream()
					.filter(ConstraintResult::hasMatches)
					.collect(Collectors.toList());
		}

		// Compact summary to stdout.
		public void printSummary() {
			logger.info("ANALYSE VAN DE SCORE: {}", totalScore());
			activeConstraints().forEach(c -> {
				logger.info("  [{}]  {}  ({} keer)", c.totalScore(), c.name(), c.matchCount());
				c.matches().forEach(m ->
						logger.info("      {}", m)
				);
			});
		}
	}

	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------

	private final static Logger logger = (Logger) LoggerFactory.getLogger(ConstraintAnalyzer.class);
	private final SolutionManager<Sportfeest, HardSoftScore> solutionManager;

	public ConstraintAnalyzer(SolverFactory<Sportfeest> solverFactory) {
		this.solutionManager = SolutionManager.create(solverFactory);
	}

	// -------------------------------------------------------------------------
	// Main method
	// -------------------------------------------------------------------------

	public AnalysisResult analyze(Sportfeest solution) {
		// FETCH_ALL  → full match list + justifications (what we want)
		// FETCH_MATCH_COUNT → counts only, no justification objects
		// FETCH_SHALLOW → no match analysis at all (fastest, scores only)
		ScoreAnalysis<HardSoftScore> scoreAnalysis =
				solutionManager.analyze(solution, ScoreAnalysisFetchPolicy.FETCH_ALL);

		List<ConstraintResult> results = scoreAnalysis.constraintMap().values().stream()
				.map(this::toConstraintResult)
				// sort: hardest violations first, then softest
				.sorted(Comparator
						.comparingInt((ConstraintResult c) -> c.totalScore().hardScore())
						.thenComparingInt(c -> c.totalScore().softScore())
				)
				.collect(Collectors.toList());

		return new AnalysisResult(scoreAnalysis.score(), results);
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	private ConstraintResult toConstraintResult(ConstraintAnalysis<HardSoftScore> ca) {
		List<MatchResult> matches = (ca.matches() == null)
				? List.of()
				: ca.matches().stream()
				.map(m -> new MatchResult(m.score(), m.justification()))
				.collect(Collectors.toList());

		return new ConstraintResult(
				ca.constraintName(),   // in 2.x: constraintId(), NOT constraintName()
				ca.score(),
				ca.matchCount(),
				matches
		);
	}
}