package ui;

import com.google.inject.Guice;
import com.google.inject.Injector;
import domain.Inschrijving;
import domain.Ring;
import domain.Sportfeest;
import javafx.application.Application;
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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import ui.importer.WizardImportController;
import ui.importer.WizardModule;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

public class SportfeestPlannerGUI extends Application {

	private Window primaryStage;

	@FXML
	private TableView tblInschrijvingen;

	@FXML
	private TableColumn tblColAfdeling, tblColDiscipline, tblColKorps, tblColRingnaam, tblColRingnummer;

	private final ObservableList<Inschrijving> data = FXCollections.observableArrayList();
	private Sportfeest sf;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		this.primaryStage = primaryStage;
		FXMLLoader loader = new FXMLLoader();
		Parent root = loader.load( getClass().getResource("/ui/Main.fxml"));
		Scene scene = new Scene(root);
		//TODO
		//JMetro jMetro = new JMetro(Style.LIGHT);
		//jMetro.setParent(root);
		//jMetro.setScene(scene);
		primaryStage.setScene( scene );
		primaryStage.setTitle("KLJ Sportfeest Planner");
		primaryStage.show();
	}

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
						(clazz) -> injector.getInstance(clazz));
				Parent root = loader.load();
				WizardImportController wizardImportController = loader.getController();
				wizardImportController.setDataCallback((Sportfeest sf) -> {
					data.clear();
					this.sf = sf;
					data.addAll(sf.getInschrijvingen());
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
		tblColRingnummer.setOnEditCommit(
				(EventHandler<TableColumn.CellEditEvent<Inschrijving, String>>) t -> {
					Inschrijving inschr = t.getTableView().getItems().get(t.getTablePosition().getRow() );
					inschr.setRing(inschr.getMogelijkeRingen().stream()
							.filter(ring -> ring.getLetter().equals(t.getNewValue().toString()))
							.findAny().orElse(null)
					);
				}
		);
		tblColRingnummer.setCellValueFactory(inschr -> new SimpleStringProperty(
				Optional.ofNullable(
						((TableColumn.CellDataFeatures<Inschrijving, String>) inschr).getValue().getRing())
						.map(Ring::getLetter)
						.orElse("")
		));
		tblInschrijvingen.setItems(data);
	}



	class EditingCell extends TableCell<Inschrijving, String> {

		private TextField textField;

		public EditingCell() {
		}

		@Override
		public void startEdit() {
			if (!isEmpty()) {
				super.startEdit();
				createTextField();
				setText(null);
				setGraphic(textField);
				textField.selectAll();
			}
		}

		@Override
		public void cancelEdit() {
			super.cancelEdit();

			setText((String) getItem());
			setGraphic(null);
		}

		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);

			if (empty) {
				setText(null);
				setGraphic(null);
			} else {
				if (((Inschrijving) getTableRow().getItem()).getMogelijkeRingen().stream()
						.anyMatch(ring -> ring.getLetter().equals(getString()))) {
					this.setStyle("-fx-my-cell-background: green;");
				} else if (((Inschrijving) getTableRow().getItem()).getMogelijkeRingen().size() == 1) {
					this.setStyle("-fx-my-cell-background: green;");
				} else {
					this.setStyle("-fx-my-cell-background: yellow;");
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
				}
			});
		}

		private String getString() {
			return getItem() == null ? "" : getItem().toString();
		}
	}

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
			comboBox = new ComboBox<>(FXCollections.observableArrayList(sf.getRingen().stream()
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
			return getItem() == null ? "" : getItem().toString();
		}
	}
/*	class ComboBoxCell extends TableCell<Inschrijving, String>
	{
		private ComboBox<String> comboBox = new ComboBox<>();

		@Override
		public void startEdit()
		{
			if ( !isEmpty() )
			{
				super.startEdit();

				comboBox.setItems( FXCollections.observableArrayList(sf.getRingen().stream()
						.filter(ring -> ring.getNaam().equals(((Inschrijving)getTableRow().getItem()).getDiscipline().getRingNaam()))
						.map(Ring::getLetter)
						.collect(Collectors.toList()) ));
				comboBox.getSelectionModel().select( getItem() );

				comboBox.focusedProperty().addListener((observable, oldValue, newValue) -> {
					if ( !newValue ) {
						commitEdit( comboBox.getSelectionModel().getSelectedItem() );
					}
				});

				setText( null );
				setGraphic( comboBox );
			}
		}

		@Override
		public void cancelEdit() {
			super.cancelEdit();
			setText( ( String ) getItem() );
			setGraphic( null );
		}

		@Override
		public void updateItem( String item, boolean empty ) {
			super.updateItem( item, empty );
			if ( empty ) {
				setText( null );
				setGraphic( null );
			} else {
				if ( isEditing() ) {
					setText( null );
					setGraphic( comboBox );
				} else {
					setText( getItem() );
					setGraphic( null );
				}
			}
		}

	}*/

}
