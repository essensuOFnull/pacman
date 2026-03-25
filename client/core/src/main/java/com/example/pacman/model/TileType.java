package com.example.pacman.model;

public enum TileType {
    EMPTY,      // проходимо
    WALL,       // непроходимо
    SPAWN;      // проходимо, точка спавна

    public static TileType fromChar(char c) {
        switch (c) {
            case '.': return EMPTY;
            case 's': return SPAWN;
            default:  return WALL; // r,g,b - всё стены
        }
    }

    public boolean isWalkable() {
        return this != WALL;
    }
}