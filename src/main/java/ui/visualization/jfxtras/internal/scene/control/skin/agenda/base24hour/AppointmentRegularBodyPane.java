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

import javafx.scene.text.Text;
import ui.visualization.jfxtras.scene.control.agenda.InschrijvingInterface;

public class AppointmentRegularBodyPane<H, I> extends AppointmentAbstractTrackedPane<H, I> {

	public AppointmentRegularBodyPane(H columnValue, InschrijvingInterface appointment, LayoutHelp layoutHelp) {
		super(columnValue, appointment, layoutHelp);
		
		// strings
		String startAsString = layoutHelp.formatTime(this.startDateTime);
		String endAsString = layoutHelp.formatTime(this.endDateTime);

		// add summary
		Text lSummaryText = new Text((String) layoutHelp.skinnable.getItemValueFactory().call(appointment));
		{
			lSummaryText.getStyleClass().add("AppointmentLabel");
			lSummaryText.setX( layoutHelp.paddingProperty.get() );
			lSummaryText.setY( lSummaryText.prefHeight(5));
			layoutHelp.clip(this, lSummaryText, widthProperty().add(0.0), heightProperty().subtract( layoutHelp.paddingProperty ), false, 0.0);
			getChildren().add(lSummaryText);			
		}

		// add the duration as text
		Text lTimeText = new Text(startAsString + "-" + endAsString);
		{
			lTimeText.getStyleClass().add("AppointmentTimeLabel");
			lTimeText.setX(layoutHelp.paddingProperty.get() );
			lTimeText.setY(lSummaryText.getY() + layoutHelp.textHeightProperty.get());
			layoutHelp.clip(this, lTimeText, widthProperty().subtract( layoutHelp.paddingProperty ), heightProperty().add(0.0), true, 0.0);
			getChildren().add(lTimeText);
		}
		
		// add the menu header
		getChildren().add(appointmentMenu);
	}
}
