package ui;

import domain.Afdeling;
import domain.Inschrijving;
import domain.Sportfeest;
import javafx.fxml.FXML;
import persistence.Visualisatie;
import ui.visualization.jfxtras.scene.control.agenda.Agenda;

import java.util.Comparator;
import java.util.stream.Collectors;

public class AfdelingenController {
	@FXML
	private Agenda<Afdeling, Inschrijving> agenda;
	private Sportfeest sportfeest;

	@FXML
	public void initialize() {
		agenda.setAllowDragging(true);
		agenda.setColumnValueFactory(Afdeling::toString);
		agenda.setItemColumnValueFactory(Inschrijving::getAfdeling);
		agenda.setItemValueFactory(inschr -> inschr.getRing() != null ? inschr.getRing().toString() : "");
		agenda.setItemColorFactory(inschr -> inschr.getRing() != null ? Visualisatie.getKleur(inschr.getRing().getNaam()) : "");
		agenda.setItemIsHeaderFactory(inschr -> inschr.getTijdslot() == null);
	}

	public void setSportfeest(Sportfeest sportfeest) {
		this.sportfeest = sportfeest;
		agenda.columns().clear();
		agenda.columns().addAll(sportfeest.getAfdelingen().stream()
				.sorted(Comparator.comparing(Afdeling::getNaam))
				.collect(Collectors.toList()));
		agenda.createDefaultSkin();
		agenda.appointments().clear();
		agenda.appointments().addAll(sportfeest.getInschrijvingen());
		agenda.refresh();
	}

	public Sportfeest getSportfeest() {
		return sportfeest;
	}
}
