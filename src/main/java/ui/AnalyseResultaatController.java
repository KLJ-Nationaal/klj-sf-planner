package ui;

import domain.Sportfeest;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.stream.Collectors;

public class AnalyseResultaatController {
	@FXML
	private Label subtitle;
	@FXML
	private TreeView<String> treeView;

	private final static class IconType {
		private final static int EXCLAMATION = 0;
		private final static int WARNING = 1;
		private final static int INFO = 2;
	}

	@FXML
	public void initialize() {
	}

	public void setSportfeest(Sportfeest sportfeest, ScoreDirector<Sportfeest> scoreDirector) {
		scoreDirector.setWorkingSolution(sportfeest);
		sportfeest.setScore((HardSoftScore) scoreDirector.calculateScore());
		TreeItem<String> root = new TreeItem<>("Score: " + sportfeest.getScore().toString());
		if (!sportfeest.getScore().isFeasible()) {
			root.getChildren().add(new TreeItem<>("DEZE OPLOSSING IS NIET HAALBAAR!", getIcon(IconType.EXCLAMATION)));
		}

		for (ConstraintMatchTotal cmt : scoreDirector.getConstraintMatchTotals()) {
			ImageView icon = getIcon(IconType.WARNING);
			if (((HardSoftScore) cmt.getScore()).getHardScore() != 0) icon = getIcon(IconType.EXCLAMATION);
			TreeItem<String> constr = new TreeItem<>(
					"Voorwaarde: " + cmt.getConstraintName() + "\nGewicht: " + cmt.getScore() + ", Aantal keer: " + cmt.getConstraintMatchCount(),
					icon);
			for (ConstraintMatch cm : cmt.getConstraintMatchSet()) {
				constr.getChildren().add(new TreeItem<>(cm.getJustificationList().stream()
						.map(Object::toString)
						.collect(Collectors.joining(", "))));
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
