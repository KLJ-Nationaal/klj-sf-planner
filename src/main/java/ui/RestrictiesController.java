package ui;

import domain.Restrictie;
import domain.RestrictieClass;
import domain.RestrictieInterface;
import domain.Sport;
import domain.importing.Reeks;
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
	public TableView<Restrictie> tblRestricties;
	@FXML
	public TableColumn<Restrictie, String> tblColAfdeling;
	@FXML
	public TableColumn<Restrictie, RestrictieClass> tblColTypeA, tblColTypeB;
	@FXML
	public TableColumn<Restrictie, RestrictieInterface> tblColValueA, tblColValueB;
	@FXML
	public TableColumn<Restrictie, Boolean> tblColLevelA, tblColLevelB;
	@FXML
	public Button btnOK, btnCancel;

	protected Consumer<List<Restrictie>> callback;

	private ObservableList<Restrictie> restricties = FXCollections.observableArrayList();
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
			Restrictie r1 = new Restrictie("(nieuw)", Sport.VENDELEN, false, Sport.DANS, false);
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
				Restrictie r = getTableRow().getItem();
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
		tblColValueA.setCellFactory(col -> new ComboBoxTableCell<>(new StringConverter<>() {
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
					Restrictie currentRestrictie = getTableRow().getItem();
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
		tblColLevelA.setCellValueFactory(x -> x.getValue().getA().alleKorpsenProperty());
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
		tblColValueB.setCellFactory(col -> new ComboBoxTableCell<>(new StringConverter<>() {
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
					Restrictie currentRestrictie = getTableRow().getItem();
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
		tblColLevelB.setCellValueFactory(x -> x.getValue().getB().alleKorpsenProperty());
		tblColLevelB.setCellFactory(CheckBoxTableCell.forTableColumn(tblColLevelB));

		tblRestricties.setItems(restricties);
		tblRestricties.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	}

	@FXML
	public void AddAction(ActionEvent actionEvent) {
		tblRestricties.getItems().add(new Restrictie("(nieuw)", Sport.DANS, false, Sport.DANS, false));
	}
	@FXML
	public void DeleteAction(ActionEvent actionEvent) {
		tblRestricties.getItems().removeAll(tblRestricties.getSelectionModel().getSelectedItems());
		tblRestricties.getSelectionModel().clearSelection();
	}
	@FXML
	public void OkAction(ActionEvent actionEvent) {
		Restricties.marshall(restricties.stream().sorted(Comparator.comparing(Restrictie::getAfdeling)).toList());
		CancelAction(actionEvent);
		if (callback != null) callback.accept(restricties.stream().toList());
	}
	@FXML
	public void CancelAction(ActionEvent actionEvent) {
		Stage stage = (Stage) btnCancel.getScene().getWindow();
		stage.close();
	}

	public void setCallback(Consumer<List<Restrictie>> callback) { this.callback = callback; }
}