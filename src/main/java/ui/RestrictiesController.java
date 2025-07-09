package ui;

import domain.RestrictieInterface;
import domain.Sport;
import domain.importing.Reeks;
import domain.importing.RestrictieClass;
import domain.importing.RestrictieOptie;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import persistence.ReeksDefinitie;
import persistence.Restricties;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class RestrictiesController {
	@FXML
	public TableView<RestrictieOptie> tblRestricties;
	@FXML
	public TableColumn<RestrictieOptie, String> tblColAfdeling;
	@FXML
	public TableColumn<RestrictieOptie, RestrictieClass> tblColTypeA, tblColTypeB;
	@FXML
	public TableColumn<RestrictieOptie, RestrictieInterface> tblColValueA, tblColValueB;
	@FXML
	public TableColumn<RestrictieOptie, Boolean> tblColLevelA, tblColLevelB;
	@FXML
	public Button btnOK, btnCancel;

	protected Consumer<List<RestrictieOptie>> callback;

	private ObservableList<RestrictieOptie> restricties = FXCollections.observableArrayList();
	private ObservableList<Reeks> reeksen;
	private ObservableList<Sport> sporten;

	@FXML
	public void initialize() {
		reeksen = FXCollections.observableList(ReeksDefinitie.unMarshall());
		sporten = FXCollections.observableList(reeksen.stream()
				.map(Reeks::getSport)
				.distinct().sorted().toList());
		restricties = FXCollections.observableList(Restricties.unMarshall());
		if (restricties.isEmpty()) {
			// één nieuwe toevoegen
			RestrictieOptie r1 = new RestrictieOptie("(nieuw)", Sport.VENDELEN, false, Sport.DANS, false);
			restricties.add(r1);
		}

		tblColAfdeling.setCellValueFactory(new PropertyValueFactory<>("afdeling"));
		tblColAfdeling.setCellFactory(col -> new EditingCell<>() {
			@Override
			public void updateIndex(int i) {
				super.updateIndex(i);
				if (i >= 0) {
					this.getStyleClass().add("editable-cell");
				}
			}
			@Override
			public void commitEditHandler(String newValue) {
				RestrictieOptie r = getTableRow().getItem();
				r.setAfdeling(newValue);
			}
		});
		tblColTypeA.setCellValueFactory(x -> x.getValue().getA().typeProperty());
		tblColTypeA.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableList(Arrays.stream(RestrictieClass.values()).toList())));
		tblColTypeA.setOnEditCommit(event -> {
			if (!event.getNewValue().equals(event.getOldValue())) {
				if (event.getNewValue().equals(RestrictieClass.SPORT)) event.getRowValue().getA().setObject(sporten.getFirst());
				else event.getRowValue().getA().setObject(reeksen.getFirst());
			}
		});
		tblColValueA.setCellValueFactory(x -> x.getValue().aProperty());
		tblColValueA.setCellFactory(col -> new ComboBoxTableCell<>(new StringConverter<RestrictieInterface>() {
			@Override
			public String toString(RestrictieInterface object) {
				if (object == null) return null;
				return object.toString();
			}
			@Override
			public RestrictieInterface fromString(String string) {
				return null;
			}
		}) {
			@Override
			public void startEdit() {
				super.startEdit();
				if (isEditing()) {
					// Haal de restrictie voor de huidige rij op
					RestrictieOptie currentRestrictie = getTableRow().getItem();
					if (currentRestrictie != null && currentRestrictie.getA().getType() != null) {
						// Bepaal welke lijst getoond moet worden
						if (currentRestrictie.getA().getType() == RestrictieClass.SPORT) {
							this.getItems().setAll(sporten);
						} else {
							this.getItems().setAll(reeksen);
						}
					}
				}
			}
		});
		tblColLevelA.setCellValueFactory(x -> x.getValue().getA().alleRingenProperty());
		tblColLevelA.setCellFactory(CheckBoxTableCell.forTableColumn(tblColLevelA));
		tblColTypeB.setCellValueFactory(x -> x.getValue().getB().typeProperty());
		tblColTypeB.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableList(Arrays.stream(RestrictieClass.values()).toList())));
		tblColTypeB.setOnEditCommit(event -> {
			if (!event.getNewValue().equals(event.getOldValue())) {
				if (event.getNewValue().equals(RestrictieClass.SPORT)) event.getRowValue().getB().setObject(sporten.getFirst());
				else event.getRowValue().getB().setObject(reeksen.getFirst());
			}
		});
		tblColValueB.setCellValueFactory(x -> x.getValue().bProperty());
		tblColValueB.setCellFactory(col -> new ComboBoxTableCell<>(new StringConverter<RestrictieInterface>() {
			@Override
			public String toString(RestrictieInterface object) {
				if (object == null) return null;
				return object.toString();
			}
			@Override
			public RestrictieInterface fromString(String string) {
				return null;
			}
		}) {
			@Override
			public void startEdit() {
				super.startEdit();
				if (isEditing()) {
					// Haal de restrictie voor de huidige rij op
					RestrictieOptie currentRestrictie = getTableRow().getItem();
					if (currentRestrictie != null && currentRestrictie.getB().getType() != null) {
						// Bepaal welke lijst getoond moet worden
						if (currentRestrictie.getB().getType() == RestrictieClass.SPORT) {
							this.getItems().setAll(sporten);
						} else {
							this.getItems().setAll(reeksen);
						}
					}
				}
			}
		});
		tblColLevelB.setCellValueFactory(x -> x.getValue().getB().alleRingenProperty());
		tblColLevelB.setCellFactory(CheckBoxTableCell.forTableColumn(tblColLevelB));

		tblRestricties.setItems(restricties);
		tblRestricties.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	}

	@FXML
	public void AddAction(ActionEvent actionEvent) {
		tblRestricties.getItems().add(new RestrictieOptie("(nieuw)", Sport.DANS, false, Sport.DANS, false));
	}
	@FXML
	public void DeleteAction(ActionEvent actionEvent) {
		tblRestricties.getItems().removeAll(tblRestricties.getSelectionModel().getSelectedItems());
		tblRestricties.getSelectionModel().clearSelection();
	}
	@FXML
	public void OkAction(ActionEvent actionEvent) {
		Restricties.marshall(restricties.stream().sorted(Comparator.comparing(RestrictieOptie::getAfdeling)).toList());
		CancelAction(actionEvent);
		if (callback != null) callback.accept(restricties.stream().toList());
	}
	@FXML
	public void CancelAction(ActionEvent actionEvent) {
		Stage stage = (Stage) btnCancel.getScene().getWindow();
		stage.close();
	}

	public void setCallback(Consumer<List<RestrictieOptie>> callback) { this.callback = callback; }
}