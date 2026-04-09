package com.SnakeGame.game;

import java.util.Random;

/**
 * 食物类 - 管理食物的位置生成和碰撞检测
 */
public class Food {
    private static int fx = 600;
    private static int fy = 300;
    private static int speedChangeRate = 5;

    static Random random = new Random();
    
    // 食物初始化
    public static void init(){
        fx = 600;
        fy = 300;
    }
    
    // 随机生成食物
    public static void create(){
        fx = random.nextInt(31)*25;
        fy = random.nextInt(22)*25+50;
        if(!Judge(fx,fy)){
            create();
        }
    }
    
    // 吃食物方法
    public static void eat(){
        if(fx == Snake.skx[0] && fy == Snake.sky[0]){
            Snake.setLength(Snake.getLength() + 1);
            GamePanel.setScore(GamePanel.getScore() + 10);
            GamePanel.setSpeed(GamePanel.getSpeed() - speedChangeRate);
            GamePanel.setSpeed(Math.max(GamePanel.getSpeed(), 30));
            create();
        }
    }
    
    public static boolean Judge(int x,int y){
        // 检查当前坐标是否与蛇身节点重合
        for(int i = 1; i < Snake.getLength(); i++){
            if(x == Snake.skx[i] && y == Snake.sky[i]){
                return false;
            }
        }
        // 食物不能刷在障碍物上，否则会出现“看得到吃不到”的无效点位。
        if (Obstacle.contains(x, y)) {
            return false;
        }
        return true;
    }

    public static void setSpeedChangeRate(int speedChangeRate) {
        Food.speedChangeRate = speedChangeRate;
    }

    public static int getFx() {
        return fx;
    }
    
    public static int getFy() {
        return fy;
    }
}