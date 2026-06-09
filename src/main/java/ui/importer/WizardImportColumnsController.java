package ui.importer;

import com.google.inject.Inject;
import domain.importing.Reeks;
import domain.importing.WizardData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import org.controlsfx.control.CheckListView;
import persistence.Marshalling;
import persistence.ReeksDefinitie;

import java.util.ArrayList;

public class WizardImportColumnsController extends WizardImportController {
	@FXML
	private ChoiceBox<String> txtAfdeling;
	@FXML
	private CheckListView<String> txtDisciplines;

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
		txtAfdeling.setItems(columns);
		txtDisciplines.setItems(columns);
		txtAfdeling.setValue(columns.stream().filter(s ->
				s.toLowerCase().contains("afdeling") || (s.toLowerCase().contains("naam") && s.toLowerCase().contains("club"))
		).findFirst().orElse(""));
		ArrayList<Reeks> reeksen = ReeksDefinitie.unMarshall();
		reeksen.forEach(reeks -> {
					columns.stream()
							.filter(column -> column.toLowerCase().contains(reeks.getNaam().toLowerCase()))
							.findFirst()
							.ifPresent(column -> txtDisciplines.getCheckModel().check(column));
				}
		);
	}

	@Validate
	public boolean validate() {

		if (txtAfdeling.getValue() == null || txtAfdeling.getValue().isEmpty()) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Kolommen");
			alert.setHeaderText("Niet toegewezen kolom");
			alert.setContentText("Kolom Afdeling niet toegewezen");
			alert.showAndWait();
			return false;
		}

		if (txtDisciplines.getCheckModel().getCheckedItems().isEmpty()) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Kolommen");
			alert.setHeaderText("Geen disciplines aangevinkt");
			alert.setContentText("Je moet minstens één discipline aanvinken");
			alert.showAndWait();
			return false;
		}

		return true;
	}

	@Submit
	public void submit() {
		model.setColAfdeling(txtAfdeling.getSelectionModel().getSelectedIndex());
		model.setColDisciplines(txtDisciplines.getCheckModel().getCheckedIndices());
	}
}
