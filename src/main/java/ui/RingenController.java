package ui;

import domain.Inschrijving;
import domain.Ring;
import domain.Sportfeest;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import persistence.Visualisatie;

import java.util.ArrayList;

public class RingenController extends AgendaController<Ring>{
	@FXML
	public void initialize() {
		agenda.setAllowDragging(true);
		agenda.setColumnValueFactory(Ring::toString);
		agenda.setItemColumnValueFactory(Inschrijving::getRing);
		agenda.setItemValueFactory(inschr -> inschr.getAfdeling().toString());
		agenda.setItemColorFactory(inschr -> Visualisatie.getKleur(inschr.getAfdeling().getNaam()));
		agenda.setItemIsHeaderFactory(inschr -> inschr.getTijdslot() == null);
		agenda.appointmentChangedCallbackProperty().bind(appointmentChangedCallbackObjectProperty);
	}

	public void setSportfeest(Sportfeest sportfeest) {
		agenda.columns().clear();
		agenda.columns().addAll(new ArrayList<>(sportfeest.getRingen()));
		agenda.setAppointmentsProperty(FXCollections.observableArrayList(sportfeest.getInschrijvingen()));
		agenda.createDefaultSkin();
	}
}
