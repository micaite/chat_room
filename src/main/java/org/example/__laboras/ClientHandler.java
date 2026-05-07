package org.example.__laboras;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Set;


public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    private String currentRoom;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            Message first = (Message) in.readObject();
            username = first.getContent();
            ChatServer.registerClient(username, this);

            String rooms = String.join(",", DataStorage.getInstance().getRooms());
            String users = String.join(",", ChatServer.clients.keySet());
            sendMessage(new Message(Message.Type.ROOM_LIST, "SERVER", username, rooms, null));
            sendMessage(new Message(Message.Type.USER_LIST, "SERVER", username, users, null));

            ChatServer.broadcastRoomList();

            Message msg;
            while ((msg = (Message) in.readObject()) != null) {
                handleMessage(msg);
            }

        } catch (EOFException e) {
            System.out.println(username + " disconnected.");
        } catch (Exception e) {
            System.out.println("Error with " + username + ": " + e.getMessage());
        } finally {
            ChatServer.unregisterClient(username);
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    private void handleMessage(Message msg) {
        switch (msg.getType()) {
            case CREATE_ROOM -> {
                String roomName = msg.getContent();
                DataStorage.getInstance().createRoom(roomName);
                DataStorage.getInstance().joinRoom(roomName, username);
                ChatServer.broadcastRoomList();
                System.out.println(username + " created room: " + roomName);
            }
            case JOIN_ROOM -> {
                String roomName = msg.getContent();
                System.out.println("JOIN_ROOM: " + roomName + " user=" + username); // <- pridėk
                DataStorage.getInstance().joinRoom(roomName, username);
                currentRoom = roomName;
                sendMessage(new Message(Message.Type.JOIN_OK, "SERVER", username, roomName, null));
            }
            case PUBLIC -> {
                String roomName = msg.getRoom();
                System.out.println("PUBLIC į kambarį: " + roomName + " nariai: " + DataStorage.getInstance().getRoomMembers(roomName));
                Set<String> members = DataStorage.getInstance().getRoomMembers(roomName);
                for (String member : members) {
                    ChatServer.sendToClient(member, msg);
                }
                DataStorage.getInstance().logMessage(username, roomName, msg.getContent());
            }
            case PRIVATE -> {
                ChatServer.sendToClient(msg.getReceiver(), msg);
                DataStorage.getInstance().logMessage(username, msg.getReceiver(), msg.getContent());
            }
            case GET_ROOMS -> {
                String rooms = String.join(",", DataStorage.getInstance().getRooms());
                sendMessage(new Message(Message.Type.ROOM_LIST, "SERVER", username, rooms, null));
            }
            case GET_USERS -> {
                String users = String.join(",", ChatServer.clients.keySet());
                sendMessage(new Message(Message.Type.USER_LIST, "SERVER", username, users, null));
            }
        }
    }

    public synchronized void sendMessage (Message msg){ // uztikrina kad tik viena gija gali rayti i out srauta
        try{
            out.writeObject(msg);
            out.flush();
            out.reset();
        } catch (IOException e){
            System.out.println("Failed to send message to: " + username);
        }
    }
}

