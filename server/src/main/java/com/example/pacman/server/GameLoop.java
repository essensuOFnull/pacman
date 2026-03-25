package com.example.pacman.server;

import com.example.pacman.model.*;
import com.example.pacman.util.MapLoader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameLoop extends Thread {
    private final MapData map;
    private final Map<Integer, PacmanState> players = new HashMap<>();
    private final CopyOnWriteArrayList<ClientHandler> clients;
    private int nextId = 1;
    private boolean running = true;

    public GameLoop(String levelPath, CopyOnWriteArrayList<ClientHandler> clients) throws IOException {
        this.map = MapLoader.load(levelPath);
        this.clients = clients;
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double nsPerTick = 1_000_000_000.0 / 30.0;
        double delta = 0;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerTick;
            lastTime = now;
            if (delta >= 1) {
                update();
                broadcastState();
                delta--;
            }
            try { Thread.sleep(5); } catch (InterruptedException e) { break; }
        }
    }

    private void update() {
        for (PacmanState pac : players.values()) movePacman(pac);
    }

    private void movePacman(PacmanState pac) {
        int x = pac.getX();
        int y = pac.getY();
        boolean up = pac.isUpPressed();
        boolean down = pac.isDownPressed();
        boolean left = pac.isLeftPressed();
        boolean right = pac.isRightPressed();

        // Бег между стенками
        if (left && right && !up && !down) {
            int dir = pac.getDirection();
            if (dir == 3) { // влево
                if (map.isWalkable(x-1, y)) pac.setX(x-1);
                else pac.setDirection(1);
            } else if (dir == 1) {
                if (map.isWalkable(x+1, y)) pac.setX(x+1);
                else pac.setDirection(3);
            }
            return;
        }
        if (up && down && !left && !right) {
            int dir = pac.getDirection();
            if (dir == 0) {
                if (map.isWalkable(x, y-1)) pac.setY(y-1);
                else pac.setDirection(2);
            } else if (dir == 2) {
                if (map.isWalkable(x, y+1)) pac.setY(y+1);
                else pac.setDirection(0);
            }
            return;
        }

        // Одновременные нажатия
        if (up && left) {
            if (map.isWalkable(x, y-1)) { pac.setY(y-1); pac.setDirection(0); }
            else if (map.isWalkable(x-1, y)) { pac.setX(x-1); pac.setDirection(3); }
            return;
        }
        if (up && right) {
            if (map.isWalkable(x, y-1)) { pac.setY(y-1); pac.setDirection(0); }
            else if (map.isWalkable(x+1, y)) { pac.setX(x+1); pac.setDirection(1); }
            return;
        }
        if (down && left) {
            if (map.isWalkable(x, y+1)) { pac.setY(y+1); pac.setDirection(2); }
            else if (map.isWalkable(x-1, y)) { pac.setX(x-1); pac.setDirection(3); }
            return;
        }
        if (down && right) {
            if (map.isWalkable(x, y+1)) { pac.setY(y+1); pac.setDirection(2); }
            else if (map.isWalkable(x+1, y)) { pac.setX(x+1); pac.setDirection(1); }
            return;
        }

        // Одиночные направления
        if (up && map.isWalkable(x, y-1)) { pac.setY(y-1); pac.setDirection(0); }
        else if (down && map.isWalkable(x, y+1)) { pac.setY(y+1); pac.setDirection(2); }
        else if (left && map.isWalkable(x-1, y)) { pac.setX(x-1); pac.setDirection(3); }
        else if (right && map.isWalkable(x+1, y)) { pac.setX(x+1); pac.setDirection(1); }
    }

    private void broadcastState() {
        StringBuilder sb = new StringBuilder("UPDATE");
        for (PacmanState p : players.values()) {
            sb.append(" ").append(p.getId())
              .append(" ").append(p.getX())
              .append(" ").append(p.getY())
              .append(" ").append(p.getDirection());
        }
        String msg = sb.toString();
        for (ClientHandler client : clients) client.sendUpdate(msg);
    }

    public synchronized int registerPlayer() {
        int id = nextId++;
        List<MapData.Point> spawns = map.getSpawnPoints();
        MapData.Point spawn = spawns.get((id-1) % spawns.size());
        players.put(id, new PacmanState(id, spawn.x, spawn.y));
        return id;
    }

    public synchronized void updateKeyMask(int playerId, int mask) {
        PacmanState pac = players.get(playerId);
        if (pac != null) pac.setKeyMask(mask);
    }

    public synchronized void removePlayer(int playerId) { players.remove(playerId); }

    // Методы для отправки карты
    public int getMapWidth() { return map.getWidth(); }
    public int getMapHeight() { return map.getHeight(); }
    public char getTileChar(int x, int y) {
        TileType type = map.getTile(x, y);
        if (type == TileType.EMPTY) return '.';
        if (type == TileType.SPAWN) return 's';
        return '#';
    }
    public List<MapData.Point> getSpawnPoints() { return map.getSpawnPoints(); }
    public int getSpawnPointsCount() { return map.getSpawnPoints().size(); }
}