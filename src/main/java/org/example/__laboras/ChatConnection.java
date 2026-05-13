package org.example.__laboras;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.Consumer;

public class ChatConnection {

    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String username;
    private Consumer<Message> onMessage;

    public ChatConnection(Consumer<Message> onMessage){
        this.onMessage = onMessage;
    }

    public boolean connect( String host, int port, String username){
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            this.username = username;

            // siust username i serveri
            sendMessage (new Message(Message.Type.PUBLIC, username, null, username, null));

            // pradet gauti thread
            Thread receiver = new Thread(this::receiveLoop);
            receiver.setDaemon(true);
            receiver.start();

            return true;
        } catch (IOException e) {
            System.out.println("Connection failed: " + e.getMessage());
            return false;
        }
    }

    private void receiveLoop() {
        try {
            while (true) {
                Message msg = (Message) in.readObject();
                System.out.println("Received from server: " + msg.getType() + " " + msg.getContent());
                onMessage.accept(msg);
            }
        } catch (Exception e) {
            System.out.println("Disconnected.");
        }
    }

    public synchronized void sendMessage(Message msg) {
        try {
            System.out.println("Sending: " + msg.getType() + " " + msg.getContent());
            out.writeObject(msg);
            out.flush();
            out.reset();
        } catch (IOException e) {
            System.out.println("Send error: " + e.getMessage());
        }
    }


    public void setOnMessage(Consumer<Message> onMessage) {
        this.onMessage = onMessage;
    }


}
