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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import jfxtras.util.NodeUtil;
import persistence.Marshalling;
import ui.visualization.jfxtras.scene.control.agenda.InschrijvingInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for rendering the appointments within a day 
 */
class DayBodyPane<H, I> extends Pane
{
	public DayBodyPane(H columnValue, LayoutHelp<H, I> layoutHints) {
		this.columnValueObjectProperty.set(columnValue);
		this.layoutHelp = layoutHints;
		construct();
	}
	final ObjectProperty<H> columnValueObjectProperty = new SimpleObjectProperty<>(this, "columnValue");
	final LayoutHelp layoutHelp;
	
	private void construct() {
		
		// for debugging setStyle("-fx-border-color:PINK;-fx-border-width:4px;");		
		getStyleClass().add("Day");
		
		// react to changes in the appointments
		layoutHelp.skinnable.addOnChangeListener(this::setupAppointments);
		setupAppointments();
		
		// change the layout related to the size
		widthProperty().addListener( (observable) -> {
			relayout();
		});
		heightProperty().addListener( (observable) -> {
			relayout();
		});

		setupMouseDrag();

		// for testing
		columnValueObjectProperty.addListener( (observable) -> setId("DayBody" + columnValueObjectProperty.get()));
		setId("DayBody" + columnValueObjectProperty.get());
	}
	
	/**
	 *
	 */
	private void setupMouseDrag() {

		// start new appointment
		setOnMousePressed((mouseEvent) -> {
			// only on primary
			if (!mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
				return;
			}

			if(!layoutHelp.divergentSelectedProperty.getValue().equals("")) {
				layoutHelp.divergentSelectedProperty.setValue("");
			}

			// this event should not be processed by the appointment area
			mouseEvent.consume();
			layoutHelp.skinnable.selectedAppointments().clear();
		});
	}

	/**
	 * The tracked panes are too complex to do via binding (unlike the wholeday flagpoles)
	 */
	private void relayout()
	{
		// prepare
		int lWholedayCnt = wholedayAppointmentBodyPanes.size();
		double lAllFlagpolesWidth = layoutHelp.wholedayAppointmentFlagpoleWidthProperty.get() * lWholedayCnt;
		double lDayWidth = layoutHelp.dayContentWidthProperty.get();
		double lRemainingWidthForAppointments = lDayWidth - lAllFlagpolesWidth;
		double lNumberOfPixelsPerMinute = layoutHelp.dayHeightProperty.get() / (Marshalling.TOTALETIJD + 4); //TODO: the 4 is needed because objects don't lineup otherwise
		
		// then add all tracked appointments (regular & task) to the day
		for (AppointmentAbstractTrackedPane lAppointmentAbstractTrackedPane : trackedAppointmentBodyPanes) {
			
			// for this pane specifically
			double lNumberOfTracks = lAppointmentAbstractTrackedPane.clusterOwner.clusterTracks.size();
			double lTrackWidth = lRemainingWidthForAppointments / lNumberOfTracks;
			double lTrackIdx = lAppointmentAbstractTrackedPane.clusterTrackIdx;
			
			// the X is determined by offsetting the wholeday appointments and then calculate the X of the track the appointment is placed in (available width / number of tracks) 
			double lX = lAllFlagpolesWidth + (lTrackWidth * lTrackIdx);
			lAppointmentAbstractTrackedPane.setLayoutX( NodeUtil.snapXY(lX));
			
			// the Y is determined by the start time in minutes projected onto the total day height (being 24 hours)
			int lStartOffsetInMinutes = lAppointmentAbstractTrackedPane.startDateTime;
			double lY = lNumberOfPixelsPerMinute * lStartOffsetInMinutes;
			lAppointmentAbstractTrackedPane.setLayoutY( NodeUtil.snapXY(lY) );

			// the width is the remaining width (subtracting the wholeday appointments) divided by the number of tracks in the cluster
			double lW = lTrackWidth;
			// all but the most right appointment get 50% extra width, so they underlap the next track 
			if (lTrackIdx < lNumberOfTracks - 1) {
				lW *= 1.75;
			}
			lAppointmentAbstractTrackedPane.setPrefWidth( NodeUtil.snapWH(lAppointmentAbstractTrackedPane.getLayoutX(), lW) );
			
			// the height is determined by the duration projected against the total dayHeight (being 24 hours)
			double lH;
			long lHeightInMinutes = lAppointmentAbstractTrackedPane.durationInMS / 1000;
			lH = lNumberOfPixelsPerMinute * lHeightInMinutes;

			// the height has a minimum size, in order to be able to render sensibly
			if (lH < 2 * layoutHelp.paddingProperty.get()) {
				lH = 2 * layoutHelp.paddingProperty.get();
			}
			lAppointmentAbstractTrackedPane.setPrefHeight( NodeUtil.snapWH(lAppointmentAbstractTrackedPane.getLayoutY(), lH) );
		}
	}			

	void setupAppointments() {
		setupWholedayAppointments();
		setupRegularAppointments();
		
		// place appointments in tracks
		trackedAppointmentBodyPanes.clear();
		trackedAppointmentBodyPanes.addAll(regularAppointmentBodyPanes);
		List<? extends AppointmentAbstractTrackedPane> determineTracks = AppointmentRegularBodyPane.determineTracks(trackedAppointmentBodyPanes);
		// add the appointments to the pane in the correct order, so they overlap nicely
		//getChildren().removeAll(determineTracks);
		getChildren().addAll(determineTracks);
		
		relayout();
	}
	final List<AppointmentAbstractTrackedPane> trackedAppointmentBodyPanes = new ArrayList<>();
	
	
	/**
	 * 
	 */
	private void setupWholedayAppointments() {
		wholedayAppointments.clear();
		wholedayAppointments.addAll( layoutHelp.skinnable.collectWholedayFor(columnValueObjectProperty.get()) );
		
		// remove all appointments
		getChildren().removeAll(wholedayAppointmentBodyPanes);
		wholedayAppointmentBodyPanes.clear();
		
		// for all wholeday appointments on this date, create a header appointment pane
		int lCnt = 0;
		for (InschrijvingInterface lAppointment : wholedayAppointments) {
			// create pane
			AppointmentWholedayBodyPane lAppointmentPane = new AppointmentWholedayBodyPane(columnValueObjectProperty, lAppointment, layoutHelp);
			wholedayAppointmentBodyPanes.add(lAppointmentPane);
			((AgendaSkinTimeScale24HourAbstract<InschrijvingInterface, H>) layoutHelp.skin).appointmentNodeMap().put(System.identityHashCode(lAppointment), lAppointmentPane);
			lAppointmentPane.setId(lAppointmentPane.getClass().getSimpleName() + columnValueObjectProperty.get() + "/" + lCnt); // for testing
			
			// position by binding
			lAppointmentPane.layoutXProperty().bind(NodeUtil.snapXY( layoutHelp.wholedayAppointmentFlagpoleWidthProperty.multiply(lCnt) ));
			lAppointmentPane.setLayoutY(0);
			lAppointmentPane.prefWidthProperty().bind(layoutHelp.wholedayAppointmentFlagpoleWidthProperty);
			lAppointmentPane.prefHeightProperty().bind(layoutHelp.dayHeightProperty);
			
			lCnt++;
		}
		getChildren().addAll(wholedayAppointmentBodyPanes);				
	}
	final private List<InschrijvingInterface> wholedayAppointments = new ArrayList<>();
	final private List<AppointmentWholedayBodyPane> wholedayAppointmentBodyPanes = new ArrayList<>();
	
	/**
	 * 
	 */
	private void setupRegularAppointments() {
		regularAppointments.clear();
		regularAppointments.addAll( layoutHelp.skinnable.collectRegularFor(columnValueObjectProperty.get()) );
		
		// remove all appointments
		getChildren().removeAll(regularAppointmentBodyPanes);
		regularAppointmentBodyPanes.clear();
		
		// for all regular appointments on this date, create a header appointment pane
		int lCnt = 0;
		for (InschrijvingInterface lAppointment : regularAppointments) {
			AppointmentRegularBodyPane lAppointmentPane = new AppointmentRegularBodyPane(columnValueObjectProperty, lAppointment, layoutHelp);
			regularAppointmentBodyPanes.add(lAppointmentPane);
            ((AgendaSkinTimeScale24HourAbstract<InschrijvingInterface, H>) layoutHelp.skin).appointmentNodeMap().put(System.identityHashCode(lAppointment), lAppointmentPane);
			lAppointmentPane.setId(lAppointmentPane.getClass().getSimpleName() + columnValueObjectProperty.get() + "/" + lCnt); // for testing
			
			lCnt++;
		}
		//getChildren().addAll(regularAppointmentBodyPanes);				
	}
	final private List<InschrijvingInterface> regularAppointments = new ArrayList<>();
	final private List<AppointmentRegularBodyPane> regularAppointmentBodyPanes = new ArrayList<>();

	int convertClickInSceneToDateTime(double x, double y) {
		Rectangle r = new Rectangle(NodeUtil.sceneX(this), NodeUtil.sceneY(this), this.getWidth(), this.getHeight());
		if (r.contains(x, y)) {
			double lHeightOffset = (y -  r.getY());
			return (int)(lHeightOffset * layoutHelp.durationInMSPerPixelProperty.get());
		}
		return -1;
	}
}

