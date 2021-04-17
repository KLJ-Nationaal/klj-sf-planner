package ui.importer;

import com.google.inject.Inject;
import domain.*;
import domain.importing.Groepsinschrijving;
import domain.importing.Reeks;
import domain.importing.WizardData;
import domain.importing.WizardRing;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import persistence.Marshalling;
import persistence.ReeksDefinitie;
import ui.EditingCell;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class WizardImportRingenController extends WizardImportController{
	@FXML
	private TableView tblRingen;

	@FXML
	private TableColumn tblColRing, tblColAantal, tblColDuur, tblColAantalRingen;

	@FXML
	private Button btnNext;

	@Inject
	WizardData model;

	private final ObservableList<WizardRing> data = FXCollections.observableArrayList();

	@FXML
	public void initialize() {
		tblColRing.setCellValueFactory(new PropertyValueFactory<WizardRing,String>("naam"));
		tblColAantal.setCellValueFactory(new PropertyValueFactory<WizardRing,Integer>("aantalAfd"));
		tblColDuur.setCellFactory(col -> new EditingCell<WizardRing, Integer>() {
			@Override
			public void updateIndex(int i) {
				super.updateIndex(i);
				if (i >= 0) {
					this.getStyleClass().add("editable-cell");
				}
			}
			@Override
			public void commitEditHandler(Integer newValue){
				WizardRing wizardRing = (WizardRing) getTableRow().getItem();
				wizardRing.setDuur(newValue);
			}
		});
		tblColDuur.setCellValueFactory(new PropertyValueFactory<WizardRing,Integer>("duur"));
		tblColAantalRingen.setCellFactory(col -> new EditingCell<WizardRing, Integer>() {
			@Override
			public void updateIndex(int i) {
				super.updateIndex(i);
				if (i >= 0) {
					this.getStyleClass().add("editable-cell");
				}
			}
			@Override
			public void commitEditHandler(Integer newValue){
				WizardRing wizardRing = (WizardRing) getTableRow().getItem();
				wizardRing.setAantalRingen(newValue);
			}
		});
		tblColAantalRingen.setCellValueFactory(new PropertyValueFactory<WizardRing,Integer>("aantalRingen"));

		tblRingen.setItems(data);
		tblRingen.getSelectionModel().setCellSelectionEnabled(true);
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
			ring.setAantalRingen((int)Math.ceil(1.0 * ring.getAantalAfd() / ring.getAfdPerRing()));
		}
	}

	@Validate
	public boolean validate() throws Exception {
		//aantal ringen groter dan 0
		if(!data.stream().allMatch(ring -> ring.getAantalRingen() > 0)) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Ringen");
			alert.setHeaderText( "Te weinig ringen" );
			alert.setContentText( "Aantal ringen moet groter zijn dan 0 " );
			alert.showAndWait();
			return false;
		}
		//schema minuten groter dan 2
		if(!data.stream().allMatch(ring -> ring.getDuur() > 2)) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Ringen");
			alert.setHeaderText( "Schema te kort" );
			alert.setContentText( "Alle schema's moeten minsten 3 minuten zijn" );
			alert.showAndWait();
			return false;
		}
		return true;
	}

	@Submit
	public void submit() throws Exception {
		Sportfeest sf = new Sportfeest();
		sf.setLocatie(model.getSfTitel());
		sf.setDatum(model.getSfDatum());

		//Disciplines toevoegen
		model.getReeksen().stream().forEach(reeks -> {
			Discipline discipline = new Discipline();
			discipline.setNaam(reeks.getNaam());
			discipline.setRingNaam(reeks.getRingNaam());
			discipline.setExtensie(reeks.getExtensie());
			discipline.setDuur(reeks.getDuur());
			sf.getDisciplines().put(reeks.getNaam(), discipline);
		});

		//Ringen maken
		final AtomicInteger ringId = new AtomicInteger(0);
		data.stream().forEach(wizardRing -> {
			for (int i = 0; i < wizardRing.getAantalRingen(); i++) {
				String ringLetter = new String(Character.toChars('A' + i));
				if (wizardRing.getAantalRingen() < 2) ringLetter = "";
				Ring ring = new Ring(wizardRing.getNaam(), ringLetter, ringId.addAndGet(1));
				sf.getDisciplines().values().stream()
						.filter(disc -> disc.getRingNaam().equals(wizardRing.getNaam()))
						.forEach(ring::addDiscipline);
				sf.getRingen().add(ring);
			}
		});

		//inschrijvingen verwerken
		final AtomicInteger aantal = new AtomicInteger(0);
		//TODO: errors in Marshalling controlleren
		ArrayList<Groepsinschrijving> groepsinschrijvingen = Marshalling.importGroepsinschrijvingen(model.getFilename(),
				Marshalling.getActiveSheet(model.getFilename()), model.getColHeaders(),
				model.getColSportfeest(), model.getColAfdeling(), model.getColDiscipline(), model.getColAantal());
		groepsinschrijvingen.stream()
				.filter(groepsinschrijving -> groepsinschrijving.getSportfeest().equalsIgnoreCase(model.getSportfeest().getValue()))
				.forEach(inschr -> {
					Afdeling afdeling = sf.getAfdelingen().stream()
							.filter(afd -> inschr.getAfdeling().equals(afd.getNaam()))
							.findAny()
							.orElse(new Afdeling(inschr.getAfdeling()));

					for(int i = 0; i < inschr.getAantal(); i++) {  //aantal korpsen
						Inschrijving inschrijving = new Inschrijving();
						inschrijving.setAfdeling(afdeling);
						inschrijving.setId(aantal.getAndAdd(1));
						inschrijving.setDiscipline(sf.getDisciplines().get(inschr.getSport()));
						inschrijving.setMogelijkeRingen(sf.getRingen().stream()
								.filter(ring -> ring.getDisciplines().stream()
										.anyMatch(discipline -> discipline.getNaam().equals(inschr.getSport())))
								.collect(Collectors.toList())
						);
						if(inschrijving.getMogelijkeRingen().size() == 1)
							inschrijving.setRing(inschrijving.getMogelijkeRingen().get(0));
						if(inschr.getAantal() > 1) inschrijving.setKorps(i+1);
						afdeling.getInschrijvingen().add(inschrijving);
					}
					sf.getAfdelingen().add(afdeling);
				});

		if(dataCallback != null) dataCallback.accept(sf);
	}
}
