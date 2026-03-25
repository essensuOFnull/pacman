package com.example.pacman;

import com.badlogic.gdx.Game;
import com.example.pacman.screens.GameScreen;

public class PacmanGame extends Game {
    @Override
    public void create() {
        setScreen(new GameScreen());
    }
}