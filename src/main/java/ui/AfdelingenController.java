package ui;

import domain.Afdeling;
import domain.Inschrijving;
import domain.Sportfeest;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import persistence.Visualisatie;

import java.util.Comparator;
import java.util.stream.Collectors;

public class AfdelingenController extends AgendaController<Afdeling>{
	@FXML
	public void initialize() {
		agenda.setAllowDragging(true);
		agenda.setColumnValueFactory(Afdeling::toString);
		agenda.setItemColumnValueFactory(Inschrijving::getAfdeling);
		agenda.setItemValueFactory(inschr -> inschr.getRing() != null ? inschr.getRing().toString() : "");
		agenda.setItemColorFactory(inschr -> inschr.getRing() != null ? Visualisatie.getKleur(inschr.getRing().getNaam()) : "");
		agenda.setItemIsHeaderFactory(inschr -> inschr.getTijdslot() == null);
		agenda.appointmentChangedCallbackProperty().bind(appointmentChangedCallbackObjectProperty);
	}

	public void setSportfeest(Sportfeest sportfeest) {
		agenda.columns().clear();
		agenda.columns().addAll(sportfeest.getAfdelingen().stream()
				.sorted(Comparator.comparing(Afdeling::getNaam))
				.collect(Collectors.toList()));
		agenda.setAppointmentsProperty(FXCollections.observableArrayList(sportfeest.getInschrijvingen()));
		agenda.createDefaultSkin();
	}
}
