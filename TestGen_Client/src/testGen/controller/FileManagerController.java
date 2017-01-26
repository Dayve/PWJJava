package testGen.controller;

import testGen.model.Test;
import testGen.model.Controller;
import testGen.model.FileInfo;
import testGen.model.NetworkConnection;
import testGen.model.Paper;
import testGen.model.SocketEvent;

import java.io.File;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class FileManagerController implements Controller {
	@FXML Parent fileManagerWindow;

	@FXML private TextField fileSearchBar;
	@FXML private TableView<FileInfo> filesTableView;

	@FXML Button addNewFileButton;
	@FXML Button downloadFileButton;
	@FXML Button removeFileButton;

	private FileChooser fileChooser = new FileChooser();
	private File chosenFile = null;

	// Those FileInfo objects contain fileIDs, as opposed to those created from
	// local files
	private static ArrayList<FileInfo> filesForThisTest = new ArrayList<FileInfo>();

	public Test browsedTest = null;

	private void setButtonsPriviledges() {
		switch (ApplicationController.usersRoleOnTheTest(ApplicationController.currentUser,
				browsedTest.getId())) {
			case ORGANIZER: {
				removeFileButton.setDisable(false);
				addNewFileButton.setDisable(false);
				downloadFileButton.setDisable(false);
				break;
			}
			case PARTICIPANT: {
				addNewFileButton.setDisable(true);
				downloadFileButton.setDisable(false);
				removeFileButton.setDisable(true);
				break;
			}
			case PENDING:
			case NONE: {
				addNewFileButton.setDisable(true);
				downloadFileButton.setDisable(true);
				removeFileButton.setDisable(true);
				break;
			}
			default:
				break;
		}
	}

	@FXML public void initialize() {
		browsedTest = fc.getSelectedTest();

		fetchCurrentlyStoredFiles(); // Calls the refresh function

		filesTableView.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("filename"));
		filesTableView.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("authorsName"));
		filesTableView.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("description"));
		filesTableView.setPlaceholder(new Label("Nie udostępniono żadnych plików."));

		fileSearchBar.textProperty().addListener(obs -> {
			filterTable(fileSearchBar.getText());
		});

		downloadFileButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(final ActionEvent e) {
				FileInfo chosenRow = filesTableView.getSelectionModel().getSelectedItem();
				if (chosenRow != null) {
					fileChooser.setInitialFileName(chosenRow.getFilename());

					chosenFile = fileChooser.showSaveDialog((Stage) fileManagerWindow.getScene().getWindow());

					if (chosenFile != null) {
						new Thread(() -> downloadFile(chosenRow.getFileID())).start();
					}

				} else {
					Platform.runLater(new Runnable() {
						@Override public void run() {
							openDialogBox(fileManagerWindow,
									"Musisz wybrać plik do pobrania. Żaden plik nie jest zaznaczony");
						}
					});
				}
			}
		});

		removeFileButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(final ActionEvent e) {
				FileInfo chosenRow = filesTableView.getSelectionModel().getSelectedItem();
				if (chosenRow != null) {
					new Thread(() -> sendRequestToRemoveFile(chosenRow.getFileID())).start();
				} else {
					Platform.runLater(new Runnable() {
						@Override public void run() {
							openDialogBox(fileManagerWindow,
									"Musisz wybrać plik do usunięcia. Żaden plik nie jest zaznaczony");
						}
					});
				}
			}
		});
		setButtonsPriviledges();
		filesTableView.getSelectionModel().selectedIndexProperty().addListener((num) -> setButtonsPriviledges());
	}

	private void filterTable(String filter) {
		ArrayList<FileInfo> filteredList = new ArrayList<FileInfo>();

		for (FileInfo fInfo : filesForThisTest) {
			if (fInfo.getAuthorsName().toLowerCase().contains(filter.toLowerCase())
					|| fInfo.getDescription().toLowerCase().contains(filter.toLowerCase())
					|| fInfo.getFilename().toLowerCase().contains(filter.toLowerCase())) {
				filteredList.add(fInfo);
			}
		}

		refreshList(filteredList);
	}

	private void sendRequestToRemoveFile(Integer givenFileID) {
		SocketEvent se = new SocketEvent("reqestRemovingChosenFile", givenFileID);
		NetworkConnection.sendSocketEvent(se);

		SocketEvent res = NetworkConnection.rcvSocketEvent("fileRemoved", "fileRemovingError");
		String eventName = res.getName();
		final String message;

		if (eventName.equals("fileRemoved")) {
			message = "Usunięto wybrany plik z bazy danych";
			fetchCurrentlyStoredFiles();
		} else {
			message = "Wystąpił błąd. Nie można usunąć pliku";
		}
		Platform.runLater(new Runnable() {
			@Override public void run() {
				openDialogBox(fileManagerWindow, message);
			}
		});
	}

	private void downloadFile(Integer givenFileID) {
		SocketEvent se = new SocketEvent("reqestSendingChosenFile", givenFileID);
		NetworkConnection.sendSocketEvent(se);

		SocketEvent res = NetworkConnection.rcvSocketEvent("fileSent", "fileSendingError");

		String eventName = res.getName();

		if (eventName.equals("fileSendingError")) {
			Platform.runLater(new Runnable() {
				@Override public void run() {
					openDialogBox(fileManagerWindow, "Wystąpił błąd. Nie można pobrać pliku");
				}
			});
		} else {
			Paper fileFetchedFromServer = new Paper();
			byte[] receivedBytes = res.getObject(byte[].class);
			fileFetchedFromServer.createFromReceivedBytes(receivedBytes);

			fileFetchedFromServer.saveAsFile(chosenFile.getAbsolutePath());
		}
	}

	@SuppressWarnings("unchecked")
	public void fetchCurrentlyStoredFiles() {
		SocketEvent se = new SocketEvent("reqestFileList", browsedTest);
		NetworkConnection.sendSocketEvent(se);

		SocketEvent res = NetworkConnection.rcvSocketEvent("fileListFetched", "fileListFetchError");
		String eventName = res.getName();
		
		if (eventName.equals("fileListFetchError")) {
			Platform.runLater(new Runnable() {
				@Override public void run() {
					openDialogBox(fileManagerWindow, "Wystąpił błąd. Nie można pobrać listy plików");
				}
			});
		} else {
			filesForThisTest = (ArrayList<FileInfo>) res.getObject(ArrayList.class);
			refreshList(filesForThisTest);
		}
		
	}

	private void refreshList(ArrayList<FileInfo> newContent) {
		filesTableView.setItems(FXCollections.observableArrayList(newContent));
	}

	@FXML private void goToUploadWindow() {
		UploadFileController.caller = this;
		openNewWindow(fileManagerWindow, "view/UploadFileLayout.fxml", 700, 500, false, "Wysyłanie pliku na serwer");
	}
}
