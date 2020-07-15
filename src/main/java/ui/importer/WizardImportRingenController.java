package ui.importer;

import com.google.inject.Inject;
import domain.importing.Reeks;
import domain.importing.WizardData;
import domain.importing.WizardRing;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;
import persistence.ReeksDefinitie;

import java.util.Comparator;
import java.util.stream.Collectors;

public class WizardImportRingenController extends WizardImportController{
	@FXML
	private TableView tblRingen;

	@FXML
	private TableColumn tblColRing, tblColAantal, tblColDuur, tblColAantalRingen;

	@Inject
	WizardData model;

	private final ObservableList<WizardRing> data = FXCollections.observableArrayList();

	@FXML
	public void initialize() {
		tblColRing.setCellValueFactory(new PropertyValueFactory<WizardRing,String>("naam"));
		tblColAantal.setCellValueFactory(new PropertyValueFactory<WizardRing,Integer>("aantal"));
		tblColDuur.setCellFactory(TextFieldTableCell.<WizardRing,Integer>forTableColumn(new IntegerStringConverter()));
		tblColDuur.setCellValueFactory(new PropertyValueFactory<WizardRing,Integer>("duur"));
		tblColAantalRingen.setCellFactory(TextFieldTableCell.<WizardRing,Integer>forTableColumn(new IntegerStringConverter()));
		tblColAantalRingen.setCellValueFactory(new PropertyValueFactory<WizardRing,Integer>("aantalRingen"));

		tblRingen.setItems(data);
	}

	@Override
	public void activate(){
		model.setTitle("Ringen instellen");
		model.setSubtitle("Vul hier het aantal ringen en minuten per ring in");

		data.clear();

		model.getReeksen().stream()
				.collect(Collectors.groupingBy(Reeks::getRingNaam, Collectors.summingInt(Reeks::getAantal)))
				.forEach((ring, aantal) -> data.add(new WizardRing(ring, aantal)));
		data.sort(Comparator.comparing(WizardRing::getNaam));
		//duur voor ring
		for(Reeks conf : ReeksDefinitie.unMarshall()){
			data.stream()
				.filter(ring -> ring.getNaam().equalsIgnoreCase(conf.getRingNaam()))
				.forEach(ring -> {
					ring.setDuur(Math.max(ring.getDuur(),conf.getDuur()));
					ring.setAfdPerRing(Math.max(ring.getAfdPerRing(),conf.getAfdPerRing()));
				});
		}
		//voorstel aantal ringen
		for(WizardRing ring : data) {
			ring.setAantalRingen((int)Math.ceil(1.0 * ring.getAantal() / ring.getAfdPerRing()));
		}
	}

	@Validate
	public boolean validate() throws Exception {
		return true;
	}

	@Submit
	public void submit() throws Exception {

		if(dataCallback != null) dataCallback.accept(model);
	}
}
