package testGen.model;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import testGen.Client;
import testGen.controller.ConfirmationWindowController;
import testGen.controller.DialogController;
import testGen.controller.FeedController;
import testGen.controller.ModifyPostController;

public interface Controller {
	public enum TestFilter {
		PAST, FUTURE, ONGOING, ALL
	};

	public enum RequestType {
		UPDATE_TEST_FEED, REQUEST_JOINING_TEST, 
		REQUEST_LEAVING_TEST, REQUEST_REMOVING_TEST, 
		REQUEST_LOGOUT
	};

	public final FeedController fc = new FeedController();

	default public void loadScene(Stage stage, String path, int w, int h, boolean resizable, int minW, int minH) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Client.class.getResource(path));
			Parent layout = (Parent) loader.load();
			stage.hide();
			stage.setMaximized(false);
			stage.setWidth(w);
			stage.setHeight(h + 25);
			stage.setMinWidth(minW);
			stage.setMinHeight(minH + 25);
			stage.setResizable(resizable);
			Scene scene = new Scene(layout);
			scene.getStylesheets().add(Client.class.getResource("application.css").toExternalForm());
			stage.setScene(scene);
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	default public void loadScene(Parent parent, String path, int w, int h, boolean resizable, int minW, int minH) {
		Stage sourceStage = (Stage) parent.getScene().getWindow();
		loadScene(sourceStage, path, w, h, resizable, minW, minH);
	}

	default public void loadScene(Event event, String path, int w, int h, boolean resizable, int minW, int minH) {
		Stage sourceStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		loadScene(sourceStage, path, w, h, resizable, minW, minH);
	}

	default public void openNewWindow(Parent sourceWindow, String path, int minW, int minH, boolean resizable,
			String title) {
		openNewWindow(new FXMLLoader(), sourceWindow, path, minW, minH, resizable, false, title);
	}

	default public void openNewWindow(Parent sourceWindow, String path, int minW, int minH, boolean resizable,
			boolean modal, String title) {
		openNewWindow(new FXMLLoader(), sourceWindow, path, minW, minH, resizable, modal, title);
	}

	default public void openNewWindow(FXMLLoader loader, Parent sourceWindow, String path, int minW, int minH,
			boolean resizable, String title) {
		openNewWindow(loader, sourceWindow, path, minW, minH, resizable, false, title);
	}

	default public void openModifyPostWindow(Parent sourceWindow, Post postToEdit) {
		Stage sourceStage = (Stage) sourceWindow.getScene().getWindow();
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Client.class.getResource("view/ModifyPostLayout.fxml"));
		Stage newStage = new Stage();
		Scene newScene = null;
		try {
			newScene = new Scene(loader.load());
			newScene.getStylesheets().add(Client.class.getResource("application.css").toExternalForm());
			newStage.setMaxHeight(250);
			newStage.setMaxWidth(300);
			newStage.initModality(Modality.WINDOW_MODAL);
			newStage.initOwner(sourceStage);
			newStage.setScene(newScene);
			newStage.setResizable(false);
			newStage.setTitle("Edytuj post");
			ModifyPostController controller = loader.<ModifyPostController>getController();
			controller.setPostToEdit(postToEdit);
			newStage.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	default public void openNewWindow(FXMLLoader loader, Parent sourceWindow, String path, int minW, int minH,
			boolean resizable, boolean modality, String title) {
		Stage sourceStage = (Stage) sourceWindow.getScene().getWindow();
		loader.setLocation(Client.class.getResource(path));
		Stage newStage = new Stage();
		Scene newScene = null;
		try {
			newScene = new Scene(loader.load());
			newStage.setMinHeight(minH + 25);
			newStage.setMinWidth(minW);
			newStage.initOwner(sourceStage);
			if (modality) {
				newStage.initModality(Modality.WINDOW_MODAL);
			}
			newScene.getStylesheets().add(Client.class.getResource("application.css").toExternalForm());
			newStage.setScene(newScene);

			newStage.setResizable(resizable);
			if (title != null) {
				newStage.setTitle(title);
			}
			newStage.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	default public void openConfirmationWindow(Parent window, String message, RequestType requestType) {
		openConfirmationWindow(window, "view/ConfirmationLayout.fxml", message, requestType, "Powiadomienie");
	}

	default public void openConfirmationWindow(Parent window, String path, String title) {
		openConfirmationWindow(window, path, null, null, title);
	}

	default public void openConfirmationWindow(Parent window, String path, String message, RequestType requestType,
			String title) {
		Stage sourceStage = (Stage) window.getScene().getWindow();
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Client.class.getResource(path));
		Stage newStage = new Stage();
		Scene newScene = null;
		try {
			newScene = new Scene(loader.load());
			newScene.getStylesheets().add(Client.class.getResource("application.css").toExternalForm());
			newStage.initModality(Modality.WINDOW_MODAL);
			newStage.initOwner(sourceStage);
			newStage.setScene(newScene);
			newStage.setResizable(false);
			newStage.setTitle(title);
			if (requestType != null) {
				ConfirmationWindowController controller = loader.<ConfirmationWindowController>getController();
				controller.setConfirmationMessage(message);
				controller.setRequestType(requestType);
			}
			newStage.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	default public void openDialogBox(Parent window, String message) {
		openDialogBox(window, message, false);
	}

	default public void openDialogBox(Parent window, String message, boolean modality) {
		Stage sourceStage = (Stage) window.getScene().getWindow();
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Client.class.getResource("view/DialogBoxLayout.fxml"));
		Stage newStage = new Stage();
		Scene newScene = null;
		try {
			newScene = new Scene(loader.load());
			newScene.getStylesheets().add(Client.class.getResource("application.css").toExternalForm());
			if (modality) {
				newStage.initModality(Modality.WINDOW_MODAL);
			}
			newStage.initOwner(sourceStage);
			newStage.setScene(newScene);
			newStage.setResizable(false);
			newStage.setTitle("Powiadomienie");
			DialogController controller = loader.<DialogController>getController();
			controller.setDialogMessage(message);
			newStage.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	default public void closeWindow(Parent window) {
		Stage stage = (Stage) window.getScene().getWindow();
		stage.close();
	}

	default public void closeWindow(Event event) {
		Parent window = (Parent) event.getSource();
		Stage stage = (Stage) window.getScene().getWindow();
		stage.close();
	}

	public default String doHash(String password) {
		String result = "";
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(password.getBytes());

			byte byteArray[] = md.digest();
			StringBuffer hash = new StringBuffer();

			for (int i = 0; i < byteArray.length; i++) {
				hash.append(Integer.toString((byteArray[i] & 0xff) + 0x100, 16).substring(1));
			}
			result = hash.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return result;
	}
}
