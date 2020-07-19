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
import javafx.scene.control.cell.TextFieldTableCell;
import persistence.Marshalling;
import persistence.ReeksDefinitie;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;


public class WizardImportReeksenController extends WizardImportController{
	@FXML
	private TableView tblReeksen;

	@FXML
	private TableColumn tblColDiscipline, tblColAantal, tblColRing, tblColExtensie;

	@Inject
	WizardData model;

	@FXML
	public void initialize() {
		tblColDiscipline.setCellValueFactory(new PropertyValueFactory<Reeks,String>("naam"));
		tblColAantal.setCellValueFactory(new PropertyValueFactory<Reeks,Integer>("aantal"));
		tblColRing.setCellFactory(TextFieldTableCell.forTableColumn());
		tblColRing.setCellValueFactory(new PropertyValueFactory<Reeks,String>("ringNaam"));
		tblColExtensie.setCellFactory(TextFieldTableCell.forTableColumn());
		tblColExtensie.setCellValueFactory(new PropertyValueFactory<Reeks,String>("extensie"));

		tblReeksen.setItems(model.getReeksen());
	}

	@Override
	public void activate(){
		model.setTitle("Disciplines aan ringen toewijzen");
		model.setSubtitle("Disciplines die in dezelfde ring moeten zitten, worden hier aangeduid, eventueel aangevuld met een extensie");

		model.getReeksen().clear();
		ArrayList<Groepsinschrijving> groepsinschrijvingen = Marshalling.importGroepsinschrijvingen(model.getFilename(),
				Marshalling.getActiveSheet(model.getFilename()), model.getColHeaders(),
				model.getColSportfeest(), model.getColAfdeling(), model.getColDiscipline(), model.getColAantal());
		groepsinschrijvingen.stream()
				.filter(groepsinschrijving -> groepsinschrijving.getSportfeest().equalsIgnoreCase(model.getSportfeest().getValue()))
				.collect(Collectors.groupingBy(Groepsinschrijving::getSport, Collectors.summingInt(Groepsinschrijving::getAantal)))
				.forEach((discipline, aantal) -> model.getReeksen().add(new Reeks(discipline, aantal)));
		model.getReeksen().sort(Comparator.comparing(Reeks::getNaam));
		for(Reeks conf : ReeksDefinitie.unMarshall()){
			model.getReeksen().stream()
					.filter(reeks -> reeks.getNaam().equalsIgnoreCase(conf.getNaam()))
					.forEach(reeks -> {
						reeks.setRingNaam(conf.getRingNaam());
						reeks.setExtensie(conf.getExtensie());
						reeks.setDuur(Math.max(reeks.getDuur(),conf.getDuur()));
					});
		}
	}

	@Validate
	public boolean validate() throws Exception {
		//ringnamen moeten minstens 8 characters zijn
		if(!model.getReeksen().stream().allMatch(reeks -> reeks.getRingNaam().length() > 8)) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Ringen");
			alert.setHeaderText( "Ringnaam te kort" );
			alert.setContentText( "Alle ringnamen moeten minsten 8 characters lang zijn" );
			alert.showAndWait();
			return false;
		}
		return true;
	}

	@Submit
	public void submit() throws Exception {

	}
}
