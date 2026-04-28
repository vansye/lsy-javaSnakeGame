package com.SnakeGame.game;

import javax.swing.*;
import java.awt.*;

/**
 * 程序入口与页面路由中心
 */
public class Main {
    private static CardLayout cardLayout = new CardLayout();
    private static JPanel mainPanel = new JPanel(cardLayout);

    // 页面实例引用（由 main 方法初始化）
    private static GamePanel gamePanel;
    private static MenuPanel menuPanel;

    private static String currentPage;
    private static String lastPage;

    public static void main(String[] args) {
        try {
            AppPaths.bootstrap();
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(null, "初始化存档目录失败：" + e.getMessage(), "启动失败", JOptionPane.ERROR_MESSAGE);
            return;
        }

        gamePanel = new GamePanel();
        menuPanel = new MenuPanel();
        RankPanel rankPanel = new RankPanel();
        GameIntroPanel gameIntroPanel = new GameIntroPanel();
        SettingsPanel settingsPanel = new SettingsPanel();

        String Pname = "menu";

        mainPanel.add(gamePanel, "game");
        mainPanel.add(menuPanel, "menu");
        mainPanel.add(rankPanel, "rank");
        mainPanel.add(settingsPanel, "setting");
        mainPanel.add(gameIntroPanel, "intro");

        JFrame frame = new JFrame("Lsy的贪吃蛇游戏");
        frame.setSize(814, 685);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.add(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setVisible(true);

        turnPage(Pname);
        System.out.println("游戏启动成功！");
    }

    public static void turnPage(String name) {
        if (name == null || name.isEmpty()) {
            return;
        }

        if (currentPage != null && !currentPage.equals(name)) {
            lastPage = currentPage;
        }
        currentPage = name;

        cardLayout.show(mainPanel, name);

        if ("game".equals(name)) {
            gamePanel.prepareGameByMode(menuPanel.getSelectedMode());
            gamePanel.requestFocusInWindow();
        }

        if ("menu".equals(name)) {
            menuPanel.resetMenu();
        }

        if ("setting".equals(name)) {
            String preferredMode = "normal";
            if ("game".equals(lastPage)) {
                preferredMode = gamePanel.getCurrentGameMode();
            }
            for (Component comp : mainPanel.getComponents()) {
                if (comp instanceof SettingsPanel) {
                    ((SettingsPanel) comp).onPageEnter(preferredMode);
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
        String target = lastPage;
        lastPage = currentPage;
        currentPage = target;
        cardLayout.show(mainPanel, target);

        if ("game".equals(target)) {
            gamePanel.requestFocusInWindow();
        }
    }

    public static String getLastPage() { return lastPage; }
}
