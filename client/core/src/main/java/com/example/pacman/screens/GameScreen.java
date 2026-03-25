package com.example.pacman.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.example.pacman.model.MapData;
import com.example.pacman.model.TileType;
import com.example.pacman.network.NetworkClient;

import java.util.HashMap;
import java.util.Map;

public class GameScreen implements Screen {
    private final OrthographicCamera camera;
    private final SpriteBatch batch;
    private final NetworkClient client;
    private MapData mapData;
    private Texture redBlock, greenBlock, blueBlock, pacmanTexture;
    private int cellSize = 32;
    private Map<Integer, Vector2> visualPositions = new HashMap<>();
    private Map<Integer, Integer> targetDirections = new HashMap<>();
    private Map<Integer, Vector2> targetPositions = new HashMap<>();
    private boolean mapLoaded = false;

    public GameScreen(NetworkClient client) {
        this.client = client;
        camera = new OrthographicCamera();
        batch = new SpriteBatch();
        redBlock = new Texture("red_block.png");
        greenBlock = new Texture("green_block.png");
        blueBlock = new Texture("blue_block.png");
        pacmanTexture = new Texture("libgdx.png"); // временно
    }

    public void setMap(MapData map) {
        this.mapData = map;
        camera.setToOrtho(false, map.getWidth() * cellSize, map.getHeight() * cellSize);
        mapLoaded = true;
    }

    public void updatePositions(String updateMsg) {
        // Формат: "UPDATE id x y dir id x y dir ..."
        String[] parts = updateMsg.split(" ");
        for (int i = 1; i < parts.length; i += 4) {
            int id = Integer.parseInt(parts[i]);
            int x = Integer.parseInt(parts[i+1]);
            int y = Integer.parseInt(parts[i+2]);
            int dir = Integer.parseInt(parts[i+3]);
            Vector2 newTarget = new Vector2(x * cellSize, y * cellSize);
            targetPositions.put(id, newTarget);
            targetDirections.put(id, dir);
            if (!visualPositions.containsKey(id)) {
                visualPositions.put(id, newTarget.cpy());
            }
        }
    }

    @Override
    public void render(float delta) {
        if (!mapLoaded) return;

        // Отправка нажатий
        int mask = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.UP))    mask |= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))  mask |= 2;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))  mask |= 4;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) mask |= 8;
        client.sendKeys(mask);

        // Плавное перемещение визуальных позиций
        for (Map.Entry<Integer, Vector2> entry : visualPositions.entrySet()) {
            int id = entry.getKey();
            Vector2 visual = entry.getValue();
            Vector2 target = targetPositions.get(id);
            if (target != null) {
                visual.lerp(target, 0.2f);
                if (visual.dst(target) < 0.1f) visual.set(target);
            }
        }

        // Отрисовка
        ScreenUtils.clear(0, 0, 0, 1);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Карта
        for (int y = 0; y < mapData.getHeight(); y++) {
            for (int x = 0; x < mapData.getWidth(); x++) {
                TileType tile = mapData.getTile(x, y);
                Texture tex = null;
                if (tile == TileType.WALL) tex = redBlock; // можно выбирать по цвету
                if (tex != null) {
                    batch.draw(tex, x * cellSize, y * cellSize, cellSize, cellSize);
                }
            }
        }

        // Пакманы
        for (Vector2 pos : visualPositions.values()) {
            batch.draw(pacmanTexture, pos.x, pos.y, cellSize, cellSize);
        }

        batch.end();
    }

    @Override public void resize(int width, int height) { camera.viewportWidth = width; camera.viewportHeight = height; camera.update(); }
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void dispose() {
        batch.dispose();
        redBlock.dispose();
        greenBlock.dispose();
        blueBlock.dispose();
        pacmanTexture.dispose();
    }
}