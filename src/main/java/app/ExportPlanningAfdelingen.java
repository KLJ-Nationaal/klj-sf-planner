package app;

import com.spire.xls.Workbook;
import com.spire.xls.Worksheet;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ExportPlanningAfdelingen extends Application {
	private static final Logger logger = LoggerFactory.getLogger(ExportPlanningAfdelingen.class);

	public static void main(String[] args) { launch(args); }

	@Override
	public void start(Stage stage) throws Exception {
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("Excel-bestanden", "*.xlsx;*.xlsb;*.xlsm")
				, new FileChooser.ExtensionFilter("Alle bestanden", "*.*")
		);
		fileChooser.setInitialDirectory(new File("data/"));
		File selectedFile = fileChooser.showOpenDialog(stage);
		if (selectedFile != null) {
			Workbook workbook = new Workbook();
			workbook.loadFromFile(selectedFile.getPath());
			int sheetCount = workbook.getWorksheets().getCount();
			workbook.dispose();
			logger.info("Workbook {} has {} sheets", selectedFile.getName(), sheetCount);

			for (int i = 0; i < sheetCount; i++) {
				workbook = new Workbook();
				workbook.loadFromFile(selectedFile.getPath());
				Worksheet worksheet = workbook.getWorksheets().get(i);
				logger.info("Writing {}", worksheet.getName());
				worksheet.saveToPdf(selectedFile.getParent() + File.separator + worksheet.getName() + ".pdf");
			}
		}
		logger.info("Done");
		Platform.exit();
		System.exit(0);
	}
}
