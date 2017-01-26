
package testGen.controller;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import testGen.model.Controller;
import testGen.model.NetworkConnection;
import testGen.model.User;
import testGen.model.SocketEvent;

public class LoginRegisterController implements Controller {

	@FXML private Parent loginWindow;
	@FXML private Parent registrationWindow;
	@FXML private TextField loginField;
	@FXML private TextField nameField;
	@FXML private TextField surnameField;
	@FXML private TextField passwordField;
	@FXML private TextField passwordRepeatField;
	private String eventName = null;

	private String message;

	public static void ConnectToServer() {
		NetworkConnection.connect("localhost", 9090);
	}

	public void initialize() {
		// get a method to call it using reflection
		new Thread(() -> ConnectToServer()).start();
	}

	public void reqLogin() {
		String login = loginField.getText();
		String password = doHash(passwordField.getText());
		
		User u = new User(login, password);
		SocketEvent se = new SocketEvent("reqLogin", u);

		NetworkConnection.sendSocketEvent(se);
		SocketEvent res = NetworkConnection.rcvSocketEvent("loginSucceeded", "loginFailed");
		
		eventName = res.getName();
		if (eventName.equals("loginFailed")) {
			message = "Niepoprawny login lub hasło.";
		}

		// run in JavaFX after background thread finishes work

		Platform.runLater(new Runnable() {
			@Override public void run() {
				if (eventName == null) {
					message = "Nie można ustanowić połączenia z serwerem.";
				}
				if (eventName != null && eventName.equals("loginSucceeded")) {
					loadScene(loginWindow, "view/ApplicationLayout.fxml", 1165, 675, true, 1165, 675);
				} else {
					openDialogBox(loginWindow, message);
				}
			}
		});
	}

	public void reqRegister() {
		String login = loginField.getText();
		String password = doHash(passwordField.getText());
		String rePassword = doHash(passwordRepeatField.getText());
		String name = nameField.getText();
		String surname = surnameField.getText();

		if (password.equals(rePassword)) {
			User u = new User(login, password, name, surname);
			SocketEvent e = new SocketEvent("reqRegister", u);

			NetworkConnection.sendSocketEvent(e);
			SocketEvent res = NetworkConnection.rcvSocketEvent("registerSucceeded", "registerFailed");

			message = res.getObject(String.class);
		} else {
			message = "Podane hasła nie są identyczne.";
		}

		// run in JavaFX after background thread finishes work
		Platform.runLater(new Runnable() {
			@Override public void run() {
				openDialogBox(registrationWindow, message);
			}
		});
	}

	@FXML private void registerBtn() {
		new Thread(() -> reqRegister()).start();
	}

	@FXML private void loginBtn() { // handler
		new Thread(() -> reqLogin()).start();
	}

	@FXML private void loginBtnEnterKey(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			new Thread(() -> reqLogin()).start();
		}
	}

	@FXML private void registerBtnEnterKey(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			new Thread(() -> reqRegister()).start();
		}
	}

	@FXML private void goToRegistrationKey(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			loadScene(loginWindow, "view/RegisterLayout.fxml", 320, 300, false, 0, 0);
		}
	}

	@FXML private void cancelBtnEnterKey(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			loadScene(registrationWindow, "view/LoginLayout.fxml", 320, 250, false, 0, 0);
		}
	}

	@FXML private void goToLogin(Event event) {
		loadScene(registrationWindow, "view/LoginLayout.fxml", 320, 250, false, 0, 0);
	}

	@FXML private void goToRegistration(Event event) {
		loadScene(loginWindow, "view/RegisterLayout.fxml", 320, 300, false, 0, 0);
	}
}
