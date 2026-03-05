package com.SnakeGame.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class Snake {
    public static int[] skx = new int[200];
    public static int[] sky = new int[200];
    private static int length = 3;
    private static String direction = "R";

    public static void init() {
        length = 10;
        int k = 300;
        for (int i = 0; i < length; i++, k -= 25) {
            skx[i] = k;
            sky[i] = 300;
        }
        direction = "R";
    }

    static void touchJudge() {
        for (int i = length - 1; i > 0; i--) {
            if (skx[0] == skx[i] && sky[0] == sky[i]) {
                GamePanel.setIsDead(true);
                GamePanel.setIsStart(false);
            }
        }
    }

    public static void move() {
        for (int i = length - 1; i > 0; i--) {
            skx[i] = skx[i - 1];
            sky[i] = sky[i - 1];
        }
        if (skx[0] <= 775 && skx[0] >= 0 && sky[0] <= 625 && sky[0] >= 50) {
            switch (direction) {
                case "R":
                    skx[0] += 25;
                    break;
                case "L":
                    skx[0] -= 25;
                    break;
                case "U":
                    sky[0] -= 25;
                    break;
                case "D":
                    sky[0] += 25;
                    break;
            }
            touchJudge();
        } else {
            GamePanel.setIsDead(true);
            GamePanel.setIsStart(false);
        }
    }

    public static void keyPressed(KeyEvent e) {
        if (e == null) return;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                if (direction != null && !direction.equals("L")) {
                    direction = "R";
                }
                break;
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                if (direction != null && !direction.equals("R")) {
                    direction = "L";
                }
                break;
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                if (direction != null && !direction.equals("D")) {
                    direction = "U";
                }
                break;
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                if (direction != null && !direction.equals("U")) {
                    direction = "D";
                }
                break;
        }
    }

    public static int getLength() {
        return length;
    }

    public static void setLength(int newLength) {
        if (newLength > 0 && newLength < 200) {
            length = newLength;
        }
    }

    public static void incrementLength() {
        if (length < 199) {
            length++;
        }
    }

    public static String getDirection() {
        return direction;
    }

    public static void setDirection(String newDirection) {
        if (newDirection != null &&
                (newDirection.equals("R") || newDirection.equals("L") ||
                        newDirection.equals("U") || newDirection.equals("D"))) {
            direction = newDirection;
        }
    }
}