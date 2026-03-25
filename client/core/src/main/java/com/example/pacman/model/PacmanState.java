package com.example.pacman.model;

public class PacmanState {
    public int id;
    public int x;
    public int y;
    public int targetX;
    public int targetY;
    public float visualX;
    public float visualY;
    public boolean upPressed;
    public boolean downPressed;
    public boolean leftPressed;
    public boolean rightPressed;

    public PacmanState(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
        this.visualX = x;
        this.visualY = y;
    }

    public void updateVisual(float delta, float speed) {
        // плавное движение к целевой клетке
        visualX += (targetX - visualX) * speed * delta;
        visualY += (targetY - visualY) * speed * delta;
        if (Math.abs(visualX - targetX) < 0.01f) visualX = targetX;
        if (Math.abs(visualY - targetY) < 0.01f) visualY = targetY;
    }
}