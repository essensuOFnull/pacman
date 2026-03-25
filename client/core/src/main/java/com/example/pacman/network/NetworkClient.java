package com.example.pacman.network;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.example.pacman.screens.GameScreen;

import java.io.*;
import java.net.Socket;

public class NetworkClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private GameScreen gameScreen;
    private String host;
    private int port;
    private Thread readerThread;
    private volatile boolean running = true;

    public NetworkClient(String host, int port, GameScreen gameScreen) {
        this.host = host;
        this.port = port;
        this.gameScreen = gameScreen;
    }

    public void connect() {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            readerThread = new Thread(this::readLoop);
            readerThread.start();
            System.out.println("Connected to server");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readLoop() {
        try {
            String line;
            while (running && (line = in.readLine()) != null) {
                if (line.startsWith("STATE:")) {
                    // Парсим: STATE:playerId:x:y:targetX:targetY;
                    String[] parts = line.split(";");
                    for (String part : parts) {
                        if (part.isEmpty()) continue;
                        String[] data = part.split(":");
                        if (data.length >= 6 && data[0].equals("STATE")) {
                            int id = Integer.parseInt(data[1]);
                            int x = Integer.parseInt(data[2]);
                            int y = Integer.parseInt(data[3]);
                            int targetX = Integer.parseInt(data[4]);
                            int targetY = Integer.parseInt(data[5]);
                            gameScreen.updatePlayerState(id, x, y, targetX, targetY);
                        }
                    }
                }
            }
        } catch (IOException e) {
            if (running) e.printStackTrace();
        }
    }

    public void updateKeys() {
        boolean up = Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean down = Gdx.input.isKeyPressed(Input.Keys.DOWN);
        boolean left = Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.RIGHT);

        // Отправляем изменения состояний
        // Для простоты отправляем все каждые 30 мс, но можно отправлять только изменения
        sendKey("up", up);
        sendKey("down", down);
        sendKey("left", left);
        sendKey("right", right);
    }

    private void sendKey(String dir, boolean pressed) {
        out.println("KEY:" + dir + ":" + (pressed ? "1" : "0"));
    }

    public void dispose() {
        running = false;
        try {
            if (readerThread != null) readerThread.interrupt();
            if (socket != null) socket.close();
        } catch (IOException e) {}
    }
}