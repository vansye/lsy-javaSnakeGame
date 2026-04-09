package com.SnakeGame.game;

import javax.swing.*;
import java.awt.*;

public class Main {
    private static CardLayout cardLayout = new CardLayout();
    private static JPanel mainPanel = new JPanel(cardLayout);
//    public static String state;
    private static String currentPage;
    private static String lastPage;
    public static void main(String[] args) {

        // 创建游戏面板

        GamePanel gamePanel = new GamePanel();
        MenuPanel menuPanel = new MenuPanel();
        RankPanel rankPanel = new RankPanel();
        SettingsPanel settingsPanel = new SettingsPanel();
        String Pname = "menu";
        //String Pname = "game";
        // 创建配置面板
        mainPanel.add(gamePanel, "game");
        mainPanel.add(menuPanel, "menu");
        mainPanel.add(rankPanel, "rank");
        mainPanel.add(settingsPanel, "setting");
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
        if (name == null || name.isEmpty()) {
            return;
        }

        // 记录页面跳转历史，为 goBack() 提供“上一页”信息。
        if (currentPage != null && !currentPage.equals(name)) {
            lastPage = currentPage;
        }
        currentPage = name;

        cardLayout.show(mainPanel, name);
        
        if ("game".equals(name)) {
            GamePanel.prepareGameByMode(MenuPanel.getState());
            Snake.init();
            Food.init();
            
            for (Component comp : mainPanel.getComponents()) {
                if (comp instanceof GamePanel) {
                    comp.requestFocusInWindow();
                    break;
                }
            }
        }

        if ("setting".equals(name)) {
            for (Component comp : mainPanel.getComponents()) {
                if (comp instanceof SettingsPanel) {
                    comp.requestFocusInWindow();
                    break;
                }
            }
        }
    }

    public static void goBack() {
        if (lastPage == null || lastPage.isEmpty()) {
            turnPage("menu");
            return;
        }
        // 交换 current/last，可实现连续返回而不丢失导航轨迹。
        String target = lastPage;
        lastPage = currentPage;
        currentPage = target;
        cardLayout.show(mainPanel, target);

        if ("game".equals(target)) {
            for (Component comp : mainPanel.getComponents()) {
                if (comp instanceof GamePanel) {
                    comp.requestFocusInWindow();
                    break;
                }
            }
        }
    }

    public static String getLastPage() {return lastPage;}

}