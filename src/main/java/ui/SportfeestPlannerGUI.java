package ui;

import app.SportfeestPlannerService;
import ch.qos.logback.classic.Logger;
import com.google.inject.Guice;
import com.google.inject.Injector;
import domain.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import javafx.util.Pair;
import org.slf4j.LoggerFactory;
import persistence.Instellingen;
import persistence.Marshalling;
import persistence.Restricties;
import ui.importer.WizardImportController;
import ui.importer.WizardModule;
import ui.visualization.jfxtras.scene.control.agenda.InschrijvingInterface;
import util.RestrictieUtils;
import util.VersionInfo;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class SportfeestPlannerGUI extends Application {

	private static Window primaryStage;
	@FXML
	private TableView<Inschrijving> tblInschrijvingen;
	@FXML
	private TableColumn<Inschrijving, String> tblColAfdeling, tblColDiscipline, tblColRingnaam, tblColRingnummer;
	@FXML
	private TableColumn<Inschrijving, Integer> tblColKorps;
	@FXML
	private Label txtStatusLabel;
	@FXML
	private ProgressBar prgStatusProgress;
	@FXML
	private MenuItem mnuStart, mnuStop, mnuExport, mnuSFSave, mnuScore, mnuRestricties;
	@FXML
	private AfdelingenController afdelingenController;
	@FXML
	private RingenController ringenController;
	@FXML
	private Tab tbInschrijvingen, tbLog, tbAfdelingen, tbRingen;

	private final ObservableList<Inschrijving> ringverdeling = FXCollections.observableArrayList();
	private SportfeestPlannerService sportfeestPlannerService;
	private final static Logger logger = (Logger) LoggerFactory.getLogger(SportfeestPlannerGUI.class);
	private final AtomicLong limiter = new AtomicLong(-1);
	private final Timeline progressUpdater = new Timeline(new KeyFrame(Duration.millis(1000), e -> progressUpdate()));

	public static void main(String[] args) { launch(args); }

	@Override
	public void start(Stage primaryStage) throws IOException {
		logStartupInfo();
		Instellingen.load();
		SportfeestPlannerGUI.primaryStage = primaryStage;
		Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/ui/Main.fxml")));
		Scene scene = new Scene(root);
		scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("Main.css")).toExternalForm());
		primaryStage.setScene(scene);
		this.setTitle("");
		primaryStage.show();
		primaryStage.setOnCloseRequest(e -> {
			Instellingen.save();
			Platform.exit();
			System.exit(0);
		});
	}

	private void setTitle(String subtitle) {
		if (subtitle.isEmpty()) ((Stage) primaryStage).setTitle("KLJ Sportfeest Planner");
		else ((Stage) primaryStage).setTitle("KLJ Sportfeest Planner - " + subtitle);
	}

	@FXML
	public void SFOpen(ActionEvent actionEvent) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("XML-bestanden", "*.xml")
				, new FileChooser.ExtensionFilter("Alle bestanden", "*.*")
		);
		fileChooser.setInitialDirectory(new File("data/"));
		Window parentWindow = ((MenuItem) (actionEvent.getTarget())).getParentPopup().getOwnerWindow();
		File selectedFile = fileChooser.showOpenDialog(parentWindow);
		if (selectedFile != null) {
			try {
				Sportfeest sf = Marshalling.unmarshallXml(selectedFile.getCanonicalPath());
				setNewSportfeestChecked(sf);
			} catch (IOException e) {
				logger.error(e.getLocalizedMessage());
			}
		}
	}

	private boolean isRingenVerdeeld() {
		return ringverdeling.stream()
				.noneMatch(inschrijving -> {
					if (inschrijving.isGereserveerdBlok()) return (inschrijving.getTijdslot() == null);
					return (inschrijving.getRing() == null);
				});
	}

	private void setNewSportfeestChecked(Sportfeest sf) {
		setNewSportfeest(sf);
		try {
			List<Restrictie> restricties = Restricties.unMarshall();
			if (sf.getRestricties().isEmpty()) {
				RestrictieUtils.replaceRestricties(sf, restricties);
			} else if (!RestrictieUtils.compareRestricties(sf, restricties)) {
				Alert alert = new Alert(Alert.AlertType.WARNING);
				alert.setTitle("Uitzonderingen inlezen");
				alert.setHeaderText("Uitzonderingen zijn niet gelijk");
				alert.setContentText("De uitzonderingen ingelezen uit deze sportfeestmap komen niet overeen met de algemeen ingestelde uitzonderingen.\n"
						+ "Wilt u de de uitzonderingen uit de sportfeestmap behouden of de uitzonderingen overschrijven met de algemeen ingestelde uitzonderingen?");
				ButtonType buttonTypeMap = new ButtonType("Sportfeestmap behouden", ButtonBar.ButtonData.CANCEL_CLOSE);
				ButtonType buttonTypeAlg = new ButtonType("Algemene uitzonderingen nemen");
				alert.getButtonTypes().setAll(buttonTypeMap, buttonTypeAlg);

				Optional<ButtonType> result = alert.showAndWait();
				if (result.isPresent() && result.get() == buttonTypeAlg) {
					RestrictieUtils.replaceRestricties(sf, restricties);
				}
			}
		} catch (Exception e) {
			logger.error("Fout bij het toevoegen/controleren van de uitzonderingen: {}", e.getLocalizedMessage());
		}
		if (!isRingenVerdeeld()) {
			// als nodige ringen nog niet verdeeld zijn, vraag of dat moet gebeuren
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setTitle("Ringen verdelen");
			alert.setHeaderText("Nog niet alle ringen zijn verdeeld");
			alert.setContentText("Er zijn inschrijvingen die nog niet verdeeld zijn over de ringen. Dit nu automatisch doen?");
			ButtonType buttonTypeYes = new ButtonType("Ja");
			ButtonType buttonTypeNo = new ButtonType("Nee", ButtonBar.ButtonData.CANCEL_CLOSE);
			alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

			Optional<ButtonType> result = alert.showAndWait();
			if (result.isPresent() && result.get() == buttonTypeYes) {
				RingenInvullen(new ActionEvent());
			}
		}
	}

	private void setNewSportfeest(Sportfeest sf) {
		ringverdeling.clear();
		for (Afdeling afdeling : sf.getAfdelingen()) {
			ringverdeling.addAll(afdeling.getInschrijvingen());
		}
		afdelingenController.setSportfeest(sf);
		ringenController.setSportfeest(sf);
		sportfeestPlannerService.setSportfeest(sf);
		setTitle(sf.getLocatie() + " " + (new SimpleDateFormat("d/MM/yyyy")).format(sf.getDatum()));
	}

	@FXML
	public void SFSave(ActionEvent actionEvent) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("XML-bestanden", "*.xml")
				, new FileChooser.ExtensionFilter("Alle bestanden", "*.*")
		);
		fileChooser.setInitialDirectory(new File("data/"));
		fileChooser.setInitialFileName(sportfeestPlannerService.getSportfeest().getLocatie());
		Window parentWindow = ((MenuItem) (actionEvent.getTarget())).getParentPopup().getOwnerWindow();
		File selectedFile = fileChooser.showSaveDialog(parentWindow);
		if (selectedFile != null) {
			try {
				Marshalling.marshallXml(sportfeestPlannerService.getSportfeest(), selectedFile.getCanonicalPath());
			} catch (IOException e) {
				logger.error(e.getLocalizedMessage());
			}
		}
	}

	@FXML
	public void OpenImportExcelWizard(ActionEvent actionEvent) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("Excel-bestanden", "*.xlsx;*.xlsb;*.xlsm")
				, new FileChooser.ExtensionFilter("Alle bestanden", "*.*")
		);
		fileChooser.setInitialDirectory(new File("data/"));
		Window parentWindow = ((MenuItem) (actionEvent.getTarget())).getParentPopup().getOwnerWindow();
		File selectedFile = fileChooser.showOpenDialog(parentWindow);
		if (selectedFile != null) {
			try {
				final Injector injector = Guice.createInjector(new WizardModule());
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/WizardImport.fxml"),
						null,
						new JavaFXBuilderFactory(),
						injector::getInstance);
				Parent root = loader.load();
				WizardImportController wizardImportController = loader.getController();
				wizardImportController.setDataCallback(this::setNewSportfeestChecked);
				wizardImportController.setFilename(selectedFile.getCanonicalPath());
				Stage stage = new Stage();
				stage.setTitle("Importeer uit Excel");
				stage.setScene(new Scene(root));
				stage.initModality(Modality.WINDOW_MODAL);
				stage.initOwner(primaryStage);
				stage.show();
			} catch (IOException e) {
				logger.debug(e.getLocalizedMessage(), e);
			}
		}
	}

	@FXML
	public void StartOplossen(ActionEvent actionEvent) {
		if (isRingenVerdeeld()) {
			sportfeestPlannerService.start();
			tbLog.getTabPane().getSelectionModel().select(tbLog);
		} else {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Ringen niet verdeeld");
			alert.setHeaderText("Nog niet alle ringen zijn verdeeld");
			alert.setContentText("Nog niet alle inschrijvingen hebben een ring toegewezen gekregen!");

			alert.showAndWait();
		}
	}

	@FXML
	public void StopOplossen(ActionEvent actionEvent) {
		sportfeestPlannerService.cancel();
	}

	@FXML
	public void ExportExcel(ActionEvent actionEvent) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("Excel-bestanden", "*.xlsx")
				, new FileChooser.ExtensionFilter("Alle bestanden", "*.*")
		);
		fileChooser.setInitialDirectory(new File("data/"));
		fileChooser.setInitialFileName(sportfeestPlannerService.getSportfeest().getLocatie());
		Window parentWindow = ((MenuItem) (actionEvent.getTarget())).getParentPopup().getOwnerWindow();
		File selectedFile = fileChooser.showSaveDialog(parentWindow);
		if (selectedFile != null) {
			try {
				Marshalling.marshall(sportfeestPlannerService.getSportfeest(), selectedFile.getPath());
			} catch (Exception e) {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("Exporteren naar Excel");
				alert.setHeaderText("Fout bij Exporteren");
				alert.setContentText(e.getMessage());
				alert.showAndWait();
				logger.error("Exporteren naar Excel", e);
			}
		}
	}

	@FXML
	public void RingenInvullen(ActionEvent actionEvent) {
		logger.info("Ringen aanvullen...");
		ringverdeling.stream()
				.filter(inschrijving -> inschrijving.getMogelijkeRingen().size() > 1)
				.filter(inschrijving -> inschrijving.getRing() == null)
				.collect(groupingBy(inschrijving -> new Pair<>(inschrijving.getDiscipline(), inschrijving.getAfdeling()), LinkedHashMap::new, toList()))
				.values().stream()
				.sorted(Comparator.comparing((Function<List<Inschrijving>, Integer>) List::size).reversed())
				.forEach(inschrijvingen -> {
					Ring leastUsedRing = inschrijvingen.getFirst().getMogelijkeRingen().stream()
							.map(ring -> new AbstractMap.SimpleEntry<>(ring, ringverdeling.stream()
									.filter(inschr -> inschr.getDiscipline().getRingNaam().equals(inschrijvingen.getFirst().getDiscipline().getRingNaam()))
									.filter(inschr -> inschr.getRing() != null)
									.collect(groupingBy(Inschrijving::getRing, Collectors.counting()))
									.getOrDefault(ring, 0L)))
							.min(Map.Entry.comparingByValue())
							.orElse(new AbstractMap.SimpleEntry<>(null, 0L))
							.getKey();
					inschrijvingen.forEach(inschrijving -> {
						logger.debug("Ring {} toewijzen aan inschrijving {}", leastUsedRing, inschrijving);
						inschrijving.setRing(leastUsedRing);
					});
				});
		logger.info("Einde ringen aanvullen...");
		tblInschrijvingen.refresh();
	}

	@FXML
	public void AnalyseResultaat(ActionEvent actionEvent) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/AnalyseResultaat.fxml"));
			Parent root = loader.load();

			AnalyseResultaatController analyseResultaatController = loader.getController();
			analyseResultaatController.setSportfeest(sportfeestPlannerService.getSportfeest(), sportfeestPlannerService.getSolutionManager());
			Stage stage = new Stage();
			stage.setTitle("Score en analyse resultaat");
			stage.setScene(new Scene(root));
			stage.initModality(Modality.NONE);
			stage.initOwner(primaryStage);
			stage.show();
		} catch (IOException e) {
			logger.debug(e.getLocalizedMessage(), e);
		}
	}

	@FXML
	public void Restricties(ActionEvent actionEvent) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/Restricties.fxml"));
			Parent root = loader.load();

			RestrictiesController restrictiesController = loader.getController();
			restrictiesController.setCallback(ros -> RestrictieUtils.replaceRestricties(sportfeestPlannerService.getSportfeest(), ros));
			Stage stage = new Stage();
			stage.setTitle("Restricties");
			stage.setScene(new Scene(root));
			stage.initModality(Modality.NONE);
			stage.initOwner(primaryStage);
			stage.show();
		} catch (IOException e) {
			logger.debug(e.getLocalizedMessage(), e);
		}
	}

	@FXML
	public void initialize() {
		tblColAfdeling.setCellValueFactory(inschr -> new SimpleStringProperty(inschr.getValue().getAfdeling().getNaam()));
		tblColDiscipline.setCellValueFactory(inschr -> new SimpleStringProperty(inschr.getValue().getDiscipline().getNaam()));
		tblColKorps.setCellValueFactory(inschr -> new SimpleIntegerProperty(inschr.getValue().getKorps()).asObject());
		tblColKorps.setCellFactory(col -> new TableCell<>() {
			@Override
			protected void updateItem(Integer korps, boolean empty) {
				super.updateItem(korps, empty);
				if (empty) {
					setText(null);
				} else {
					setText(korps > 0 ? korps.toString() : "");
				}
			}
		});
		tblColRingnaam.setCellValueFactory(inschr -> new SimpleStringProperty(inschr.getValue().getDiscipline().getRingNaam()));
		tblColRingnummer.setCellFactory(col -> new EditingCell<>() {
			@Override
			public void updateItemHandler(String str) {
				Inschrijving inschrijving = getTableRow().getItem();
				if (inschrijving != null) {
					if (inschrijving.getRing() == null || (inschrijving.isGereserveerdBlok() && inschrijving.getTijdslot() == null)) {
						this.setStyle("-fx-background-color: yellow;");
					} else {
						this.setStyle("-fx-background-color: lightgreen;");
					}
				}
			}
			@Override
			public void commitEditHandler(String newValue) {
				Inschrijving inschr = getTableRow().getItem();
				if (inschr.isGereserveerdBlok()) {
					try {
						int intValue = Integer.parseInt(newValue);
						inschr.setTijdslot(inschr.getRing().getTijdslots().get(intValue));
					} catch (NumberFormatException | NullPointerException e) {
						//veronderstel dat we de cel leegmaken
						inschr.setTijdslot(null);
						logger.error("Kan tijdsslot " + newValue + " voor gereserveerd blok niet instellen. Inschrijving: " + inschr);
					}
				} else {
					inschr.setRing(inschr.getMogelijkeRingen().stream()
							.filter(ring -> ring.getLetter().equals(newValue.trim()))
							.findAny().orElse(null)
					);
				}
			}
		});
		tblColRingnummer.setCellValueFactory(inschr -> new SimpleStringProperty(
				Optional.ofNullable(inschr.getValue().getRing())
						.map(Ring::getLetter)
						.orElse("")
		));
		tblColRingnummer.setCellValueFactory(inschr -> {
			Inschrijving inschrijving = ((TableColumn.CellDataFeatures<Inschrijving, String>) inschr).getValue();
			if (inschrijving.isGereserveerdBlok()) {
				return new SimpleStringProperty(
						(inschrijving.getTijdslot() == null ? "" : String.valueOf(inschrijving.getTijdslots().indexOf(inschrijving.getTijdslot())))
				);
			}
			return new SimpleStringProperty(
					Optional.ofNullable(inschrijving.getRing())
							.map(Ring::getLetter)
							.orElse("")
			);
		});
		tblInschrijvingen.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				int row = tblInschrijvingen.getSelectionModel().getSelectedIndex();
				Platform.runLater(() -> tblInschrijvingen.edit(row, tblColRingnummer));
			}
		});

		tblInschrijvingen.setItems(ringverdeling);

		sportfeestPlannerService = new SportfeestPlannerService();
		sportfeestPlannerService.getScoreProperty().addListener((listener, oldScore, newScore) -> {
			if (limiter.get() == -1) {
				limiter.set(0);
				Platform.runLater(() -> {
					txtStatusLabel.setText("Score " + newScore.toString());
					progressUpdate();
					limiter.set(-1);
				});
			}
		});
		sportfeestPlannerService.setOnSucceeded(event -> {
			progressUpdater.stop();
			prgStatusProgress.setProgress(0);
			setNewSportfeest(sportfeestPlannerService.getSportfeest());
			AnalyseResultaat(new ActionEvent());
			txtStatusLabel.setText("Einde van de berekening");
			sportfeestPlannerService.reset();
		});
		sportfeestPlannerService.setOnCancelled(event -> {
			progressUpdater.stop();
			prgStatusProgress.setProgress(0);
			setNewSportfeest(sportfeestPlannerService.getSportfeest());
			AnalyseResultaat(new ActionEvent());
			txtStatusLabel.setText("Berekening gestopt");
			sportfeestPlannerService.reset();
		});
		sportfeestPlannerService.setOnFailed(event -> {
			progressUpdater.stop();
			prgStatusProgress.setProgress(0);
			txtStatusLabel.setText("Berekening gestopt (fout)");
			sportfeestPlannerService.reset();
		});
		sportfeestPlannerService.setOnRunning(event -> {
			progressUpdater.play();
			prgStatusProgress.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
			txtStatusLabel.setText("Berekening starten...");
		});
		mnuStart.disableProperty().bind(sportfeestPlannerService.runningProperty());
		mnuStop.disableProperty().bind(sportfeestPlannerService.runningProperty().not());
		mnuExport.disableProperty().bind(sportfeestPlannerService.getSportfeestProperty().isNull());
		mnuSFSave.disableProperty().bind(sportfeestPlannerService.getSportfeestProperty().isNull());
		mnuScore.disableProperty().bind(sportfeestPlannerService.getSportfeestProperty().isNull());
		progressUpdater.setCycleCount(Animation.INDEFINITE);
		ringenController.setAppointmentChangedCallback(this::refreshAgendas);
		afdelingenController.setAppointmentChangedCallback(this::refreshAgendas);
	}

	private Void refreshAgendas(InschrijvingInterface inschrijving) {
		System.out.println("Callback veranderd:" + inschrijving);
		ringenController.refresh();
		afdelingenController.refresh();
		return null;
	}

	private void progressUpdate() {
		if (sportfeestPlannerService.isRunning()) {
			Pair<Long, Long> progress = sportfeestPlannerService.getTimeScheduled();
			if (progress.getValue() == 0L) {
				prgStatusProgress.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
			} else {
				prgStatusProgress.setProgress((double) progress.getKey() / progress.getValue());
			}
		}
	}

	public static void logStartupInfo() {
		try {
			logger.info("SportfeestPlanner versie {}", VersionInfo.getVersion());
			// Java info
			logger.info("Java {} {} in {}", System.getProperty("java.version"),
					System.getProperty("java.vendor"), System.getProperty("java.home"));
			// JavaFX versie
			try {
				String javafxVersion = System.getProperty("javafx.version");
				if (javafxVersion != null) {
					logger.info("JavaFX version: {}", javafxVersion);
				} else {
					logger.info("JavaFX not found or not in use.");
				}
			} catch (Exception e) {
				logger.warn("Could not determine JavaFX version: {}", e.getMessage());
			}

			OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
			Runtime runtime = Runtime.getRuntime();
			long totalMemory = runtime.totalMemory();
			long maxMemory = runtime.maxMemory();
			long freeMemory = runtime.freeMemory();

			logger.info("OS {} ({}) {} cpu",
					System.getProperty("os.name"),
					System.getProperty("os.arch"),
					osBean.getAvailableProcessors());
			logger.info("JVM memory (used/total/max): {}/{}MB/{}MB",
					(totalMemory - freeMemory) / 1024 / 1024,
					totalMemory / 1024 / 1024,
					maxMemory / 1024 / 1024);

		} catch (Exception e) {
			logger.error("Failed to log system information", e);
		}
	}


}
