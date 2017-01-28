package testGen.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

import com.sun.javafx.scene.control.skin.TableHeaderRow;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import testGen.Client;
import testGen.model.Test;
import testGen.model.Controller;
import testGen.model.NetworkConnection;
import testGen.model.SocketEvent;
import testGen.model.User;
import testGen.model.User.UsersRole;
import testGen.model.Week;

@SuppressWarnings("restriction")
public class ApplicationController implements Controller {

	@FXML Parent applicationWindow;
	@FXML ComboBox<String> monthsCB;
	@FXML private ComboBox<String> yearsCB;
	@FXML private Button prevMonth;
	@FXML private Button nextMonth;
	@FXML private TableView<Week> calendarTable; // A TableView representing the
													// calendar
	@FXML Button joinLeaveManageTestBtn;
	@FXML Button removeTestBtn;
	@FXML Button filesMenuButton;
	@FXML private ListView<Label> testFeedList;
	@FXML private AnchorPane feedAnchorPane;
	@FXML private ComboBox<String> testFeedCB;
	@FXML private ComboBox<String> testFeedNumberCB;
	@FXML private Label loginLabel;
	@FXML private ListView<Label> listOfSelectedDaysEvents;
	@FXML private TabPane eventDetailsTP;
	@FXML private TextField searchField;
	@FXML private TextArea forumsMessage;

	private CalendarController calendar = new CalendarController();

	public static User currentUser;
	private String message = null;
	private int checkedRequestsWithoutUpdate = 0;

	private TestFilter filter;

	public enum feedReqPeriod {
		PAST, FUTURE, ALL
	};

	public static final int CHAR_LIMIT_IN_TITLEPANE = 30;

	private static LinkedBlockingQueue<RequestType> requestQueue = new LinkedBlockingQueue<RequestType>();

	@FXML public void initialize() {
		new Thread(() -> reqCurrentUser()).start();
		setupFeedFilterCBs();
		setupTabPane();
		setupTimer();
		setupForumTextArea();
		setupMonthsYearsCBs();
		setupCalendar();

		Platform.runLater(new Runnable() {
			@Override public void run() {
				reqTestFeed(currentUser.getId());
				loginLabel.setText(currentUser.getLogin());
				setupTabResizeEvent();
			}
		});
		fc.mainApplicationWindow = applicationWindow;
	}

	// static method allowing other controllers to make requests
	// which will be fulfilled by ApplicationController with every timer's tick
	public static void makeRequest(RequestType newRequest) {
		requestQueue.add(newRequest);
	}

	private void setupForumTextArea() {
		forumsMessage.setVisible(false);
		forumsMessage.setOnKeyPressed(new EventHandler<KeyEvent>() {

			@Override public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ENTER
						& (event.isControlDown() | event.isShiftDown())) {
					forumsMessage.setText(forumsMessage.getText() + "\n");
					forumsMessage.end();
				} else if (event.getCode() == KeyCode.ENTER) {
					if (forumsMessage.getText().length() > 0) {
						reqSendForumMessage(forumsMessage.getText());
						forumsMessage.clear();
						fc.refreshTestTab(eventDetailsTP,
								fc.getSelectedTestId(), fc.getFeed());
					}
					event.consume();
				}
			}
		});
	}

	private void setupTabResizeEvent() {
		Stage mainStage = (Stage) applicationWindow.getScene().getWindow();

		mainStage.heightProperty().addListener(new ChangeListener<Number>() {
			@Override public void changed(
					ObservableValue<? extends Number> arg0, Number arg1,
					Number arg2) {
				fc.resizeSelectedTestTab(eventDetailsTP, arg2.intValue());
			}
		});
	}

	// Sets up the TabPane - makes it modify selectedTestId on tab selection
	// change
	private void setupTabPane() {
		eventDetailsTP.getSelectionModel().selectedItemProperty()
				.addListener(new ChangeListener<Tab>() {
					@Override public void changed(
							ObservableValue<? extends Tab> ov, Tab from,
							Tab to) {
						if (to != null) {
							fc.resizeSelectedTestTab(eventDetailsTP,
									applicationWindow.getScene().getWindow()
											.heightProperty().getValue()
											.intValue());
							fc.setSelectedTestId(Integer.parseInt(to.getId()));
							checkUsersParticipation();

						} else {
							fc.setSelectedTestId(null);
							fc.refreshTestTab(eventDetailsTP,
									fc.getSelectedTestId(), fc.getFeed());
							checkUsersParticipation();
						}
					}
				});
	}

	// sets up the ComboBoxes allowing user to filter tests
	private void setupFeedFilterCBs() {
		ObservableList<String> feedOptions = FXCollections.observableArrayList(
				"Nadchodzące testy", "Wszystkie testy", "Zakończone testy");

		testFeedCB.getItems().addAll(feedOptions);
		testFeedCB.setValue("Nadchodzące testy");

		ObservableList<String> feedNumberOptions = FXCollections
				.observableArrayList("5", "15", "30", "60", "...");

		testFeedNumberCB.getItems().addAll(feedNumberOptions);
		testFeedNumberCB.setValue("15");

		searchField.textProperty().addListener(obs -> {
			refreshTestListView(searchField.getText());
		});
	}

	private void setupMonthsYearsCBs() {
		ObservableList<String> monthsFeedOptions = FXCollections
				.observableArrayList("Styczeń", "Luty", "Marzec", "Kwiecień",
						"Maj", "Czerwiec", "Lipiec", "Sierpień", "Wrzesień",
						"Październik", "Listopad", "Grudzień");

		monthsCB.getItems().addAll(monthsFeedOptions);
		monthsCB.setValue("miesiąc");

		ObservableList<String> yearsFeedOptions = FXCollections
				.observableArrayList("2016", "2017", "2018", "2019", "2020");

		yearsCB.getItems().addAll(yearsFeedOptions);
		yearsCB.setValue("rok");
	}

	// Sets the calendar up - fills it according to the current date and lets
	// user select its cells
	private void setupCalendar() {
		calendar.setCalendarsDate(LocalDate.now());
		calendar.fillCalendarTable(calendarTable, fc.getFeed(), eventDetailsTP,
				listOfSelectedDaysEvents);
		calendarTable.getSelectionModel().setCellSelectionEnabled(true);
		// Set initial ComboBox values
		String currentDateInPolish = CalendarController
				.localDateToPolishDateString(calendar.getCalendarsDate());
		monthsCB.setValue(currentDateInPolish.substring(0,
				currentDateInPolish.indexOf(" ")));
		yearsCB.setValue(currentDateInPolish
				.substring(currentDateInPolish.indexOf(" ") + 1));

		calendarTable.widthProperty().addListener(new ChangeListener<Number>() {
			@Override public void changed(
					ObservableValue<? extends Number> source, Number oldWidth,
					Number newWidth) {
				TableHeaderRow header = (TableHeaderRow) calendarTable
						.lookup("TableHeaderRow");
				header.reorderingProperty()
						.addListener(new ChangeListener<Boolean>() {
							@Override public void changed(
									ObservableValue<? extends Boolean> observable,
									Boolean oldValue, Boolean newValue) {
								header.setReordering(false);
							}
						});
			}
		});
	}

	// sets the timer up - every second timer checks requestsQueue, which
	// contains
	// tasks from other controllers for ApplicationController to perform
	private void setupTimer() {
		Client.timer = new Timer();
		Client.timer.scheduleAtFixedRate(new TimerTask() {
			@Override public void run() {
				Platform.runLater(new Runnable() {
					@Override public void run() {
						// TODO: requestQueue.contains() and remove() should be
						// changed to something
						// more appropriate once we extend requestType
						fc.refreshTestTab(eventDetailsTP,
								fc.getSelectedTestId(), fc.getFeed());
						if (requestQueue.contains(RequestType.UPDATE_TEST_FEED)
								|| checkedRequestsWithoutUpdate > 10) {
							reqTestFeed(currentUser.getId());
							checkedRequestsWithoutUpdate = 0;
							requestQueue.remove(RequestType.UPDATE_TEST_FEED);
						} else
							checkedRequestsWithoutUpdate++;

						if (requestQueue
								.contains(RequestType.REQUEST_JOINING_TEST)) {
							reqJoinTest();
							requestQueue
									.remove(RequestType.REQUEST_JOINING_TEST);
						}

						if (requestQueue
								.contains(RequestType.REQUEST_LEAVING_TEST)) {
							reqLeaveTest();
							requestQueue
									.remove(RequestType.REQUEST_LEAVING_TEST);
						}

						if (requestQueue
								.contains(RequestType.REQUEST_REMOVING_TEST)) {
							reqRemoveTest();
							requestQueue
									.remove(RequestType.REQUEST_REMOVING_TEST);
						}
						if (requestQueue.contains(RequestType.REQUEST_LOGOUT)) {
							logout();
						}
					}
				});
			}
		}, 0, 200);
	}

	public static UsersRole usersRoleOnTheTest(User user, Integer testId) {
		Test test = null;

		for (Test c : fc.getFeed()) {
			if (testId.equals(c.getId())) {
				test = c;
			}
		}
		for (User u : test.getParticipants()) {
			if (u.getId().equals(user.getId()))
				return UsersRole.PARTICIPANT;
		}
		for (User u : test.getOrganizers()) {
			if (u.getId().equals(user.getId()))
				return UsersRole.ORGANIZER;
		}
		for (User u : test.getPending()) {
			if (u.getId().equals(user.getId()))
				return UsersRole.PENDING;
		}
		return UsersRole.NONE;
	}

	private void reqSendForumMessage(String message) {
		if (fc.getSelectedTestId() != null) {
			if (message.length() > 0) {
				ArrayList<Integer> userIdTestId = new ArrayList<Integer>();
				userIdTestId.add(currentUser.getId());
				userIdTestId.add(fc.getSelectedTestId());
				SocketEvent se = new SocketEvent("reqSendForumMessage",
						userIdTestId, message);

				NetworkConnection.sendSocketEvent(se);
				SocketEvent res = NetworkConnection.rcvSocketEvent(
						"sendForumMessageSucceeded", "sendForumMessageFailed");
				String eventName = res.getName();
				if (!eventName.equals("sendForumMessageSucceeded")) {
					Platform.runLater(new Runnable() {
						@Override public void run() {
							openDialogBox(applicationWindow,
									"Wiadomość nie została wysłana.");
						}
					});
				}
			}
		}
	}

	// filters feed depending on test CB's value - future/all/past
	// tests
	@FXML private void filterFeed() {
		String feedPeriodCB = testFeedCB.getValue();
		filter = TestFilter.ALL;
		if (feedPeriodCB.equals("Zakończone testy")) {
			filter = TestFilter.PAST;
		} else if (feedPeriodCB.equals("Nadchodzące testy")) {
			filter = TestFilter.FUTURE;
		}
		ArrayList<Test> filtered = fc.filterFeed(fc.getFeed(), filter,
				testFeedNumberCB.getValue());
		Platform.runLater(new Runnable() {
			@Override public void run() {
				fc.fillListWithLabels(testFeedList, filtered, eventDetailsTP,
						filter, CHAR_LIMIT_IN_TITLEPANE, true,
						testFeedNumberCB.getValue());
				refreshTestListView(searchField.getText());
			}
		});
	}

	// checks if currentUser participates in given (selected) test
	// and modifies leave/join button text and behaviour accordingly
	private void checkUsersParticipation() {
		Integer selectedTestId = fc.getSelectedTestId();
		// look for test thats id is clicked
		if (selectedTestId != null) {
			try {
				Test selectedTest = fc.getSelectedTest();
				UsersRole role = usersRoleOnTheTest(currentUser,
						selectedTestId);
				switch (role) {
					case ORGANIZER: {
						removeTestBtn.setDisable(false);
						filesMenuButton.setDisable(false);
						joinLeaveManageTestBtn.setText("Zarządzaj");
						// if the test has already ended, don't let change
						// users' roles
						if (selectedTest.getEndTime()
								.isAfter(LocalDateTime.now())) {
							// the test didn't end yet
							if (selectedTest.getStartTime()
									.isBefore(LocalDateTime.now())) {
								// if the test has begun but didn't end yet
								joinLeaveManageTestBtn.setDisable(false);
								
								joinLeaveManageTestBtn.setOnAction((event) -> {
									ConductTestController.conductedTest = fc.getSelectedTest();
									conductTest();
								});
								joinLeaveManageTestBtn.setText("Rozwiąż");
							} else {
								// the test didn't begin nor ended
								joinLeaveManageTestBtn.setDisable(false);
								joinLeaveManageTestBtn.setOnAction((event) -> {
									manageTestBtn();
								});
							}
						} else {
							// the test has ended
							joinLeaveManageTestBtn.setDisable(true);
						}
						GridPane.setRowSpan(eventDetailsTP, 4);
						forumsMessage.setVisible(true);
						break;
					}
					case PARTICIPANT: {
						if (selectedTest.getEndTime()
								.isAfter(LocalDateTime.now())) {
							// the test didn't end yet
							joinLeaveManageTestBtn.setDisable(false);
							joinLeaveManageTestBtn.setOnAction((event) -> {
								new Thread(() -> leaveTestBtn()).start();
							});
							joinLeaveManageTestBtn.setText("Wycofaj się");
							if (selectedTest.getStartTime()
									.isBefore(LocalDateTime.now())) {
								// if the test has begun but didn't end yet
								joinLeaveManageTestBtn.setDisable(false);
								joinLeaveManageTestBtn.setOnAction((event) -> {
									ConductTestController.conductedTest = fc.getSelectedTest();
									conductTest();
								});
								joinLeaveManageTestBtn.setText("Rozwiąż");
							} else {
								// the test didn't begin nor ended
								joinLeaveManageTestBtn.setDisable(true);
							}
						}
						removeTestBtn.setDisable(true);
						
						filesMenuButton.setDisable(false);
						GridPane.setRowSpan(eventDetailsTP, 4);
						forumsMessage.setVisible(true);
						break;
					}
					case NONE: {
						joinLeaveManageTestBtn.setDisable(false);
						filesMenuButton.setDisable(true);
						removeTestBtn.setDisable(true);
						GridPane.setRowSpan(eventDetailsTP, 5);
						forumsMessage.setVisible(false);
						joinLeaveManageTestBtn.setOnAction((event) -> {
							new Thread(() -> joinTestBtn()).start();
						});
						joinLeaveManageTestBtn.setText("Weź udział");
						break;
					}
					case PENDING: {
						joinLeaveManageTestBtn.setDisable(false);
						filesMenuButton.setDisable(true);
						removeTestBtn.setDisable(true);
						GridPane.setRowSpan(eventDetailsTP, 5);
						forumsMessage.setVisible(false);
						joinLeaveManageTestBtn.setOnAction((event) -> {
							new Thread(() -> leaveTestBtn()).start();
						});
						joinLeaveManageTestBtn.setText("Wycofaj się");
						break;
					}
					default:
						break;
				}
			} catch (NoSuchElementException e) {
				fc.setSelectedTestId(null);
				checkUsersParticipation();
			}
		} else { // if no test is selected
			filesMenuButton.setDisable(true);
			removeTestBtn.setDisable(true);
			forumsMessage.setVisible(false);
			joinLeaveManageTestBtn.setDisable(true);
		}
	}

	// requests data about test from the database through the server
	// compares it with current data and if there is difference, updates
	// information
	@SuppressWarnings("unchecked") @FXML public void reqTestFeed(
			Integer callerId) {
		SocketEvent e = new SocketEvent("reqTestFeed", callerId);
		NetworkConnection.sendSocketEvent(e);
		SocketEvent res = NetworkConnection.rcvSocketEvent("updateTestFeed");

		String eventName = res.getName();
		ArrayList<Test> tempFeed;

		if (eventName.equals("updateTestFeed")) {
			// get temp feed to compare it with current one
			tempFeed = res.getObject(ArrayList.class);
			// fc.setFeed(tempFeed);
			if (tempFeed != null
					&& !tempFeed.toString().equals(fc.getFeed().toString())) {
				fc.setFeed(tempFeed);
				// run in JavaFX after background thread finishes work
				// compare if feeds match, if so, don't fill vbox with new
				// content
				Platform.runLater(new Runnable() {
					@Override public void run() {
						ArrayList<Test> feed = fc.getFeed();
						fc.refreshTestTabs(eventDetailsTP, feed);
						// fill FeedBox and Calendar in JavaFX UI Thread
						checkUsersParticipation();
						filterFeed();
						calendar.refreshCalendarTable(calendarTable,
								calendar.getCalendarsDate(), feed,
								eventDetailsTP, listOfSelectedDaysEvents);
						refreshTestListView(searchField.getText());
						fc.fillListViewWithSelectedDaysTests(
								calendar.getCalendarsDate(), feed,
								eventDetailsTP, listOfSelectedDaysEvents, false,
								testFeedNumberCB.getValue());
					}
				});
			}
		}

	}

	// bound to searchField, this is reaction to typed text
	private void refreshTestListView(String searchBoxContent) {

		String periodFilterFromComboBox = testFeedCB.getValue();
		filter = TestFilter.ALL;
		if (periodFilterFromComboBox.equals("Zakończone testy")) {
			filter = TestFilter.PAST;
		} else if (periodFilterFromComboBox.equals("Nadchodzące testy")) {
			filter = TestFilter.FUTURE;
		}

		ArrayList<Test> filteringResults = new ArrayList<Test>();

		for (Test iteratedTest : fc.filterFeed(fc.getFeed(), filter,
				testFeedNumberCB.getValue())) {
			if (iteratedTest.getName().toLowerCase()
					.contains(searchBoxContent.toLowerCase())
					|| iteratedTest.getCategory().toLowerCase()
							.contains(searchBoxContent.toLowerCase())) {
				filteringResults.add(iteratedTest);
			}
		}

		Platform.runLater(new Runnable() {
			@Override public void run() {
				fc.fillListWithLabels(testFeedList, filteringResults,
						eventDetailsTP, filter, CHAR_LIMIT_IN_TITLEPANE, true,
						testFeedNumberCB.getValue());
			}
		});
	}

	// sends request for the current user object and puts it in currentUser
	// static variable
	public static void reqCurrentUser() {
		SocketEvent se = new SocketEvent("reqCurrentUser");
		NetworkConnection.sendSocketEvent(se);
		SocketEvent res = NetworkConnection
				.rcvSocketEvent("currentUserSucceeded");

		String eventName = res.getName();
		if (eventName.equals("currentUserSucceeded")) {
			currentUser = res.getObject(User.class);
		}
	}

	public void conductTest() {
		openNewWindow(applicationWindow, "view/ConductTestLayout.fxml", 800,
				500, true, true, "Test: \"" + fc.getSelectedTest().getName() + "\"");
	}

	@FXML private void startIndividualTestCreationBtn() {
		IndividualTestCreatorController.caller = this;
		openNewWindow(applicationWindow,
				"view/IndividualTestCreatorLayout.fxml", 500, 270, false,
				"Rozpocznij indywidualny test");
	}

	@FXML public void manageTestBtn() {
		Integer selectedTestId = fc.getSelectedTestId();
		if (selectedTestId != null) {
			String selectedTestName = fc.getSelectedTest().getName();
			openNewWindow(applicationWindow, "view/TestManagerLayout.fxml", 650,
					600, false,
					"Zarządzaj testem \"" + selectedTestName + "\"");
		}
	}

	@FXML public void manageFilesBtn() {
		Integer selectedTestId = fc.getSelectedTestId();

		if (selectedTestId != null) {
			UploadFileController.setSelectedTestId(selectedTestId);
			String selectedTestName = fc.getSelectedTest().getName();
			openNewWindow(applicationWindow, "view/FileManagerLayout.fxml", 700,
					500, false,
					"Zarządzaj plikami testu \"" + selectedTestName + "\"");
		}
	}

	// sends request to join a test after user confirms it
	@FXML public void joinTestBtn() {
		Integer selectedTestId = fc.getSelectedTestId();

		if (selectedTestId != null) {
			String testName = fc.getSelectedTest().getName();

			String message = "Czy na pewno chcesz wziąć udział w teście \""
					+ testName + "\"?";
			Platform.runLater(new Runnable() {
				@Override public void run() {
					openConfirmationWindow(applicationWindow, message,
							RequestType.REQUEST_JOINING_TEST);
					fc.refreshTestTab(eventDetailsTP, fc.getSelectedTestId(),
							fc.getFeed());
				}
			});
		}
	}

	// actual request for joining a test
	private void reqJoinTest() {
		ArrayList<Integer> userIdTestId = new ArrayList<Integer>();
		userIdTestId.add(currentUser.getId());
		userIdTestId.add(fc.getSelectedTestId());

		SocketEvent se = new SocketEvent("reqJoinTest", userIdTestId);
		NetworkConnection.sendSocketEvent(se);

		SocketEvent res = NetworkConnection.rcvSocketEvent("joinTestSucceeded",
				"joinTestFailed");
		String eventName = res.getName();
		if (eventName.equals("joinTestSucceeded")) {
			reqTestFeed(currentUser.getId());
			message = "Wysłano prośbę o udział w teście do jej organizatora.";
		} else {
			message = "Nie udało się zapisać na test.";
		}

		Platform.runLater(new Runnable() {
			@Override public void run() {
				openDialogBox(applicationWindow, message);
			}
		});
	}

	// sends request to leave a test after user confirms it
	@FXML public void leaveTestBtn() {
		Integer selectedTestId = fc.getSelectedTestId();

		if (selectedTestId != null) {
			String testName = fc.getSelectedTest().getName();
			String message = "Czy na pewno chcesz zrezygnować z udziału w teście \""
					+ testName + "\"?";

			Platform.runLater(new Runnable() {
				@Override public void run() {
					openConfirmationWindow(applicationWindow, message,
							RequestType.REQUEST_LEAVING_TEST);
					fc.refreshTestTab(eventDetailsTP, fc.getSelectedTestId(),
							fc.getFeed());
				}
			});
		}
	}

	// actual request for leaving a test
	private void reqLeaveTest() {
		ArrayList<Integer> userIdTestId = new ArrayList<Integer>();
		userIdTestId.add(currentUser.getId());
		userIdTestId.add(fc.getSelectedTestId());

		SocketEvent se = new SocketEvent("reqLeaveTest", userIdTestId);
		NetworkConnection.sendSocketEvent(se);

		SocketEvent res = NetworkConnection.rcvSocketEvent("leaveTestSucceeded",
				"leaveTestFailed");
		String eventName = res.getName();

		if (eventName.equals("leaveTestSucceeded")) {
			reqTestFeed(currentUser.getId());
			message = "Zrezygnowałeś z udziału w teście.";
		} else {
			message = "Nie udało się zrezygnować z udziału w teście.";
		}

		Platform.runLater(new Runnable() {
			@Override public void run() {
				openDialogBox(applicationWindow, message);
			}
		});
	}

	@FXML public void removeTestBtn() {
		Integer selectedConfId = fc.getSelectedTestId();
		if (selectedConfId != null) {
			String testName = fc.getSelectedTest().getName();
			String message = "Czy na pewno chcesz usunąć test \"" + testName
					+ "\"?";
			Platform.runLater(new Runnable() {
				@Override public void run() {
					openConfirmationWindow(applicationWindow, message,
							RequestType.REQUEST_REMOVING_TEST);
				}
			});
		}
	}

	// actual request for leaving a test
	private void reqRemoveTest() {
		SocketEvent se = new SocketEvent("reqRemoveTest",
				fc.getSelectedTestId());
		NetworkConnection.sendSocketEvent(se);

		SocketEvent res = NetworkConnection
				.rcvSocketEvent("removeTestSucceeded", "removeTestFailed");
		String eventName = res.getName();
		if (eventName.equals("removeTestSucceeded")) {
			reqTestFeed(currentUser.getId());
			message = "Udało się usunąć test.";
		} else {
			message = "Nie udało się usunąć testu.";
		}

		Platform.runLater(new Runnable() {
			@Override public void run() {
				openDialogBox(applicationWindow, message);
			}
		});
	}

	@FXML private void logoutButton() {
		// here check if login is valid
		new Thread(() -> logout()).start();
	}

	public void logout() {
//		NetworkConnection.serverCommunicationTimer.cancel();
		NetworkConnection.disconnect();
		fc.clear();
		Client.timer.cancel();
		requestQueue.clear();
		NetworkConnection.serverCommunicationTimer.cancel();
		NetworkConnection.serverCommunicationTimer = null;
		new Thread(() -> NetworkConnection.connect("localhost", 9090)).start();

		Platform.runLater(new Runnable() {
			@Override public void run() {
				loadScene(applicationWindow, "view/LoginLayout.fxml", 320, 250,
						false, 0, 0);
			}
		});
	}

	@FXML public void addTestBtn() {
		openNewWindow(applicationWindow, "view/TestCreatorLayout.fxml", 600,
				650, false, "Dodaj test");
	}

	@FXML public void editProfileBtn() {
		openNewWindow(applicationWindow, "view/ProfileEditorLayout.fxml", 400,
				465, false, "Edytuj profil");
	}

	public void changeMonthToNext() {
		calendar.setCalendarsDate(calendar.getCalendarsDate().plusMonths(1));
		calendar.refreshCalendarTable(calendarTable,
				calendar.getCalendarsDate(), fc.getFeed(), eventDetailsTP,
				listOfSelectedDaysEvents);
		updateComboBoxesAccordingToDate(calendar.getCalendarsDate());
	}

	public void changeMonthToPrevious() {
		calendar.setCalendarsDate(calendar.getCalendarsDate().minusMonths(1));
		calendar.refreshCalendarTable(calendarTable,
				calendar.getCalendarsDate(), fc.getFeed(), eventDetailsTP,
				listOfSelectedDaysEvents);
		updateComboBoxesAccordingToDate(calendar.getCalendarsDate());
	}

	public void changeMonthToChosen() {
		String polishMonth = monthsCB.getValue();
		String engShortMonth = calendar
				.PolishDateStringToEngDateString(polishMonth);
		try {
			Date date = new SimpleDateFormat("MMM", Locale.ENGLISH)
					.parse(engShortMonth);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			int month = cal.get(Calendar.MONTH) + 1; // months begin with 0
			calendar.setCalendarsDate(
					calendar.getCalendarsDate().withMonth(month));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		calendar.refreshCalendarTable(calendarTable,
				calendar.getCalendarsDate(), fc.getFeed(), eventDetailsTP,
				listOfSelectedDaysEvents);
	}

	public void changeYearToChosen() {
		int year = Integer.parseInt(yearsCB.getValue());
		calendar.setCalendarsDate(calendar.getCalendarsDate().withYear(year));
		calendar.refreshCalendarTable(calendarTable,
				calendar.getCalendarsDate(), fc.getFeed(), eventDetailsTP,
				listOfSelectedDaysEvents);
	}

	private void updateComboBoxesAccordingToDate(LocalDate givenDate) {
		// Set new ComboBox values:
		String currentDateInPolish = CalendarController
				.localDateToPolishDateString(givenDate);
		monthsCB.setValue(currentDateInPolish.substring(0,
				currentDateInPolish.indexOf(" ")));
		yearsCB.setValue(currentDateInPolish
				.substring(currentDateInPolish.indexOf(" ") + 1));
	}
}
