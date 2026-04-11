package com.SnakeGame.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 食物类 - 管理食物的位置生成和碰撞检测
 */
public class Food {
    // 定义食物数量常量
    private static final int FOOD_COUNT = 5;
    
    // 使用列表存储多个食物的坐标
    private static List<Integer> foodXList = new ArrayList<>();
    private static List<Integer> foodYList = new ArrayList<>();
    
    private static int speedChangeRate = 5;
    static Random random = new Random();
    
    // 食物初始化：一次性生成 5 个食物
    public static void init(){
        foodXList.clear();
        foodYList.clear();
        for (int i = 0; i < FOOD_COUNT; i++) {
            spawnOneFood();
        }
    }
    
    // 随机生成单个食物（带防重叠判定）
    private static void spawnOneFood() {
        int x = random.nextInt(31) * 25;
        int y = random.nextInt(22) * 25 + 50;
        if (!Judge(x, y)) {
            spawnOneFood(); // 递归直到找到合法位置
        } else {
            foodXList.add(x);
            foodYList.add(y);
        }
    }
    
    // 吃食物方法：遍历所有食物，吃到后移除并立即补位
    public static void eat(){
        int headX = Snake.skx[0];
        int headY = Snake.sky[0];
        
        // 倒序遍历，防止删除元素时索引错位
        for (int i = foodXList.size() - 1; i >= 0; i--) {
            if (headX == foodXList.get(i) && headY == foodYList.get(i)) {
                Snake.setLength(Snake.getLength() + 1);
                GamePanel.setScore(GamePanel.getScore() + 10);
                GamePanel.setSpeed(GamePanel.getSpeed() - speedChangeRate);
                GamePanel.setSpeed(Math.max(GamePanel.getSpeed(), 30));
                
                // 移除被吃的食物并生成新的
                foodXList.remove(i);
                foodYList.remove(i);
                spawnOneFood();
            }
        }
    }
    
    // 检查坐标是否合法（不与蛇身或障碍物重叠）
    public static boolean Judge(int x, int y){
        // 检查是否与蛇身节点重合
        for(int i = 0; i < Snake.getLength(); i++){
            if(x == Snake.skx[i] && y == Snake.sky[i]){
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
        if (Obstacle.contains(x, y)) {
            return false;
        }
        return true;
    }

    public static void setSpeedChangeRate(int speedChangeRate) {
        Food.speedChangeRate = speedChangeRate;
    }

    // 获取指定索引的食物 X 坐标
    public static int getFx(int index) {
        return foodXList.get(index);
    }
    
    // 获取指定索引的食物 Y 坐标
    public static int getFy(int index) {
        return foodYList.get(index);
    }

    // 获取当前食物总数
    public static int getFoodCount() {
        return foodXList.size();
    }
}