package com.SnakeGame.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 食物类 - 管理食物的位置生成和碰撞检测
 */
public class Food {
    private static final int FOOD_COUNT = 5;

    private List<Integer> foodXList = new ArrayList<>();
    private List<Integer> foodYList = new ArrayList<>();

    private int speedChangeRate = 5;
    private Random random = new Random();

    // 食物初始化：重置并补满固定数量
    public void init(Snake snake, Obstacle obstacle) {
        foodXList.clear();
        foodYList.clear();
        for (int i = 0; i < FOOD_COUNT; i++) {
            spawnOneFood(snake, obstacle);
        }
    }

    // 随机生成单个食物（与蛇、障碍、其他食物互斥）
    private void spawnOneFood(Snake snake, Obstacle obstacle) {
        int x = random.nextInt(31) * 25;
        int y = random.nextInt(22) * 25 + 50;
        if (!isValidPosition(x, y, snake, obstacle)) {
            spawnOneFood(snake, obstacle);
        } else {
            foodXList.add(x);
            foodYList.add(y);
        }
    }

    // 吃食物：命中后加分、加长、调速并立即补位
    public void eat(Snake snake, GamePanel gamePanel) {
        int headX = snake.segmentX[0];
        int headY = snake.segmentY[0];

        // 倒序遍历，防止删除元素时索引错位
        for (int i = foodXList.size() - 1; i >= 0; i--) {
            if (headX == foodXList.get(i) && headY == foodYList.get(i)) {
                snake.setLength(snake.getLength() + 1);
                gamePanel.setScore(gamePanel.getScore() + 10);
                gamePanel.setSpeed(gamePanel.getSpeed() - speedChangeRate);

                // 移除被吃的食物并生成新的
                foodXList.remove(i);
                foodYList.remove(i);
                spawnOneFood(snake, gamePanel.getObstacle());
            }
        }
    }

    // 检查坐标是否合法（不与蛇身/食物/障碍重叠）
    public boolean isValidPosition(int x, int y, Snake snake, Obstacle obstacle) {
        // 检查是否与蛇身节点重合
        for (int i = 0; i < snake.getLength(); i++) {
            if (x == snake.segmentX[i] && y == snake.segmentY[i]) {
                return false;
            }
        }
        // 检查是否与其他已存在的食物重叠
        for (int i = 0; i < foodXList.size(); i++) {
            if (x == foodXList.get(i) && y == foodYList.get(i)) {
                return false;
            }
        }
        // 食物不能刷在障碍物上
        if (obstacle.contains(x, y)) {
            return false;
        }
        return true;
    }

    // 设置每次进食后的速度变化值
    public void setSpeedChangeRate(int speedChangeRate) {
        this.speedChangeRate = speedChangeRate;
    }

    public int getFoodX(int index) {
        return foodXList.get(index);
    }

    public int getFoodY(int index) {
        return foodYList.get(index);
    }

    public int getFoodCount() {
        return foodXList.size();
    }
}
