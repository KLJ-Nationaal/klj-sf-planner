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
	private ChoiceBox<String> txtDansen, txtPiramide, txtAfdeling, txtWimpelen, txtVendelen, txtTouwtrekken;
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
		txtAfdeling.setItems(columns);
		txtDansen.setItems(columns);
		txtPiramide.setItems(columns);
		txtWimpelen.setItems(columns);
		txtVendelen.setItems(columns);
		txtTouwtrekken.setItems(columns);
		txtAfdeling.setValue(columns.stream().filter(s -> s.toLowerCase().contains("afdeling")).findFirst().orElse(""));
		txtDansen.setValue(columns.stream().filter(s -> s.toLowerCase().contains("dansen")).findFirst().orElse(""));
		txtPiramide.setValue(columns.stream().filter(s -> s.toLowerCase().contains("piramide")).findFirst().orElse(""));
		txtWimpelen.setValue(columns.stream().filter(s -> s.toLowerCase().contains("wimpelen")).findFirst().orElse(""));
		txtVendelen.setValue(columns.stream().filter(s -> s.toLowerCase().contains("vendelen")).findFirst().orElse(""));
		txtTouwtrekken.setValue(columns.stream().filter(s -> s.toLowerCase().contains("touwtrekken")).findFirst().orElse(""));
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

		if (txtDansen.getValue() == null || txtDansen.getValue().isEmpty()) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Kolommen");
			alert.setHeaderText("Niet toegewezen kolom");
			alert.setContentText("Kolom Dansen niet toegewezen");
			alert.showAndWait();
			return false;
		}

		if (txtPiramide.getValue() == null || txtPiramide.getValue().isEmpty()) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Kolommen");
			alert.setHeaderText("Niet toegewezen kolom");
			alert.setContentText("Kolom Piramide is niet toegewezen. Touwtrekken zal niet gelijk verdeeld worden.");
			alert.showAndWait();
		}

		if (txtWimpelen.getValue() == null || txtWimpelen.getValue().isEmpty()) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Kolommen");
			alert.setHeaderText("Niet toegewezen kolom");
			alert.setContentText("Kolom Wimpelen niet toegewezen");
			alert.showAndWait();
			return false;
		}

		if (txtVendelen.getValue() == null || txtVendelen.getValue().isEmpty()) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Kolommen");
			alert.setHeaderText("Niet toegewezen kolom");
			alert.setContentText("Kolom Vendelen niet toegewezen");
			alert.showAndWait();
			return false;
		}

		return true;
	}

	@Submit
	public void submit() {
		model.setColAfdeling(txtAfdeling.getSelectionModel().getSelectedIndex());
		model.setColDansen(txtDansen.getSelectionModel().getSelectedIndex());
		model.setColPiramide(txtPiramide.getSelectionModel().getSelectedIndex());
		model.setColWimpelen(txtWimpelen.getSelectionModel().getSelectedIndex());
		model.setColVendelen(txtVendelen.getSelectionModel().getSelectedIndex());
		model.setColTouwtrekken(txtTouwtrekken.getSelectionModel().getSelectedIndex());
		model.setColHeaders(chkKopteksten.isSelected());
	}
}
