package testGen.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import testGen.model.Controller;
import testGen.model.NetworkConnection;
import testGen.model.Post;
import testGen.model.SocketEvent;

public class ModifyPostController implements Controller {
	@FXML private Parent modifyPostWindow;
	@FXML private TextArea postsContent;
	private Post postToEdit = null;

	
	public void setPostToEdit(Post postToEdit) {
		this.postToEdit = new Post(postToEdit);
		postsContent.setText(postToEdit.getContent());
	}
	
	private void reqEditPost() {
		String msgContent = postsContent.getText();
		
		if (msgContent.length() > 0) {
			postToEdit.setMessage(msgContent);
			SocketEvent e = new SocketEvent("reqEditPost", ApplicationController.currentUser, postToEdit);

			NetworkConnection.sendSocketEvent(e);
			SocketEvent res = NetworkConnection.rcvSocketEvent("editPostSucceeded", "editPostFailed");
			String evtName = res.getName();
			if(evtName.equals("editPostFailed")) {
				Platform.runLater(new Runnable() {
					@Override public void run() {
						openDialogBox(modifyPostWindow, "Edycja posta nie powiodła się.");
					}
				});
			}
		}
	}

	@FXML private void confirmPostsModification() {
		new Thread(() -> reqEditPost()).start();
	}

	@FXML private void closeBtnEnterKey(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			closeWindow(modifyPostWindow);
		}
	}

	@FXML public void closeWindowBtn(ActionEvent event) {
		closeWindow(modifyPostWindow);
	}
}
