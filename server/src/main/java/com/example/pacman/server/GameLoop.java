package com.example.pacman.server;

import com.example.pacman.model.*;
import java.util.*;
import java.util.concurrent.*;

public class GameLoop {
    private MapData map;
    private Map<Integer, PacmanState> players = new ConcurrentHashMap<>();
    private Map<Integer, ClientHandler> clients = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public GameLoop(MapData map) {
        this.map = map;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::update, 0, 33, TimeUnit.MILLISECONDS); // ~30 fps
    }

    public void addPlayer(int id, PacmanState state) {
        players.put(id, state);
    }

    public void removePlayer(int id) {
        players.remove(id);
    }

    public void setKeyState(int playerId, String dir, boolean pressed) {
        PacmanState p = players.get(playerId);
        if (p == null) return;
        switch (dir) {
            case "up": p.upPressed = pressed; break;
            case "down": p.downPressed = pressed; break;
            case "left": p.leftPressed = pressed; break;
            case "right": p.rightPressed = pressed; break;
        }
    }

    private void update() {
        for (PacmanState p : players.values()) {
            movePacman(p);
        }
        // Отправляем состояния всем клиентам
        StringBuilder sb = new StringBuilder();
        for (PacmanState p : players.values()) {
            sb.append("STATE:").append(p.id).append(":")
              .append(p.x).append(":").append(p.y).append(":")
              .append(p.targetX).append(":").append(p.targetY).append(";");
        }
        String fullState = sb.toString();
        for (ClientHandler client : clients.values()) {
            client.sendState(fullState);
        }
    }

    private void movePacman(PacmanState p) {
        // Определяем желаемое направление на основе нажатых клавиш
        boolean up = p.upPressed;
        boolean down = p.downPressed;
        boolean left = p.leftPressed;
        boolean right = p.rightPressed;

        // Сначала проверяем, находится ли пакман в середине движения к клетке
        // В этой реализации мы перемещаемся сразу на клетку, а анимацию делает клиент.
        // Логика движения:
        int dx = 0, dy = 0;
        if (up && left && !down && !right) {
            // Сначала пытаемся вверх, потом влево
            if (canMove(p.x, p.y - 1)) {
                dy = -1;
            } else if (canMove(p.x - 1, p.y)) {
                dx = -1;
            }
        } else if (up && right && !down && !left) {
            if (canMove(p.x, p.y - 1)) {
                dy = -1;
            } else if (canMove(p.x + 1, p.y)) {
                dx = 1;
            }
        } else if (down && left && !up && !right) {
            if (canMove(p.x, p.y + 1)) {
                dy = 1;
            } else if (canMove(p.x - 1, p.y)) {
                dx = -1;
            }
        } else if (down && right && !up && !left) {
            if (canMove(p.x, p.y + 1)) {
                dy = 1;
            } else if (canMove(p.x + 1, p.y)) {
                dx = 1;
            }
        } else if (left && right && !up && !down) {
            // Бег между левой и правой стенками
            if (canMove(p.x - 1, p.y) && canMove(p.x + 1, p.y)) {
                // нужно определить направление
                // здесь упростим: если можем идти влево, идём влево, иначе вправо
                if (canMove(p.x - 1, p.y)) dx = -1;
                else if (canMove(p.x + 1, p.y)) dx = 1;
            } else {
                // если в одну сторону нельзя, идём в другую
                if (canMove(p.x - 1, p.y)) dx = -1;
                else if (canMove(p.x + 1, p.y)) dx = 1;
            }
        } else if (up && down && !left && !right) {
            // Бег между верхней и нижней стенками
            if (canMove(p.x, p.y - 1) && canMove(p.x, p.y + 1)) {
                if (canMove(p.x, p.y - 1)) dy = -1;
                else if (canMove(p.x, p.y + 1)) dy = 1;
            } else {
                if (canMove(p.x, p.y - 1)) dy = -1;
                else if (canMove(p.x, p.y + 1)) dy = 1;
            }
        } else {
            // Одиночное направление
            if (up && canMove(p.x, p.y - 1)) dy = -1;
            else if (down && canMove(p.x, p.y + 1)) dy = 1;
            else if (left && canMove(p.x - 1, p.y)) dx = -1;
            else if (right && canMove(p.x + 1, p.y)) dx = 1;
        }

        if (dx != 0 || dy != 0) {
            p.targetX = p.x + dx;
            p.targetY = p.y + dy;
            p.x = p.targetX;
            p.y = p.targetY;
        }
    }

    private boolean canMove(int x, int y) {
        TileType tile = map.getTile(x, y);
        return tile != TileType.RED_WALL && tile != TileType.GREEN_WALL && tile != TileType.BLUE_WALL;
    }
}