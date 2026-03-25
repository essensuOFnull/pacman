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

    // Настройки движения
    private static final int TICKS_PER_MOVE = 6;   // 30 тиков/сек => 5 движений/сек
    private int tickCounter = 0;

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
                updateMovement();   // обновляем движение с задержкой
                broadcastState();   // рассылаем новое состояние
                delta--;
            }
            try { Thread.sleep(5); } catch (InterruptedException e) { break; }
        }
    }

    private void updateMovement() {
        tickCounter++;
        if (tickCounter >= TICKS_PER_MOVE) {
            tickCounter = 0;
            for (PacmanState pac : players.values()) movePacman(pac);
        }
    }

    private void movePacman(PacmanState pac) {
        int x = pac.getX();
        int y = pac.getY();
        int currentDir = pac.getDirection();
        int desiredDir = -1;

        boolean up = pac.isUpPressed();
        boolean down = pac.isDownPressed();
        boolean left = pac.isLeftPressed();
        boolean right = pac.isRightPressed();

        // 1. Бег влево-вправо (только горизонтальные)
        if (left && right && !up && !down) {
            if (currentDir == 3) { // влево
                if (map.isWalkable(x-1, y)) desiredDir = 3;
                else desiredDir = 1;
            } else if (currentDir == 1) { // вправо
                if (map.isWalkable(x+1, y)) desiredDir = 1;
                else desiredDir = 3;
            } else {
                desiredDir = map.isWalkable(x-1, y) ? 3 : 1;
            }
        }
        // 2. Бег вверх-вниз (только вертикальные)
        else if (up && down && !left && !right) {
            if (currentDir == 0) { // вверх
                if (map.isWalkable(x, y-1)) desiredDir = 0;
                else desiredDir = 2;
            } else if (currentDir == 2) { // вниз
                if (map.isWalkable(x, y+1)) desiredDir = 2;
                else desiredDir = 0;
            } else {
                desiredDir = map.isWalkable(x, y-1) ? 0 : 2;
            }
        }
        // 3. Обычное движение (одиночные или комбинации)
        else {
            // Комбинации вверх+влево и т.д.
            if (up && left) {
                desiredDir = map.isWalkable(x, y-1) ? 0 : 3;
            } else if (up && right) {
                desiredDir = map.isWalkable(x, y-1) ? 0 : 1;
            } else if (down && left) {
                desiredDir = map.isWalkable(x, y+1) ? 2 : 3;
            } else if (down && right) {
                desiredDir = map.isWalkable(x, y+1) ? 2 : 1;
            }
            // Одиночные направления
            else if (up) desiredDir = 0;
            else if (down) desiredDir = 2;
            else if (left) desiredDir = 3;
            else if (right) desiredDir = 1;
        }

        // Попытка движения в желаемом направлении
        if (desiredDir != -1) {
            int newX = x, newY = y;
            switch (desiredDir) {
                case 0: newY--; break;
                case 2: newY++; break;
                case 3: newX--; break;
                case 1: newX++; break;
            }
            if (map.isWalkable(newX, newY)) {
                pac.setX(newX);
                pac.setY(newY);
                pac.setDirection(desiredDir);
                return;
            }
        }

        // Если желаемое недоступно, пытаемся продолжить движение в текущем направлении
        if (currentDir != -1) {
            int newX = x, newY = y;
            switch (currentDir) {
                case 0: newY--; break;
                case 2: newY++; break;
                case 3: newX--; break;
                case 1: newX++; break;
            }
            if (map.isWalkable(newX, newY)) {
                pac.setX(newX);
                pac.setY(newY);
                // направление не меняем
            }
        }
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
        MapData.Point spawn = spawns.isEmpty() ? new MapData.Point(1,1) : spawns.get((id-1) % spawns.size());
        players.put(id, new PacmanState(id, spawn.x, spawn.y));
        return id;
    }

    public synchronized void updateKeyMask(int playerId, int mask) {
        PacmanState pac = players.get(playerId);
        if (pac != null) pac.setKeyMask(mask);
    }

    public synchronized void removePlayer(int playerId) { players.remove(playerId); }

    // Методы для отправки карты клиентам
    public int getMapWidth() { return map.getWidth(); }
    public int getMapHeight() { return map.getHeight(); }
    public char getTileChar(int x, int y) {
        TileType type = map.getTile(x, y);
        if (type == TileType.EMPTY) return '.';
        if (type == TileType.SPAWN) return 's';
        if (type == TileType.RED_WALL) return 'r';
        if (type == TileType.GREEN_WALL) return 'g';
        if (type == TileType.BLUE_WALL) return 'b';
        return '#';
    }
    public List<MapData.Point> getSpawnPoints() { return map.getSpawnPoints(); }
    public int getSpawnPointsCount() { return map.getSpawnPoints().size(); }
}