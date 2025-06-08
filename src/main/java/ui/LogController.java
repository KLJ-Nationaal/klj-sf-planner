package ui;

import javafx.fxml.FXML;
import logging.TextAreaLogAppender;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.slf4j.LoggerFactory;

public class LogController {

	@FXML
	private StyleClassedTextArea txtLogArea;

	@FXML
	public void initialize() {
		ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		((TextAreaLogAppender) logger.getAppender("guiAppender")).setTextArea(txtLogArea);
		logger.detachAppender("guiAppender");
		//TODO: optioneel maken, lijkt alles te vertragen
		txtLogArea.textProperty().addListener((observableValue, oldVal, newVal) -> txtLogArea.requestFollowCaret());
		txtLogArea.requestFollowCaret();
	}

}
