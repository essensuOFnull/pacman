package com.example.pacman;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.example.pacman.model.MapData;
import com.example.pacman.model.TileType;
import com.example.pacman.network.NetworkClient;
import com.example.pacman.screens.GameScreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PacmanGame extends Game {
    private NetworkClient networkClient;
    private GameScreen gameScreen;
    private int mapWidth, mapHeight;
    private List<String> mapRows = new ArrayList<>();
    private List<MapData.Point> spawnPoints = new ArrayList<>();
    private int expectedSpawns = 0;

    @Override
    public void create() {
        networkClient = new NetworkClient();
        try {
            networkClient.connect("localhost", 12345, this::handleMessage);
        } catch (IOException e) {
            e.printStackTrace();
            Gdx.app.exit();
        }
        gameScreen = new GameScreen(networkClient);
    }

    private void handleMessage(String msg) {
        if (msg.startsWith("MAP ")) {
            String[] parts = msg.split(" ");
            mapWidth = Integer.parseInt(parts[1]);
            mapHeight = Integer.parseInt(parts[2]);
            mapRows.clear();
        } else if (msg.startsWith("SPAWNS ")) {
            expectedSpawns = Integer.parseInt(msg.substring(7));
            spawnPoints.clear();
        } else if (mapRows.size() < mapHeight) {
            mapRows.add(msg);
            if (mapRows.size() == mapHeight) {
                // ожидаем сообщение SPAWNS
            }
        } else if (msg.matches("\\d+ \\d+") && spawnPoints.size() < expectedSpawns) {
            String[] xy = msg.split(" ");
            int x = Integer.parseInt(xy[0]);
            int y = Integer.parseInt(xy[1]);
            spawnPoints.add(new MapData.Point(x, y));
            if (spawnPoints.size() == expectedSpawns) {
                buildMap();
            }
        } else if (msg.startsWith("UPDATE")) {
            if (gameScreen != null) gameScreen.updatePositions(msg);
        } else if (msg.equals("DISCONNECT")) {
            Gdx.app.exit();
        }
    }

    private void buildMap() {
        TileType[][] tiles = new TileType[mapHeight][mapWidth];
        for (int y = 0; y < mapHeight; y++) {
            String row = mapRows.get(y);
            for (int x = 0; x < mapWidth; x++) {
                tiles[y][x] = TileType.fromChar(row.charAt(x));
            }
        }
        MapData map = new MapData(mapWidth, mapHeight, tiles, spawnPoints);
        gameScreen.setMap(map);
        setScreen(gameScreen);
    }
}