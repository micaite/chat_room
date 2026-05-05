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
            System.out.println(username + " connected. Thread: " + Thread.currentThread().getName());
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
                System.out.println(username + " created room: " + roomName);
            }
            case JOIN_ROOM -> {
                String roomName = msg.getContent();
                DataStorage.getInstance().joinRoom(roomName, username);
                System.out.println(username + " joined room: " + roomName);
            }
            case PUBLIC -> {
                String roomName = msg.getRoom();
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

