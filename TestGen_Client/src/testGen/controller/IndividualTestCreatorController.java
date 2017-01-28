package testGen.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import testGen.model.Answer;
import testGen.model.Category;
import testGen.model.Test;
import testGen.model.Controller;
import testGen.model.NetworkConnection;
import testGen.model.Question;
import testGen.model.SocketEvent;

public class IndividualTestCreatorController implements Controller {

	@FXML Parent individualTestCreatorWindow;

	public static ApplicationController caller = null;

	@FXML private TextField numberOfQuestionsBox;
	@FXML private TextField numberOfAnswersBox;

	@FXML private ComboBox<String> categoriesComboBox;

	@FXML private ComboBox<String> durationHrCB;
	@FXML private ComboBox<String> durationMinCB;

	@FXML private CheckBox multipleChoiceCheckBox;

	private Test receivedTestWithQuestions;

	// Date which will be used to initialize the DatePicker:
	private String message = new String("MSG");
	private String eventName;

	@FXML public void initialize() {
		ObservableList<String> hours = FXCollections.observableArrayList("00",
				"01", "02", "03", "04", "05", "06", "07", "08", "09", "10",
				"11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
				"21", "22", "23");
		ObservableList<String> minutes = FXCollections.observableArrayList("00",
				"05", "10", "15", "20", "25", "30", "35", "40", "45", "50",
				"55");

		durationHrCB.getItems().addAll(hours);
		durationMinCB.getItems().addAll(minutes);

		// new Thread(() -> fetchAllCategoriesList()).start();
		fetchAllCategoriesList();
	}

	// This function is called when a day is clicked (from CalendarController):

	private void fetchAllCategoriesList() {
		SocketEvent se = new SocketEvent("reqAllCategories");
		NetworkConnection.sendSocketEvent(se);

		SocketEvent res = NetworkConnection.rcvSocketEvent("categoriesFetched",
				"categoriesFetchingError");
		String eventName = res.getName();

		if (eventName.equals("categoriesFetched")) {
			ArrayList<Category> categories = (ArrayList<Category>) res
					.getObject(ArrayList.class);
			ObservableList<String> categoryNames = FXCollections
					.observableArrayList();

			for (Category cat : categories) {
				categoryNames.add(cat.getCategoryName());
			}

			categoriesComboBox.getItems().addAll(categoryNames);
		} else {
			categoriesComboBox.getItems().addAll(FXCollections
					.observableArrayList("Brak kategorii do wyboru"));
		}
	}

	@FXML public void reqAddTest() {
		String name = new String("Test indywidualny użytkownika " + ApplicationController.currentUser.getLogin());
		Integer numberOfQuestions = null, numberOfAnswers= null;
		
		try {
			numberOfQuestions = Integer.parseInt(numberOfQuestionsBox.getText());
			numberOfAnswers = Integer.parseInt(numberOfAnswersBox.getText());
		} catch (NumberFormatException formatExc) {
			Platform.runLater(new Runnable() {
				@Override public void run() {
					openDialogBox(individualTestCreatorWindow, "Proszę uzupełnić wszystkie pola", true);
				}
			});
			return;
		}

		String category = categoriesComboBox.getValue();
		String description = new String("Test indywidualny.\n" 
				+ "Autor: " + ApplicationController.currentUser.getLogin() + "\n"
				+ "Czas utworzenia rządania: " + LocalDateTime.now().toString().replace('T', ' ')
		);

		// get LocalDateTime from LocalDate
		String durationHr = durationHrCB.getSelectionModel().getSelectedItem();
		String durationMin = durationMinCB.getSelectionModel().getSelectedItem();

		// check if all hour and min combo boxes are filled
		if (category != null && durationHr != null && durationMin != null
				&& numberOfQuestions != null && numberOfAnswers != null) {

			LocalDateTime startTime = LocalDateTime.now();
			LocalDateTime endTime = LocalDateTime.now().plusHours(Long.parseLong(durationHr)).plusMinutes(Long.parseLong(durationMin));

			Test newTest = new Test(name, category, numberOfQuestions, numberOfAnswers, startTime, endTime, description,
					ApplicationController.currentUser);
			
			// ========================== 
			
			SocketEvent se = new SocketEvent("generateQuestionSetForTest", newTest);
			NetworkConnection.sendSocketEvent(se);
			SocketEvent res = NetworkConnection.rcvSocketEvent("questionsFetched", "questionsFetchingError");

			eventName = res.getName();
			
			if (eventName.equals("questionsFetched")) {
				receivedTestWithQuestions = res.getObject(Test.class);
			}
			
			// ========================== 

			se = new SocketEvent("reqAddTest", newTest);
			NetworkConnection.sendSocketEvent(se);

			res = NetworkConnection.rcvSocketEvent("addTestSucceeded", "addTestFailed");
			eventName = res.getName();

			if (eventName.equals("addTestSucceeded")) {
				message = "Dodano nowy test do bazy danych.";
				ApplicationController.makeRequest(RequestType.UPDATE_TEST_FEED);
			}
			else if (eventName.equals("addTestFailed")) {
				message = res.getObject(String.class);
			}
			else {
				message = "Nie udało się dodać testu. Serwer nie odpowiada.";
			}
		} else {
			message = "Proszę wypełnić wszystkie pola z godziną i minutą oraz upewnić się, że wybrano kategorię.";
		}
		Platform.runLater(new Runnable() {
			@Override public void run() {
				openDialogBox(individualTestCreatorWindow, message, true);
				
				if(eventName.equals("addTestSucceeded")) {
					ConductTestController.conductedTest = receivedTestWithQuestions;
					openNewWindow(individualTestCreatorWindow, 
							"view/ConductTestLayout.fxml", 800, 500, true, "Test indywidualny");
				}
			}
		});
	}

	@FXML public void startIndividualTestBtn() {
		new Thread(() -> reqAddTest()).start();
	}

	@FXML private void startIndividualTestBtnEnterKey(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			new Thread(() -> reqAddTest()).start();
		}
	}

	@FXML public void closeWindowBtn(ActionEvent event) {
		closeWindow(individualTestCreatorWindow);
	}

	@FXML private void closeBtnEnterKey(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			closeWindow(individualTestCreatorWindow);
		}
	}

}
