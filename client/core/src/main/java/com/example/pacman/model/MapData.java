package com.example.pacman.model;

import java.util.ArrayList;
import java.util.List;

public class MapData {
    private final int width;
    private final int height;
    private final TileType[][] tiles;
    private final List<Point> spawnPoints;

    public MapData(int width, int height, TileType[][] tiles, List<Point> spawnPoints) {
        this.width = width;
        this.height = height;
        this.tiles = tiles;
        this.spawnPoints = spawnPoints;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public TileType getTile(int x, int y) { return tiles[y][x]; }
    public List<Point> getSpawnPoints() { return spawnPoints; }

    public boolean isWalkable(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return false;
        return tiles[y][x].isWalkable();
    }

    public static class Point {
        public final int x, y;
        public Point(int x, int y) { this.x = x; this.y = y; }
    }
}