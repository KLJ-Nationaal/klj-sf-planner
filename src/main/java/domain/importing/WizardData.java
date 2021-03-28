package domain.importing;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.Date;

public class WizardData {

    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty subtitle = new SimpleStringProperty();
    private final IntegerProperty colSportfeest = new SimpleIntegerProperty();
    private final IntegerProperty colAfdeling = new SimpleIntegerProperty();
    private final IntegerProperty colDiscipline = new SimpleIntegerProperty();
    private final IntegerProperty colAantal = new SimpleIntegerProperty();
    private final BooleanProperty colHeaders = new SimpleBooleanProperty();
    private String filename;
    private final StringProperty sportfeest = new SimpleStringProperty();
    private final StringProperty sfTitel = new SimpleStringProperty();
    private final Property<LocalDate> sfDatum = new SimpleObjectProperty<>();
    private ObservableList<Reeks> reeksen = FXCollections.observableArrayList();

    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public void setTitle(String field1) {
        this.title.set(field1);
    }

    public String getSubtitle() {
        return subtitle.get();
    }

    public StringProperty subtitleProperty() {
        return subtitle;
    }

    public void setSubtitle(String field2) {
        this.subtitle.set(field2);
    }

    public int getColSportfeest() {
        return colSportfeest.get();
    }

    public IntegerProperty colSportfeestProperty() {
        return colSportfeest;
    }

    public void setColSportfeest(int colSportfeest) {
        this.colSportfeest.set(colSportfeest);
    }

    public int getColAfdeling() {
        return colAfdeling.get();
    }

    public IntegerProperty colAfdelingProperty() {
        return colAfdeling;
    }

    public void setColAfdeling(int colAfdeling) {
        this.colAfdeling.set(colAfdeling);
    }

    public int getColDiscipline() {
        return colDiscipline.get();
    }

    public IntegerProperty colDisciplineProperty() {
        return colDiscipline;
    }

    public void setColDiscipline(int colDiscipline) {
        this.colDiscipline.set(colDiscipline);
    }

    public int getColAantal() {
        return colAantal.get();
    }

    public IntegerProperty colAantalProperty() {
        return colAantal;
    }

    public void setColAantal(int colAantal) {
        this.colAantal.set(colAantal);
    }

    public boolean getColHeaders() {
        return colHeaders.get();
    }

    public BooleanProperty colHeadersProperty() {
        return colHeaders;
    }

    public void setColHeaders(boolean colHeaders) {
        this.colHeaders.set(colHeaders);
    }

    public void reset() {
        title.set("");
        subtitle.set("");
    }

    public void setFilename(String filename) { this.filename = filename; }

    public String getFilename() { return filename; }

    public void setSfTitel(String sfTitel) { this.sfTitel.set(sfTitel); }

    public StringProperty sfTitelProperty() {
        return sfTitel;
    }

    public String getSfTitel() { return sfTitel.get(); }

    public void setSportfeest(String sportfeest) { this.sportfeest.set(sportfeest); }

    public StringProperty sportfeestProperty() { return sportfeest; }

    public StringProperty getSportfeest() { return sportfeest; }

    public void setSfDatum(Date sfDatum) { this.sfDatum.setValue(new java.sql.Date(sfDatum.getTime()).toLocalDate()); }

    public Property<LocalDate> sfDatumProperty() { return sfDatum; }

    public Date getSfDatum() { return (sfDatum.getValue() != null ? java.sql.Date.valueOf(sfDatum.getValue()) : null); }

    public void setReeksen(ObservableList<Reeks> reeksen) { this.reeksen = reeksen; }

    public ObservableList<Reeks> getReeksen() { return reeksen; }
}
