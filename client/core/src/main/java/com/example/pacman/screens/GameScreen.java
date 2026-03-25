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
    private int mapHeight;  // сохранённая высота карты
    private Texture redBlock, greenBlock, blueBlock, pacman0, pacman1;
    private int cellSize = 32;
    private Map<Integer, Vector2> visualPositions = new HashMap<>();
    private Map<Integer, Integer> targetDirections = new HashMap<>();
    private Map<Integer, Vector2> targetPositions = new HashMap<>();
    private boolean mapLoaded = false;
    private float animationTimer = 0;

    public GameScreen(NetworkClient client) {
        this.client = client;
        camera = new OrthographicCamera();
        batch = new SpriteBatch();
        redBlock = new Texture("red_block.png");
        greenBlock = new Texture("green_block.png");
        blueBlock = new Texture("blue_block.png");
        pacman0 = new Texture("pacman0.png");
        pacman1 = new Texture("pacman1.png");
    }

    public void setMap(MapData map) {
        this.mapData = map;
        this.mapHeight = map.getHeight();
        camera.setToOrtho(false, map.getWidth() * cellSize, map.getHeight() * cellSize);
        mapLoaded = true;
    }

    public void updatePositions(String updateMsg) {
        // Формат: "UPDATE id x y dir id x y dir ..."
        String[] parts = updateMsg.split(" ");
        for (int i = 1; i < parts.length; i += 4) {
            int id = Integer.parseInt(parts[i]);
            int logicalX = Integer.parseInt(parts[i+1]);
            int logicalY = Integer.parseInt(parts[i+2]);
            int dir = Integer.parseInt(parts[i+3]);

            // Преобразуем логические координаты в экранные:
            // screenX = logicalX * cellSize
            // screenY = (mapHeight - 1 - logicalY) * cellSize
            float screenX = logicalX * cellSize;
            float screenY = (mapHeight - 1 - logicalY) * cellSize;
            Vector2 newTarget = new Vector2(screenX, screenY);
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

        // Отправка нажатий клавиш
        int mask = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.UP))    mask |= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))  mask |= 2;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))  mask |= 4;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) mask |= 8;
        client.sendKeys(mask);

        // Плавная интерполяция визуальных позиций
        for (Map.Entry<Integer, Vector2> entry : visualPositions.entrySet()) {
            int id = entry.getKey();
            Vector2 visual = entry.getValue();
            Vector2 target = targetPositions.get(id);
            if (target != null) {
                visual.lerp(target, 0.2f);
                if (visual.dst(target) < 0.1f) visual.set(target);
            }
        }

        // Анимация рта
        animationTimer += delta;
        if (animationTimer > 0.1f) animationTimer -= 0.1f;
        Texture pacTex = (animationTimer < 0.05f) ? pacman0 : pacman1;

        // Отрисовка
        ScreenUtils.clear(0, 0, 0, 1);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Отрисовка карты: переворачиваем по Y
        for (int y = 0; y < mapData.getHeight(); y++) {
            int screenY = (mapData.getHeight() - 1 - y) * cellSize;
            for (int x = 0; x < mapData.getWidth(); x++) {
                TileType tile = mapData.getTile(x, y);
                Texture tex = null;
                if (tile == TileType.RED_WALL) tex = redBlock;
                else if (tile == TileType.GREEN_WALL) tex = greenBlock;
                else if (tile == TileType.BLUE_WALL) tex = blueBlock;
                if (tex != null) {
                    batch.draw(tex, x * cellSize, screenY, cellSize, cellSize);
                }
            }
        }

        // Отрисовка пакманов с учётом направления (коррекция поворота)
        for (Map.Entry<Integer, Vector2> entry : visualPositions.entrySet()) {
            int id = entry.getKey();
            Vector2 pos = entry.getValue();
            int logicalDir = targetDirections.getOrDefault(id, 1);

            // Преобразуем логическое направление в экранный поворот:
            // логическое: 0=вверх, 1=вправо, 2=вниз, 3=влево
            // экранное:   вверх = поворот 90, вниз = поворот 270, влево = 180, вправо = 0
            float rotation = 0;
            switch (logicalDir) {
                case 0: rotation = 90; break;   // вверх логически -> вниз экранно
                case 1: rotation = 0; break;    // вправо -> вправо
                case 2: rotation = 270; break;  // вниз логически -> вверх экранно
                case 3: rotation = 180; break;  // влево -> влево
            }

            batch.draw(pacTex, pos.x, pos.y, cellSize/2f, cellSize/2f,
                    cellSize, cellSize, 1, 1, rotation, 0, 0,
                    pacTex.getWidth(), pacTex.getHeight(), false, false);
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
        pacman0.dispose();
        pacman1.dispose();
    }
}