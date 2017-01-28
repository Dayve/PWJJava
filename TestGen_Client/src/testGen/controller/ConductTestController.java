package testGen.controller;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.util.Duration;
import testGen.model.Answer;
import testGen.model.Controller;
import testGen.model.Test;

public class ConductTestController implements Controller {

	@FXML Parent conductTestWindow;

	@FXML private Label totalNumberOfQuestionsLabel;
	@FXML private Label numberOfCurrentQuestionLabel;
	@FXML private Label timeLeftLabel;
	@FXML private Label questionContentLabel;
	@FXML private TextArea questionTextArea;
	@FXML private FlowPane answersFlowPane;

	public static Test conductedTest;
	private Integer currentQuestionNumber;

	private void bindToTime() {
		Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0),
				new EventHandler<ActionEvent>() {
					@Override public void handle(ActionEvent actionEvent) {
						LocalDateTime currentTime = LocalDateTime.now();

						Long diffInSeconds = ChronoUnit.SECONDS.between(
								currentTime, conductedTest.getEndTime());
						Long secondsToDisplay = diffInSeconds % 60;
						Long minutesToDisplay = (diffInSeconds / 60) % 60;
						Long hoursToDisplay = (diffInSeconds / 3600) % 24;

						timeLeftLabel.setText((hoursToDisplay.toString() + ":"
								+ minutesToDisplay.toString() + ":"
								+ secondsToDisplay.toString()));
					}
				}), new KeyFrame(Duration.seconds(1)));
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();
	}

	private void updateQuestionAndAnswersContainers() {
		questionContentLabel.setText(conductedTest.getQuestions()
				.get(currentQuestionNumber).getContent());
		numberOfCurrentQuestionLabel.setText(currentQuestionNumber.toString());
		
		answersFlowPane.getChildren().clear();
		for (Answer answer : conductedTest.getQuestions()
				.get(currentQuestionNumber).getPossibleAnswers()) {
			
			Label answerLabel = new Label(answer.getAnswerContent());
			answerLabel.setId(answer.getId().toString());
			answerLabel.setPrefWidth(answersFlowPane.getWidth());
			answerLabel.setStyle("-fx-font: 14px Inconsolata;");
			
			answersFlowPane.getChildren().add(answerLabel);
		}
	}

	private void refresh() {

	}

	@FXML public void initialize() {
		if (conductedTest == null) {
			closeWindow(conductTestWindow);
		}
		currentQuestionNumber = 0;
		totalNumberOfQuestionsLabel
		.setText(conductedTest.getnOfQuestions().toString());
		updateQuestionAndAnswersContainers();
		
		bindToTime();
	}

	@FXML private void sendTest() {

	}

	@FXML private void previousQuestion() {
		if (currentQuestionNumber > 0) {
			currentQuestionNumber--;
		} else {
			currentQuestionNumber = conductedTest.getnOfQuestions() - 1;
		}
		updateQuestionAndAnswersContainers();
	}

	@FXML private void nextQuestion() {
		if (currentQuestionNumber < conductedTest.getnOfQuestions() - 1) {
			currentQuestionNumber++;
		} else {
			currentQuestionNumber = 0;
		}
		updateQuestionAndAnswersContainers();
	}
}
