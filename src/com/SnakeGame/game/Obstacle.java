package com.SnakeGame.game;

import java.awt.*;
import java.util.Random;

/**
 * 障碍物管理类 - 负责生成、绘制和碰撞判断
 */
public class Obstacle {
    private final int[] obstacleX = new int[60];
    private final int[] obstacleY = new int[60];
    private int count = 0;
    private final Random random = new Random();

    // 按数量重建障碍物
    public void init(int obstacleCount, Snake snake) {
        count = Math.max(0, Math.min(obstacleCount, 60));
        for (int i = 0; i < count; i++) {
            generateObstacle(i, snake);
        }
    }

    // 生成单个障碍，有限次数重试避免死循环
    private void generateObstacle(int index, Snake snake) {
        int tryCount = 0;
        while (tryCount < 120) {
            int x = random.nextInt(31) * 25;
            int y = random.nextInt(22) * 25 + 50;

            if (isSafeForSpawn(x, y, index, snake)) {
                obstacleX[index] = x;
                obstacleY[index] = y;
                return;
            }
            tryCount++;
        }

        obstacleX[index] = 0;
        obstacleY[index] = 50;
    }

    // 刷新规则：避开初始区域、蛇身和已有障碍
    private boolean isSafeForSpawn(int x, int y, int currentIndex, Snake snake) {
        // 避开蛇初始活动区域，防止开局即"必撞"
        if ((x >= 200 && x <= 400) && (y >= 250 && y <= 350)) {
            return false;
        }

        // 避开蛇身（此时蛇已初始化）
        for (int i = 0; i < snake.getLength(); i++) {
            if (x == snake.segmentX[i] && y == snake.segmentY[i]) {
                return false;
            }
        }

        // 避开其他已生成的障碍
        for (int i = 0; i < currentIndex; i++) {
            if (x == obstacleX[i] && y == obstacleY[i]) {
                return false;
            }
        }

        return true;
    }

    // 判断给定坐标是否被障碍占用
    public boolean contains(int x, int y) {
        for (int i = 0; i < count; i++) {
            if (x == obstacleX[i] && y == obstacleY[i]) {
                return true;
            }
        }
        return false;
    }

    // 绘制障碍：金身期间使用提示色
    public void draw(Graphics g, boolean goldActive) {
        if (goldActive) {
            g.setColor(new Color(0xB8F5A6));
        } else {
            g.setColor(GameConfig.getObstacleColor());
        }
        for (int i = 0; i < count; i++) {
            g.fillRoundRect(obstacleX[i], obstacleY[i], 25, 25, 8, 8);
        }
    }
}
