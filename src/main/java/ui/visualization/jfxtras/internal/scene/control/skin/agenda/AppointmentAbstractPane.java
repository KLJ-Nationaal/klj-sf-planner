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
package ui.visualization.jfxtras.internal.scene.control.skin.agenda;

import ch.qos.logback.classic.Logger;
import domain.Inschrijving;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;
import javafx.scene.Cursor;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.Pair;
import jfxtras.util.NodeUtil;
import org.slf4j.LoggerFactory;
import ui.visualization.jfxtras.scene.control.agenda.InschrijvingInterface;

import java.util.Comparator;
import java.util.Objects;

abstract class AppointmentAbstractPane<H> extends Pane {
	private final static Logger logger = (Logger) LoggerFactory.getLogger(AppointmentAbstractPane.class);

	AppointmentAbstractPane(InschrijvingInterface appointment, LayoutHelp<H> layoutHelp) {
		this.appointment = appointment;
		this.layoutHelp = layoutHelp;
		appointmentMenu = new AppointmentMenu<>(this, appointment, layoutHelp);

		// for debugging setStyle("-fx-border-color:PINK;-fx-border-width:1px;");
		getStyleClass().add("Appointment");
		color = "#AAAAAA";
		if (layoutHelp.skinnable.getItemColorFactory() != null) {
			color = layoutHelp.skinnable.getItemColorFactory().call(appointment);
		}
		setStyle("-fx-background-color: " + color + "; -fx-fill: " + color + ";");
		layoutHelp.divergentSelectedProperty.addListener((observable, oldValue, newValue) -> {
			Object col = layoutHelp.skinnable.getItemValueFactory().call(appointment);
			if (newValue.isEmpty() | col.equals(newValue)) {
				setStyle("-fx-background-color: " + color + "; -fx-fill: " + color + ";");
			} else {
				setStyle("-fx-background-color: #efefef; -fx-border-color: " + color + "; -fx-border-width: 1px; -fx-fill: efefef;");
			}
		});

		// tooltip
		if (layoutHelp.skinnable.getItemValueFactory() != null) {
			Tooltip.install(this, new Tooltip(layoutHelp.skinnable.getItemValueFactory().call(appointment)));
		}

		// dragging
		setupDragging();

		// react to changes in the selected appointments
		ListChangeListener<InschrijvingInterface> listChangeListener = changes -> setOrRemoveSelected();
		layoutHelp.skinnable.selectedAppointments().addListener(new WeakListChangeListener<>(listChangeListener));
	}

	final protected InschrijvingInterface appointment;
	final protected LayoutHelp<H> layoutHelp;
	final protected AppointmentMenu<H> appointmentMenu;
	protected String color;

	private void setOrRemoveSelected() {
		// remove class if not selected
		if (getStyleClass().contains(SELECTED) // visually selected
				&& !layoutHelp.skinnable.selectedAppointments().contains(appointment) // but no longer in the selected collection
		) {
			getStyleClass().remove(SELECTED);
		}

		// add class if selected
		if (!getStyleClass().contains(SELECTED) // visually not selected
				&& layoutHelp.skinnable.selectedAppointments().contains(appointment) // but still in the selected collection
		) {
			getStyleClass().add(SELECTED);
		}
	}

	private static final String SELECTED = "Selected";

	private void setupDragging() {
		// start drag
		setOnMousePressed((mouseEvent) -> {
			// action without select: middle button
			if (mouseEvent.getButton().equals(MouseButton.MIDDLE)) {
				handleAction();
				return;
			}
			// popup: right button
			if (mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
				appointmentMenu.showMenu(mouseEvent);
				return;
			}
			// only on primary
			if (!mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
				return;
			}

			// we handle this event
			mouseEvent.consume();

			// if this an action
			if (mouseEvent.getClickCount() > 1) {
				handleSelect(mouseEvent);
				return;
			}

			// is dragging allowed
			if (!layoutHelp.skinnable.getAllowDragging() || !appointment.isDraggable()) {
				handleSelect(mouseEvent);
				return;
			}

			// remember
			startX = mouseEvent.getX();
			startY = mouseEvent.getY();
			dragPickupDateTime = layoutHelp.skin.convertClickInSceneToDateTime(mouseEvent.getSceneX(), mouseEvent.getSceneY());
			mouseActuallyHasDragged = false;
			dragging = true;
		});

		// visualize dragging
		setOnMouseDragged((mouseEvent) -> {
			if (!dragging) return;

			// we handle this event
			mouseEvent.consume();

			// show the drag rectangle when we actually drag
			if (dragRectangle == null) {
				setCursor(Cursor.MOVE);
				// TODO: when dropping an appointment overlapping the day edge, the appointment is correctly(?) split in two. When dragging up such a splitted appointment the visualization does not match the actual time 
				dragRectangle = new Rectangle(0, 0, NodeUtil.snapWH(0, getWidth()), NodeUtil.snapWH(0, (appointment.isWholeDay() ? layoutHelp.titleDateTimeHeightProperty.get() : getHeight())));
				dragRectangle.getStyleClass().add("GhostRectangle");
				layoutHelp.dragPane.getChildren().add(dragRectangle);

				// place a text node at the bottom of the resize rectangle
				startTimeText = new Text("...");
				startTimeText.getStyleClass().add("GhostRectangleText");
				layoutHelp.dragPane.getChildren().add(startTimeText);

				endTimeText = new Text("...");
				endTimeText.getStyleClass().add("GhostRectangleText");
				layoutHelp.dragPane.getChildren().add(endTimeText);

				// we use a clone for calculating the current time during the drag
				appointmentForDrag = new AppointmentForDrag(appointment);
			}

			//double lX = NodeUtil.xInParent(this, layoutHelp.dragPane) + (mouseEvent.getX() - startX); // top-left of the original appointment pane + offset of drag
			double lX = NodeUtil.xInParent(this, layoutHelp.dragPane); // top-left of the original appointment pane + offset of drag
			//double lY = NodeUtil.yInParent(this, layoutHelp.dragPane) + (mouseEvent.getY() - startY); // top-left of the original appointment pane + offset of drag
			double lY = this.getLayoutY() + (mouseEvent.getY() - startY) + layoutHelp.headerHeightProperty.get();
			double lPaneView = this.getLayoutY() - NodeUtil.yInParent(this, layoutHelp.dragPane);

			// update the start time
			double lMinutes = (lY - dragRectangle.getHeight() / 2) / layoutHelp.hourHeightProperty.get() * 60;
			appointmentForDrag.setStartTime((int) lMinutes);
			appointmentForDrag.setWholeDay(lMinutes < 0);
			startTimeText.setText(appointmentForDrag.isWholeDay() ? "" : layoutHelp.formatTime(appointmentForDrag.getStartTime()));
			endTimeText.setText(appointmentForDrag.isWholeDay() ? "" : layoutHelp.formatTime(appointmentForDrag.getEndTime()));

			// move the drag rectangle
			dragRectangle.setX(lX);
			dragRectangle.setY((appointmentForDrag.getStartTime() * layoutHelp.hourHeightProperty.get() / 60) - lPaneView);
			startTimeText.layoutXProperty().set(dragRectangle.getX());
			startTimeText.layoutYProperty().set(dragRectangle.getY() - 3);
			endTimeText.layoutXProperty().set(dragRectangle.getX());
			endTimeText.layoutYProperty().set(dragRectangle.getY() + dragRectangle.getHeight() + endTimeText.getBoundsInParent().getHeight() - 3);
			mouseActuallyHasDragged = true;

			// determine start and end DateTime of the drag, if we're dragging between panes (TODO)
			Pair<H, Integer> dragTime = layoutHelp.skin.convertClickInSceneToDateTime(mouseEvent.getSceneX(), mouseEvent.getSceneY());
			if (dragTime != null) { // not dropped somewhere outside
				//TODO: handleDrag(appointmentForDrag, dragPickupDateTime, dragTime);
			}

		});

		// end drag
		setOnMouseReleased((mouseEvent) -> {
			if (!dragging) return;

			// we handle this event
			mouseEvent.consume();
			dragging = false;

			// if not dragged, then we're selecting
			if (!mouseActuallyHasDragged) {
				handleSelect(mouseEvent);
				return;
			}

			// determine start and end DateTime of the drag
			if (!Objects.equals(appointment.getTijdslot(), appointmentForDrag.getTijdslot())) {
				logger.info("Verplaatsing {} van {} naar {}", appointment, appointment.getTijdslot(), appointmentForDrag.getTijdslot());
				appointment.setTijdslot(appointmentForDrag.getTijdslot());
				//Pair<H,Integer> dragTime = layoutHelp.skin.convertClickInSceneToDateTime(mouseEvent.getSceneX(), mouseEvent.getSceneY());
				//if (dragTime != null) { // not dropped somewhere outside
				//handleDrag(appointment, dragPickupDateTime, dragTime);
				layoutHelp.callAppointmentChangedCallback(appointment);
				// relayout whole week
				layoutHelp.skin.setupAppointments();
			}

			// reset ui
			setCursor(Cursor.HAND);
			if (dragRectangle != null) {
				layoutHelp.dragPane.getChildren().remove(dragRectangle);
				layoutHelp.dragPane.getChildren().remove(startTimeText);
				layoutHelp.dragPane.getChildren().remove(endTimeText);
				dragRectangle = null;
				startTimeText = null;
				endTimeText = null;
				appointmentForDrag = null;
			}
		});
	}

	private boolean dragging = false;
	private Rectangle dragRectangle = null;
	private double startX = 0;
	private double startY = 0;
	private Pair<H, Integer> dragPickupDateTime;
	private boolean mouseActuallyHasDragged = false;
	private Text startTimeText = null;
	private Text endTimeText = null;
	private InschrijvingInterface appointmentForDrag = null;

	public static class AppointmentForDrag extends Inschrijving {
		public AppointmentForDrag(InschrijvingInterface inschrijving) {
			this.setStartTime(inschrijving.getStartTime());
			this.setAfdeling(inschrijving.getAfdeling());
			this.setDiscipline(inschrijving.getDiscipline());
			this.setId(inschrijving.getId());
			this.setTijdslot(inschrijving.getTijdslot());
			this.setKorps(inschrijving.getKorps());
			this.setMogelijkeRingen(inschrijving.getMogelijkeRingen());
			this.setVerbondenInschrijving(((Inschrijving) inschrijving).getVerbondenInschrijving());
			this.setRing(inschrijving.getRing());
		}
	}

	private void handleDrag(InschrijvingInterface appointment, Pair<H, Integer> dragPickupDateTime, Pair<H, Integer> dragDropDateTime) {

		// drag start
		boolean dragPickupInDayBody = dragInDayBody(dragPickupDateTime);
		boolean dragPickupInDayHeader = dragInDayHeader(dragPickupDateTime);
		//TODO: dragPickupDateTime = layoutHelp.roundTimeToNearestMinutes(dragPickupDateTime, roundToMinutes);

		// drag end
		boolean dragDropInDayBody = dragInDayBody(dragDropDateTime);
		boolean dragDropInDayHeader = dragInDayHeader(dragDropDateTime);
		//TODO: dragDropDateTime = layoutHelp.roundTimeToNearestMinutes(dragDropDateTime, roundToMinutes);

		// if dragged from day to day or header to header
		if ((dragPickupInDayBody && dragDropInDayBody) || (dragPickupInDayHeader && dragDropInDayHeader)) {
			// simply add the duration
			if (appointment.getTijdslot() != null) {
				appointment.setTijdslot(appointment.getClosestTijdslot(dragDropDateTime.getValue()));
			}
			//TODO: setRing for between ringen
			layoutHelp.callAppointmentChangedCallback(appointment);
		}

		// if dragged from day to header
		else if ((dragPickupInDayBody && dragDropInDayHeader)) {
			appointment.setTijdslot(null);
			//TODO: setRing for between ringen
			layoutHelp.callAppointmentChangedCallback(appointment);
		}

		// if dragged from header to day
		else if ((dragPickupInDayHeader && dragDropInDayBody)) {
			appointment.setTijdslot(
					appointment.getTijdslots().stream()
							.min(Comparator.comparingInt(i -> Math.abs(i.getStartTijd() - dragDropDateTime.getValue())))
							.orElse(appointment.getTijdslot())
			);
			//TODO: setRing for between ringen
			layoutHelp.callAppointmentChangedCallback(appointment);
		}
	}

	private void handleSelect(MouseEvent mouseEvent) {
		// if not shift pressed, clear the selection
		if (!mouseEvent.isShiftDown() && !mouseEvent.isControlDown()) {
			layoutHelp.skinnable.selectedAppointments().clear();
		}

		//double click
		if (mouseEvent.getClickCount() > 1) {
			if (layoutHelp.divergentSelectedProperty.getValue().equals(layoutHelp.skinnable.getItemValueFactory().call(appointment))) {
				layoutHelp.divergentSelectedProperty.setValue("");
			} else {
				layoutHelp.divergentSelectedProperty.setValue(layoutHelp.skinnable.getItemValueFactory().call(appointment));
			}
		}

		// add to selection if not already added
		if (!layoutHelp.skinnable.selectedAppointments().contains(appointment)) {
			layoutHelp.skinnable.selectedAppointments().add(appointment);
		}
		// pressing control allows to toggle
		else if (mouseEvent.isControlDown()) {
			layoutHelp.skinnable.selectedAppointments().remove(appointment);
		}
	}

	private void handleAction() {
		// has the client registered an action
		Callback<InschrijvingInterface, Void> lCallback = layoutHelp.skinnable.getActionCallback();
		if (lCallback != null) {
			lCallback.call(appointment);
		}
	}

	private boolean dragInDayBody(Pair<H, Integer> localDateTime) {
		return ((InschrijvingInterface) localDateTime.getKey()).getTijdslot() != null;
	}

	private boolean dragInDayHeader(Pair<H, Integer> localDateTime) {
		return ((InschrijvingInterface) localDateTime.getKey()).getTijdslot() == null;
	}

	public String toString() {
		return "appointment=" + appointment.getStartTime() + "-" + appointment.getEndTime()
				+ ";" + "summary=" + appointment;
	}
}
