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
import domain.Tijdslot;
import javafx.event.Event;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.util.Callback;
import jfxtras.scene.control.ImageViewButton;
import jfxtras.util.NodeUtil;
import org.slf4j.LoggerFactory;
import ui.visualization.jfxtras.scene.control.agenda.InschrijvingInterface;

import java.util.Objects;

class AppointmentMenu<H> extends Rectangle {
	private final static Logger logger = (Logger) LoggerFactory.getLogger(AppointmentMenu.class);

	AppointmentMenu(Pane pane, InschrijvingInterface appointment, LayoutHelp<H> layoutHelp) {
		this.pane = pane;
		this.appointment = appointment;
		this.layoutHelp = layoutHelp;

		// layout
		setX(NodeUtil.snapXY(layoutHelp.paddingProperty.get()));
		setY(NodeUtil.snapXY(layoutHelp.paddingProperty.get()));
		setWidth(6);
		setHeight(3);

		// style
		getStyleClass().add("MenuIcon");

		// mouse
		layoutHelp.setupMouseOverAsBusy(this);
		setupMouseClick();
	}

	final Pane pane;
	final InschrijvingInterface appointment;
	final LayoutHelp<H> layoutHelp;

	/**
	 *
	 */
	private void setupMouseClick() {
		setOnMousePressed(Event::consume);
		setOnMouseReleased(Event::consume);
		setOnMouseClicked((mouseEvent) -> {
			mouseEvent.consume();
			showMenu(mouseEvent);
		});
	}

	void showMenu(MouseEvent mouseEvent) {
		// has the client done his own popup?
		Callback<InschrijvingInterface, Void> lEditCallback = layoutHelp.skinnable.getEditAppointmentCallback();
		if (lEditCallback != null) {
			lEditCallback.call(appointment);
			return;
		}

		// only if not already showing
		if (popup != null && popup.isShowing()) return;

		// create popup
		popup = new Popup();
		popup.setAutoFix(true);
		popup.setAutoHide(true);
		popup.setHideOnEscape(true);
		popup.setOnHidden(this::menuHidden);

		// popup contents
		BorderPane lBorderPane = new BorderPane() {
			// As of 1.8.0_40 CSS files are added in the scope of a control, the popup does not fall under the control, so the stylesheet must be reapplied 
			// When JFxtras is based on 1.8.0_40+: @Override 
			public String getUserAgentStylesheet() {
				return layoutHelp.skinnable.getUserAgentStylesheet();
			}
		};
		lBorderPane.getStyleClass().add(layoutHelp.skinnable.getClass().getSimpleName() + "Popup");
		popup.getContent().add(lBorderPane);

		// close icon
		lBorderPane.setRight(createCloseIcon());

		// initial layout
		VBox lVBox = new VBox(layoutHelp.paddingProperty.get());
		lBorderPane.setCenter(lVBox);

		// start and end
		lVBox.getChildren().add(createStartTextField());
		lVBox.getChildren().add(createDuurTextField());

		// wholeday
		//lVBox.getChildren().add(createWholedayCheckbox());

		lVBox.getChildren().add(createLabelTextField("Afdeling:", appointment.getAfdeling().toString()));
		lVBox.getChildren().add(createLabelTextField("Korps:", String.valueOf(appointment.getKorps())));
		lVBox.getChildren().add(createLabelTextField("Discipline:", appointment.getDiscipline().getNaam()));
		lVBox.getChildren().add(createLabelTextField("Ring:", appointment.getRing().toString()));

		lVBox.getChildren().add(createActions());

		// show it just below the menu icon
		popup.show(pane, NodeUtil.screenX(pane), NodeUtil.screenY(pane));
	}

	private Popup popup;

	private ImageViewButton createCloseIcon() {
		ImageViewButton closeIconImageView = new ImageViewButton();
		closeIconImageView.getStyleClass().add("close-icon");
		closeIconImageView.setPickOnBounds(true);
		closeIconImageView.setOnMouseClicked(this::menuHidden);
		return closeIconImageView;
	}

	private HBox createLabelTextField(String label, String text) {
		HBox lHBox = new HBox();
		lHBox.getChildren().add(new Text(label + " "));

		Text textField = new Text(text);
		textField.getStyleClass().add("data");
		lHBox.getChildren().add(textField);

		return lHBox;
	}

	private HBox createStartTextField() {
		HBox lHBox = new HBox();
		lHBox.getChildren().add(new Text("Start:"));

		TextField startTextField = new TextField();
		startTextField.setText(layoutHelp.formatTime(appointment.getStartTijd()));

		// event handling
		startTextField.textProperty().addListener((observable, oldValue, newValue) -> {
			// probeer het tijdslot te verkrijgen met wat de gebruiker heeft ingevoerd. Als het ongeldig is, moet het huidige tijdslot worden geretourneerd.
			Tijdslot tijdslot = new Tijdslot(
					layoutHelp.parseTime(newValue),
					appointment.getTijdslot().getDuur(),
					appointment.getRing());
			// verifiëren of er daadwerkelijk iets is veranderd en opslaan
			if (!Objects.equals(tijdslot, appointment.getTijdslot())) {
				logger.info("Aanpassing {} gewenst startuur {} naar {}", appointment, newValue, tijdslot);
				appointment.setTijdslot(tijdslot);
			}
			// ververs bij het sluiten van de pop-up
		});
		startTextField.setPrefWidth(100);

		lHBox.getChildren().add(startTextField);
		return lHBox;
	}

	private CheckBox createWholedayCheckbox() {
		CheckBox wholedayCheckBox = new CheckBox("Wholeday");
		wholedayCheckBox.setId("wholeday-checkbox");
		wholedayCheckBox.selectedProperty().set(appointment.isWholeDay());

		wholedayCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
			appointment.setWholeDay(newValue);
			if (newValue) {
				appointment.setTijdslot(null);
			} else {
				appointment.setTijdslot(appointment.getTijdslots().get(0));
			}
			// refresh is done upon popup close
			layoutHelp.callAppointmentChangedCallback(appointment);
		});

		return wholedayCheckBox;
	}

	private HBox createDuurTextField() {
		HBox lHBox = new HBox();
		lHBox.getChildren().add(new Text("Duur:"));

		TextField duurTextField = new TextField();
		duurTextField.setText(String.valueOf(appointment.getDiscipline().getDuur()));
		duurTextField.textProperty().addListener((observable, oldValue, newValue) -> {
			try {
				int duur = Integer.parseUnsignedInt(newValue);
				// probeer het tijdslot te verkrijgen met wat de gebruiker heeft ingevoerd. Als het ongeldig is, moet het huidige tijdslot worden geretourneerd.
				Tijdslot tijdslot = new Tijdslot(appointment.getStartTijd(), duur, appointment.getRing());
				// verifiëren of er daadwerkelijk iets is veranderd en opslaan
				if (!Objects.equals(tijdslot, appointment.getTijdslot())) {
					appointment.setTijdslot(tijdslot);
					logger.info("Aanpassing {} gewenste duur {}, opgeslagen {}", appointment, duur, appointment.getTijdslot().getDuur());
				}
				// ververs bij het sluiten van de pop-up
			} catch (NumberFormatException ignored) {}
		});
		duurTextField.setPrefWidth(60);

		lHBox.getChildren().add(duurTextField);

		lHBox.getChildren().add(new Text("minuten"));
		return lHBox;
	}

	private HBox createActions() {
		HBox lHBox = new HBox();

		// action
		if (layoutHelp.skinnable.getActionCallback() != null) {
			ImageViewButton actionImageViewButton = createActionButton("action-icon", "Action");
			actionImageViewButton.setOnMouseClicked((mouseEvent) -> {
				popup.hide();
				layoutHelp.skinnable.getActionCallback().call(appointment);
				layoutHelp.callAppointmentChangedCallback(appointment);
				// any refresh is done via the collection events
			});
			lHBox.getChildren().add(actionImageViewButton);
		}

		return lHBox;
	}

	private ImageViewButton createActionButton(String styleClass, String tooltipText) {
		ImageViewButton lImageViewButton = new ImageViewButton();
		lImageViewButton.getStyleClass().add(styleClass);
		lImageViewButton.setPickOnBounds(true);
		Tooltip.install(lImageViewButton, new Tooltip(tooltipText));
		return lImageViewButton;
	}
	private void menuHidden(Event event) {
		popup.hide();
		layoutHelp.callAppointmentChangedCallback(appointment);
		layoutHelp.skin.setupAppointments();
	}
}