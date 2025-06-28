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
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import ui.visualization.jfxtras.scene.control.agenda.InschrijvingInterface;
import ui.visualization.jfxtras.util.NodeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for rendering the day header (whole day appointments).
 */
public class DayHeaderPane<H> extends Pane {
	public DayHeaderPane(H columnValue, LayoutHelp<H> layoutHelp) {
		this.columnValueObjectProperty.set(columnValue);
		this.layoutHelp = layoutHelp;
		construct();
	}

	final ObjectProperty<H> columnValueObjectProperty = new SimpleObjectProperty<>(this, "columnValue");
	final LayoutHelp<H> layoutHelp;

	private void construct() {

		// for debugging setStyle("-fx-border-color:PINK;-fx-border-width:4px;");
		getStyleClass().add("DayHeader");

		// set day label
		dayText = new Label("?");
		dayText.getStyleClass().add("DayLabel");
		dayText.setLayoutX(layoutHelp.paddingProperty.get()); // align left
		dayText.setLayoutY(dayText.prefHeight(0));
		getChildren().add(dayText);

		// clip the visible part
		Rectangle lClip = new Rectangle(0, 0, 0, 0);
		lClip.widthProperty().bind(widthProperty().subtract(layoutHelp.paddingProperty.get()));
		lClip.heightProperty().bind(heightProperty());
		dayText.setClip(lClip);

		// react to changes in the calendar by updating the label
		columnValueObjectProperty.addListener((observable) -> setLabel());
		setLabel();

		// react to changes in the appointments
		layoutHelp.skinnable.addOnChangeListener(this::setupAppointments);
		setupAppointments();
	}

	private void setLabel() {
		String lLabel = layoutHelp.skinnable.getColumnValueFactory().call(columnValueObjectProperty.getValue());
		dayText.setTooltip(new Tooltip(lLabel));
		dayText.setText(lLabel);

		// for testing
		setId("DayHeader" + columnValueObjectProperty.get());
	}

	private Label dayText = new Label("?");

	public void setupAppointments() {

		// remove all appointments
		getChildren().removeAll(appointmentHeaderPanes);
		appointmentHeaderPanes.clear();

		// for all wholeday appointments on this date, create a header appointment pane
		appointments.clear();
		appointments.addAll(layoutHelp.skinnable.collectWholedayFor(columnValueObjectProperty.get()));
		int lCnt = 0;
		for (InschrijvingInterface lAppointment : appointments) {
			// create pane
			AppointmentWholedayHeaderPane<H> lAppointmentHeaderPane = new AppointmentWholedayHeaderPane<>(lAppointment, layoutHelp);
			getChildren().add(lAppointmentHeaderPane);
			appointmentHeaderPanes.add(lAppointmentHeaderPane);
			lAppointmentHeaderPane.setId(lAppointmentHeaderPane.getClass().getSimpleName() + columnValueObjectProperty.get() + "/" + lCnt); // for testing

			// position by binding
			lAppointmentHeaderPane.layoutXProperty().bind(layoutHelp.wholedayAppointmentFlagpoleWidthProperty.multiply(lCnt)); // each pane is cascade offset to the right to allow connecting to the wholeday appointment on the day pane
			lAppointmentHeaderPane.layoutYProperty().bind(heightProperty().subtract(layoutHelp.appointmentHeaderPaneHeightProperty.multiply(appointments.size() - lCnt))); // each pane is cascaded offset down so the title label is visible 
			lAppointmentHeaderPane.prefWidthProperty().bind(widthProperty().subtract(layoutHelp.wholedayAppointmentFlagpoleWidthProperty.multiply(lCnt))); // make sure the size matches the cascading
			lAppointmentHeaderPane.prefHeightProperty().bind(heightProperty().subtract(lAppointmentHeaderPane.layoutYProperty())); // and the height reaches all the way to the bottom to connect to the flagpole

			lCnt++;
		}
	}

	final private List<InschrijvingInterface> appointments = new ArrayList<>();
	final private List<AppointmentWholedayHeaderPane<H>> appointmentHeaderPanes = new ArrayList<>();

	/**
	 * So the out view knows how much room (height) we need
	 *
	 * @return number of whole day appointments
	 */
	public int getNumberOfWholeDayAppointments() {
		return appointments.size();
	}

	int convertClickInSceneToDateTime(double x, double y) {
		Rectangle r = new Rectangle(NodeUtil.sceneX(this), NodeUtil.sceneY(this), this.getWidth(), this.getHeight());
		if (r.contains(x, y)) return -1;
		return 0;
	}

}