package ui.importer;

import ch.qos.logback.classic.Logger;
import com.google.inject.Inject;
import domain.*;
import domain.importing.Groepsinschrijving;
import domain.importing.Reeks;
import domain.importing.WizardData;
import domain.importing.WizardRing;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.LoggerFactory;
import persistence.Marshalling;
import persistence.ReeksDefinitie;
import ui.EditingCell;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class WizardImportRingenController extends WizardImportController{
	@FXML
	private TableView<WizardRing> tblRingen;

	@FXML
	private TableColumn<WizardRing, String> tblColRing;
	@FXML
	private TableColumn<WizardRing, Integer> tblColDuur, tblColAantalAfdelingen, tblColAantalRingen, tblColMaxPerRing;
	@FXML
	private TableColumn<WizardRing, Void> tblColAfdPerRing;

	@FXML
	private Button btnNext;

	@Inject
	WizardData model;

	private final ObservableList<WizardRing> data = FXCollections.observableArrayList();
	private final static Logger logger = (Logger) LoggerFactory.getLogger(Marshalling.class);

	@FXML
	public void initialize() {
		tblColRing.setCellValueFactory(new PropertyValueFactory<>("naam"));
		tblColAantalAfdelingen.setCellValueFactory(new PropertyValueFactory<>("aantalAfd"));
		tblColAfdPerRing.setCellFactory(column -> new TableCell<WizardRing, Void>() {
			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);

				if (empty || getTableRow() == null || getTableRow().getItem() == null) {
					setText(null);
				} else {
					WizardRing ring = (WizardRing) getTableRow().getItem();

					if (ring.getAantalRingen() > 0) {
						double resultaat = 1.0 * ring.getAantalAfd() / ring.getAantalRingen();
						setText(String.format("%.1f", resultaat));
					} else {
						setText("-");
					}
				}
			}
		});
		tblColMaxPerRing.setCellValueFactory(new PropertyValueFactory<>("maxAfdPerRing"));
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
		tblColDuur.setCellValueFactory(new PropertyValueFactory<>("duur"));
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
				tblRingen.refresh(); //update berekende kolommen
			}
		});
		tblColAantalRingen.setCellValueFactory(new PropertyValueFactory<>("aantalRingen"));

		tblRingen.setItems(data);
		tblRingen.getSelectionModel().setCellSelectionEnabled(true);
	}

	@Override
	public void activate(boolean fromPrevious){
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
					ring.setMaxAfdPerRing(Math.max(ring.getMaxAfdPerRing(),conf.getAfdPerRing()));
				});
		}
		//voorstel aantal ringen
		for(WizardRing ring : data) {
			ring.setAantalRingen((int) Math.ceil(1.0 * ring.getAantalAfd() / ring.getMaxAfdPerRing()));
			// als er toch finales zijn, verlaag aantal per ring
			if (ring.getAantalRingen() > 1) {
				ring.setMaxAfdPerRing((int) Math.floor(0.85 * ring.getMaxAfdPerRing()));
				// herbereken aantal ringen
				ring.setAantalRingen((int) Math.ceil(1.0 * ring.getAantalAfd() / ring.getMaxAfdPerRing()));
			}
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
		model.getReeksen().forEach(reeks -> {
			Discipline discipline = new Discipline();
			discipline.setNaam(reeks.getNaam());
			discipline.setRingNaam(reeks.getRingNaam());
			discipline.setExtensie(reeks.getExtensie());
			discipline.setDuur(reeks.getDuur());
			sf.getDisciplines().put(reeks.getNaam(), discipline);
		});

		//Ringen maken
		final AtomicInteger ringId = new AtomicInteger(0);
		data.forEach(wizardRing -> {
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
		// verbonden inschrijvingen instellen (voor piramides in verschillende disciplines
		sf.getAfdelingen().forEach(afdeling -> {
			List<Inschrijving> inschr = afdeling.getInschrijvingen().stream()
					.filter(inschrijving -> inschrijving.getDiscipline().getNaam().toLowerCase().contains("piramide"))
					.collect(Collectors.toList());
			if (inschr.size() > 2) {
				logger.error("{} heeft meer dan twee inschrijvingen voor de piramides!", afdeling.getNaam());
			}
			if (inschr.size() > 1 & inschr.stream().map(Inschrijving::getRing).count() > 1) {
				inschr.get(0).setVerbondenInschrijving(inschr.get(1));
				inschr.get(1).setVerbondenInschrijving(inschr.get(0));
			}

		});

		if(dataCallback != null) dataCallback.accept(sf);
	}
}
