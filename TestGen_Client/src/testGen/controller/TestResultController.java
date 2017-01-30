package testGen.controller;

import java.util.ArrayList;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import testGen.model.Controller;
import testGen.model.Result;
import testGen.model.VerifiedQuestionDescription;

public class TestResultController implements Controller {
	@FXML private Parent testResultWindow;
	@FXML private Label resultTitleLabel;
	@FXML private ScrollPane resultScrollPane;

	public static Result testResult;

	@FXML public void initialize() {

		resultTitleLabel.setText("Wynik:  " + testResult.getNumOfPoints() + " punktów na "
				+ testResult.getOutOf() + "\n");
		TextFlow resultDescription = new TextFlow();
		ArrayList<Text> descriptionLines = new ArrayList<Text>();

		for (VerifiedQuestionDescription questionsResult : testResult
				.getPartialResultDescriptions()) {
			Text descriptionLine = new Text(
					"Pytanie: " + questionsResult.questionContent + "\n");
			if (questionsResult.wereYouRight) {
				// question's content
				descriptionLine.setStyle(
						"-fx-font: 18px Inconsolata; -fx-font-weight:bold; -fx-text-fill: #14B602;");
				descriptionLines.add(descriptionLine);

				// users (right) answers
				descriptionLine = new Text("\nTwoja odpowiedź:\n"
						+ questionsResult.yourAnswer + "\n");
				descriptionLine.setFont(Font.font("Inconsolata", 16));
				descriptionLines.add(descriptionLine);
			} else {
				// question's content
				descriptionLine.setStyle(
						"-fx-font: 18px Inconsolata; -fx-font-weight:bold; -fx-text-fill: #FF2222;");
				descriptionLines.add(descriptionLine);

				// user's and right answers
				descriptionLine = new Text("\nTwoja odpowiedź:\n"
						+ questionsResult.yourAnswer + "\nPoprawna odpowiedź: "
						+ questionsResult.rightAnswer + "\n");
				descriptionLine.setFont(Font.font("Inconsolata", 16));
				descriptionLines.add(descriptionLine);
			}
		}
		resultDescription.getChildren().addAll(descriptionLines);
		resultScrollPane.setContent(resultDescription);
	}

	@FXML public void closeWindowBtn(ActionEvent event) {
		closeWindow(testResultWindow);
	}

	@FXML private void closeBtnEnterKey(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			closeWindow(testResultWindow);
		}
	}
}
