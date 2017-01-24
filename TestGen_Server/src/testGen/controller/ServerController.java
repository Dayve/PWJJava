package testGen.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import testGen.TestGenServer;

public class ServerController {

	private Thread serverThread;
	@FXML private TextArea textField;

	@FXML private void startServer() {
		TestGenServer s = new TestGenServer(9090);
		serverThread = new Thread(s);
		serverThread.start();

		textField.appendText("Server started.\n");
	}

	@FXML private void stopServer() {
		// serverThread.interrupt();
		textField.appendText("Server stopped.\n");
	}
}
