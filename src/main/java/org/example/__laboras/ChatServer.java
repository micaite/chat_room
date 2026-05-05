package org.example.__laboras;

import org.example.__laboras.Message;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {

    public static final int PORT = 5000;

    private static Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public static void main (String[] args) throws IOException{
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started on port: " + PORT);

        while (true){
            Socket socket = serverSocket.accept();
            System.out.println("New connection");

            ClientHandler handler = new ClientHandler(socket);
            Thread t = new Thread(handler);
            t.start();
        }
    }

    public static void registerClient(String username, ClientHandler handler) {
        clients.put(username, handler);
    }

    public static void unregisterClient(String username) {
        clients.remove(username);
    }

    public static boolean isUsernameTaken(String username) {
        return clients.containsKey(username);
    }

    public static Set<String> getOnlineUsers() {
        return clients.keySet();
    }

    public static void sendToClient(String username, Message msg) {
        ClientHandler handler = clients.get(username);
        if (handler != null) {
            handler.sendMessage(msg);
        }
    }
}
