package com.example.pacman.server;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

import com.example.pacman.model.MapData;
import com.example.pacman.util.MapLoader;

import com.example.pacman.model.PacmanState;

public class ServerMain {
    private static final int PORT = 12345;
    private ServerSocket serverSocket;
    private ExecutorService clientThreads = Executors.newCachedThreadPool();
    private GameLoop gameLoop;
    private int nextPlayerId = 0;

    public static void main(String[] args) {
        String levelPath = args.length > 0 ? args[0] : "levels/level1.txt";
        new ServerMain().start(levelPath);
    }

    public void start(String levelPath) {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);

            // Загружаем карту
            MapData map = MapLoader.load(levelPath);
            gameLoop = new GameLoop(map);
            gameLoop.start();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getRemoteSocketAddress());
                int playerId = nextPlayerId++;
                PacmanState newPacman = new PacmanState(playerId, map.getSpawnPoints().get(0)[0], map.getSpawnPoints().get(0)[1]);
                gameLoop.addPlayer(playerId, newPacman);
                clientThreads.submit(new ClientHandler(clientSocket, playerId, gameLoop));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}