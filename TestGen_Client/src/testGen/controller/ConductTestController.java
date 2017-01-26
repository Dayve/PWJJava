package testGen.controller;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

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
	@FXML private LocalDateTime testEndTime;

	private void bindToTime() {
    	Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0), new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent actionEvent) {
				LocalDateTime currentTime = LocalDateTime.now();
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

				long diffInSeconds = ChronoUnit.SECONDS.between(currentTime, testEndTime);
				long diffInMinutes = ChronoUnit.MINUTES.between(currentTime, testEndTime);
				long diffInHours = ChronoUnit.HOURS.between(currentTime, testEndTime);

				timeLeftLabel.setText(simpleDateFormat.format(diffInHours + ":" + diffInMinutes + ":" + diffInSeconds));

			}
		}), new KeyFrame(Duration.seconds(1)));
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();
	}

	private void refresh() {

	}

	@FXML public void initialize() {
		//numberOfCurrentQuestionLabel;
	}

	private void previousQuestion() {

	}

	private void nextQuestion() {

	}
}
