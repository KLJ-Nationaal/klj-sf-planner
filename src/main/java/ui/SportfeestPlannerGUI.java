package ui;

import app.SportfeestPlannerService;
import ch.qos.logback.classic.Logger;
import com.google.inject.Guice;
import com.google.inject.Injector;
import domain.Afdeling;
import domain.Inschrijving;
import domain.Ring;
import domain.Sportfeest;
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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.*;
import javafx.util.Duration;
import javafx.util.Pair;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;
import org.slf4j.LoggerFactory;
import persistence.Marshalling;
import ui.importer.WizardImportController;
import ui.importer.WizardModule;
import ui.visualization.jfxtras.scene.control.agenda.InschrijvingInterface;

import java.io.File;
import java.io.IOException;
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
	private TableColumn tblColAfdeling, tblColDiscipline, tblColKorps, tblColRingnaam, tblColRingnummer;
	@FXML
	private Label txtStatusLabel;
	@FXML
	private ProgressBar prgStatusProgress;
	@FXML
	private MenuItem mnuStart, mnuStop, mnuExport, mnuSFSave, mnuScore;
	@FXML
	private AfdelingenController afdelingenController;
	@FXML
	private RingenController ringenController;
	@FXML
	private Tab tbInschrijvingen, tbLog, tbAfdelingen;

	private final ObservableList<Inschrijving> ringverdeling = FXCollections.observableArrayList();
	private SportfeestPlannerService sportfeestPlannerService;
	private final static Logger logger = (Logger) LoggerFactory.getLogger(SportfeestPlannerGUI.class);
	private final AtomicLong limiter = new AtomicLong(-1);
	private final Timeline progressUpdater = new Timeline(new KeyFrame(Duration.millis(500), e -> progressUpdate()));

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		SportfeestPlannerGUI.primaryStage = primaryStage;
		Parent root = FXMLLoader.load(getClass().getResource("/ui/Main.fxml"));
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		this.setTitle("");
		primaryStage.show();
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent e) {
				Platform.exit();
				System.exit(0);
			}
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
				setNewSportfeest(sf);
			} catch (IOException e) {
				logger.error(e.getLocalizedMessage());
			}
		}
	}

	private boolean isRingenVerdeeld(Sportfeest sf) {
		return ringverdeling.stream()
				.noneMatch(inschrijving -> inschrijving.getRing() == null);
	}

	private void setNewSportfeestChecked(Sportfeest sf) {
		setNewSportfeest(sf);
		if (!isRingenVerdeeld(sf)) {
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
		if (isRingenVerdeeld(sportfeestPlannerService.getSportfeest())) {
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
		fileChooser.setInitialFileName("uurschema");
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
					Ring leastUsedRing = inschrijvingen.get(0).getMogelijkeRingen().stream()
							.map(ring -> new AbstractMap.SimpleEntry<Ring, Long>(ring, ringverdeling.stream()
									.filter(inschr -> inschr.getDiscipline().getRingNaam().equals(inschrijvingen.get(0).getDiscipline().getRingNaam()))
									.filter(inschr -> inschr.getRing() != null)
									.collect(groupingBy(Inschrijving::getRing, Collectors.counting()))
									.getOrDefault(ring, 0L)))
							.min(Map.Entry.comparingByValue())
							.orElse(new AbstractMap.SimpleEntry<Ring, Long>(null, 0L))
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
			analyseResultaatController.setSportfeest(sportfeestPlannerService.getSportfeest(), sportfeestPlannerService.getScoreDirector());
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
	public void initialize() {
		tblColAfdeling.setCellValueFactory(inschr -> new SimpleStringProperty(((TableColumn.CellDataFeatures<Inschrijving, String>) inschr).getValue().getAfdeling().getNaam()));
		tblColDiscipline.setCellValueFactory(inschr -> new SimpleStringProperty(((TableColumn.CellDataFeatures<Inschrijving, String>) inschr).getValue().getDiscipline().getNaam()));
		tblColKorps.setCellValueFactory(inschr -> new SimpleIntegerProperty(((TableColumn.CellDataFeatures<Inschrijving, Integer>) inschr).getValue().getKorps()));
		tblColKorps.setCellFactory(col -> new TableCell<Inschrijving, Integer>() {
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
		tblColRingnaam.setCellValueFactory(inschr -> new SimpleStringProperty(((TableColumn.CellDataFeatures<Inschrijving, String>) inschr).getValue().getDiscipline().getRingNaam()));
		tblColRingnummer.setCellFactory(col -> new EditingCell<Inschrijving, String>() {
			@Override
			public void updateItemHandler(String str) {
				Inschrijving inschrijving = (Inschrijving) getTableRow().getItem();
				if (inschrijving != null) {
					if (inschrijving.getRing() == null) {
						this.setStyle("-fx-background-color: yellow;");
					} else {
						this.setStyle("-fx-background-color: lightgreen;");
					}
				}
			}
			@Override
			public void commitEditHandler(String newValue) {
				Inschrijving inschr = (Inschrijving) getTableRow().getItem();
				inschr.setRing(inschr.getMogelijkeRingen().stream()
						.filter(ring -> ring.getLetter().equals(newValue.trim()))
						.findAny().orElse(null)
				);
			}
		});
		tblColRingnummer.setCellValueFactory(inschr -> new SimpleStringProperty(
				Optional.ofNullable(
								((TableColumn.CellDataFeatures<Inschrijving, String>) inschr).getValue().getRing())
						.map(Ring::getLetter)
						.orElse("")
		));
		tblInschrijvingen.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				int row = tblInschrijvingen.getSelectionModel().getSelectedIndex();
				Platform.runLater(() -> tblInschrijvingen.edit(row, tblColRingnummer));
			}
		});

		tblInschrijvingen.setItems(ringverdeling);

		sportfeestPlannerService = new SportfeestPlannerService();
		sportfeestPlannerService.addSolverEventListener(bestSolutionChangedEvent -> {
			if (limiter.get() == -1) {
				limiter.set(0);
				bestSolutionChanged(bestSolutionChangedEvent);
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

	private void bestSolutionChanged(BestSolutionChangedEvent<Sportfeest> bestSolutionChangedEvent) {
		if (bestSolutionChangedEvent.isEveryProblemFactChangeProcessed()) {
			//TODO: optie om te updaten
			//final Sportfeest nSportfeest = (Sportfeest) bestSolutionChangedEvent.getNewBestSolution();
			Platform.runLater(() -> {
				txtStatusLabel.setText("Score " + bestSolutionChangedEvent.getNewBestScore().toString());
				progressUpdate();
				limiter.set(-1);
			});
		}
	}

	private void progressUpdate() {
		if (sportfeestPlannerService.isRunning()) {
			if (sportfeestPlannerService.getTimeScheduled() == Long.MAX_VALUE) {
				prgStatusProgress.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
			} else {
				prgStatusProgress.setProgress(sportfeestPlannerService.getTimeScheduled());
			}
		}
	}


}
