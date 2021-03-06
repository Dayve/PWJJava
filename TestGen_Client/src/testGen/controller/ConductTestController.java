package testGen.controller;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import testGen.model.Answer;
import testGen.model.Controller;
import testGen.model.NetworkConnection;
import testGen.model.Question;
import testGen.model.Result;
import testGen.model.SocketEvent;
import testGen.model.Test;
import testGen.model.VerifiedQuestionDescription;

public class ConductTestController implements Controller {

	@FXML Parent conductTestWindow;

	@FXML private Label totalNumberOfQuestionsLabel;
	@FXML private Label numberOfCurrentQuestionLabel;
	@FXML private Label timeLeftLabel;
	@FXML private Label questionContentLabel;
	@FXML private TextArea questionTextArea;
	@FXML private FlowPane answersFlowPane;
	@FXML private ListView<Label> questionsListView;

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

		if (conductedTest.getIsSingleChoice()) {
			ToggleGroup radioGroup = new ToggleGroup();

			for (Answer answer : conductedTest.getQuestions()
					.get(currentQuestionIndex).getPossibleAnswers()) {
				RadioButton radioButton = new RadioButton(
						answer.getAnswerContent());

				radioButton.setId(answer.getId().toString());
				radioButton.setPrefWidth(answersFlowPane.getWidth());
				radioButton.setToggleGroup(radioGroup);
				radioButton.setStyle("-fx-font: 18px Inconsolata; -fx-padding: 5 5 5 5;");

				radioButton.setSelected(answer.getIsSelected());
				radioButton.selectedProperty()
						.addListener(new ChangeListener<Boolean>() {
							@Override public void changed(
									ObservableValue<? extends Boolean> observable,
									Boolean oldValue, Boolean newValue) {
								answer.setIsSelected(newValue.booleanValue());
							}
						});

				answersFlowPane.getChildren().add(radioButton);
			}
		} else {
			for (Answer answer : conductedTest.getQuestions()
					.get(currentQuestionIndex).getPossibleAnswers()) {
				CheckBox checkBox = new CheckBox(answer.getAnswerContent());

				checkBox.setId(answer.getId().toString());
				checkBox.setPrefWidth(answersFlowPane.getWidth());
				checkBox.setStyle("-fx-font: 18px Inconsolata; -fx-padding: 5 5 5 5;");

				checkBox.setSelected(answer.getIsSelected());
				checkBox.selectedProperty()
						.addListener(new ChangeListener<Boolean>() {
							@Override public void changed(
									ObservableValue<? extends Boolean> observable,
									Boolean oldValue, Boolean newValue) {
								answer.setIsSelected(newValue.booleanValue());
							}
						});
				answersFlowPane.getChildren().add(checkBox);
			}
		}

		questionsListView.getSelectionModel().select(currentQuestionIndex);
		questionsListView.getFocusModel().focus(currentQuestionIndex);
		questionsListView.scrollTo(currentQuestionIndex);
	}

	private void fillListOfQuestions() {
		ObservableList<Label> labelsList = FXCollections.observableArrayList();
		questionsListView.getItems().clear();

		Label label = null;
		int i = 0;

		for (Question question : conductedTest.getQuestions()) {
			label = new Label((i + 1) + ". " + question.getContent());
			label.setFont(Font.font("Inconsolata", 18));

			label.setId(new Integer(i).toString());
			label.setPrefWidth(1000);

			labelsList.add(label);
			i++;
		}
		questionsListView.setItems(labelsList);
	}

	@FXML public void initialize() {
		requestTestWithQuestions();
		if (conductedTest == null) {
			closeWindow(conductTestWindow);
		}
		currentQuestionIndex = 0;

		totalNumberOfQuestionsLabel
				.setText(conductedTest.getnOfQuestions().toString());

		fillListOfQuestions();
		bindToTime();

		questionsListView.getSelectionModel().selectedIndexProperty()
				.addListener(new ChangeListener<Number>() {
					@Override public void changed(
							ObservableValue<? extends Number> observable,
							Number oldValue, Number newValue) {

						currentQuestionIndex = newValue.intValue();
						updateQuestionAndAnswersContainers();
					}
				});

		Platform.runLater(new Runnable() {
			@Override public void run() {
				updateQuestionAndAnswersContainers();
				Stage stage = (Stage) conductTestWindow.getScene().getWindow();
				stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
					public void handle(WindowEvent we) {
						timeline.stop();
					}
				});
			}
		});
	}

//	private void stopTest() {
//		timeline.stop();
//	}

	@FXML private void checkTest() {
		SocketEvent se = new SocketEvent("checkThisTest", conductedTest);
		NetworkConnection.sendSocketEvent(se);
		SocketEvent res = NetworkConnection.rcvSocketEvent("testVerified",
				"testVerifyingError");

		final String eventName = res.getName();

		Platform.runLater(new Runnable() {
			@Override public void run() {
				if(eventName.equals("testVerified")) {
					timeline.stop();
					
					Result testResult = res.getObject(Result.class);
					TestResultController.testResult = testResult;
					
					openNewWindow(conductTestWindow,
							"view/TestResultLayout.fxml", 600, 600, false,
							"Wyniki testu: \"" + testResult.getTestName() + "\"");
					closeWindow(conductTestWindow);
				} else {
					openDialogBox(conductTestWindow, "Nie udało się uzyskać wyników testu.");
				}
			}
		});
	}

	private void updateTestsAnswers() {
		SocketEvent se = new SocketEvent("updateTestsAnswers", conductedTest);
		NetworkConnection.sendSocketEvent(se);
	}

	@FXML private void previousQuestion() {
		if (currentQuestionIndex > 0) {
			currentQuestionIndex--;
		}

		updateQuestionAndAnswersContainers();
		new Thread(() -> updateTestsAnswers());
	}

	@FXML private void nextQuestion() {
		if (currentQuestionIndex < conductedTest.getQuestions().size() - 1) {
			currentQuestionIndex++;
		}
		updateQuestionAndAnswersContainers();
		new Thread(() -> updateTestsAnswers());
	}
}