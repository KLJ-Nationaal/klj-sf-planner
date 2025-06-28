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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import persistence.Instellingen;
import ui.visualization.jfxtras.scene.control.agenda.InschrijvingInterface;
import ui.visualization.jfxtras.util.NodeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for rendering the appointments within a day
 */
public class DayBodyPane<H> extends Pane {
	public DayBodyPane(H columnValue, LayoutHelp<H> layoutHints) {
		this.columnValueObjectProperty.set(columnValue);
		this.layoutHelp = layoutHints;
		construct();
	}

	final ObjectProperty<H> columnValueObjectProperty = new SimpleObjectProperty<>(this, "columnValue");
	final LayoutHelp<H> layoutHelp;

	private void construct() {

		// for debugging setStyle("-fx-border-color:PINK;-fx-border-width:4px;");		
		getStyleClass().add("Day");

		// react to changes in the appointments
		//layoutHelp.skinnable.addOnChangeListener(this::setupAppointments);
		setupAppointments();

		// change the layout related to the size
		widthProperty().addListener((observable) -> relayout());
		heightProperty().addListener((observable) -> relayout());

		setupMouseDrag();

		// for testing
		columnValueObjectProperty.addListener((observable) -> setId("DayBody" + columnValueObjectProperty.get()));
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

			if (!layoutHelp.divergentSelectedProperty.getValue().isEmpty()) {
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
	private void relayout() {
		// prepare
		int lWholedayCnt = wholedayAppointmentBodyPanes.size();
		double lAllFlagpolesWidth = layoutHelp.wholedayAppointmentFlagpoleWidthProperty.get() * lWholedayCnt;
		double lDayWidth = layoutHelp.dayContentWidthProperty.get();
		double lRemainingWidthForAppointments = lDayWidth - lAllFlagpolesWidth;
		double lNumberOfPixelsPerMinute = layoutHelp.dayHeightProperty.get() / (Instellingen.Opties().TOTALETIJD + 4); //TODO: the 4 is needed because objects don't lineup otherwise

		// then add all tracked appointments (regular & task) to the day
		for (AppointmentAbstractTrackedPane<H> lAppointmentAbstractTrackedPane : trackedAppointmentBodyPanes) {

			// for this pane specifically
			double lNumberOfTracks = lAppointmentAbstractTrackedPane.clusterOwner.clusterTracks.size();
			double lTrackWidth = lRemainingWidthForAppointments / lNumberOfTracks;
			double lTrackIdx = lAppointmentAbstractTrackedPane.clusterTrackIdx;

			// the X is determined by offsetting the wholeday appointments and then calculate the X of the track the appointment is placed in (available width / number of tracks) 
			double lX = lAllFlagpolesWidth + (lTrackWidth * lTrackIdx);
			lAppointmentAbstractTrackedPane.setLayoutX(NodeUtil.snapXY(lX));

			// the Y is determined by the start time in minutes projected onto the total day height (being 24 hours)
			int lStartOffsetInMinutes = lAppointmentAbstractTrackedPane.startDateTime;
			double lY = lNumberOfPixelsPerMinute * lStartOffsetInMinutes;
			lAppointmentAbstractTrackedPane.setLayoutY(NodeUtil.snapXY(lY));

			// the width is the remaining width (subtracting the wholeday appointments) divided by the number of tracks in the cluster
			double lW = lTrackWidth;
			// all but the most right appointment get 50% extra width, so they underlap the next track 
			if (lTrackIdx < lNumberOfTracks - 1) {
				lW *= 1.75;
			}
			lAppointmentAbstractTrackedPane.setPrefWidth(NodeUtil.snapWH(lAppointmentAbstractTrackedPane.getLayoutX(), lW));

			// the height is determined by the duration projected against the total dayHeight (being 24 hours)
			double lH;
			long lHeightInMinutes = lAppointmentAbstractTrackedPane.durationInMS / 1000;
			lH = lNumberOfPixelsPerMinute * lHeightInMinutes;

			// the height has a minimum size, in order to be able to render sensibly
			if (lH < 2 * layoutHelp.paddingProperty.get()) {
				lH = 2 * layoutHelp.paddingProperty.get();
			}
			lAppointmentAbstractTrackedPane.setPrefHeight(NodeUtil.snapWH(lAppointmentAbstractTrackedPane.getLayoutY(), lH));
		}
	}

	void setupAppointments() {
		setupWholedayAppointments();
		setupRegularAppointments();

		// place appointments in tracks
		trackedAppointmentBodyPanes.clear();
		trackedAppointmentBodyPanes.addAll(regularAppointmentBodyPanes);
		List<AppointmentAbstractTrackedPane<H>> determineTracks = determineTracks(trackedAppointmentBodyPanes);
		// add the appointments to the pane in the correct order, so they overlap nicely
		//getChildren().removeAll(determineTracks);
		getChildren().addAll(determineTracks);

		relayout();
	}

	final List<AppointmentAbstractTrackedPane<H>> trackedAppointmentBodyPanes = new ArrayList<>();

	/**
	 *
	 */
	private void setupWholedayAppointments() {
		wholedayAppointments.clear();
		wholedayAppointments.addAll(layoutHelp.skinnable.collectWholedayFor(columnValueObjectProperty.get()));

		// remove all appointments
		getChildren().removeAll(wholedayAppointmentBodyPanes);
		wholedayAppointmentBodyPanes.clear();

		// for all wholeday appointments on this date, create a header appointment pane
		int lCnt = 0;
		for (InschrijvingInterface lAppointment : wholedayAppointments) {
			// create pane
			AppointmentWholedayBodyPane<H> lAppointmentPane = new AppointmentWholedayBodyPane<>(columnValueObjectProperty.get(), lAppointment, layoutHelp);
			wholedayAppointmentBodyPanes.add(lAppointmentPane);
			((AgendaSkinAbstract<H>) layoutHelp.skin).appointmentNodeMap().put(System.identityHashCode(lAppointment), lAppointmentPane);
			lAppointmentPane.setId(lAppointmentPane.getClass().getSimpleName() + columnValueObjectProperty.get() + "/" + lCnt); // for testing

			// position by binding
			lAppointmentPane.layoutXProperty().bind(NodeUtil.snapXY(layoutHelp.wholedayAppointmentFlagpoleWidthProperty.multiply(lCnt)));
			lAppointmentPane.setLayoutY(0);
			lAppointmentPane.prefWidthProperty().bind(layoutHelp.wholedayAppointmentFlagpoleWidthProperty);
			lAppointmentPane.prefHeightProperty().bind(layoutHelp.dayHeightProperty);

			lCnt++;
		}
		getChildren().addAll(wholedayAppointmentBodyPanes);
	}

	final private List<InschrijvingInterface> wholedayAppointments = new ArrayList<>();
	final private List<AppointmentWholedayBodyPane<H>> wholedayAppointmentBodyPanes = new ArrayList<>();

	/**
	 *
	 */
	private void setupRegularAppointments() {
		regularAppointments.clear();
		regularAppointments.addAll(layoutHelp.skinnable.collectRegularFor(columnValueObjectProperty.get()));

		// remove all appointments
		getChildren().removeAll(regularAppointmentBodyPanes);
		regularAppointmentBodyPanes.clear();

		// for all regular appointments on this date, create a header appointment pane
		int lCnt = 0;
		for (InschrijvingInterface lAppointment : regularAppointments) {
			AppointmentRegularBodyPane<H> lAppointmentPane = new AppointmentRegularBodyPane<>(columnValueObjectProperty.get(), lAppointment, layoutHelp);
			regularAppointmentBodyPanes.add(lAppointmentPane);
			((AgendaSkinAbstract<H>) layoutHelp.skin).appointmentNodeMap().put(System.identityHashCode(lAppointment), lAppointmentPane);
			lAppointmentPane.setId(lAppointmentPane.getClass().getSimpleName() + columnValueObjectProperty.get() + "/" + lCnt); // for testing

			lCnt++;
		}
		//getChildren().addAll(regularAppointmentBodyPanes);				
	}

	final private List<InschrijvingInterface> regularAppointments = new ArrayList<>();
	final private List<AppointmentRegularBodyPane<H>> regularAppointmentBodyPanes = new ArrayList<>();

	int convertClickInSceneToDateTime(double x, double y) {
		Rectangle r = new Rectangle(NodeUtil.sceneX(this), NodeUtil.sceneY(this), this.getWidth(), this.getHeight());
		if (r.contains(x, y)) {
			double lHeightOffset = (y - r.getY());
			return (int) (lHeightOffset * layoutHelp.durationInMSPerPixelProperty.get());
		}
		return -1;
	}

	/**
	 * This method prepares a day for being drawn.
	 * The appointments within one day might overlap, this method will create a data structure so it is clear how these overlapping appointments should be drawn.
	 * All appointments in one day are process based on their start time; earliest first, and if there are more with the same start time, longest duration first.
	 * The appointments are then place onto (parallel) tracks; an appointment initially is placed in track 0.
	 * But if there is already an (partially overlapping) appointment there, then the appointment is moved to track 1.
	 * Unless there also is an appointment already in that track 1, then the next track is tried, and so forth, until a free track is found.
	 * For example (the letters are not the sequence in which the appointments are processed, they're just for identifying them):
	 * <p>
	 * tracks
	 * 0 1 2 3
	 * -------
	 * . . . .
	 * . . . .
	 * A . . .
	 * A B C .
	 * A B C D
	 * A B . D
	 * A . . D
	 * A E . D
	 * A . . D
	 * . . . D
	 * . . . D
	 * F . . D
	 * F H . D
	 * . . . .
	 * G . . .
	 * . . . .
	 * <p>
	 * Appointment A was rendered first and put into track 0 and its start time.
	 * Then appointment B was added, initially it was put in track 0, but appointment A already uses the that slot, so B was moved into track 1.
	 * C moved from track 0, conflicting with A, to track 1, conflicting with B, and ended up in track 2. And so forth.
	 * F and H show that even though D overlaps them, they could perfectly be placed in lower tracks.
	 * <p>
	 * A cluster of appointments always starts with a free standing appointment in track 0, for example A or G, such appointment is called the cluster owner.
	 * When the next appointment is added to the tracks, and finds that it cannot be put in track 0, it will be added as a member to the cluster represented by the appointment in track 0.
	 * Special attention must be paid to an appointment that is placed in track 0, but is linked to a cluster by a earlier appointment in a higher track; such an appointment is not the cluster owner.
	 * In the example above, F is linked through D to the cluster owned by A. So F is not a cluster owner, but a member of the cluster owned by A.
	 * And appointment H through F is also part of the cluster owned by A.
	 * G finally starts a new cluster.
	 * The cluster owner knows all members and how many tracks there are, each member knows in what track it is and has a direct link to the cluster owner.
	 * <p>
	 * When rendering the appointments above, parallel appointments are rendered narrower & indented, so appointments partially overlap and the left side of an appointment is always visible to the user.
	 * In the example above the single appointment G is rendered full width, while for example A, B, C and D are overlapping.
	 * F and H are drawn in the same dimensions as A and B in order to allow D to overlap then.
	 * The size and amount of indentation depends on the number of appointments that are rendered next to each other.
	 * In order to compute its location and size, each appointment needs to know:
	 * - its start and ending time,
	 * - its track number,
	 * - its total number of tracks,
	 * - and naturally the total width and height available to draw the day.
	 */
	private List<AppointmentAbstractTrackedPane<H>> determineTracks(List<AppointmentAbstractTrackedPane<H>> appointmentAbstractTrackedPanes) {

		// sort on start time and then decreasing duration
		appointmentAbstractTrackedPanes.sort((o1, o2) -> {
			// if not same start, then compare on starttime
			if (o1.startDateTime != o2.startDateTime) {
				return o1.startDateTime - o2.startDateTime;
			}

			// longest last
			return Long.compare(o2.durationInMS, o1.durationInMS);
		});

		// start placing appointments in the tracks
		AppointmentAbstractTrackedPane<H> lClusterOwner = null;
		for (AppointmentAbstractTrackedPane<H> lAppointmentPane : appointmentAbstractTrackedPanes) {
			// if there is no cluster owner
			if (lClusterOwner == null) {

				// than the current becomes an owner
				// only create a minimal cluster, because it will be setup fully in the code below
				lClusterOwner = lAppointmentPane;
				lClusterOwner.clusterTracks = new ArrayList<>();
			}

			// in which track should it be added
			int lTrackNr = lAppointmentPane.determineTrackWhereAppointmentCanBeAdded(lClusterOwner.clusterTracks);
			// if it can be added to track 0, then we have a "situation". Track 0 could mean
			// - we must start a new cluster
			// - the appointment is still linked to the running cluster by means of a linking appointment in the higher tracks
			if (lTrackNr == 0) {

				// So let's see if there is a linking appointment higher up
				boolean lOverlaps = false;
				for (int i = 1; i < lClusterOwner.clusterTracks.size() && !lOverlaps; i++) {
					lOverlaps = lAppointmentPane.checkIfTheAppointmentOverlapsAnAppointmentAlreadyInThisTrack(lClusterOwner.clusterTracks, i);
				}

				// if it does not overlap, we start a new cluster
				if (!lOverlaps) {
					lClusterOwner = lAppointmentPane;
					lClusterOwner.clusterMembers = new ArrayList<>();
					lClusterOwner.clusterTracks = new ArrayList<>();
					lClusterOwner.clusterTracks.add(new ArrayList<>());
				}
			}

			// add it to the track (and setup all other cluster data)
			lClusterOwner.clusterMembers.add(lAppointmentPane);
			lClusterOwner.clusterTracks.get(lTrackNr).add(lAppointmentPane);
			lAppointmentPane.clusterOwner = lClusterOwner;
			lAppointmentPane.clusterTrackIdx = lTrackNr;
			// for debug  System.out.println("----"); for (int i = 0; i < lClusterOwner.clusterTracks.size(); i++) { System.out.println(i + ": " + lClusterOwner.clusterTracks.get(i) ); } System.out.println("----");
		}

		// done
		return appointmentAbstractTrackedPanes;
	}
}

