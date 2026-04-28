package com.SnakeGame.game;

import java.awt.event.KeyEvent;

/**
 * 蛇实体：维护身体坐标、移动方向与插值渲染坐标
 */
public class Snake {
    public int[] segmentX = new int[200];
    public int[] segmentY = new int[200];
    private int[] prevSegmentX = new int[200];
    private int[] prevSegmentY = new int[200];
    private int length = 3;
    private String direction = "R";
    private String bodyDirection = "R";

    // 重置蛇到开局状态
    public void init() {
        length = 10;
        int k = 300;
        for (int i = 0; i < length; i++, k -= 25) {
            segmentX[i] = k;
            segmentY[i] = 300;
            prevSegmentX[i] = k;
            prevSegmentY[i] = 300;
        }
        direction = "R";
        bodyDirection = "R";
    }

    // 记录上一逻辑帧坐标，用于渲染阶段插值
    private void capturePreviousState() {
        int copyLen = Math.max(1, Math.min(length, 200));
        System.arraycopy(segmentX, 0, prevSegmentX, 0, copyLen);
        System.arraycopy(segmentY, 0, prevSegmentY, 0, copyLen);
    }

    // 碰撞判定：身体碰撞与障碍碰撞
    private void checkCollision(Obstacle obstacle, GamePanel gamePanel) {
        for (int i = length - 1; i > 0; i--) {
            if (segmentX[0] == segmentX[i] && segmentY[0] == segmentY[i]) {
                gamePanel.setDead(true);
                gamePanel.setStarted(false);
            }
        }

        if (obstacle.contains(segmentX[0], segmentY[0]) && !gamePanel.isGoldActive()) {
            gamePanel.setDead(true);
            gamePanel.setStarted(false);
        }
    }

    // 逻辑移动：先整体后移，再推进蛇头
    public void move(Obstacle obstacle, GamePanel gamePanel) {
        capturePreviousState();

        // 从尾到头回填坐标
        for (int i = length - 1; i > 0; i--) {
            segmentX[i] = segmentX[i - 1];
            segmentY[i] = segmentY[i - 1];
            bodyDirection = direction;
        }

        switch (direction) {
            case "R": segmentX[0] += 25; break;
            case "L": segmentX[0] -= 25; break;
            case "U": segmentY[0] -= 25; break;
            case "D": segmentY[0] += 25; break;
        }

        if (segmentX[0] <= 775 && segmentX[0] >= 0 && segmentY[0] <= 625 && segmentY[0] >= 50) {
            checkCollision(obstacle, gamePanel);
        } else {
            gamePanel.setDead(true);
            gamePanel.setStarted(false);
        }
    }

    // 键盘转向：禁止 180 度瞬间掉头
    public void keyPressed(KeyEvent e) {
        if (e == null) return;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                if (direction != null && !direction.equals("L") && !bodyDirection.equals("L")) {
                    direction = "R";
                }
                break;
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                if (direction != null && !direction.equals("R") && !bodyDirection.equals("R")) {
                    direction = "L";
                }
                break;
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                if (direction != null && !direction.equals("D") && !bodyDirection.equals("D")) {
                    direction = "U";
                }
                break;
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                if (direction != null && !direction.equals("U") && !bodyDirection.equals("U")) {
                    direction = "D";
                }
                break;
        }
    }

    public int getLength() { return length; }

    public void setLength(int newLength) {
        if (newLength > 0 && newLength < 200) {
            if (newLength > length && length > 0) {
                int tailX = segmentX[length - 1];
                int tailY = segmentY[length - 1];
                int prevTailX = prevSegmentX[length - 1];
                int prevTailY = prevSegmentY[length - 1];
                for (int i = length; i < newLength; i++) {
                    segmentX[i] = tailX;
                    segmentY[i] = tailY;
                    prevSegmentX[i] = prevTailX;
                    prevSegmentY[i] = prevTailY;
                }
            }
            length = newLength;
        }
    }

    public void incrementLength() {
        if (length < 199) {
            length++;
        }
    }

    public String getDirection() { return direction; }

    public void setDirection(String newDirection) {
        if (newDirection != null &&
                (newDirection.equals("R") || newDirection.equals("L") ||
                 newDirection.equals("U") || newDirection.equals("D"))) {
            direction = newDirection;
        }
    }

    // 渲染 X：逻辑坐标与上一帧坐标线性插值
    public int getRenderX(int index, float alpha) {
        if (index < 0 || index >= length) return 0;
        float t = Math.max(0f, Math.min(1f, alpha));
        return Math.round(prevSegmentX[index] + (segmentX[index] - prevSegmentX[index]) * t);
    }

    // 渲染 Y：逻辑坐标与上一帧坐标线性插值
    public int getRenderY(int index, float alpha) {
        if (index < 0 || index >= length) return 0;
        float t = Math.max(0f, Math.min(1f, alpha));
        return Math.round(prevSegmentY[index] + (segmentY[index] - prevSegmentY[index]) * t);
    }
}
