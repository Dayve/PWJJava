package testGen.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import testGen.model.Controller;

public class DialogController implements Controller {

	@FXML Parent dialogBoxWindow;
	@FXML private TextArea dialogMessage;

	public void setDialogMessage(String text) {
		dialogMessage.setText(text);
	}

	@FXML private void closeBtnEnterKey(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			closeWindow(dialogBoxWindow);
		}
	}

	@FXML public void closeWindowBtn(ActionEvent event) {
		closeWindow(dialogBoxWindow);
	}
}
