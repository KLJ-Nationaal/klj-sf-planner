package app;

import domain.*;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import persistence.Marshalling;

public class SportfeestPlannerApp {
    public static void main(String[] args) {
        SolverFactory<Sportfeest> solverFactory = SolverFactory.createFromXmlResource(
                    "sportfeestPlannerSolverConfig.xml");
        Solver<Sportfeest> solver = solverFactory.buildSolver();

        Sportfeest unsolvedSportfeest = Marshalling.unMarshall("data/ringverdeling.xlsx");

        Sportfeest solvedSportfeest = solver.solve(unsolvedSportfeest);

        System.out.println("\nOpgelost sportfeest:\n" + toDisplayString(solvedSportfeest));
        System.out.println("Score: " + solvedSportfeest.getScore().toString());
        Marshalling.marshall(solvedSportfeest, "data/output.xml");

    }

    public static String toDisplayString(Sportfeest sportfeest) {
        StringBuilder displayString = new StringBuilder();
        /*for (Ring ring : sportfeest.getRingen()) {
            displayString.append(" *** ").append(ring.getLabel()).append(" *** ");
            if(ring.getTijdslots() != null) {
                for (Tijdslot tijdslot : ring.getTijdslots()) {
                    displayString.append("   ").append(tijdslot.getLabel()).append(" -> ")
                            .append(tijdslot.getAfdeling() == null ? null : tijdslot.getAfdeling().getNaam()).append("\n");
                }
            }
        }*/
        for (Inschrijving inschr : sportfeest.getInschrijvingen()) {
            displayString.append(" *** ").append(inschr.getLabel()).append(" *** \n");
            if(inschr.getTijdslot() != null) displayString.append(inschr.getTijdslot().getLabel()).append("\n");
        }
        return displayString.toString();
    }
}
