package com.example.pacman.model;

public enum TileType {
    EMPTY,
    RED_WALL,
    GREEN_WALL,
    BLUE_WALL,
    SPAWN;

    public static TileType fromChar(char c) {
        switch (c) {
            case '.': return EMPTY;
            case 's': return SPAWN;
            case 'r': return RED_WALL;
            case 'g': return GREEN_WALL;
            case 'b': return BLUE_WALL;
            default:  return RED_WALL; // на всякий случай
        }
    }

    public boolean isWalkable() {
        return this == EMPTY || this == SPAWN;
    }
}