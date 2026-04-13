package com.SnakeGame.game;

import javax.swing.*;
import java.awt.*;
public class GameIntroPanel extends JPanel {
    private final JButton backButton = new JButton("<");
    private final JButton startButton = new JButton("开始游戏");
    private final JButton menuButton = new JButton("返回菜单");
    private final JTextPane introTextPane = new JTextPane();

    public GameIntroPanel() {
        setLayout(null);
        setFocusable(true);
        setBackground(new Color(0xEAF8FF));

        setupButtons();
        setupIntroText();
        addComponents();
    }

    private void setupButtons() {
        backButton.setBounds(12, 10, 34, 34);
        startButton.setBounds(620, 602, 150, 42);
        menuButton.setBounds(450, 602, 150, 42);

        UIFactory.styleIconButton(backButton, new Color(0x4A88D0), Color.WHITE);
        UIFactory.styleMainButton(startButton, new Color(0x58C6A9), Color.WHITE);
        UIFactory.styleMainButton(menuButton, new Color(0x58C6A9), Color.WHITE);

        backButton.addActionListener(e -> Main.goBack());
        startButton.addActionListener(e -> Main.turnPage("game"));
        menuButton.addActionListener(e -> Main.turnPage("menu"));
    }

    private void setupIntroText() {
        introTextPane.setEditable(false);
        introTextPane.setFocusable(false);
        introTextPane.setContentType("text/html");
        introTextPane.setBackground(new Color(255, 255, 255, 0));
        introTextPane.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        introTextPane.setText(buildUserIntroHtml());
        introTextPane.setCaretPosition(0);
    }

    private void addComponents() {
        JScrollPane scrollPane = new JScrollPane(introTextPane);
        scrollPane.setBounds(42, 106, 730, 484);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0xA7D3F0), 2, true));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        add(backButton);
        add(scrollPane);
        add(startButton);
        add(menuButton);
    }

    private String buildUserIntroHtml() {
        return """
                <html>
                <head>
                    <style>
                        body {
                            font-family: \"Microsoft YaHei\", sans-serif;
                            font-size: 15px;
                            color: #244A69;
                            line-height: 1.65;
                            margin: 4px 8px;
                        }
                        h1 {
                            color: #1A5E95;
                            font-size: 26px;
                            margin-bottom: 8px;
                        }
                        h2 {
                            color: #2674A8;
                            font-size: 19px;
                            margin-top: 16px;
                            margin-bottom: 6px;
                        }
                        ul { margin-top: 4px; margin-bottom: 8px; }
                        li { margin-bottom: 4px; }
                        .tip {
                            background: #F2FAFF;
                            border: 1px solid #B9DCF5;
                            border-radius: 8px;
                            padding: 8px 10px;
                            margin-top: 8px;
                        }
                    </style>
                </head>
                <body>
                    <h1>欢迎来到贪吃蛇</h1>
                    <p>这是一款基于 Java Swing 的经典贪吃蛇游戏。你的目标很简单：控制蛇移动、吃到更多食物、拿到更高分数。</p>

                    <h2>三种模式，按你节奏来</h2>
                    <ul>
                        <li><b>Normal</b>：标准速度，适合入门和热身。</li>
                        <li><b>Hard</b>：移动更快，考验反应和路线规划。</li>
                        <li><b>Crazy</b>：吃得越多越快，挑战上限操作。</li>
                    </ul>

                    <h2>核心操作</h2>
                    <ul>
                        <li><b>移动</b>：方向键或 WASD。</li>
                        <li><b>开始/暂停</b>：空格键。</li>
                        <li><b>加速</b>：该状态下能一定程度上加速。<br>方式：按住 Q 或按住鼠标右键（最多 5 秒，冷却 3 秒）。</li>
                        <li><b>金身</b>：该状态下能穿过障碍物，但撞到墙和身体仍然会被判死亡。<br>方式：按住 F 或按住鼠标左键（最多 2 秒，冷却 5 秒）。</li>
                    </ul>

                    <h2>规则说明</h2>
                    <ul>
                        <li>每吃到 1 个食物可得 10 分，蛇身也会变长。</li>
                        <li>蛇头撞墙、自己或障碍物会结束游戏。</li>
                        <li>排行榜按模式分别记录，最高分会自动保存。</li>
                    </ul>

                    <div class=\"tip\">
                        建议：新手先从 Normal 开始，先稳定路线，再尝试技能连用冲高分。
                    </div>
                </body>
                </html>
                """;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        UIFactory.paintSoftGradient(g2d, getWidth(), getHeight(), new Color(0xECFAFF), new Color(0xDDF4EA));
        UIFactory.drawRoundedPanel(g2d, 52, 24, 710, 64, 24,
                new Color(255, 255, 255, 185), new Color(0xA7D3F0));
        UIFactory.drawCenteredTitle(g2d, "游戏介绍", 407, 65,
                new Font("幼圆", Font.BOLD, 38), new Color(0x27557E), new Color(255, 255, 255, 190));

        g2d.dispose();
    }
}
