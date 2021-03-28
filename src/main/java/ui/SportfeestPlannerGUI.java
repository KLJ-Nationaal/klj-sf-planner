package ui;

import app.SportfeestPlannerService;
import ch.qos.logback.classic.Logger;
import com.google.inject.Guice;
import com.google.inject.Injector;
import domain.Afdeling;
import domain.Inschrijving;
import domain.Ring;
import domain.Sportfeest;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.*;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;
import org.slf4j.LoggerFactory;
import persistence.Marshalling;
import ui.importer.WizardImportController;
import ui.importer.WizardModule;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class SportfeestPlannerGUI extends Application {

	private Window primaryStage;
	@FXML
	private TableView<Inschrijving> tblInschrijvingen;
	@FXML
	private TableColumn tblColAfdeling, tblColDiscipline, tblColKorps, tblColRingnaam, tblColRingnummer;
	@FXML
	private Label txtStatusLabel;
	@FXML
	private ProgressBar prgStatusProgress;
	@FXML
	private MenuItem mnuStart, mnuStop;
	@FXML
	private AfdelingenController afdelingenController;
	@FXML
	private Tab tbInschrijvingen, tbLog, tbAfdelingen;

	private final ObservableList<Inschrijving> data = FXCollections.observableArrayList();
	private SportfeestPlannerService sportfeestPlannerService;
	private final static Logger logger = (Logger) LoggerFactory.getLogger(SportfeestPlannerGUI.class);
	private final AtomicLong limiter = new AtomicLong(-1);

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		this.primaryStage = primaryStage;
		FXMLLoader loader = new FXMLLoader();
		Parent root = FXMLLoader.load( getClass().getResource("/ui/Main.fxml"));
		Scene scene = new Scene(root);
		//TODO
		/*JMetro jMetro = new JMetro(Style.LIGHT);
		jMetro.setParent(root);
		jMetro.setScene(scene);*/
		primaryStage.setScene( scene );
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
		if(subtitle == "") ((Stage)primaryStage).setTitle("KLJ Sportfeest Planner");
		else ((Stage)primaryStage).setTitle("KLJ Sportfeest Planner - " + subtitle);
	}

	@FXML
	public void SFOpen(ActionEvent actionEvent){
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("XML-bestanden", "*.xml")
				,new FileChooser.ExtensionFilter("Alle bestanden", "*.*")
		);
		fileChooser.setInitialDirectory(new File("data/"));
		Window parentWindow = ((MenuItem)(actionEvent.getTarget())).getParentPopup().getOwnerWindow();
		File selectedFile = fileChooser.showOpenDialog(parentWindow);
		if(selectedFile != null) {
			try {
				Sportfeest sf = Marshalling.unmarshallXml(selectedFile.getCanonicalPath());
				afdelingenController.setSportfeest(sf);
				sportfeestPlannerService.setSportfeest(sf);
				setTitle(sf.getLocatie() + " " + (new SimpleDateFormat("d/MM/yyyy")).format(sf.getDatum()));
			} catch (IOException e) {
				logger.error(e.getLocalizedMessage());
			}
		}
	}

	@FXML
	public void SFSave(ActionEvent actionEvent){
		if(sportfeestPlannerService.getSportfeest() != null) {
			FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().addAll(
					new FileChooser.ExtensionFilter("XML-bestanden", "*.xml")
					,new FileChooser.ExtensionFilter("Alle bestanden", "*.*")
			);
			fileChooser.setInitialDirectory(new File("data/"));
			Window parentWindow = ((MenuItem)(actionEvent.getTarget())).getParentPopup().getOwnerWindow();
			File selectedFile = fileChooser.showSaveDialog(parentWindow);
			if(selectedFile != null) {
				try {
					Marshalling.marshallXml(sportfeestPlannerService.getSportfeest(), selectedFile.getCanonicalPath());
				} catch (IOException e) {
					logger.error(e.getLocalizedMessage());
				}
			}
		} else {
			logger.error("Sportfeest is nog niet geinitialiseerd of opgelost");
		}
	}

	@FXML
	public void OpenImportExcelWizard(ActionEvent actionEvent) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("Excel-bestanden", "*.xlsx;*.xlsb;*.xlsm")
				,new FileChooser.ExtensionFilter("Alle bestanden", "*.*")
		);
		fileChooser.setInitialDirectory(new File("data/"));
		Window parentWindow = ((MenuItem)(actionEvent.getTarget())).getParentPopup().getOwnerWindow();
		File selectedFile = fileChooser.showOpenDialog(parentWindow);
		if(selectedFile != null) {
			try {
				final Injector injector = Guice.createInjector( new WizardModule() );
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/WizardImport.fxml"),
						null,
						new JavaFXBuilderFactory(),
						injector::getInstance);
				Parent root = loader.load();
				WizardImportController wizardImportController = loader.getController();
				wizardImportController.setDataCallback((Sportfeest sf) -> {
					data.clear();
					sportfeestPlannerService.setSportfeest(sf);
					for(Afdeling afdeling : sf.getAfdelingen()){
						data.addAll(afdeling.getInschrijvingen());
					}
					setTitle(sf.getLocatie() + " " + (new SimpleDateFormat("d/MM/yyyy")).format(sf.getDatum()));
				});
				wizardImportController.setFilename(selectedFile.getCanonicalPath());
				Stage stage = new Stage();
				stage.setTitle("Importeer uit Excel");
				stage.setScene(new Scene(root));
				stage.initModality(Modality.WINDOW_MODAL);
				stage.initOwner(primaryStage);
				stage.show();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@FXML
	public void StartOplossen(ActionEvent actionEvent) {
		//TODO: controleer of alle nodige ringen ingevuld zijn
		sportfeestPlannerService.start();
		tbLog.getTabPane().getSelectionModel().select(tbLog);
	}

	@FXML
	public void StopOplossen(ActionEvent actionEvent) {
		sportfeestPlannerService.cancel();
	}

	@FXML
	public void RingenInvullen(ActionEvent actionEvent) {
		//TODO: zelfde afdelingen, zelfde ring. nok!
		logger.info("Ringen aanvullen...");
		data.stream()
				.sorted(Comparator.comparingInt(Inschrijving::getKorps).reversed())
				.filter(inschrijving -> inschrijving.getMogelijkeRingen().size() > 1)
				.filter(inschrijving -> inschrijving.getRing() == null)
				.forEach(inschrijving -> {
					Ring leastUsedRing = inschrijving.getMogelijkeRingen().stream()
							.map(ring -> new AbstractMap.SimpleEntry<Ring, Long>(ring, data.stream()
									.filter(inschr -> inschr.getDiscipline().getRingNaam().equals(inschrijving.getDiscipline().getRingNaam()))
									.filter(inschr -> inschr.getRing() != null)
									.collect(Collectors.groupingBy(Inschrijving::getRing, Collectors.counting()))
									.getOrDefault(ring, 0L)))
							.min(Map.Entry.comparingByValue())
							.orElse(new AbstractMap.SimpleEntry<Ring, Long>(null, 0L))
							.getKey();
					logger.debug("Ring " + leastUsedRing + " toewijzen aan inschrijving " + inschrijving);
					inschrijving.setRing(leastUsedRing);
				});
		logger.info("Einde ringen aanvullen...");
		tblInschrijvingen.refresh();
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
					setText(korps.intValue() > 0 ? korps.toString() : "");
				}
			}
		});
		tblColRingnaam.setCellValueFactory(inschr -> new SimpleStringProperty(((TableColumn.CellDataFeatures<Inschrijving, String>) inschr).getValue().getDiscipline().getRingNaam()));
		tblColRingnummer.setCellFactory(param -> new EditingCell());
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

		tblInschrijvingen.setItems(data);

		sportfeestPlannerService = new SportfeestPlannerService();
		sportfeestPlannerService.addSolverEventListener(bestSolutionChangedEvent -> {
			if (limiter.get() == -1) {
				limiter.set(0);
				bestSolutionChanged(bestSolutionChangedEvent);
			}
		});
		sportfeestPlannerService.setOnSucceeded(event -> {
			prgStatusProgress.setProgress(0);
			txtStatusLabel.setText("Einde van de berekening");
			sportfeestPlannerService.reset();
		});
		sportfeestPlannerService.setOnCancelled(event -> {
			prgStatusProgress.setProgress(0);
			txtStatusLabel.setText("Berekening gestopt");
			sportfeestPlannerService.reset();
		});
		sportfeestPlannerService.setOnFailed(event -> {
			prgStatusProgress.setProgress(0);
			txtStatusLabel.setText("Berekening gestopt (fout)");
			sportfeestPlannerService.reset();
		});
		sportfeestPlannerService.setOnRunning(event -> {
			prgStatusProgress.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
			txtStatusLabel.setText("Berekening starten...");
		});
		mnuStart.disableProperty().bind(sportfeestPlannerService.runningProperty());
		mnuStop.disableProperty().bind(sportfeestPlannerService.runningProperty().not());
	}

	private void bestSolutionChanged(BestSolutionChangedEvent bestSolutionChangedEvent) {
		if(bestSolutionChangedEvent.isEveryProblemFactChangeProcessed()) {
			final Sportfeest nSportfeest = (Sportfeest) bestSolutionChangedEvent.getNewBestSolution();
			Platform.runLater(() -> {
				txtStatusLabel.setText("Score " + bestSolutionChangedEvent.getNewBestScore().toString());
				if(bestSolutionChangedEvent.getTimeMillisSpent() > 0) {
					if(sportfeestPlannerService.getTimeScheduled() == Long.MAX_VALUE) {
						prgStatusProgress.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
					} else {
						prgStatusProgress.setProgress(bestSolutionChangedEvent.getTimeMillisSpent() / sportfeestPlannerService.getTimeScheduled());
					}
				}
				limiter.set(-1);
			});
		}
	}


	class EditingCell extends TableCell<Inschrijving, String> {

		private TextField textField;

		public EditingCell() {}

		@Override
		public void startEdit() {
			if (!isEmpty()) {
				super.startEdit();
				createTextField();
				setText(null);
				setGraphic(textField);
				textField.requestFocus();
				textField.selectAll();
			}
		}

		@Override
		public void commitEdit(String newValue) {
			super.commitEdit(newValue);

			Inschrijving inschr = (Inschrijving) getTableRow().getItem();
			inschr.setRing(inschr.getMogelijkeRingen().stream()
					.filter(ring -> ring.getLetter().equals(newValue))
					.findAny().orElse(null)
			);

			setText(getItem());
			setGraphic(null);
		}

		@Override
		public void cancelEdit() {
			super.cancelEdit();

			setText(getItem());
			setGraphic(null);
		}

		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);

			if (empty) {
				setText(null);
				setGraphic(null);
			} else {
				Inschrijving inschrijving = (Inschrijving) getTableRow().getItem();
				if (inschrijving.getRing() == null) {
					this.setStyle("-fx-background-color: yellow;");
				} else {
					this.setStyle("-fx-background-color: lightgreen;");
				}
				if (isEditing()) {
					if (textField != null) {
						textField.setText(getString());
					}
					setText(null);
					setGraphic(textField);
				} else {
					setText(getString());
					setGraphic(null);
				}
			}
		}

		private void createTextField() {
			textField = new TextField(getString());
			textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()* 2);
			textField.focusedProperty().addListener(new ChangeListener<Boolean>(){
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0,
				                    Boolean arg1, Boolean arg2) {
					if (!arg2) {
						commitEdit(textField.getText());
					}
					//getTableView().refresh();
				}
			});
			textField.setOnKeyPressed(new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent t) {
					if (t.getCode() == KeyCode.ENTER) {
						commitEdit(textField.getText());
					} else if (t.getCode() == KeyCode.TAB) {
						EditingCell.this.getTableView().requestFocus();//why does it lose focus??
						EditingCell.this.getTableView().getSelectionModel().selectBelowCell();
					} else if (t.getCode() == KeyCode.ESCAPE) {
						cancelEdit();
					}
				}
			});
			/*textField.setOnKeyReleased(new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent t) {
					if (t.getCode().isDigitKey()) {
						if (CellField.isLessOrEqualOneSym()) {
							CellField.addSymbol(t.getText());
						} else {
							CellField.setText(textField.getText());
						}
						textField.setText(CellField.getText());
						textField.deselect();
						textField.end();
						textField.positionCaret(textField.getLength() + 2);//works sometimes

					}
				}
			});*/
		}

		private String getString() {
			return getItem() == null ? "" : getItem();
		}
	}

	//TODO
	public class ComboBoxCell<I, S> extends TableCell<Inschrijving, String> {

		private ComboBox<String> comboBox;

		public ComboBoxCell() {}

		@Override
		public void startEdit() {
			super.startEdit();

			if (comboBox == null) {
				createComboBox();
			}

			setGraphic(comboBox);
			setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		}

		@Override
		public void cancelEdit() {
			super.cancelEdit();

			setText(String.valueOf(getItem()));
			setContentDisplay(ContentDisplay.TEXT_ONLY);
		}

		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);

			if (empty) {
				setText(null);
				setGraphic(null);
			} else {
				if (isEditing()) {
					if (comboBox != null) {
						comboBox.setValue(getString());
					}
					setGraphic(comboBox);
					setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				} else {
					setText(getString());
					setContentDisplay(ContentDisplay.TEXT_ONLY);
				}
			}
		}

		private void createComboBox() {
			// ClassesController.getLevelChoice() is the observable list of String
			comboBox = new ComboBox<>(FXCollections.observableArrayList(sportfeestPlannerService.getSportfeest().getRingen().stream()
					.filter(ring -> ring.getNaam().equals(((Inschrijving)getTableRow().getItem()).getDiscipline().getRingNaam()))
					.map(Ring::getLetter)
					.collect(Collectors.toList()) ));
			comboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap()*2);
			comboBox.setOnKeyPressed(new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent t) {
					if (t.getCode() == KeyCode.ENTER) {
						commitEdit(comboBox.getSelectionModel().getSelectedItem());
					} else if (t.getCode() == KeyCode.ESCAPE) {
						cancelEdit();
					}
				}
			});
		}

		private String getString() {
			return getItem() == null ? "" : getItem();
		}

	}
}
