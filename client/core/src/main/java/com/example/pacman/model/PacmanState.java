package com.example.pacman.model;

public class PacmanState {
    private final int id;
    private int x, y;
    private int direction; // 0=вверх,1=вправо,2=вниз,3=влево
    private int keyMask;   // битовая маска: 1=вверх,2=вниз,4=влево,8=вправо

    public PacmanState(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.direction = 1;
        this.keyMask = 0;
    }

    public int getId() { return id; }
    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public int getDirection() { return direction; }
    public void setDirection(int direction) { this.direction = direction; }
    public int getKeyMask() { return keyMask; }
    public void setKeyMask(int mask) { this.keyMask = mask; }

    public boolean isUpPressed()    { return (keyMask & 1) != 0; }
    public boolean isDownPressed()  { return (keyMask & 2) != 0; }
    public boolean isLeftPressed()  { return (keyMask & 4) != 0; }
    public boolean isRightPressed() { return (keyMask & 8) != 0; }
}