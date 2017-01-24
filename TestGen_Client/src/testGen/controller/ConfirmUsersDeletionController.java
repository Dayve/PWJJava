package testGen.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import testGen.model.NetworkConnection;
import testGen.model.SocketEvent;
import testGen.model.User;
import testGen.model.Controller;

public class ConfirmUsersDeletionController implements Controller {
	
	@FXML private Parent confirmUserDeletionWindow;
	@FXML private TextField confirmLogin;
	@FXML private TextField confirmPassword;
	private String message;
	
	private void reqDeleteUser() {
		User caller = new User(confirmLogin.getText(), 
				doHash(confirmPassword.getText()));
		SocketEvent e = new SocketEvent("reqDeleteUser", caller);
		NetworkConnection.sendSocketEvent(e);
		SocketEvent res = NetworkConnection.rcvSocketEvent("deleteUserSucceeded", "deleteUserFailed");
		String resName = res.getName();
		message = res.getObject(String.class);
		if (message == null) {
			message = "Nie można połączyć się z serwerem.";
		}
		// run in JavaFX after background thread finishes work
		Platform.runLater(new Runnable() {
			@Override public void run() {
				openDialogBox(confirmUserDeletionWindow, message);
				closeWindow(confirmUserDeletionWindow);
				if (resName.equals("deleteUserSucceeded")) {
					ApplicationController.makeRequest(RequestType.REQUEST_LOGOUT);
				}
//				refresh();
			}
		});
	}

	@FXML private void confirmDeletingAccount() {
		if (confirmLogin.getText().length() > 0  && confirmPassword.getText().length() > 0) {
			new Thread(() -> reqDeleteUser()).start();
		} else {
			openDialogBox(confirmUserDeletionWindow, "Podaj swój login i hasło.");
		}
	}
	
	@FXML private void closeBtnEnterKey(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			closeWindow(confirmUserDeletionWindow);
		}
	}

	@FXML public void closeWindowBtn(ActionEvent event) {
		closeWindow(confirmUserDeletionWindow);
	}
}
