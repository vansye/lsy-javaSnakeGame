package com.SnakeGame.game;

import java.awt.*;
import java.util.Random;

/**
 * 障碍物管理类 - 负责生成、绘制和碰撞判断
 */
public class Obstacle {
    private static final int[] ox = new int[60];
    private static final int[] oy = new int[60];
    private static int count = 0;
    private static final Random random = new Random();

    private Obstacle() {
    }

    public static void init(int obstacleCount) {
        // 限制上限，避免障碍过多导致几乎无可用格子。
        count = Math.max(0, Math.min(obstacleCount, 60));
        for (int i = 0; i < count; i++) {
            generateObstacle(i);
        }
    }

    private static void generateObstacle(int index) {
        int tryCount = 0;
        while (tryCount < 120) {
            int x = random.nextInt(31) * 25;
            int y = random.nextInt(22) * 25 + 50;

            if (isSafeForSpawn(x, y, index)) {
                ox[index] = x;
                oy[index] = y;
                return;
            }
            tryCount++;
        }

        // 重试失败后兜底到固定点，保证数组里一定有值，避免未初始化坐标。
        ox[index] = 0;
        oy[index] = 50;
    }

    private static boolean isSafeForSpawn(int x, int y, int currentIndex) {
        // 避开蛇初始活动区域，防止开局即“必撞”或几乎无路可走。
        if ((x >= 200 && x <= 400) && (y >= 250 && y <= 350)) {
            return false;
        }

        if (x == Food.getFx() && y == Food.getFy()) {
            return false;
        }

        for (int i = 0; i < Snake.getLength(); i++) {
            if (x == Snake.skx[i] && y == Snake.sky[i]) {
                return false;
            }
        }

        for (int i = 0; i < currentIndex; i++) {
            if (x == ox[i] && y == oy[i]) {
                return false;
            }
        }

        return true;
    }

    public static boolean contains(int x, int y) {
        for (int i = 0; i < count; i++) {
            if (x == ox[i] && y == oy[i]) {
                return true;
            }
        }
        return false;
    }

    public static void draw(Graphics g) {
        if (GamePanel.isGoldActive()) {
            g.setColor(new Color(0xB8F5A6));
        } else {
            g.setColor(GameConfig.getObstacleColor());
        }
        for (int i = 0; i < count; i++) {
            g.fillRoundRect(ox[i], oy[i], 25, 25, 8, 8);
        }
    }
}

