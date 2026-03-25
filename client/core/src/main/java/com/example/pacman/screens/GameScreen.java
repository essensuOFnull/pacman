package com.example.pacman.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.example.pacman.model.MapData;
import com.example.pacman.model.PacmanState;
import com.example.pacman.model.TileType;
import com.example.pacman.network.NetworkClient;
import com.example.pacman.util.MapLoader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GameScreen implements Screen {
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;

    private MapData map;
    private Texture redTexture, greenTexture, blueTexture, pacmanTexture;
    private Map<Integer, PacmanState> players = new HashMap<>();

    private NetworkClient networkClient;

    public GameScreen() {
        // Загружаем карту из ресурсов (в assets)
        try {
            map = MapLoader.load("levels/level1.txt");
        } catch (IOException e) {
            e.printStackTrace();
            // fallback
        }

        redTexture = new Texture("red_block.png");
        greenTexture = new Texture("green_block.png");
        blueTexture = new Texture("blue_block.png");
        pacmanTexture = new Texture("libgdx.png"); // временно, потом заменить

        // Настройка камеры
        camera = new OrthographicCamera();
        viewport = new FitViewport(map.getWidth() * 32, map.getHeight() * 32, camera);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        batch = new SpriteBatch();

        // Подключаемся к серверу
        networkClient = new NetworkClient("localhost", 12345, this);
        networkClient.connect();
    }

    public void updatePlayerState(int id, int x, int y, int targetX, int targetY) {
        PacmanState state = players.get(id);
        if (state == null) {
            state = new PacmanState(id, x, y);
            players.put(id, state);
        }
        state.x = x;
        state.y = y;
        state.targetX = targetX;
        state.targetY = targetY;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Обновляем визуальные позиции всех пакманов
        for (PacmanState p : players.values()) {
            p.updateVisual(delta, 5.0f);
        }

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        // Рисуем карту
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                TileType tile = map.getTile(x, y);
                Texture tex = null;
                switch (tile) {
                    case RED_WALL: tex = redTexture; break;
                    case GREEN_WALL: tex = greenTexture; break;
                    case BLUE_WALL: tex = blueTexture; break;
                    default: continue;
                }
                batch.draw(tex, x * 32, y * 32, 32, 32);
            }
        }
        // Рисуем пакманов
        for (PacmanState p : players.values()) {
            batch.draw(pacmanTexture, p.visualX * 32, p.visualY * 32, 32, 32);
        }
        batch.end();

        // Отправляем нажатия клавиш на сервер
        networkClient.updateKeys();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        redTexture.dispose();
        greenTexture.dispose();
        blueTexture.dispose();
        pacmanTexture.dispose();
        networkClient.dispose();
    }

    // остальные методы Screen оставляем пустыми
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}