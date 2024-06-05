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

import ch.qos.logback.classic.Logger;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import jfxtras.util.NodeUtil;
import org.slf4j.LoggerFactory;
import persistence.Marshalling;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

class TimeScale24Hour extends Pane {
	private final static Logger logger = (Logger) LoggerFactory.getLogger(TimeScale24Hour.class);

	TimeScale24Hour(Pane pane, LayoutHelp layoutHelp) {
		this.pane = pane;
		this.layoutHelp = layoutHelp;
		
		// position
		layoutXProperty().set(0);
		layoutYProperty().set(0);
		prefWidthProperty().bind(pane.widthProperty());
		prefHeightProperty().bind(pane.heightProperty());

		// make completely transparent for all events
		setMouseTransparent(true);
		
		// add contents
		addTimeScale();
	}
	final Pane pane;
	final LayoutHelp layoutHelp;
	
	private void addTimeScale() {
		SimpleDateFormat df = new SimpleDateFormat("HH:mm");
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(df.parse(Marshalling.STARTTIJD));
		} catch (ParseException e) {
			logger.error(e.getLocalizedMessage());
		}
		final int mStart = cal.get(Calendar.HOUR_OF_DAY) * 60;
		
		// draw hours
		for (int lMinute = 0; lMinute < Marshalling.TOTALETIJD + 16; lMinute++)
		{
			// hour
			if (lMinute % 60 == 0){
				// line
				Line l = new Line(0,10,100,10);
				l.setId("hourLine" + lMinute);
				l.getStyleClass().add("HourLine");
				l.startXProperty().set(0.0);
				l.startYProperty().bind( NodeUtil.snapXY(layoutHelp.hourHeightProperty.multiply(lMinute / 60.0)) );
				l.endXProperty().bind( NodeUtil.snapXY(pane.widthProperty()));
				l.endYProperty().bind( NodeUtil.snapXY(l.startYProperty()));
				getChildren().add(l);
				// text
				Text t = new Text((mStart + lMinute) / 60 + ":00");
				t.xProperty().bind(layoutHelp.timeWidthProperty.subtract(t.getBoundsInParent().getWidth()).subtract(layoutHelp.timeColumnWhitespaceProperty.get() / 2));
				t.yProperty().bind(layoutHelp.hourHeightProperty.multiply(lMinute / 60.0));
				t.setTranslateY(t.getBoundsInParent().getHeight()); // move it under the line
				t.getStyleClass().add("HourLabel");
				t.setFontSmoothingType(FontSmoothingType.LCD);
				getChildren().add(t);
			}
			// half hour
			if (lMinute % 30 == 0 && lMinute % 60 != 0){
				// line
				Line l = new Line(0,10,100,10);
				l.setId("HourLine" + lMinute);
				l.getStyleClass().add("HourLine");
				l.startXProperty().bind( NodeUtil.snapXY(layoutHelp.timeWidthProperty));
				l.endXProperty().bind( NodeUtil.snapXY(pane.widthProperty()));
				l.startYProperty().bind( NodeUtil.snapXY(layoutHelp.hourHeightProperty.multiply(lMinute / 60.0)));
				l.endYProperty().bind( NodeUtil.snapXY(l.startYProperty()));
				getChildren().add(l);
				// text
				Text t = new Text((mStart + lMinute) / 60 + ":30");
				t.xProperty().bind(layoutHelp.timeWidthProperty.subtract(t.getBoundsInParent().getWidth()).subtract(layoutHelp.timeColumnWhitespaceProperty.get() / 2));
				t.yProperty().bind(layoutHelp.hourHeightProperty.multiply(lMinute / 60.0));
				t.setTranslateY(t.getBoundsInParent().getHeight()); // move it under the line
				t.getStyleClass().add("HourLabel");
				t.setFontSmoothingType(FontSmoothingType.LCD);
				getChildren().add(t);
			}
			//graticule lines
			if (lMinute % Marshalling.MINMINUTEN == 0) {
				Line l = new Line(0,10,100,10);
				l.setId("halfHourLine" + lMinute);
				l.getStyleClass().add("HalfHourLine");
				l.startXProperty().bind( NodeUtil.snapXY(layoutHelp.timeWidthProperty));
				l.endXProperty().bind( NodeUtil.snapXY(pane.widthProperty()));
				l.startYProperty().bind( NodeUtil.snapXY(layoutHelp.hourHeightProperty.multiply(lMinute / 60.0)));
				l.endYProperty().bind( NodeUtil.snapXY(l.startYProperty()));
				getChildren().add(l);
			}
		}

	}
}
