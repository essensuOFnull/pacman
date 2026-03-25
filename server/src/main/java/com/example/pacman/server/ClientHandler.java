package com.example.pacman.server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private int playerId;
    private GameLoop gameLoop;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket, int playerId, GameLoop gameLoop) {
        this.socket = socket;
        this.playerId = playerId;
        this.gameLoop = gameLoop;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.startsWith("KEY:")) {
                    // формат: KEY:direction:state
                    String[] parts = inputLine.split(":");
                    if (parts.length == 3) {
                        String dir = parts[1];
                        boolean pressed = "1".equals(parts[2]);
                        gameLoop.setKeyState(playerId, dir, pressed);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Client " + playerId + " disconnected");
        } finally {
            gameLoop.removePlayer(playerId);
            try {
                socket.close();
            } catch (IOException e) {}
        }
    }

    public void sendState(String state) {
        out.println(state);
    }
}