package com.example.pacman.model;

import java.util.List;

public class MapData {
    private final TileType[][] tiles;
    private final List<int[]> spawnPoints;

    public MapData(TileType[][] tiles, List<int[]> spawnPoints) {
        this.tiles = tiles;
        this.spawnPoints = spawnPoints;
    }

    public TileType getTile(int x, int y) {
        if (y >= 0 && y < tiles.length && x >= 0 && x < tiles[0].length) {
            return tiles[y][x];
        }
        return TileType.RED_WALL; // за пределами карты считаем стеной
    }

    public int getWidth() { return tiles[0].length; }
    public int getHeight() { return tiles.length; }
    public List<int[]> getSpawnPoints() { return spawnPoints; }
}