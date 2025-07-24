package ui.importer;

import com.google.inject.Inject;
import domain.importing.WizardData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import persistence.Marshalling;

import java.util.ArrayList;

public class WizardImportColumnsController extends WizardImportController {
	@FXML
	private ChoiceBox<String> txtSportfeest, txtRegio, txtAfdeling, txtDiscipline, txtAantal;
	@FXML
	private CheckBox chkKopteksten;

	@Inject
	WizardData model;

	@FXML
	public void initialize() {

	}

	@Override
	public void activate(boolean fromPrevious) {
		model.setTitle("Kolommen toewijzen");
		model.setSubtitle("Selecteer de juiste kolommen uit het Excel bestand");
	}

	public void loadFile(String filename) {
		model.setFilename(filename);
		ObservableList<String> columns = FXCollections.observableArrayList();
		ArrayList<String> groepsinschrijvingenFirstLine = Marshalling.getGroepsinschrijvingenFirstLine(model.getFilename(),
				Marshalling.getActiveSheet(model.getFilename()));
		columns.addAll(groepsinschrijvingenFirstLine);
		txtSportfeest.setItems(columns);
		txtRegio.setItems(columns);
		txtAfdeling.setItems(columns);
		txtDiscipline.setItems(columns);
		txtAantal.setItems(columns);
		txtSportfeest.setValue(columns.stream().filter(s -> s.equalsIgnoreCase("sportfeest")
				|| s.toLowerCase().startsWith("cursus")).findFirst().orElse(""));
		txtRegio.setValue(columns.stream().filter(s -> s.toLowerCase().contains("regio")).findFirst().orElse(""));
		txtAfdeling.setValue(columns.stream().filter(s -> s.equalsIgnoreCase("afdeling")).findFirst().orElse(""));
		txtDiscipline.setValue(columns.stream().filter(s -> s.equalsIgnoreCase("discipline")
				|| s.equalsIgnoreCase("sport")).findFirst().orElse(""));
		txtAantal.setValue(columns.stream().filter(s -> s.toLowerCase().contains("aantal")).findFirst().orElse(""));
	}

	@Validate
	public boolean validate() {

		if (txtSportfeest.getValue() == null || txtSportfeest.getValue().isEmpty()) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Kolommen");
			alert.setHeaderText("Niet toegewezen kolom");
			alert.setContentText("Kolom Sportfeest niet toegewezen");
			alert.showAndWait();
			return false;
		}

		if (txtRegio.getValue() == null || txtRegio.getValue().isEmpty()) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Kolommen");
			alert.setHeaderText("Niet toegewezen kolom");
			alert.setContentText("Kolom Regio is niet toegewezen. Touwtrekken zal niet gelijk verdeeld worden.");
			alert.showAndWait();
		}

		if (txtAfdeling.getValue() == null || txtAfdeling.getValue().isEmpty()) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Kolommen");
			alert.setHeaderText("Niet toegewezen kolom");
			alert.setContentText("Kolom Afdeling niet toegewezen");
			alert.showAndWait();
			return false;
		}

		if (txtDiscipline.getValue() == null || txtDiscipline.getValue().isEmpty()) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Kolommen");
			alert.setHeaderText("Niet toegewezen kolom");
			alert.setContentText("Kolom Discipline niet toegewezen");
			alert.showAndWait();
			return false;
		}

		if (txtAantal.getValue() == null || txtAantal.getValue().isEmpty()) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Kolommen");
			alert.setHeaderText("Niet toegewezen kolom");
			alert.setContentText("Kolom Aantal niet toegewezen");
			alert.showAndWait();
			return false;
		}

		return true;
	}

	@Submit
	public void submit() {
		model.setColSportfeest(txtSportfeest.getSelectionModel().getSelectedIndex());
		model.setColRegio(txtRegio.getSelectionModel().getSelectedIndex());
		model.setColAfdeling(txtAfdeling.getSelectionModel().getSelectedIndex());
		model.setColDiscipline(txtDiscipline.getSelectionModel().getSelectedIndex());
		model.setColAantal(txtAantal.getSelectionModel().getSelectedIndex());
		model.setColHeaders(chkKopteksten.isSelected());
	}
}
