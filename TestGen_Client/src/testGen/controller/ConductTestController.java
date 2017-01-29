package testGen.controller;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import testGen.model.Answer;
import testGen.model.Controller;
import testGen.model.NetworkConnection;
import testGen.model.Result;
import testGen.model.SocketEvent;
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
	private Integer currentQuestionIndex;

	private static Timeline timeline;

	private void requestTestWithQuestions() {
		String eventName;
		SocketEvent se = new SocketEvent("generateQuestionSetForTest",
				conductedTest);
		NetworkConnection.sendSocketEvent(se);
		SocketEvent res = NetworkConnection.rcvSocketEvent("questionsFetched",
				"questionsFetchingError");

		eventName = res.getName();

		if (eventName.equals("questionsFetched")) {
			conductedTest = res.getObject(Test.class);
		}
	}

	private void bindToTime() {
		Long timerDuration = ChronoUnit.SECONDS.between(LocalDateTime.now(),
				conductedTest.getEndTime());
		timeline = new Timeline(new KeyFrame(Duration.seconds(0),
				new EventHandler<ActionEvent>() {
					@Override public void handle(ActionEvent actionEvent) {
						LocalDateTime currentTime = LocalDateTime.now();

						Long diffInSeconds = ChronoUnit.SECONDS.between(
								currentTime, conductedTest.getEndTime());
						Long secondsToDisplay = diffInSeconds % 60;
						Long minutesToDisplay = (diffInSeconds / 60) % 60;
						Long hoursToDisplay = (diffInSeconds / 3600) % 24;

						String secondsToDisplayStr = secondsToDisplay < 10
								? "0" + secondsToDisplay.toString()
								: secondsToDisplay.toString();

						String minutesToDisplayStr = minutesToDisplay < 10
								? "0" + minutesToDisplay.toString()
								: minutesToDisplay.toString();

						String hoursToDisplayStr = hoursToDisplay < 10
								? "0" + hoursToDisplay.toString()
								: hoursToDisplay.toString();

						timeLeftLabel.setText(
								(hoursToDisplayStr + ":" + minutesToDisplayStr
										+ ":" + secondsToDisplayStr));
						System.out.println(diffInSeconds);
					}
				}), new KeyFrame(Duration.seconds(1)));
		timeline.setCycleCount(timerDuration.intValue() + 1);
		timeline.play();
	}

	private void updateQuestionAndAnswersContainers() {
		questionContentLabel.setText(conductedTest.getQuestions()
				.get(currentQuestionIndex).getContent());
		Integer questionDisplayNumber = currentQuestionIndex + 1;
		numberOfCurrentQuestionLabel.setText(questionDisplayNumber.toString());

		answersFlowPane.getChildren().clear();
		for (Answer answer : conductedTest.getQuestions()
				.get(currentQuestionIndex).getPossibleAnswers()) {

			Label answerLabel = new Label(answer.getAnswerContent());
			answerLabel.setId(answer.getId().toString());
			answerLabel.setPrefWidth(2017);
			answerLabel.setStyle(
					"-fx-font: 18px Inconsolata; -fx-padding: 5 5 5 5;");

			answersFlowPane.getChildren().add(answerLabel);
		}
	}

	@FXML public void initialize() {
		
		requestTestWithQuestions();
		System.out.println("Dostałem test: " + conductedTest.getName());
		if (conductedTest == null) {
			closeWindow(conductTestWindow);
		}
		currentQuestionIndex = 0;

		totalNumberOfQuestionsLabel
				.setText(conductedTest.getnOfQuestions().toString());
		updateQuestionAndAnswersContainers();
		bindToTime();
		Platform.runLater(new Runnable() {
			@Override public void run() {
				Stage stage = (Stage) conductTestWindow.getScene().getWindow();
				stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
					public void handle(WindowEvent we) {
						timeline.stop();
					}
				});
			}
		});
	}

	private void stopTest() {
		timeline.stop();
//		String eventName;
//		SocketEvent se = new SocketEvent("reqCheckTest",
//				conductedTest);
//		NetworkConnection.sendSocketEvent(se);
//		SocketEvent res = NetworkConnection.rcvSocketEvent(
//				"questionsFetched", "questionsFetchingError");
//
//		eventName = res.getName();
//
//		if (eventName.equals("questionsFetched")) {
//			conductedTest = res.getObject(Test.class);
//		}
	}

	@FXML private void checkTest() {
		String eventName;
		SocketEvent se = new SocketEvent("checkThisTest", conductedTest);
		NetworkConnection.sendSocketEvent(se);
		SocketEvent res = NetworkConnection.rcvSocketEvent("testVerified",
				"testVerifyingError");

		eventName = res.getName();

		if (eventName.equals("testVerified")) {
			Result testsResult = res.getObject(Result.class);
			System.out.println(testsResult.getPartialResultDescriptions());
		}
	}

	private void updateTestsAnswers() {
		SocketEvent se = new SocketEvent("updateTestsAnswers", conductedTest);
		NetworkConnection.sendSocketEvent(se);
	}

	@FXML private void previousQuestion() {
		if (currentQuestionIndex > 0) {
			currentQuestionIndex--;
		} else {
			currentQuestionIndex = conductedTest.getQuestions().size() - 1;
		}
		updateQuestionAndAnswersContainers();

		new Thread(() -> updateTestsAnswers());
	}

	@FXML private void nextQuestion() {
		if (currentQuestionIndex < conductedTest.getQuestions().size() - 1) {
			currentQuestionIndex++;
		} else {
			currentQuestionIndex = 0;
		}
		updateQuestionAndAnswersContainers();

		new Thread(() -> updateTestsAnswers());
	}
}
