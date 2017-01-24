package testGen.controller;

import java.io.File;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import testGen.model.Controller;
import testGen.model.NetworkConnection;
import testGen.model.Paper;
import testGen.model.SocketEvent;

public class UploadFileController implements Controller {
	@FXML private Parent fileUploadWindow;
	
	@FXML private Button chooseFromDiskButton;
	@FXML private Button confirmationButton;
	@FXML private Label chosenFileLabel;
	@FXML private TextArea fileDescriptionBox;
	
	private FileChooser fileChooser = new FileChooser();
	private File chosenFile = null;
	
	private static Integer selectedConferenceId = new Integer(-1);
	
	public static FileManagerController caller = null;
	
	
	public static void setSelectedConferenceId(Integer givenId) {
		selectedConferenceId = givenId;
	}
	
	
	@FXML public void initialize() {
		chooseFromDiskButton.setOnAction(
		    new EventHandler<ActionEvent>() {
		        @Override
		        public void handle(final ActionEvent e) {
		        	chosenFile = fileChooser.showOpenDialog((Stage)fileUploadWindow.getScene().getWindow());
		            if(chosenFile != null) { 
		            	setSelectedFileInfoLabel("Plik: " + chosenFile.getName());
		            }
		        }
		    }
		);
	}
	
	
	private void setSelectedFileInfoLabel(String newContent) {
		chosenFileLabel.setText(newContent);
	}
	
	
	@FXML private void onSendActionConfirmed() {
		new Thread(() -> readAndSendFile()).start();
	}
	
	
	private void readAndSendFile() {	
		Paper examplePaper = new Paper();
		
		examplePaper.createFromExistingFile(chosenFile.getAbsolutePath(), 
				ApplicationController.currentUser.getName() + " " + ApplicationController.currentUser.getSurname(),
				ApplicationController.currentUser.getId(),
				selectedConferenceId, fileDescriptionBox.getText());
		
		SocketEvent se = new SocketEvent("fileSentToServer", examplePaper.getWholeBufferAsByteArray());
		NetworkConnection.sendSocketEvent(se);
		
		SocketEvent res = NetworkConnection.rcvSocketEvent("fileReceivedByServer", "errorWhileSavingFile");
		String eventName = res.getName();
		
		final String message;
		
		if (eventName.equals("fileReceivedByServer")) {
			message = "Zapisano plik na serwerze.";
			caller.fetchCurrentlyStoredFiles();
		} else {
			message = "Nie udało się zapisać pliku na serwerze.";
		}

		Platform.runLater(new Runnable() {
			@Override public void run() {
				openDialogBox(fileUploadWindow, message);
				closeWindow(fileUploadWindow);
			}
		});
	}
}
