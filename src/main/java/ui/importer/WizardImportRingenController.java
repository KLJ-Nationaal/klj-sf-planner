package ui.importer;

import ch.qos.logback.classic.Logger;
import com.google.inject.Inject;
import domain.*;
import domain.importing.Groepsinschrijving;
import domain.importing.Reeks;
import domain.importing.WizardData;
import domain.importing.WizardRing;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.LoggerFactory;
import persistence.Marshalling;
import persistence.ReeksDefinitie;
import ui.EditingCell;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WizardImportRingenController extends WizardImportController {
	@FXML
	private TableView<WizardRing> tblRingen;

	@FXML
	private TableColumn<WizardRing, String> tblColRing;
	@FXML
	private TableColumn<WizardRing, Integer> tblColDuur, tblColAantalAfdelingen, tblColAantalRingen, tblColMaxPerRing, tblColEinduur;
	@FXML
	private TableColumn<WizardRing, Void> tblColAfdPerRing;

	@FXML
	private Button btnNext;

	@Inject
	WizardData model;

	private final ObservableList<WizardRing> data = FXCollections.observableArrayList();
	private final static Logger logger = (Logger) LoggerFactory.getLogger(WizardImportRingenController.class);

	@FXML
	public void initialize() {
		tblColRing.setCellValueFactory(new PropertyValueFactory<>("naam"));
		tblColAantalAfdelingen.setCellValueFactory(new PropertyValueFactory<>("aantalAfd"));
		tblColAfdPerRing.setCellFactory(column -> new TableCell<>() {
			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);

				if (empty || getTableRow() == null || getTableRow().getItem() == null) {
					setText(null);
				} else {
					WizardRing ring = getTableRow().getItem();

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
		tblColDuur.setCellFactory(col -> new EditingCell<>() {
			@Override
			public void updateIndex(int i) {
				super.updateIndex(i);
				if (i >= 0) {
					this.getStyleClass().add("editable-cell");
				}
			}
			@Override
			public void commitEditHandler(Integer newValue) {
				WizardRing wizardRing = (WizardRing) getTableRow().getItem();
				wizardRing.setDuur(newValue);
			}
		});
		tblColDuur.setCellValueFactory(new PropertyValueFactory<>("duur"));
		tblColAantalRingen.setCellFactory(col -> new EditingCell<>() {
			@Override
			public void updateIndex(int i) {
				super.updateIndex(i);
				if (i >= 0) {
					this.getStyleClass().add("editable-cell");
				}
			}
			@Override
			public void commitEditHandler(Integer newValue) {
				WizardRing wizardRing = (WizardRing) getTableRow().getItem();
				wizardRing.setAantalRingen(newValue);
				tblRingen.refresh(); //update berekende kolommen
			}
		});
		tblColAantalRingen.setCellValueFactory(new PropertyValueFactory<>("aantalRingen"));

		tblColEinduur.setCellFactory(col -> new EditingCell<WizardRing, String>() {
			@Override
			public void updateIndex(int i) {
				super.updateIndex(i);
				if (i >= 0) {
					this.getStyleClass().add("editable-cell");
				}
			}
			@Override
			public void commitEditHandler(String newValue){
				WizardRing wizardRing = (WizardRing) getTableRow().getItem();
				final Pattern pattern = Pattern.compile("(\\d{1,2})\\D*?(\\d{2})");
				final Matcher matcher = pattern.matcher(newValue);
				if(matcher.find())
				{
					SimpleDateFormat df = new SimpleDateFormat("HH:mm");
					try {
						Date d1 = df.parse(Marshalling.STARTTIJD);
						Date d2 = df.parse(matcher.group(1) + ":" + matcher.group(2));
						TimeUnit timeUnit = TimeUnit.MINUTES;
						long diff = timeUnit.convert(d2.getTime() - d1.getTime(), TimeUnit.MILLISECONDS);
						wizardRing.setEinduur((int) diff);
					} catch (ParseException e) {
						logger.error("Kan nieuw einduur (" + newValue + ") niet instellen", e);
					}
				}
			}
		});
		tblColEinduur.setCellValueFactory(cellValue -> {
			SimpleDateFormat df = new SimpleDateFormat("HH:mm");
			Calendar cal = Calendar.getInstance();
			SimpleStringProperty formattedTime = new SimpleStringProperty("---");
			int einduur = ((TableColumn.CellDataFeatures<WizardRing, String>) cellValue).getValue().getEinduur();
			try {
				Date d = df.parse(Marshalling.STARTTIJD);
				cal.setTime(d);
				cal.add(Calendar.MINUTE, einduur);
				formattedTime.set(df.format(cal.getTime()));
			} catch (ParseException e) {
				logger.error("Kan einduur (" + einduur + ") niet tonen", e);
			}
			return formattedTime;
		});

		tblRingen.setItems(data);
		tblRingen.getSelectionModel().setCellSelectionEnabled(true);
	}

	@Override
	public void activate(boolean fromPrevious) {
		model.setTitle("Ringen instellen");
		model.setSubtitle("Vul hier het aantal ringen en minuten per ring in");

		data.clear();

		model.getReeksen().stream()
				.collect(Collectors.groupingBy(Reeks::getRingNaam, Collectors.summingInt(Reeks::getAantal)))
				.forEach((ring, aantal) -> data.add(new WizardRing(ring, aantal)));
		data.sort(Comparator.comparing(WizardRing::getNaam));
		//duur voor ring
		for (Reeks conf : ReeksDefinitie.unMarshall()) {
			data.stream()
					.filter(ring -> ring.getNaam().equalsIgnoreCase(conf.getRingNaam()))
					.forEach(ring -> {
						ring.setDuur(Math.max(ring.getDuur(), conf.getDuur()));
						ring.setMaxAfdPerRing(Math.max(ring.getMaxAfdPerRing(), conf.getAfdPerRing()));
					});
		}
		//voorstel aantal ringen
		for (WizardRing ring : data) {
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
	public boolean validate() {
		//aantal ringen groter dan 0
		if (!data.stream().allMatch(ring -> ring.getAantalRingen() > 0)) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Ringen");
			alert.setHeaderText("Te weinig ringen");
			alert.setContentText("Aantal ringen moet groter zijn dan 0 ");
			alert.showAndWait();
			return false;
		}
		//schema minuten groter dan 2
		if (!data.stream().allMatch(ring -> ring.getDuur() > 2)) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Ringen");
			alert.setHeaderText("Schema te kort");
			alert.setContentText("Alle schema's moeten minsten 3 minuten zijn");
			alert.showAndWait();
			return false;
		}
		return true;
	}

	@Submit
	public void submit() {
		Sportfeest sf = new Sportfeest();
		sf.setLocatie(model.getSfTitel());
		sf.setDatum(model.getSfDatum());

		// Disciplines toevoegen
		model.getReeksen().forEach(reeks -> {
			Discipline discipline = new Discipline();
			discipline.setNaam(reeks.getNaam().replace(" -16", "").replace(" +16", ""));
			discipline.setRingNaam(reeks.getRingNaam());
			discipline.setExtensie(reeks.getExtensie());
			discipline.setDuur(reeks.getDuur());
			sf.getDisciplines().put(reeks.getNaam(), discipline);
		});

		// Ringen maken
		final AtomicInteger ringId = new AtomicInteger(0);
		data.forEach(wizardRing -> {
			for (int i = 0; i < wizardRing.getAantalRingen(); i++) {
				String ringLetter = new String(Character.toChars('A' + i));
				if (wizardRing.getAantalRingen() < 2) ringLetter = "";
				Ring ring = new Ring(wizardRing.getNaam(), ringLetter, ringId.addAndGet(1), wizardRing.getEinduur());
				sf.getDisciplines().values().stream()
						.filter(disc -> disc.getRingNaam().equals(wizardRing.getNaam()))
						.forEach(ring::addDiscipline);
				sf.getRingen().add(ring);
			}
		});

		// inschrijvingen verwerken
		final AtomicInteger aantal = new AtomicInteger(0);
		// TODO: errors in Marshalling controleren
		ArrayList<Groepsinschrijving> groepsinschrijvingen = Marshalling.importGroepsinschrijvingen(model.getFilename(),
				Marshalling.getActiveSheet(model.getFilename()), model.getColHeaders(),
				model.getColSportfeest(), model.getColAfdeling(), model.getColDiscipline(), model.getColAantal());
		groepsinschrijvingen.stream()
				.filter(groepsinschrijving -> groepsinschrijving.getSportfeest().equalsIgnoreCase(model.getSportfeest().getValue()))
				// voor touwtrekken houden we geen rekening met leeftijd, dus verwijder dubbele (-16 & +16)
				.collect(Collectors.collectingAndThen(
						toCollection(() -> new TreeSet<>(
									Comparator.comparing(Groepsinschrijving::getAfdeling)
											.thenComparing(s -> s.getSport().replace(" -16", "").replace(" +16", "")))),
						ArrayList::new))
				.forEach(inschr -> {
					Afdeling afdeling = sf.getAfdelingen().stream()
							.filter(afd -> inschr.getAfdeling().equals(afd.getNaam()))
							.findAny()
							.orElse(new Afdeling(inschr.getAfdeling()));

					for (int i = 0; i < inschr.getAantal(); i++) {  //aantal korpsen
						Inschrijving inschrijving = new Inschrijving();
						String sport = inschr.getSport().replace(" -16", "").replace(" +16", "");
						inschrijving.setAfdeling(afdeling);
						inschrijving.setId(aantal.getAndAdd(1));
						sf.getDisciplines().values().stream()
								.filter(discipline -> discipline.getNaam().equals(sport))
								.findAny().ifPresent(inschrijving::setDiscipline);
						inschrijving.setMogelijkeRingen(sf.getRingen().stream()
								.filter(ring -> ring.getDisciplines().stream()
										.anyMatch(discipline -> discipline.getNaam().equals(sport)))
								.collect(Collectors.toList())
						);
						if (inschrijving.getMogelijkeRingen().size() == 1)
							inschrijving.setRing(inschrijving.getMogelijkeRingen().getFirst());
						inschrijving.setGereserveerdBlok(inschrijving.getDiscipline().isGereserveerdBlok());
						if (inschr.getAantal() > 1) inschrijving.setKorps(i + 1);
						afdeling.getInschrijvingen().add(inschrijving);
					}
					sf.getAfdelingen().add(afdeling);
				});
		// verbonden inschrijvingen instellen (voor piramides in verschillende disciplines
		sf.getAfdelingen().forEach(afdeling -> {
			List<Inschrijving> inschr = afdeling.getInschrijvingen().stream()
					.filter(inschrijving -> inschrijving.getDiscipline().getNaam().toLowerCase().contains("piramide"))
					.toList();
			if (inschr.size() > 2) {
				logger.error("{} heeft meer dan twee inschrijvingen voor de piramides!", afdeling.getNaam());
			}
			if (inschr.size() > 1 & inschr.stream().map(Inschrijving::getRing).count() > 1) {
				inschr.get(0).setVerbondenInschrijving(inschr.get(1));
				inschr.get(1).setVerbondenInschrijving(inschr.get(0));
			}

			// extra fix voor touwtrekken: verwijder meisjes/jongens indien gemengd aanwezig bij touwtrekken (of voeg gemengd toe)
			inschr = afdeling.getInschrijvingen().stream()
					.filter(inschrijving -> inschrijving.getDiscipline().getNaam().toLowerCase().contains("touwtrekken"))
					.collect(Collectors.toList());
			if (inschr.stream().anyMatch(inschrijving -> inschrijving.getDiscipline().isJongens())
					&& inschr.stream().anyMatch(inschrijving -> inschrijving.getDiscipline().isMeisjes())) {
				// er zijn jongens én meisjes ingeschreven: vervangen door één gemengd blok
				// check als er al een gemengde ring is
				boolean heeftGemengde = inschr.stream().filter(inschrijving -> inschrijving.getDiscipline().isMeisjes())
						.anyMatch(inschrijving -> inschrijving.getDiscipline().isJongens());
				if (!heeftGemengde) {
					// converteer jongens naar gemengd
					inschr.stream()
							.filter(inschrijving -> inschrijving.getDiscipline().isJongens())
							.filter(inschrijving -> !inschrijving.getDiscipline().isMeisjes())
							.forEach(inschrijving -> {
								Optional<Discipline> gemengde = sf.getDisciplines().values().stream()
										.filter(disc -> disc.getNaam().toLowerCase().contains("touwtrekken"))
										.filter(Discipline::isMeisjes)
										.filter(Discipline::isJongens)
										.findAny();
								gemengde.ifPresent(inschrijving::setDiscipline);
							});
				}
				// verwijder nu alle inschrijvingen met jongens of meisjes alleen
				List<Inschrijving> meisjesweg = inschr.stream()
						.filter(inschrijving -> inschrijving.getDiscipline().isMeisjes())
						.filter(inschrijving -> !inschrijving.getDiscipline().isJongens())
						.collect(Collectors.toList());
				afdeling.getInschrijvingen().removeAll(meisjesweg);
				List<Inschrijving> jongensweg = inschr.stream()
						.filter(inschrijving -> inschrijving.getDiscipline().isJongens())
						.filter(inschrijving -> !inschrijving.getDiscipline().isMeisjes())
						.collect(Collectors.toList());
				afdeling.getInschrijvingen().removeAll(jongensweg);
			}
		});

		if (dataCallback != null) dataCallback.accept(sf);
	}
}
