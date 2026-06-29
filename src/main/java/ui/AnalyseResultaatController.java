package ui;

import ai.timefold.solver.core.api.solver.SolverFactory;
import ch.qos.logback.classic.Logger;
import domain.Sportfeest;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.slf4j.LoggerFactory;
import solver.ConstraintAnalyzer;

public class AnalyseResultaatController {
	@FXML
	private TreeView<String> treeView;

	private final static Logger logger = (Logger) LoggerFactory.getLogger(AnalyseResultaatController.class);

	private final static class IconType {
		private final static int EXCLAMATION = 0;
		private final static int WARNING = 1;
		private final static int INFO = 2;
	}

	@FXML
	public void initialize() {
	}

	public void setSportfeest(Sportfeest sportfeest, SolverFactory<Sportfeest> solverFactory) {
		var analyzer = new ConstraintAnalyzer(solverFactory);
		var analysisResult = analyzer.analyze(sportfeest);
		TreeItem<String> root = new TreeItem<>("Score: " + sportfeest.getScore().toString());
		if (!sportfeest.getScore().isFeasible()) {
			root.getChildren().add(new TreeItem<>("DEZE OPLOSSING IS NIET HAALBAAR!", getIcon(IconType.EXCLAMATION)));
		}

		// sanity check
		if (!analysisResult.totalScore().equals(sportfeest.getScore())) {
			logger.error("Verschil tussen de scores van de solver en de analyzer ({} en {}). Dit mag niet voorkomen! ", sportfeest.getScore(), analysisResult.totalScore());
		}
		for (var constraintResult : analysisResult.constraints()) {
			ImageView icon = getIcon(IconType.WARNING);
			if (constraintResult.totalScore().hardScore() != 0) icon = getIcon(IconType.EXCLAMATION);
			else if (constraintResult.totalScore().softScore() == 0) icon = getIcon(IconType.INFO);
			TreeItem<String> constr = new TreeItem<>(
					"Voorwaarde: " + constraintResult.name() + "\nGewicht: " + constraintResult.totalScore() + ", Aantal keer: " + constraintResult.matchCount(),
					icon);
			for (var matchResult : constraintResult.matches()) {
				constr.getChildren().add(new TreeItem<>(matchResult.toString()));
			}
			root.getChildren().add(constr);
		}


		treeView.setRoot(root);
		root.setExpanded(true);
	}

	@FXML
	public void CloseAction(ActionEvent actionEvent) {
		Stage stage = (Stage) ((Button) actionEvent.getSource()).getScene().getWindow();
		stage.close();
	}

	private ImageView getIcon(int iconType) {
		ColorAdjust bright = new ColorAdjust(0, 1, 1, 1);
		ImageView imageview = new ImageView(new Image(getClass().getResourceAsStream("/icons/times-circle.png")));
		Lighting lighting = new Lighting(new Light.Distant(45, 90, Color.RED));
		switch (iconType) {
			case IconType.EXCLAMATION:
				//set as defaults
				break;
			case IconType.WARNING:
				imageview = new ImageView(new Image(getClass().getResourceAsStream("/icons/exclamation-triangle.png")));
				lighting = new Lighting(new Light.Distant(45, 90, Color.GOLD));
				break;
			case IconType.INFO:
				imageview = new ImageView(new Image(getClass().getResourceAsStream("/icons/info-circle.png")));
				lighting = new Lighting(new Light.Distant(45, 90, Color.DEEPSKYBLUE));
				break;
		}
		imageview.setPreserveRatio(true);
		imageview.setFitHeight(30);
		lighting.setContentInput(bright);
		lighting.setSurfaceScale(0.0);
		imageview.setEffect(lighting);
		return imageview;
	}
}
