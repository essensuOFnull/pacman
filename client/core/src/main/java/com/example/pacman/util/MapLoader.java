package com.example.pacman.util;

import com.example.pacman.model.MapData;
import com.example.pacman.model.TileType;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapLoader {
    public static MapData load(String path) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) lines.add(line);
            }
        }
        if (lines.isEmpty()) throw new IOException("Empty file");
        int height = lines.size();
        int width = lines.get(0).length();
        TileType[][] tiles = new TileType[height][width];
        List<MapData.Point> spawnPoints = new ArrayList<>();

        for (int y = 0; y < height; y++) {
            String row = lines.get(y);
            for (int x = 0; x < width; x++) {
                char c = row.charAt(x);
                TileType type = TileType.fromChar(c);
                tiles[y][x] = type;
                if (c == 's') spawnPoints.add(new MapData.Point(x, y));
            }
        }
        return new MapData(width, height, tiles, spawnPoints);
    }
}