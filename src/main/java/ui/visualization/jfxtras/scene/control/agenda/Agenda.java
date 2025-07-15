/**
 * Copyright (c) 2011-2020, JFXtras
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the name of the organization nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ui.visualization.jfxtras.scene.control.agenda;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.util.Callback;
import ui.visualization.jfxtras.internal.scene.control.skin.agenda.AgendaSkinAbstract;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Agenda<H> extends Control {
	// ==================================================================================================================
	// CONSTRUCTOR

	public Agenda() { construct(); }

	private void construct() {
		// pref size
		setPrefSize(1000, 800);

		// setup the CSS
		this.getStyleClass().add(Agenda.class.getSimpleName());

		// appointments
		constructAppointments();
	}

	/**
	 * Return the path to the CSS file so things are setup right
	 */
	@Override
	public String getUserAgentStylesheet() {
		return getClass().getResource("/ui/controls/Agenda.css").toExternalForm();
	}

	@Override
	public Skin<?> createDefaultSkin() {
		return new AgendaSkinAbstract<>(this) {};
	}

	// ==================================================================================================================
	// PROPERTIES

	/**
	 * Appointments:
	 */
	private ListProperty<InschrijvingInterface> appointments() { return appointmentsProperty; }
	public final void setAppointmentsProperty(ObservableList<InschrijvingInterface> value) { appointmentsProperty.set(value); }
	public final ObservableList<InschrijvingInterface> getAppointmentsProperty() { return appointmentsProperty.get(); }
	public final ListProperty<InschrijvingInterface> appointmentsProperty = new SimpleListProperty<>(this, "itemAppointmentsProperty", FXCollections.observableArrayList());

	private void constructAppointments() {
		// when appointments are removed, they can't be selected anymore
		getAppointmentsProperty().addListener((ListChangeListener<InschrijvingInterface>) changes -> {
			while (changes.next()) {
				for (InschrijvingInterface lAppointment : changes.getRemoved())
					selectedAppointments.remove(lAppointment);
				runnables.forEach(Runnable::run);
			}
		});
	}

	public void addOnChangeListener(Runnable runnable) { this.runnables.add(runnable); }
	public void removeOnChangeListener(Runnable runnable) { this.runnables.remove(runnable); }
	private final List<Runnable> runnables = new ArrayList<>();

	public List<InschrijvingInterface> collectWholedayFor(H columnValue) {
		return getAppointmentsProperty().stream()
				.filter(i -> getItemIsHeaderFactory().call(i))
				.filter(i -> columnValue.equals(getItemColumnValueFactory().call(i)))
				.collect(Collectors.toList());
	}

	public List<InschrijvingInterface> collectRegularFor(H columnValue) {
		return getAppointmentsProperty().stream()
				.filter(i -> !getItemIsHeaderFactory().call(i))
				.filter(i -> columnValue.equals(getItemColumnValueFactory().call(i)))
				.collect(Collectors.toList());
	}

	public ObservableList<H> columns() { return columns; }
	final private ObservableList<H> columns = javafx.collections.FXCollections.observableArrayList();

	/**
	 * AllowDragging: allow appointments being dragged by the mouse
	 */
	public SimpleBooleanProperty allowDraggingProperty() { return allowDraggingObjectProperty; }
	final private SimpleBooleanProperty allowDraggingObjectProperty = new SimpleBooleanProperty(this, "allowDragging", true);
	public boolean getAllowDragging() { return allowDraggingObjectProperty.getValue(); }
	public void setAllowDragging(boolean value) { allowDraggingObjectProperty.setValue(value); }

	/**
	 * AllowResize: allow appointments to be resized using the mouse
	 */
	public SimpleBooleanProperty allowResizeProperty() { return allowResizeObjectProperty; }
	final private SimpleBooleanProperty allowResizeObjectProperty = new SimpleBooleanProperty(this, "allowResize", true);
	public boolean getAllowResize() { return allowResizeObjectProperty.getValue(); }
	public void setAllowResize(boolean value) { allowResizeObjectProperty.setValue(value); }

	private ObjectProperty<Callback<InschrijvingInterface, H>> itemColumnValueFactory() { return itemColumnValueFactoryProperty; }
	public final void setItemColumnValueFactory(Callback<InschrijvingInterface, H> value) { itemColumnValueFactoryProperty.set(value); }
	public final Callback<InschrijvingInterface, H> getItemColumnValueFactory() { return itemColumnValueFactoryProperty.get(); }
	public final ObjectProperty<Callback<InschrijvingInterface, H>> itemColumnValueFactoryProperty = new SimpleObjectProperty<>(this, "itemColumnValueFactory");

	private ObjectProperty<Callback<H, String>> columnValueFactory() { return columnValueFactoryProperty; }
	public final void setColumnValueFactory(Callback<H, String> value) { columnValueFactoryProperty.set(value); }
	public final Callback<H, String> getColumnValueFactory() { return columnValueFactoryProperty.get(); }
	public final ObjectProperty<Callback<H, String>> columnValueFactoryProperty = new SimpleObjectProperty<>(this, "columnValueFactory");

	private ObjectProperty<Callback<InschrijvingInterface, String>> itemValueFactory() { return itemValueFactoryProperty; }
	public final void setItemValueFactory(Callback<InschrijvingInterface, String> value) { itemValueFactoryProperty.set(value); }
	public final Callback<InschrijvingInterface, String> getItemValueFactory() { return itemValueFactoryProperty.get(); }
	public final ObjectProperty<Callback<InschrijvingInterface, String>> itemValueFactoryProperty = new SimpleObjectProperty<>(this, "itemValueFactory");

	private ObjectProperty<Callback<InschrijvingInterface, String>> itemColorFactory() { return itemColorFactoryProperty; }
	public final void setItemColorFactory(Callback<InschrijvingInterface, String> value) { itemColorFactoryProperty.set(value); }
	public final Callback<InschrijvingInterface, String> getItemColorFactory() { return itemColorFactoryProperty.get(); }
	public final ObjectProperty<Callback<InschrijvingInterface, String>> itemColorFactoryProperty = new SimpleObjectProperty<>(this, "itemColorFactory");

	private ObjectProperty<Callback<InschrijvingInterface, Boolean>> itemIsHeaderFactory() { return itemIsHeaderFactoryProperty; }
	public final void setItemIsHeaderFactory(Callback<InschrijvingInterface, Boolean> value) { itemIsHeaderFactoryProperty.set(value); }
	public final Callback<InschrijvingInterface, Boolean> getItemIsHeaderFactory() { return itemIsHeaderFactoryProperty.get(); }
	public final ObjectProperty<Callback<InschrijvingInterface, Boolean>> itemIsHeaderFactoryProperty = new SimpleObjectProperty<>(this, "itemIsHeaderFactory");

	/**
	 * selectedAppointments: a list of selected appointments
	 */
	public ObservableList<InschrijvingInterface> selectedAppointments() { return selectedAppointments; }
	final private ObservableList<InschrijvingInterface> selectedAppointments = javafx.collections.FXCollections.observableArrayList();

	/**
	 * editAppointmentCallback:
	 * Agenda has a default popup, but maybe you want to do something yourself.
	 * If so, you need to set this callback method and open your own window.
	 * Because Agenda does not dictate a event/callback in the implementation of appointment, it has no way of being informed of changes on the appointment.
	 * So when the custom edit is done, make sure that control gets updated, if this does not happen automatically through any of the existing listeners, then call refresh().
	 */
	public ObjectProperty<Callback<InschrijvingInterface, Void>> editAppointmentCallbackProperty() { return editAppointmentCallbackObjectProperty; }
	final private ObjectProperty<Callback<InschrijvingInterface, Void>> editAppointmentCallbackObjectProperty = new SimpleObjectProperty<>(this, "editAppointmentCallback", null);
	public Callback<InschrijvingInterface, Void> getEditAppointmentCallback() { return this.editAppointmentCallbackObjectProperty.getValue(); }
	public void setEditAppointmentCallback(Callback<InschrijvingInterface, Void> value) { this.editAppointmentCallbackObjectProperty.setValue(value); }

	/**
	 * appointmentChangedCallback:
	 * When an appointment is changed by Agenda (e.g. drag-n-drop to new time) change listeners will not fire.
	 * To enable the client to process those changes this callback can be used.  Additionally, for a repeatable
	 * appointment, this can be used to prompt the user if they want the change to occur to one, this-and-future
	 * or all events in series.
	 */
	public ObjectProperty<Callback<InschrijvingInterface, Void>> appointmentChangedCallbackProperty() { return appointmentChangedCallbackObjectProperty; }
	final private ObjectProperty<Callback<InschrijvingInterface, Void>> appointmentChangedCallbackObjectProperty = new SimpleObjectProperty<>(this, "appointmentChangedCallback", null);
	public Callback<InschrijvingInterface, Void> getAppointmentChangedCallback() { return this.appointmentChangedCallbackObjectProperty.getValue(); }
	public void setAppointmentChangedCallback(Callback<InschrijvingInterface, Void> value) { this.appointmentChangedCallbackObjectProperty.setValue(value); }

	/**
	 * actionCallback:
	 * This triggered when the action is called on an appointment, usually this is a double click
	 */
	public ObjectProperty<Callback<InschrijvingInterface, Void>> actionCallbackProperty() { return actionCallbackObjectProperty; }
	final private ObjectProperty<Callback<InschrijvingInterface, Void>> actionCallbackObjectProperty = new SimpleObjectProperty<>(this, "actionCallback", null);
	public Callback<InschrijvingInterface, Void> getActionCallback() { return this.actionCallbackObjectProperty.getValue(); }
	public void setActionCallback(Callback<InschrijvingInterface, Void> value) { this.actionCallbackObjectProperty.setValue(value); }

	/**
	 * Force the agenda to completely refresh itself
	 */
	public void refresh() {
		((AgendaSkinAbstract<?>) getSkin()).reconstruct();
	}

	@Override
	public String toString() {
		if (!columns.isEmpty()) {
			Class<?> clazz = columns.getFirst().getClass();
			return String.format("Agenda-%s", clazz.getSimpleName());
		}
		return "Agenda";
	}
}
