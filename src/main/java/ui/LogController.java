package ui;

import javafx.fxml.FXML;
import logging.TextAreaLogAppender;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.slf4j.LoggerFactory;

public class LogController {

	@FXML
	private VirtualizedScrollPane<StyleClassedTextArea> scrLogPane;

	@FXML
	public void initialize() {
		StyleClassedTextArea txtLogArea = scrLogPane.getContent();
		txtLogArea.getStylesheets().add(getClass().getResource("/ui/Log.css").toExternalForm());
		ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		((TextAreaLogAppender<?>) logger.getAppender("guiAppender")).setTextArea(txtLogArea);
		logger.detachAppender("guiAppender");
		//TODO: optioneel maken, lijkt alles te vertragen
		txtLogArea.textProperty().addListener((observableValue, oldVal, newVal) -> txtLogArea.requestFollowCaret());
		txtLogArea.requestFollowCaret();
	}

}
