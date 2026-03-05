package com.SnakeGame.game;

import javax.swing.*;
import java.awt.*;

public class Main {
    private static CardLayout cardLayout = new CardLayout();
    private static JPanel mainPanel = new JPanel(cardLayout);
    public static String state;
    public static void main(String[] args) {

        // 创建游戏面板

        GamePanel gamePanel = new GamePanel();
        MenuPanel menuPanel = new MenuPanel();
        RankPanel rankPanel = new RankPanel();
        String Pname = "menu";
        //String Pname = "game";
        // 创建配置面板
        mainPanel.add(gamePanel, "game");
        mainPanel.add(menuPanel, "menu");
        mainPanel.add(rankPanel, "rank");
        // 创建并配置窗口
        JFrame frame = new JFrame("Lsy的贪吃蛇游戏");
        frame.setSize(814, 685);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.add(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // 显示窗口
        frame.setVisible(true);

        //默认面板
        //cardLayout.show(mainPanel, "menu");
        turnPage(Pname);
        System.out.println("游戏启动成功！");
    }

    public static void turnPage(String name) {
        cardLayout.show(mainPanel, name);
        
        if ("game".equals(name)) {
            Snake.init();
            Food.init();
            GamePanel.setIsStart(false);
            GamePanel.setIsDead(false);
            GamePanel.setScore(0);
            GamePanel.setScoreUpdate(false);
            
            for (Component comp : mainPanel.getComponents()) {
                if (comp instanceof GamePanel) {
                    comp.requestFocusInWindow();
                    break;
                }
            }
        }
    }

}