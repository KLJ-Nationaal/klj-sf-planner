package ui.importer;

import com.google.inject.Inject;
import domain.importing.Groepsinschrijving;
import domain.importing.WizardData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import persistence.Marshalling;

import java.util.ArrayList;
import java.util.Objects;

public class WizardImportSportfeestController extends WizardImportController {

	private ObservableList<String> columns;

	@FXML
	private ChoiceBox<String> txtSportfeest;
	@FXML
	private TextField txtTitel;
	@FXML
	private DatePicker txtDatum;

	@Inject
	WizardData model;

	@FXML
	public void initialize() {
		txtSportfeest.valueProperty().bindBidirectional(model.sportfeestProperty());
		txtTitel.textProperty().bindBidirectional(model.sfTitelProperty());
		txtDatum.valueProperty().bindBidirectional(model.sfDatumProperty());
	}

	@Override
	public void activate(boolean fromPrevious) {
		model.setTitle("Instellingen");
		model.setSubtitle("Duid het sportfeest aan, de tekst en de datum komen op verdeling");

		if (fromPrevious && (model.getFilename() != null) && !Objects.equals(model.getFilename(), "")) {
			columns = FXCollections.observableArrayList();
			ArrayList<Groepsinschrijving> groepsinschrijvingen = Marshalling.importGroepsinschrijvingen(model.getFilename(),
					Marshalling.getActiveSheet(model.getFilename()), model.getColHeaders(),
					model.getColSportfeest(), model.getColAfdeling(), model.getColDiscipline(), model.getColAantal());
			groepsinschrijvingen.stream()
					.map(Groepsinschrijving::getSportfeest)
					.distinct()
					.forEach(col -> columns.add(col));
			txtSportfeest.setItems(columns);
			txtSportfeest.setValue(columns.stream().filter(s -> s.equalsIgnoreCase("sportfeest")).findFirst().orElse(""));
		}

		txtSportfeest.setOnAction(event -> txtTitel.setText(txtSportfeest.getValue().replaceAll("(?i)sportfeest", "").trim()));
	}

	@Validate
	public boolean validate() {

		if (txtSportfeest.getValue() == null || txtSportfeest.getValue().isEmpty()) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Kolommen");
			alert.setHeaderText("Niet toegewezen");
			alert.setContentText("Je moet een bestaande keuze selecteren");
			alert.showAndWait();
			return false;
		}

		return true;
	}

	@Submit
	public void submit() {

	}
}
