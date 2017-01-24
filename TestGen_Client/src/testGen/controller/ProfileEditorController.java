package testGen.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import testGen.model.Controller;
import testGen.model.NetworkConnection;
import testGen.model.SocketEvent;
import testGen.model.User;

public class ProfileEditorController implements Controller {
	@FXML private Parent profileEditorWindow;
	@FXML private Label titleField;
	@FXML private TextField nameField;
	@FXML private TextField surnameField;
	@FXML private TextField currentPasswordField;
	@FXML private TextField newPasswordField;
	@FXML private TextField emailField;
	@FXML private TextField newPasswordRepeatField;
	@FXML private TextField organizationField;
	@FXML private TextField confirmLogin;
	private String login;
	private String currentPassword;
	private String message;

	private void setTextField(TextInputControl textDestination, String text) {
		if (text == null) {
			System.out.println("");
			text = "";
		}
		textDestination.setText(text);
	}

	private void refresh() {
//		ApplicationController.reqCurrentUser();

		setTextField(nameField, ApplicationController.currentUser.getName());
		setTextField(surnameField, ApplicationController.currentUser.getSurname());
		setTextField(emailField, ApplicationController.currentUser.getEmail());
		setTextField(organizationField, ApplicationController.currentUser.getOrganization());
	}

	@FXML public void initialize() {
		login = ApplicationController.currentUser.getLogin();
		refresh();
		titleField.setText(login + ": edycja profilu");
	}

	@FXML public void reqUpdateProfile() {
		currentPassword = doHash(currentPasswordField.getText());
		String newPassword = newPasswordField.getText();
		String reNewPassword = newPasswordField.getText();
		String name = nameField.getText();
		String surname = surnameField.getText();
		String email = emailField.getText();
		String organization = organizationField.getText();

		if (newPassword.equals(reNewPassword) && (newPassword.length() >= 6 || newPassword.length() == 0)) {
			/*
			 * if new password/ repeat new password fields are empty then just
			 * pass null, otherwise demand at least 6 characters and equal
			 * passwords and hash these passwords
			 */
			if (newPassword.length() > 0) {
				newPassword = doHash(newPassword);
				reNewPassword = doHash(reNewPassword);
			} else {
				newPassword = null;
				reNewPassword = null;
			}
			User u = new User(ApplicationController.currentUser.getId(), login, 
					newPassword, name, surname, email, organization);
			SocketEvent e = new SocketEvent("reqUpdateProfile", u, currentPassword);

			NetworkConnection.sendSocketEvent(e);
			SocketEvent res = NetworkConnection.rcvSocketEvent("updateProfileSucceeded", 
					"updateProfileFailed");
			message = res.getObject(String.class);
			User newFetchedProfile = res.getObject(User.class);
			if (newFetchedProfile != null) {
				ApplicationController.currentUser = newFetchedProfile;
			}
		} else {
			message = "Podane hasła nie są identyczne lub nie dłuższe niż 5 znaków.";
		}

		// run in JavaFX after background thread finishes work
		Platform.runLater(new Runnable() {
			@Override public void run() {
				openDialogBox(profileEditorWindow, message);
				refresh();
			}
		});
	}

	@FXML private void confirmBtn() { // handler
		new Thread(() -> reqUpdateProfile()).start();
	}

	@FXML private void confirmBtnEnterKey(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			new Thread(() -> reqUpdateProfile()).start();
		}
	}

	@FXML private void deleteAccountBtn() {
		openNewWindow(profileEditorWindow, "view/ConfirmUsersDeletionLayout.fxml", 
				300, 270, false, "Usuń konto");
	}

	@FXML private void deleteBtnEnterKey(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			deleteAccountBtn();
		}
	}

	@FXML private void closeBtnEnterKey(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			closeWindow(profileEditorWindow);
		}
	}

	@FXML public void closeWindowBtn(ActionEvent event) {
		closeWindow(profileEditorWindow);
	}
}
