package ui.importer;

import com.google.inject.Inject;
import domain.importing.WizardData;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

public class WizardImportSportfeestController extends WizardImportController {

	private ObservableList<String> columns;

	@FXML
	private TextField txtTitel;
	@FXML
	private DatePicker txtDatum;

	@Inject
	WizardData model;

	@FXML
	public void initialize() {
		txtTitel.textProperty().bindBidirectional(model.sfTitelProperty());
		txtDatum.valueProperty().bindBidirectional(model.sfDatumProperty());
	}

	@Override
	public void activate(boolean fromPrevious) {
		model.setTitle("Instellingen");
		model.setSubtitle("Stel de titel en de datum van het sportfeest in, deze komen op verdeling");
	}

	@Validate
	public boolean validate() {
		return true;
	}

	@Submit
	public void submit() {

	}
}
