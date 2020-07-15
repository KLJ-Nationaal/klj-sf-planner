package ui;

import com.google.inject.Guice;
import com.google.inject.Injector;
import domain.importing.WizardData;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import ui.importer.WizardImportController;
import ui.importer.WizardModule;

import java.io.File;
import java.io.IOException;

public class SportfeestPlannerGUI extends Application {

	private Window primaryStage;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		this.primaryStage = primaryStage;
		FXMLLoader loader = new FXMLLoader();
		Parent root = loader.load( getClass().getResource("/ui/Main.fxml"));
		Scene scene = new Scene(root);
		//TODO
		//JMetro jMetro = new JMetro(Style.LIGHT);
		//jMetro.setParent(root);
		//jMetro.setScene(scene);
		primaryStage.setScene( scene );
		primaryStage.setTitle("KLJ Sportfeest Planner");
		primaryStage.show();
	}

	public void OpenImportExcelWizard(ActionEvent actionEvent) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("Excel-bestanden", "*.xlsx;*.xlsb;*.xlsm")
				,new FileChooser.ExtensionFilter("Alle bestanden", "*.*")
		);
		fileChooser.setInitialDirectory(new File("data/"));
		Window parentWindow = ((MenuItem)(actionEvent.getTarget())).getParentPopup().getOwnerWindow();
		File selectedFile = fileChooser.showOpenDialog(parentWindow);
		if(selectedFile != null) {
			try {
				final Injector injector = Guice.createInjector( new WizardModule() );
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/WizardImport.fxml"),
						null,
						new JavaFXBuilderFactory(),
						(clazz) -> injector.getInstance(clazz));
				Parent root = loader.load();
				WizardImportController wizardImportController = loader.getController();
				wizardImportController.setFilename(selectedFile.getCanonicalPath());
				wizardImportController.setTextCallback(SportfeestPlannerGUI::ImportDone);
				Stage stage = new Stage();
				stage.setTitle("Importeer uit Excel");
				stage.setScene(new Scene(root));
				stage.initModality(Modality.WINDOW_MODAL);
				stage.initOwner(primaryStage);
				stage.show();

			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void ImportDone(WizardData wizardData) {

	}
}
