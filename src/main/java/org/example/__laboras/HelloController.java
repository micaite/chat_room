package org.example.__laboras;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.__laboras.ChatConnection;
import org.example.__laboras.Message;

public class HelloController {

    @FXML private TextField hostField;
    @FXML private TextField portField;
    @FXML private TextField usernameField;
    @FXML private Label errorLabel;

    private ChatConnection connection;
    private String username;

    @FXML
    private void onConnectClick() {
        username = usernameField.getText().trim();
        String host = hostField.getText().trim();
        int port = Integer.parseInt(portField.getText().trim());

        connection = new ChatConnection(msg -> handleIncoming(msg));

        if (connection.connect(host, port, username)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("chat-view.fxml"));
                Scene scene = new Scene(loader.load(), 600, 400);

                ChatController chatController = loader.getController();
                chatController.init(connection, username);

                // Nustatyti handleIncoming į ChatController
                connection.setOnMessage(msg -> chatController.handleIncoming(msg));

                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(scene);
            } catch (Exception e) {
                errorLabel.setText("Error: " + e.getMessage());
            }
        } else {
            errorLabel.setText("Could not connect!");
        }
    }

    private void handleIncoming(Message msg) {
        // Tuoj užpildysime
    }
}