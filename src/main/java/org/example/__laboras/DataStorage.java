package org.example.__laboras;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class DataStorage {

    private static final String FILE = "chat_data.json";
    private static DataStorage instance;

    private Map<String, Set<String>> rooms = new LinkedHashMap<>();
    private List<String> messages = new ArrayList<>();

    private DataStorage() {
        load();
    }

    public static synchronized DataStorage getInstance() {
        if (instance == null) instance = new DataStorage();
        return instance;
    }

    public synchronized boolean createRoom(String name) {
        if (rooms.containsKey(name)) return false;
        rooms.put(name, new LinkedHashSet<>());
        save();
        return true;
    }

    public synchronized boolean joinRoom(String name, String user) {
        if (!rooms.containsKey(name)) return false;
        rooms.get(name).add(user);
        save();
        return true;
    }

    public synchronized void leaveRoom(String name, String user) {
        if (rooms.containsKey(name)) {
            rooms.get(name).remove(user);
            save();
        }
    }

    public synchronized Set<String> getRoomMembers(String name) {
        return rooms.containsKey(name) ? new LinkedHashSet<>(rooms.get(name)) : new LinkedHashSet<>();
    }

    public synchronized Set<String> getRooms() {
        return new LinkedHashSet<>(rooms.keySet());
    }

    public synchronized boolean roomExists(String name) {
        return rooms.containsKey(name);
    }

    public synchronized void logMessage(String from, String to, String msg) {
        String line = from + " -> " + to + ": " + msg;
        messages.add(line);
        save();
    }

    private void save() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            pw.println("=== ROOMS ===");
            for (String room : rooms.keySet()) {
                pw.println(room + ": " + rooms.get(room));
            }
            pw.println("=== MESSAGES ===");
            for (String m : messages) {
                pw.println(m);
            }
        } catch (IOException e) {
            System.out.println("Save error: " + e.getMessage());
        }
    }

    private void load() {}
}