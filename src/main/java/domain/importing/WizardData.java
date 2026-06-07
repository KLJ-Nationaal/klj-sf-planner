package domain.importing;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.Date;

public class WizardData {

	private final StringProperty title = new SimpleStringProperty();
	private final StringProperty subtitle = new SimpleStringProperty();
	private final IntegerProperty colDansen = new SimpleIntegerProperty();
	private final IntegerProperty colPiramide = new SimpleIntegerProperty();
	private final IntegerProperty colAfdeling = new SimpleIntegerProperty();
	private final IntegerProperty colWimpelen = new SimpleIntegerProperty();
	private final IntegerProperty colVendelen = new SimpleIntegerProperty();
	private final IntegerProperty colTouwtrekken = new SimpleIntegerProperty();
	private final BooleanProperty colHeaders = new SimpleBooleanProperty();
	private String filename;
	private final StringProperty sfTitel = new SimpleStringProperty();
	private final Property<LocalDate> sfDatum = new SimpleObjectProperty<>();
	private ObservableList<Reeks> reeksen = FXCollections.observableArrayList();

	public String getTitle() { return title.get(); }
	public StringProperty titleProperty() { return title; }
	public void setTitle(String field1) { this.title.set(field1); }

	public String getSubtitle() { return subtitle.get(); }
	public StringProperty subtitleProperty() { return subtitle; }
	public void setSubtitle(String field2) { this.subtitle.set(field2); }

	public int getColAfdeling() { return colAfdeling.get(); }
	public IntegerProperty colAfdelingProperty() { return colAfdeling; }
	public void setColAfdeling(int colAfdeling) { this.colAfdeling.set(colAfdeling); }

	public int getColDansen() { return colDansen.get(); }
	public IntegerProperty colDansenProperty() { return colDansen; }
	public void setColDansen(int colDansen) { this.colDansen.set(colDansen); }

	public int getColPiramide() { return colPiramide.get(); }
	public IntegerProperty colPiramideProperty() { return colPiramide; }
	public void setColPiramide(int colPiramide) { this.colPiramide.set(colPiramide); }

	public int getColWimpelen() { return colWimpelen.get(); }
	public IntegerProperty colWimpelenProperty() { return colWimpelen; }
	public void setColWimpelen(int colWimpelen) { this.colWimpelen.set(colWimpelen); }

	public int getColVendelen() { return colVendelen.get(); }
	public IntegerProperty colVendelenProperty() { return colVendelen; }
	public void setColVendelen(int colVendelen) { this.colVendelen.set(colVendelen); }

	public int getColTouwtrekken() { return colTouwtrekken.get(); }
	public IntegerProperty colTouwtrekkenProperty() { return colTouwtrekken; }
	public void setColTouwtrekken(int colTouwtrekken) { this.colTouwtrekken.set(colTouwtrekken); }

	public boolean getColHeaders() { return colHeaders.get(); }
	public BooleanProperty colHeadersProperty() { return colHeaders; }
	public void setColHeaders(boolean colHeaders) { this.colHeaders.set(colHeaders); }

	public void reset() {
		title.set("");
		subtitle.set("");
	}

	public void setFilename(String filename) { this.filename = filename; }
	public String getFilename() { return filename; }

	public void setSfTitel(String sfTitel) { this.sfTitel.set(sfTitel); }
	public StringProperty sfTitelProperty() { return sfTitel; }
	public String getSfTitel() { return sfTitel.get(); }

	public void setSfDatum(Date sfDatum) { this.sfDatum.setValue(new java.sql.Date(sfDatum.getTime()).toLocalDate()); }
	public Property<LocalDate> sfDatumProperty() { return sfDatum; }
	public Date getSfDatum() { return (sfDatum.getValue() != null ? java.sql.Date.valueOf(sfDatum.getValue()) : null); }

	public void setReeksen(ObservableList<Reeks> reeksen) { this.reeksen = reeksen; }
	public ObservableList<Reeks> getReeksen() { return reeksen; }
}
