/**
 * Copyright (c) 2011-2020, JFXtras
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *    Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    Neither the name of the organization nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
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
package ui.visualization.jfxtras.internal.scene.control.skin.agenda.base24hour;

import domain.Inschrijving;
import domain.Tijdslot;
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
import ui.visualization.jfxtras.scene.control.agenda.InschrijvingInterface;

import java.util.Comparator;
import java.util.function.Supplier;

abstract class AppointmentAbstractPane<H, I> extends Pane {

	AppointmentAbstractPane(InschrijvingInterface appointment, LayoutHelp layoutHelp)
	{
		this.appointment = appointment;
		this.layoutHelp = layoutHelp;
		appointmentMenu = new AppointmentMenu(this, appointment, layoutHelp);
		
		// for debugging setStyle("-fx-border-color:PINK;-fx-border-width:1px;");
		getStyleClass().add("Appointment");
		color = "#AAAAAA";
		if (layoutHelp.skinnable.getItemColorFactory() != null) {
			color = (String) layoutHelp.skinnable.getItemColorFactory().call(appointment);
		}
		setStyle("-fx-background-color: " + color + "; -fx-fill: " + color + ";");
		layoutHelp.divergentSelectedProperty.addListener((observable, oldValue, newValue) -> {
			Object col = layoutHelp.skinnable.getItemValueFactory().call(appointment);
			if(newValue.equals("") | col.equals(newValue) ) {
				setStyle("-fx-background-color: " + color + "; -fx-fill: " + color + ";");
			} else {
				setStyle("-fx-background-color: #efefef; -fx-border-color: " + color + "; -fx-border-width: 1px; -fx-fill: efefef;");
			}
		});

		// tooltip
		if (layoutHelp.skinnable.getItemValueFactory() != null) {
			Tooltip.install(this, new Tooltip((String) layoutHelp.skinnable.getItemValueFactory().call(appointment)));
		}
		
		// dragging
		setupDragging();
		
		// react to changes in the selected appointments
		layoutHelp.skinnable.selectedAppointments().addListener( new WeakListChangeListener<>(listChangeListener) );
	}
	final protected InschrijvingInterface appointment;
	final protected LayoutHelp layoutHelp;
	final protected AppointmentMenu appointmentMenu;
	final private ListChangeListener<InschrijvingInterface> listChangeListener = changes -> setOrRemoveSelected();
	protected String color;

	private void setOrRemoveSelected() {
		// remove class if not selected
		if ( getStyleClass().contains(SELECTED) // visually selected
		  && !layoutHelp.skinnable.selectedAppointments().contains(appointment) // but no longer in the selected collection
		) {
			getStyleClass().remove(SELECTED);
		}
		
		// add class if selected
		if ( !getStyleClass().contains(SELECTED) // visually not selected
		  && layoutHelp.skinnable.selectedAppointments().contains(appointment) // but still in the selected collection
		) {
			getStyleClass().add(SELECTED); 
		}
	}
	private static final String SELECTED = "Selected";
	
	private void setupDragging() {
		// start drag
		setOnMousePressed( (mouseEvent) -> {
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
		setOnMouseDragged( (mouseEvent) -> {
			if (!dragging) return;

			// we handle this event
			mouseEvent.consume();
			
			// show the drag rectangle when we actually drag
			if (dragRectangle == null) {
				setCursor(Cursor.MOVE);
				// TODO: when dropping an appointment overlapping the day edge, the appointment is correctly(?) split in two. When dragging up such a splitted appointment the visualization does not match the actual time 
				dragRectangle = new Rectangle(0, 0, NodeUtil.snapWH(0, getWidth()), NodeUtil.snapWH(0, (appointment.isWholeDay() ? layoutHelp.titleDateTimeHeightProperty.get() : getHeight())) );
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
				appointmentForDrag = new AppointmentForDrag();
			}
			
			// move the drag rectangle
			double lX = NodeUtil.xInParent(this, layoutHelp.dragPane) + (mouseEvent.getX() - startX); // top-left of the original appointment pane + offset of drag 
			double lY = NodeUtil.yInParent(this, layoutHelp.dragPane) + (mouseEvent.getY() - startY); // top-left of the original appointment pane + offset of drag 
			dragRectangle.setX(NodeUtil.snapXY(lX));
			dragRectangle.setY(NodeUtil.snapXY(lY));
			startTimeText.layoutXProperty().set(dragRectangle.getX()); 
			startTimeText.layoutYProperty().set(dragRectangle.getY()); 
			endTimeText.layoutXProperty().set(dragRectangle.getX()); 
			endTimeText.layoutYProperty().set(dragRectangle.getY() + dragRectangle.getHeight() + endTimeText.getBoundsInParent().getHeight()); 
			mouseActuallyHasDragged = true;
			
			// update the start time
			appointmentForDrag.setStartTime(appointment.getStartTime());
			appointmentForDrag.setWholeDay(appointment.isWholeDay());
			// determine start and end DateTime of the drag
			Pair<H,Integer> dragTime = layoutHelp.skin.convertClickInSceneToDateTime(mouseEvent.getSceneX(), mouseEvent.getSceneY());
			if (dragTime != null) { // not dropped somewhere outside
				handleDrag(appointmentForDrag, dragPickupDateTime, dragTime);
				startTimeText.setText(appointmentForDrag.isWholeDay() ? "" : layoutHelp.formatTime(appointmentForDrag.getStartTime()));
				endTimeText.setText(appointmentForDrag.isWholeDay() ? "" : layoutHelp.formatTime(appointmentForDrag.getEndTime()));
			}
			
		});

		// end drag
		setOnMouseReleased((mouseEvent) -> {
			if (!dragging) return;
			
			// we handle this event
			mouseEvent.consume();
			dragging = false;

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
			
			// if not dragged, then we're selecting
			if (!mouseActuallyHasDragged) {
				handleSelect(mouseEvent);
				return;
			}
			
			// determine start and end DateTime of the drag
			Pair<H,Integer> dragTime = layoutHelp.skin.convertClickInSceneToDateTime(mouseEvent.getSceneX(), mouseEvent.getSceneY());
			if (dragTime != null) { // not dropped somewhere outside
				handleDrag(appointment, dragPickupDateTime, dragTime);

				// relayout whole week
				layoutHelp.skin.setupAppointments();
			}
		});
	}
	private boolean dragging = false;
	private Rectangle dragRectangle = null;
	private double startX = 0;
	private double startY = 0;
	private Pair<H,Integer> dragPickupDateTime;
	private boolean mouseActuallyHasDragged = false;
	private Text startTimeText = null;
	private Text endTimeText = null;
	private InschrijvingInterface appointmentForDrag = null;

	public static class AppointmentForDrag extends Inschrijving {}

	private void handleDrag(InschrijvingInterface appointment, Pair<H,Integer> dragPickupDateTime, Pair<H,Integer> dragDropDateTime) {
		
		// drag start
		boolean dragPickupInDayBody = dragInDayBody(dragPickupDateTime);
		boolean dragPickupInDayHeader = dragInDayHeader(dragPickupDateTime);
		//TODO: dragPickupDateTime = layoutHelp.roundTimeToNearestMinutes(dragPickupDateTime, roundToMinutes);
		
		// drag end
		boolean dragDropInDayBody = dragInDayBody(dragDropDateTime);
		boolean dragDropInDayHeader = dragInDayHeader(dragDropDateTime);
		//TODO: dragDropDateTime = layoutHelp.roundTimeToNearestMinutes(dragDropDateTime, roundToMinutes);

		// if dragged from day to day or header to header
		if ( (dragPickupInDayBody && dragDropInDayBody) || (dragPickupInDayHeader && dragDropInDayHeader)) {
			// simply add the duration
			if (appointment.getTijdslot() != null) {
				appointment.setTijdslot(
					appointment.getTijdslots().stream()
						.min(Comparator.comparingInt(i -> Math.abs(i.getStartTijd() - dragDropDateTime.getValue())))
						.orElseGet((Supplier<? extends Tijdslot>) appointment.getTijdslot())
				);
			}
			//TODO: setRing for between ringen
			layoutHelp.callAppointmentChangedCallback(appointment);
		}
		
		// if dragged from day to header
		else if ( (dragPickupInDayBody && dragDropInDayHeader) ) {
			appointment.setTijdslot(null);
			//TODO: setRing for between ringen
			layoutHelp.callAppointmentChangedCallback(appointment);
		}
		
		// if dragged from header to day
		else if ( (dragPickupInDayHeader && dragDropInDayBody) ) {
			appointment.setTijdslot(
					appointment.getTijdslots().stream()
							.min(Comparator.comparingInt(i -> Math.abs(i.getStartTijd() - dragDropDateTime.getValue())))
							.orElseGet((Supplier<? extends Tijdslot>) appointment.getTijdslot())
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
			if(layoutHelp.divergentSelectedProperty.getValue().equals(layoutHelp.skinnable.getItemValueFactory().call(appointment))) {
				layoutHelp.divergentSelectedProperty.setValue("");
			} else {
				layoutHelp.divergentSelectedProperty.setValue((String)layoutHelp.skinnable.getItemValueFactory().call(appointment));
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

	private boolean dragInDayBody(Pair<H,Integer> localDateTime) {
		return ((InschrijvingInterface)localDateTime.getKey()).getTijdslot() != null;
	}
	
	private boolean dragInDayHeader(Pair<H,Integer> localDateTime) {
		return ((InschrijvingInterface)localDateTime.getKey()).getTijdslot() == null;
	}
	
	public String toString()
	{
		return "appointment=" + appointment.getStartTime() + "-" + appointment.getEndTime()
		     + ";" + "summary=" + appointment.toString();
	}
}
