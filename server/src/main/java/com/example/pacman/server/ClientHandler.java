package com.example.pacman.server;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final GameLoop gameLoop;
    private final CopyOnWriteArrayList<ClientHandler> allClients;
    private PrintWriter out;
    private BufferedReader in;
    private int playerId;

    public ClientHandler(Socket socket, GameLoop gameLoop, CopyOnWriteArrayList<ClientHandler> allClients) {
        this.socket = socket;
        this.gameLoop = gameLoop;
        this.allClients = allClients;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            playerId = gameLoop.registerPlayer();
            System.out.println("Player " + playerId + " registered.");

            sendMap();

            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("KEYS ")) {
                    int mask = Integer.parseInt(line.substring(5));
                    gameLoop.updateKeyMask(playerId, mask);
                }
            }
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException e) { }
            allClients.remove(this);
            gameLoop.removePlayer(playerId);
            System.out.println("Player " + playerId + " disconnected.");
        }
    }

    private void sendMap() {
        out.println("MAP " + gameLoop.getMapWidth() + " " + gameLoop.getMapHeight());
        for (int y = 0; y < gameLoop.getMapHeight(); y++) {
            StringBuilder line = new StringBuilder();
            for (int x = 0; x < gameLoop.getMapWidth(); x++) {
                line.append(gameLoop.getTileChar(x, y));
            }
            out.println(line.toString());
        }
        out.println("SPAWNS " + gameLoop.getSpawnPointsCount());
        for (var p : gameLoop.getSpawnPoints()) {
            out.println(p.x + " " + p.y);
        }
    }

    public void sendUpdate(String update) {
        out.println(update);
    }
}