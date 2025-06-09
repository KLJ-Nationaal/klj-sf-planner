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

import ui.visualization.jfxtras.scene.control.agenda.InschrijvingInterface;

import java.util.ArrayList;
import java.util.List;

abstract class AppointmentAbstractTrackedPane<H> extends AppointmentAbstractPane<H> {
	AppointmentAbstractTrackedPane(H colValue, InschrijvingInterface appointment, LayoutHelp<H> layoutHelp) {
		super(appointment, layoutHelp);

		// we know start and end optionally are set
		startDateTime = appointment.getStartTime();
		endDateTime = appointment.getEndTime();
		durationInMS = (endDateTime - startDateTime) * 1000L;
	}

	protected final int startDateTime;
	protected final int endDateTime;
	protected final long durationInMS;

	// for the role of cluster owner
	List<AppointmentAbstractTrackedPane<H>> clusterMembers = new ArrayList<>();
	List<List<AppointmentAbstractTrackedPane<H>>> clusterTracks = new ArrayList<>();

	// for the role of cluster member
	AppointmentAbstractTrackedPane<H> clusterOwner = this;
	int clusterTrackIdx = -1;

	/**
	 *
	 */
	public int determineTrackWhereAppointmentCanBeAdded(List<List<AppointmentAbstractTrackedPane<H>>> tracks) {
		int lTrackNr = 0;
		while (true) {
			// make sure there is a arraylist for this track
			if (lTrackNr == tracks.size()) {
				tracks.add(new ArrayList<>());
			}

			// scan all existing appointments in this track and see if there is an overlap
			if (!checkIfTheAppointmentOverlapsAnAppointmentAlreadyInThisTrack(tracks, lTrackNr)) {
				// no overlap, it can be added here
				return lTrackNr;
			}

			// overlap, try next track
			lTrackNr++;
		}
	}

	/**
	 *
	 */
	public boolean checkIfTheAppointmentOverlapsAnAppointmentAlreadyInThisTrack(List<List<AppointmentAbstractTrackedPane<H>>> tracks, int tracknr) {
		// get the track
		List<AppointmentAbstractTrackedPane<H>> lTrack = tracks.get(tracknr);

		// scan all existing appointments in this track
		for (AppointmentAbstractTrackedPane<H> lAppointmentPane : lTrack) {
			// There is an overlap:
			// if the start time of the already placed appointment is before or equals the new appointment's end time
			// and the end time of the already placed appointment is after the new appointment's start time (equals will put two consequative appointments into separate tracks)
			// ...PPPPPPPPP...
			// .NNNN.......... -> Ps <= Ne & Pe >= Ns -> overlap
			// .....NNNNN..... -> Ps <= Ne & Pe >= Ns -> overlap
			// ..........NNN.. -> Ps <= Ne & Pe >= Ns -> overlap
			// .NNNNNNNNNNNNN. -> Ps <= Ne & Pe >= Ns -> overlap
			// .N............. -> false    & Pe >= Ns -> no overlap
			// .............N. -> Ps <= Ne & false	  -> no overlap
			int lPlacedStart = lAppointmentPane.startDateTime;
			int lPlacedEnd = (lAppointmentPane.endDateTime != 0 ? lAppointmentPane.endDateTime : lAppointmentPane.startDateTime + 10);
			int lNewStart = this.startDateTime;
			int lNewEnd = (this.endDateTime != 0 ? this.endDateTime : this.startDateTime + 10);
			if ((lPlacedStart == lNewStart || lNewEnd == 0 || lPlacedStart < lNewEnd)
					&& lPlacedEnd != 0 && lPlacedEnd > lNewStart
			) {
				// overlap
				return true;
			}
		}
		// no overlap
		return false;
	}

	/**
	 *
	 */
	public String toString() {
		return "pane=" + startDateTime + "-" + endDateTime + ";" + super.toString();
	}
}
