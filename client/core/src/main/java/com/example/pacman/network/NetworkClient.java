package com.example.pacman.network;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class NetworkClient implements Disposable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread listenerThread;
    private volatile boolean running = true;
    private Consumer<String> messageHandler;

    public void connect(String host, int port, Consumer<String> handler) throws IOException {
        this.messageHandler = handler;
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        listenerThread = new Thread(this::listen);
        listenerThread.start();
    }

    private void listen() {
        try {
            String line;
            while (running && (line = in.readLine()) != null) {
                final String msg = line;
                Gdx.app.postRunnable(() -> messageHandler.accept(msg));
            }
        } catch (IOException e) {
            if (running) e.printStackTrace();
        } finally {
            Gdx.app.postRunnable(() -> messageHandler.accept("DISCONNECT"));
        }
    }

    public void sendKeys(int mask) {
        if (out != null) out.println("KEYS " + mask);
    }

    @Override
    public void dispose() {
        running = false;
        try {
            if (listenerThread != null) listenerThread.interrupt();
            if (socket != null) socket.close();
        } catch (IOException e) { }
    }
}