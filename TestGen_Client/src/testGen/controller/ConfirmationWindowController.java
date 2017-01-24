package testGen.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import testGen.model.Controller;

public class ConfirmationWindowController implements Controller {
	@FXML Parent confirmationWindow;
	@FXML private TextArea confirmationMessage;
	private RequestType requestType;

	public void setRequestType(RequestType requestType) {
		this.requestType = requestType;
	}

	public void setConfirmationMessage(String text) {
		confirmationMessage.setText(text);
	}

	@FXML public void yesBtnEnterKey(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			ApplicationController.makeRequest(requestType);
			closeWindow(confirmationWindow);
		}
	}

	@FXML public void yesBtn() {
		ApplicationController.makeRequest(requestType);
		closeWindow(confirmationWindow);
	}

	@FXML public void noBtnEnterKey(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			closeWindow(confirmationWindow);
		}
	}

	@FXML public void noBtn(ActionEvent event) {
		closeWindow(confirmationWindow);
	}
}
