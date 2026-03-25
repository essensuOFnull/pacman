package com.example.pacman.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain {
    private static final int PORT = 12345;
    private ServerSocket serverSocket;
    private ExecutorService clientThreads = Executors.newCachedThreadPool();
    private CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private GameLoop gameLoop;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java -jar server.jar <level_file>");
            System.exit(1);
        }
        new ServerMain().start(args[0]);
    }

    public void start(String levelPath) {
        try {
            gameLoop = new GameLoop(levelPath, clients);
            gameLoop.start();

            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client: " + clientSocket.getRemoteSocketAddress());
                ClientHandler handler = new ClientHandler(clientSocket, gameLoop, clients);
                clients.add(handler);
                clientThreads.submit(handler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}