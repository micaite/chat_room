package org.example.__laboras;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.__laboras.ChatConnection;
import org.example.__laboras.Message;

import static java.lang.System.out;


public class ChatController {

    @FXML private ListView<String> roomList;
    @FXML private ListView<String> userList;
    @FXML private TextArea chatArea;
    @FXML private TextField messageField;

    private ChatConnection connection;
    private String username;
    private String currentRoom;

    public void init(ChatConnection connection, String username) {
        this.connection = connection;
        this.username   = username;
    }

    @FXML
    private void onCreateRoom() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setHeaderText("Room name:");
        dlg.showAndWait().ifPresent(name -> {
            connection.sendMessage(new Message(
                    Message.Type.CREATE_ROOM, username, null, name, null));
            currentRoom = name;
        });
    }

    @FXML
    private void onJoinRoom() {
        String selected = roomList.getSelectionModel().getSelectedItem();
        out.println("onJoinRoom clicked, selected: " + selected); // <- pridėk
        if (selected != null) {
            connection.sendMessage(new Message(
                    Message.Type.JOIN_ROOM, username, null, selected, null));
            currentRoom = selected;
        }
    }

    @FXML
    private void onSend() {
        String text = messageField.getText().trim();
        if (text.isEmpty() || currentRoom == null) return;

        connection.sendMessage(new Message(
                Message.Type.PUBLIC, username, null, text, currentRoom));
        messageField.clear();
    }

    public void handleIncoming(Message msg) {
        Platform.runLater(() -> {
            switch (msg.getType()) {
                case ROOM_LIST -> {
                    roomList.getItems().clear();
                    for (String r : msg.getContent().split(",")) {
                        if (!r.isEmpty()) roomList.getItems().add(r);
                    }
                }
                case JOIN_OK -> {
                    currentRoom = msg.getContent();
                    chatArea.appendText("Joined room: " + currentRoom + "\n");
                }
                case PUBLIC -> {
                    chatArea.appendText(msg.getSender() + ": " + msg.getContent() + "\n");
                }
                case USER_LIST -> {
                    userList.getItems().clear();
                    for (String u : msg.getContent().split(",")) {
                        if (!u.isEmpty()) userList.getItems().add(u);
                    }
                }
            }
        });
    }

}