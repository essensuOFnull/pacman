
package com.example.pacman.util;

import com.example.pacman.model.MapData;
import com.example.pacman.model.TileType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MapLoader {
    public static MapData load(String path) throws IOException {
        List<String> lines = new ArrayList<>();
        try (InputStream is = MapLoader.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new IOException("Resource not found: " + path);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }
        }
        int height = lines.size();
        int width = lines.get(0).length();
        TileType[][] tiles = new TileType[height][width];
        List<int[]> spawnPoints = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            String line = lines.get(y);
            for (int x = 0; x < width; x++) {
                char c = line.charAt(x);
                TileType tile;
                switch (c) {
                    case 'r':
                        tile = TileType.RED_WALL;
                        break;
                    case 'g':
                        tile = TileType.GREEN_WALL;
                        break;
                    case 'b':
                        tile = TileType.BLUE_WALL;
                        break;
                    case '.':
                        tile = TileType.EMPTY;
                        break;
                    case 's':
                        tile = TileType.SPAWN;
                        spawnPoints.add(new int[]{x, y});
                        break;
                    default:
                        tile = TileType.EMPTY;
                        break;
                }
                tiles[y][x] = tile;
            }
        }
        return new MapData(tiles, spawnPoints);
    }
}