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

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.*;
import javafx.geometry.NodeOrientation;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Callback;
import persistence.Marshalling;
import ui.visualization.jfxtras.internal.scene.control.skin.agenda.AgendaSkin;
import ui.visualization.jfxtras.scene.control.agenda.Agenda;
import ui.visualization.jfxtras.scene.control.agenda.InschrijvingInterface;

import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * This class is not a class but a data holder, a record, all fields are accessed directly.
 * Its methods are utility methods, which normally would be statics in a util class. 
 */
class LayoutHelp<H, I> {
	public LayoutHelp(Agenda<H, I> skinnable, AgendaSkin<H> skin) {
		this.skinnable = skinnable;
		this.skin = skin;
		dragPane = new DragPane(this);
		
		// header
		titleDateTimeHeightProperty.bind( textHeightProperty.multiply(1.5) ); 
		appointmentHeaderPaneHeightProperty.bind( textHeightProperty.add(5) ); // not sure why the 5 is needed
		headerHeightProperty.bind( highestNumberOfWholedayAppointmentsProperty.multiply( appointmentHeaderPaneHeightProperty ).add( titleDateTimeHeightProperty ) );

		// day columns
		dayFirstColumnXProperty.bind( timeWidthProperty );
		dayContentWidthProperty.bind( dayWidthProperty.subtract(0) ); // the 10 is a margin at the right so that there is always room to start a new appointment
		
		// hour height
		dayHeightProperty.bind(hourHeightProperty.multiply((Marshalling.TOTALETIJD) / 60.0).add(15));
		durationInMSPerPixelProperty.bind( msPerDayProperty.divide(dayHeightProperty) );
		
		// generic
		Text textHeight = new Text("X");
		textHeight.getStyleClass().add("Agenda");		
		textHeightProperty.set( textHeight.getBoundsInParent().getHeight() );
		
		// time column
		Text textWidth = new Text("88:88");
		textWidth.getStyleClass().add("Agenda");		
		timeWidthProperty.bind( timeColumnWhitespaceProperty.add( textWidth.getBoundsInParent().getWidth() )  );
	}
	final Agenda<H, I> skinnable;
	final AgendaSkin<H> skin;
	final DragPane dragPane;
	
	final DoubleProperty msPerDayProperty = new SimpleDoubleProperty(24 * 60 * 60 * 1000);
	final IntegerProperty highestNumberOfWholedayAppointmentsProperty = new SimpleIntegerProperty(0);
	final DoubleProperty paddingProperty = new SimpleDoubleProperty(3);
	final DoubleProperty timeColumnWhitespaceProperty = new SimpleDoubleProperty(10);
	final DoubleProperty wholedayAppointmentFlagpoleWidthProperty = new SimpleDoubleProperty(5);
	final DoubleProperty textHeightProperty = new SimpleDoubleProperty(0);
	final DoubleProperty titleDateTimeHeightProperty = new SimpleDoubleProperty(0);
	final DoubleProperty headerHeightProperty = new SimpleDoubleProperty(0);
	final DoubleProperty appointmentHeaderPaneHeightProperty = new SimpleDoubleProperty(0);
	final DoubleProperty timeWidthProperty = new SimpleDoubleProperty(0); 
	final DoubleProperty dayFirstColumnXProperty = new SimpleDoubleProperty(0); 
	final DoubleProperty dayWidthProperty = new SimpleDoubleProperty(0); 
	final DoubleProperty dayContentWidthProperty = new SimpleDoubleProperty(0); 
	final DoubleProperty dayHeightProperty = new SimpleDoubleProperty(0);  
	final DoubleProperty durationInMSPerPixelProperty = new SimpleDoubleProperty(0);
	final DoubleProperty hourHeightProperty = new SimpleDoubleProperty(0);
	final StringProperty divergentSelectedProperty = new SimpleStringProperty("");
	DateTimeFormatter timeDateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");

	String formatTime(int minutes) {
		String sTime = "";
		try {
			LocalTime d = LocalTime.parse(Marshalling.STARTTIJD, timeDateTimeFormatter);
			d = d.plusMinutes(minutes);
			sTime = d.format(timeDateTimeFormatter);
		} catch (DateTimeException e) {
			e.printStackTrace();
		}
		return sTime;
	}

	int parseTime(String time){
		try {
			LocalTime localTime = LocalTime.parse(time);
			LocalTime startTime = LocalTime.parse(Marshalling.STARTTIJD);
			localTime = localTime.minusSeconds(startTime.toSecondOfDay());
			return localTime.toSecondOfDay() / 60;
		} catch (DateTimeParseException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * I have no clue why the wholeday appointment header needs an additional 10.0 px X offset in right-to-left mode
	 */
	void clip(Pane pane, Text text, DoubleBinding width, DoubleBinding height, boolean mirrorWidth, double additionMirorXOffset) {
		Rectangle lClip = new Rectangle(0,0,0,0);
		if (mirrorWidth && skinnable.getNodeOrientation().equals(NodeOrientation.RIGHT_TO_LEFT)) {
			lClip.xProperty().bind(pane.widthProperty().multiply(-1.0).add(text.getBoundsInParent().getWidth()).add(additionMirorXOffset));
		}
		lClip.widthProperty().bind(width.subtract(3));
		lClip.heightProperty().bind(height.subtract(3));
		text.setClip(lClip);
	}

	void setupMouseOverAsBusy(final Node node) {
		// play with the mouse pointer to show something can be done here
		node.setOnMouseEntered( (mouseEvent) -> {
			if (!mouseEvent.isPrimaryButtonDown()) {						
				node.setCursor(Cursor.HAND);
				mouseEvent.consume();
			}
		});
		node.setOnMouseExited( (mouseEvent) -> {
			if (!mouseEvent.isPrimaryButtonDown()) {
				node.setCursor(Cursor.DEFAULT);
				mouseEvent.consume();
			}
		});
	}

	int roundTimeToNearestMinutes(int localDateTime, int minutes)
	{
		return (int) (minutes * Math.round(1.0 * localDateTime / minutes));
	}
	
    /**
     * Has the client added a callback to process the change?
     * @param appointment
     */
	void callAppointmentChangedCallback(InschrijvingInterface appointment) {
		// ignore temp appointments
		if (!(appointment instanceof AppointmentAbstractPane.AppointmentForDrag)) {
		    Callback<InschrijvingInterface, Void> lChangedCallback = skinnable.getAppointmentChangedCallback();
		    if (lChangedCallback != null) {
		        lChangedCallback.call(appointment);
		    }
		}
	}

}
