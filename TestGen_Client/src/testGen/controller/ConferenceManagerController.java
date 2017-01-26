package testGen.controller;

import java.util.ArrayList;
import java.util.HashMap;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import testGen.model.Test;
import testGen.model.Controller;
import testGen.model.NetworkConnection;
import testGen.model.SocketEvent;
import testGen.model.User;
import testGen.model.User.UsersRole;

public class ConferenceManagerController implements Controller {

	@FXML Parent confManagerWindow;
	@FXML private TextField searchUserField;
	@FXML private ListView<Label> usersLV;

	@FXML private ComboBox<String> userOperationCB;
	@FXML private ComboBox<String> fileOperationCB;

	private int selectedConferenceId;
	private Test selectedConference;
	private HashMap<Integer, User> selectedUsers = new HashMap<Integer, User>();
	private HashMap<Integer, User> deselectedUsers = new HashMap<Integer, User>();

	private String message;
	private boolean notAnAdminAnymore = false;

	private void setSelectedConference(Test c) {
		selectedConference = c;
	}

	public void refresh() {
		fillUsersList();
		for (Label l : usersLV.getItems()) {
			l.setStyle(selectedUsers.containsKey(Integer.parseInt(l.getId())) ? "-fx-font-weight: bold;"
					: "-fx-font-weight: normal;");
		}
	}

	private void setupFilterCBs() {
		ObservableList<String> userActions = FXCollections.observableArrayList("Ustaw status: organizator",
				"Ustaw status: sponsor", "Ustaw status: prelegent", "Ustaw status: uczestnik", "Wyproś");

		userOperationCB.getItems().addAll(userActions);
	}

	private void addUserLabelsWithRoles(ObservableList<Label> ol, ArrayList<User> group, String role) {
		Label label = null;
		for (User u : group) {
			String title = u.getLogin() + " (" + u.getName() + " " + u.getSurname() + "), " + role;
			label = new Label(FeedController.addNLsIfTooLong(title, 35));

			label.setId(u.getId().toString());
			label.setPrefWidth(usersLV.getPrefWidth());
			if(!selectedUsers.containsKey(u.getId())) {
				deselectedUsers.put(u.getId(), u);
			}
			label.setOnMouseClicked(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent t) {
					Label selectedLabel = ((Label) usersLV.getSelectionModel().getSelectedItem());
					Integer selectedUsersId = Integer.parseInt(selectedLabel.getId());
					if (selectedUsers.containsKey(selectedUsersId)) {
						selectedUsers.remove(selectedUsersId);
						deselectedUsers.put(selectedUsersId, u);
						selectedLabel.setStyle("-fx-font-weight: normal;");
					} else {
						deselectedUsers.remove(selectedUsersId);
						selectedUsers.put(selectedUsersId, u);
						selectedLabel.setStyle("-fx-font-weight: bold;");
					}
				}
			});
			ol.add(label);
		}
	}

	public void fillUsersList() {
		ObservableList<Label> ol = FXCollections.observableArrayList();
		usersLV.getItems().clear();
		ArrayList<ArrayList<User>> selectedConferencesUsersGroups = new ArrayList<ArrayList<User>>();
		selectedConferencesUsersGroups.add(selectedConference.getOrganizers());
		selectedConferencesUsersGroups.add(selectedConference.getParticipants());
		selectedConferencesUsersGroups.add(selectedConference.getPending());
		String[] roles = { "organizator", "uczestnik", "oczekujący" };
		for (int i = 0; i < selectedConferencesUsersGroups.size(); i++) {
			addUserLabelsWithRoles(ol, selectedConferencesUsersGroups.get(i), roles[i]);
		}
		usersLV.setItems(ol);
	}

	@FXML public void initialize() {
		selectedConferenceId = fc.getSelectedConferenceId();
		selectedConference = fc.getSelectedConference();

		setupFilterCBs();
		fillUsersList();
	}

	private void deselectAllUsers() {
		for (Label l : usersLV.getItems()) {
			l.setStyle("-fx-font-weight: normal;");
		}
		deselectedUsers.putAll(selectedUsers);
		selectedUsers.clear();
	}

	private void selectAllUsers() {
		for (Label l : usersLV.getItems()) {
			l.setStyle("-fx-font-weight: bold;");
		}
		selectedUsers.putAll(deselectedUsers);
		deselectedUsers.clear();
	}

	@FXML public void deselectSelectAllUsers() {
		if (selectedUsers.isEmpty()) {
			selectAllUsers();
		} else {
			deselectAllUsers();
		}
	}

	@FXML public void confirmUserOperationBtn() {
		new Thread(() -> confirmUserOperation()).start();
	}

	private boolean willThereBeAnyOrganizerLeft() {
		ArrayList<User> organizers = selectedConference.getOrganizers();
		for (User u : deselectedUsers.values()) {
			if (organizers.contains(u)) {
				return true; // if there is any deselected organizer, break
			}
		}
		return false; // if all deselected users are checked and none of them is
						// an organizer
	}

	@FXML public void confirmUserOperation() {
		String operation = userOperationCB.getValue();
		if(operation == null) {
			return ;
		}
		ArrayList<Integer> usersIds = new ArrayList<Integer>(selectedUsers.keySet());
		User.UsersRole targetRole = null;
		
		switch (operation) {
			case "Ustaw status: organizator": {
				targetRole = UsersRole.ORGANIZER;
				break;
			}
			case "Ustaw status: uczestnik": {
				targetRole = UsersRole.PARTICIPANT;
				break;
			}
			case "Wyproś": {
				targetRole = UsersRole.NONE;
				break;
			}
			default:
				break;
		}

		if (ApplicationController.usersRoleOnConference(ApplicationController.currentUser,
				selectedConferenceId) == UsersRole.ORGANIZER) {
			if (targetRole != null && usersIds.size() > 0) {
				if (targetRole == UsersRole.ORGANIZER || willThereBeAnyOrganizerLeft()) {
					SocketEvent se = new SocketEvent("reqSetRole", targetRole, selectedConferenceId, usersIds);
					NetworkConnection.sendSocketEvent(se);

					SocketEvent res = NetworkConnection.rcvSocketEvent("setRoleSucceeded", 
							"expellSucceeded", "setRoleFailed", "expellFailed");
					String eventName = res.getName();

					if (eventName.equals("setRoleSucceeded") || eventName.equals("expellSucceeded")) {
						setSelectedConference(res.getObject(Test.class));
						message = "Pomyślnie wprowadzono zmiany.";
						ApplicationController.makeRequest(RequestType.UPDATE_CONFERENCE_FEED);
					} else if (eventName.equals("setRoleFailed")) {
						message = "Nie udało się wprowadzić zmian.";
					} else {
						message = "Nie udało się wprowadzić zmian. Serwer nie odpowiada.";
					}
				} else {
					message = "Po wykonaniu zmian musi pozostać co najmniej jeden organizator.";
				}
			} else {
				message = "Zaznacz conajmniej jednego użytkownika i akcję do wykonania.";
			}
		} else {
			notAnAdminAnymore = true;
		}

		Platform.runLater(new Runnable() {
			@Override public void run() {

				if (notAnAdminAnymore) {
					// if theres no current user in organizers arraylist
					message = " Nie jesteś już organizatorem i menadżer zostanie zamknięty.";
					notAnAdminAnymore = true;
				}
				refresh();
				openDialogBox(confManagerWindow, message, true);
				if (notAnAdminAnymore) {
					Stage st = (Stage) confManagerWindow.getScene().getWindow();
					st.close();
				}
			}
		});
	}

	@FXML public void closeWindowBtn(ActionEvent event) {
		closeWindow(confManagerWindow);
	}

	@FXML private void closeBtnEnterKey(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			closeWindow(confManagerWindow);
		}
	}
}
