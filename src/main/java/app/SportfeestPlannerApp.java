package app;

import domain.Inschrijving;
import domain.Sportfeest;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import persistence.Marshalling;

import java.util.List;

public class SportfeestPlannerApp {
    public static void main(String[] args) {
        SolverFactory<Sportfeest> solverFactory = SolverFactory.createFromXmlResource(
                    "sportfeestPlannerSolverConfig.xml");
        Solver<Sportfeest> solver = solverFactory.buildSolver();

        Sportfeest unsolvedSportfeest = Marshalling.unMarshall("data/ringverdeling.xlsx");

        List<Inschrijving> test = unsolvedSportfeest.getInschrijvingen();
        Sportfeest solvedSportfeest = solver.solve(unsolvedSportfeest);

        System.out.println("\nOpgelost sportfeest\n");
        System.out.println(solver.explainBestScore());
        System.out.println("Score: " + solvedSportfeest.getScore().toString());
        Marshalling.marshall(solvedSportfeest, "data/output.xml");

    }
}
