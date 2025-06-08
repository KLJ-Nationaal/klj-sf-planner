package ui.importer;

import com.google.inject.Inject;
import domain.importing.Groepsinschrijving;
import domain.importing.Reeks;
import domain.importing.WizardData;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import persistence.Marshalling;
import persistence.ReeksDefinitie;
import ui.EditingCell;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;


public class WizardImportReeksenController extends WizardImportController {
	@FXML
	private TableView<Reeks> tblReeksen;
	@FXML
	private TableColumn<Reeks, String> tblColDiscipline, tblColRing, tblColExtensie;
	@FXML
	private TableColumn<Reeks, Integer> tblColAantal;

	@Inject
	WizardData model;

	@FXML
	public void initialize() {
		tblColDiscipline.setCellValueFactory(new PropertyValueFactory<>("naam"));
		tblColAantal.setCellValueFactory(new PropertyValueFactory<>("aantal"));
		tblColRing.setCellFactory(col -> new EditingCell<Reeks, String>() {
			@Override
			public void updateIndex(int i) {
				super.updateIndex(i);
				if (i >= 0) {
					this.getStyleClass().add("editable-cell");
				}
			}
			@Override
			public void commitEditHandler(String newValue) {
				Reeks reeks = (Reeks) getTableRow().getItem();
				reeks.setRingNaam(newValue.trim());
			}
		});
		tblColRing.setCellValueFactory(new PropertyValueFactory<>("ringNaam"));
		tblColExtensie.setCellFactory(col -> new EditingCell<Reeks, String>() {
			@Override
			public void updateIndex(int i) {
				super.updateIndex(i);
				if (i >= 0) {
					this.getStyleClass().add("editable-cell");
				}
			}
			@Override
			public void commitEditHandler(String newValue) {
				Reeks reeks = (Reeks) getTableRow().getItem();
				reeks.setExtensie(newValue.trim());
			}
		});
		tblColExtensie.setCellValueFactory(new PropertyValueFactory<>("extensie"));

		tblReeksen.setItems(model.getReeksen());
		tblReeksen.getSelectionModel().setCellSelectionEnabled(true);
	}

	@Override
	public void activate(boolean fromPrevious) {
		model.setTitle("Disciplines aan ringen toewijzen");
		model.setSubtitle("Disciplines die in dezelfde ring moeten zitten, worden hier aangeduid, eventueel aangevuld met een extensie");

		if (fromPrevious) {
			model.getReeksen().clear();
			//TODO: errors in Marshalling controlleren
			ArrayList<Groepsinschrijving> groepsinschrijvingen = Marshalling.importGroepsinschrijvingen(model.getFilename(),
					Marshalling.getActiveSheet(model.getFilename()), model.getColHeaders(),
					model.getColSportfeest(), model.getColAfdeling(), model.getColDiscipline(), model.getColAantal());
			groepsinschrijvingen.stream()
					.filter(groepsinschrijving -> groepsinschrijving.getSportfeest().equalsIgnoreCase(model.getSportfeest().getValue()))
					.collect(Collectors.groupingBy(Groepsinschrijving::getSport, Collectors.summingInt(Groepsinschrijving::getAantal)))
					.forEach((discipline, aantal) -> model.getReeksen().add(new Reeks(discipline, aantal)));
			model.getReeksen().sort(Comparator.comparing(Reeks::getNaam));
			for (Reeks conf : ReeksDefinitie.unMarshall()) {
				model.getReeksen().stream()
						.filter(reeks -> reeks.getNaam().equalsIgnoreCase(conf.getNaam()))
						.forEach(reeks -> {
							reeks.setRingNaam(conf.getRingNaam());
							reeks.setExtensie(conf.getExtensie());
							reeks.setDuur(Math.max(reeks.getDuur(), conf.getDuur()));
						});
			}
		}
	}

	@Validate
	public boolean validate() throws Exception {
		//ringnamen moeten minstens 6 characters zijn
		if (!model.getReeksen().stream().allMatch(reeks -> reeks.getRingNaam() != null && reeks.getRingNaam().length() > 5)) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Ringen");
			alert.setHeaderText("Ringnaam te kort");
			alert.setContentText("Alle ringnamen moeten minsten 6 characters lang zijn");
			alert.showAndWait();
			return false;
		}
		return true;
	}

	@Submit
	public void submit() throws Exception {

	}
}
