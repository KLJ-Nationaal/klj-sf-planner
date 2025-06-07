package ui;

import domain.Inschrijving;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.util.Callback;
import ui.visualization.jfxtras.scene.control.agenda.Agenda;
import ui.visualization.jfxtras.scene.control.agenda.InschrijvingInterface;

public abstract class AgendaController<H> {
	@FXML
	protected Agenda<H, Inschrijving> agenda;

	public ObjectProperty<Callback<InschrijvingInterface, Void>> appointmentChangedCallbackProperty() { return appointmentChangedCallbackObjectProperty; }
	final protected ObjectProperty<Callback<InschrijvingInterface, Void>> appointmentChangedCallbackObjectProperty = new SimpleObjectProperty<>(this, "appointmentChangedCallback", null);
	public Callback<InschrijvingInterface, Void> getAppointmentChangedCallback() { return this.appointmentChangedCallbackObjectProperty.getValue(); }
	public void setAppointmentChangedCallback(Callback<InschrijvingInterface, Void> value) { this.appointmentChangedCallbackObjectProperty.setValue(value); }

	public void refresh(){
		agenda.refresh();
	}
}
