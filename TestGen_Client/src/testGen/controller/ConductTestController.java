package testGen.controller;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.util.Duration;

public class ConductTestController {
	@FXML private Label totalNumberOfQuestionsLabel;
	@FXML private Label numberOfCurrentQuestionLabel;
	@FXML private Label timeLeftLabel;
	@FXML private TextArea questionTextArea;
	@FXML private FlowPane answersFlowPane;
	private LocalDateTime testEndTime;
	private Integer currentQuestionNumber = 0;

	private void bindToTime() {
		Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0), new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent actionEvent) {
				LocalDateTime currentTime = LocalDateTime.now();

				Long diffInSeconds = ChronoUnit.SECONDS.between(currentTime, testEndTime);
				Long secondsToDisplay = diffInSeconds % 60;
				Long minutesToDisplay = (diffInSeconds / 60) % 60;
				Long hoursToDisplay = (diffInSeconds / 3600) % 24;

				timeLeftLabel.setText(
						(hoursToDisplay.toString() + ":" + minutesToDisplay.toString() + ":" + secondsToDisplay.toString()));
			}
		}), new KeyFrame(Duration.seconds(1)));
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();
	}

	private void refresh() {

	}

	@FXML public void initialize() {
		numberOfCurrentQuestionLabel.setText(currentQuestionNumber.toString());
		testEndTime = LocalDateTime.now().plusHours(13);
		bindToTime();
	}

	@FXML private void sendTest() {

	}

	@FXML private void previousQuestion() {

	}

	@FXML private void nextQuestion() {

	}
}
