package testGen.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import testGen.model.Category;
import testGen.model.Test;
import testGen.model.Controller;
import testGen.model.NetworkConnection;
import testGen.model.SocketEvent;
import testGen.model.User;

public class TestCreatorController implements Controller {

	@FXML Parent testCreatorWindow;
	
	@FXML private TextField nameField;
	@FXML private TextField numberOfQuestionsBox;
	@FXML private TextField numberOfAnswersBox;
	
	@FXML private ComboBox<String> categoriesComboBox;
	
	@FXML private DatePicker startDateField;
	@FXML private DatePicker endDateField;
	
	@FXML private ComboBox<String> startHr;
	@FXML private ComboBox<String> startMin;
	@FXML private ComboBox<String> endHr;
	@FXML private ComboBox<String> endMin;
	
	@FXML private TextArea descriptionField;
		
	// Date which will be used to initialize the DatePicker:
	private static LocalDate testDestinedDay = LocalDate.now();
	private String message;

	@FXML public void initialize() {
		ObservableList<String> hours = FXCollections.observableArrayList("00", "01", "02", "03", "04", "05", "06", "07",
				"08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23");
		ObservableList<String> minutes = FXCollections.observableArrayList("00", "05", "10", "15", "20", "25", "30",
				"35", "40", "45", "50", "55");
		
		startHr.getItems().addAll(hours);
		endHr.getItems().addAll(hours);
		startMin.getItems().addAll(minutes);
		endMin.getItems().addAll(minutes);
		startDateField.setValue(testDestinedDay);
		endDateField.setValue(testDestinedDay);
		
		new Thread(() -> fetchAllCategoriesList()).start();
	}
	
	// This function is called when a day is clicked (from CalendarController):
	public static void setChosenDay(LocalDate when) {
		testDestinedDay = when;
	}
	
	private void fetchAllCategoriesList() {
		SocketEvent se = new SocketEvent("reqAllCategories");
		NetworkConnection.sendSocketEvent(se);

		SocketEvent res = NetworkConnection.rcvSocketEvent("categoriesFetched", "categoriesFetchingError");
		String eventName = res.getName();
		
		if (eventName.equals("categoriesFetched")) {
			ArrayList<Category> categories = (ArrayList<Category>) res.getObject(ArrayList.class);
			ObservableList<String> categoryNames = FXCollections.observableArrayList();
			
			for(Category cat : categories) {
				categoryNames.add(cat.getCategoryName());
			}
			
			categoriesComboBox.getItems().addAll(categoryNames);
		}
		else {
			categoriesComboBox.getItems().addAll(FXCollections.observableArrayList("Brak kategorii do wyboru"));
		}
	}

	@FXML public void reqAddTest() {
		String name = nameField.getText();
		Integer numberOfQuestions = Integer.parseInt(numberOfQuestionsBox.getText());
		Integer numberOfAnswers = Integer.parseInt(numberOfAnswersBox.getText());

		String category = categoriesComboBox.getValue();
		String description = descriptionField.getText();

		// get LocalDateTime from LocalDate
		LocalDateTime startDate = startDateField.getValue().atStartOfDay();
		LocalDateTime endDate = endDateField.getValue().atStartOfDay();
		String startHrCB = startHr.getSelectionModel().getSelectedItem();
		String startMinCB = startMin.getSelectionModel().getSelectedItem();
		String endHrCB = endHr.getSelectionModel().getSelectedItem();
		String endMinCB = endMin.getSelectionModel().getSelectedItem();

		// check if all hour and min combo boxes are filled
		if (category != null && startHrCB != null && startMinCB != null && endHrCB != null && endMinCB != null) {

			LocalDateTime startTime = startDate.plusHours(Long.parseLong(startHrCB)).plusMinutes(Long.parseLong(startMinCB));
			LocalDateTime endTime = endDate.plusHours(Long.parseLong(endHrCB)).plusMinutes(Long.parseLong(endMinCB));

			Test conf = new Test(name, category, numberOfQuestions, numberOfAnswers, startTime, endTime, description,
					ApplicationController.currentUser);

			SocketEvent se = new SocketEvent("reqAddTest", conf);
			NetworkConnection.sendSocketEvent(se);

			SocketEvent res = NetworkConnection.rcvSocketEvent("addTestSucceeded", "addTestFailed");
			String eventName = res.getName();

			if (eventName.equals("addTestSucceeded")) {
				message = "Dodano konferencję.";
				ApplicationController.makeRequest(RequestType.UPDATE_TEST_FEED);
			}
			else if (eventName.equals("addTestFailed")) {
				message = res.getObject(String.class);
			}
			else {
				message = "Nie udało się dodać konferencji. Serwer nie odpowiada.";
			}
		} else {
			message = "Wypełnij wszystkie pola z godziną i minutą.";
		}
		Platform.runLater(new Runnable() {
			@Override public void run() {
				openDialogBox(testCreatorWindow, message, true);
			}
		});

	}

	@FXML public void addTestBtn() {
		new Thread(() -> reqAddTest()).start();
	}

	@FXML private void addTestBtnEnterKey(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			new Thread(() -> reqAddTest()).start();
		}
	}

	@FXML public void closeWindowBtn(ActionEvent event) {
		closeWindow(testCreatorWindow);
	}

	@FXML private void closeBtnEnterKey(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			closeWindow(testCreatorWindow);
		}
	}

}
