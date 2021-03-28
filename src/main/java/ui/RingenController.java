package ui;

import domain.Inschrijving;
import domain.Ring;
import domain.Sportfeest;
import javafx.fxml.FXML;
import persistence.Visualisatie;
import ui.visualization.jfxtras.scene.control.agenda.Agenda;

import java.util.ArrayList;

public class RingenController {
	@FXML
	private Agenda<Ring, Inschrijving> agenda;
	private Sportfeest sportfeest;

	@FXML
	public void initialize() {
		agenda.setAllowDragging(true);
		agenda.setColumnValueFactory(Ring::toString);
		agenda.setItemColumnValueFactory(Inschrijving::getRing);
		agenda.setItemValueFactory(inschr -> inschr.getAfdeling().toString());
		agenda.setItemColorFactory(inschr -> Visualisatie.getKleur(inschr.getAfdeling().getNaam()));
		agenda.setItemIsHeaderFactory(inschr -> inschr.getTijdslot() == null);
	}

	public void setSportfeest(Sportfeest sportfeest) {
		this.sportfeest = sportfeest;
		agenda.columns().clear();
		agenda.columns().addAll(new ArrayList<>(sportfeest.getRingen()));
		agenda.createDefaultSkin();
		agenda.appointments().clear();
		agenda.appointments().addAll(sportfeest.getInschrijvingen());
		agenda.refresh();
	}

	public Sportfeest getSportfeest() {
		return sportfeest;
	}
}
