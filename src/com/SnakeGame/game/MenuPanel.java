package com.SnakeGame.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 主菜单面板：负责模式入口与导航
 */
public class MenuPanel extends JPanel {
    private String selectedMode = "normal";
    private final JButton normalButton = new JButton("普通模式");
    private final JButton hardButton = new JButton("困难模式");
    private final JButton crazyButton = new JButton("狂暴模式");
    private final JButton IntroButton = new JButton("游戏介绍");
    private final JButton rankingButton = new JButton("排行榜");
    private final JButton settingButton = new JButton("⚙");
    private static final Color HERO_FILL = new Color(255, 255, 255, 170);
    private static final Color HERO_BORDER = new Color(0xA8D8FF);

    public MenuPanel() {
        setupProperties();
        addButtons();
        setAddMouseListener();
    }

    private void setupProperties() {
        this.setLayout(null);
        this.setBackground(new Color(0xE8F7FF));
        this.setVisible(true);
        this.setFocusable(true);
    }

    private void addButtons() {
        normalButton.setBounds(300, 140, 220, 56);
        hardButton.setBounds(300, 230, 220, 56);
        crazyButton.setBounds(300, 320, 220, 56);
        rankingButton.setBounds(300, 410, 220, 56);
        IntroButton.setBounds(300, 500, 220, 56);
        settingButton.setBounds(764, 12, 36, 36);

        UIFactory.styleMainButton(normalButton, new Color(0x58C6A9), Color.WHITE);
        UIFactory.styleMainButton(hardButton, new Color(0xEA8A72), Color.WHITE);
        UIFactory.styleMainButton(crazyButton, new Color(0xA682F0), Color.WHITE);
        UIFactory.styleMainButton(rankingButton, new Color(0xF0B868), Color.WHITE);
        UIFactory.styleIconButton(settingButton, new Color(0x4F8FDB), Color.WHITE);
        UIFactory.styleMainButton(IntroButton, new Color(0xEA8A72), Color.WHITE);

        add(normalButton);
        add(hardButton);
        add(crazyButton);
        add(rankingButton);
        add(settingButton);
        add(IntroButton);
    }

    private void setAddMouseListener() {
        normalButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedMode = "normal";
                Main.turnPage("game");
                repaint();
            }
        });
        hardButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedMode = "hard";
                Main.turnPage("game");
                repaint();
            }
        });
        crazyButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedMode = "crazy";
                Main.turnPage("game");
                repaint();
            }
        });
        rankingButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                RankPanel.showRank("normal");
                Main.turnPage("rank");
                repaint();
            }
        });

        settingButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Main.turnPage("setting");
            }
        });

        IntroButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Main.turnPage("intro");
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        try {
            drawBackGround(g);
            setMyLabel(g);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setMyLabel(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        UIFactory.drawRoundedPanel(g2d, 110, 16, 590, 72, 28, HERO_FILL, HERO_BORDER);
        UIFactory.drawCenteredTitle(
                g2d,
                "My SnakeGame By Vansye",
                405,
                62,
                new Font("幼圆", Font.BOLD, 40),
                new Color(0x2A4A75),
                new Color(255, 255, 255, 190)
        );
        g2d.dispose();
    }

    public String getSelectedMode() {
        return selectedMode;
    }

    public void resetMenu() {
        rankingButton.setVisible(true);
    }

    private void drawBackGround(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        UIFactory.paintSoftGradient(g2d, getWidth(), getHeight(), new Color(0xECFAFF), new Color(0xD4F5E9));

        g2d.setColor(new Color(255, 255, 255, 95));
        g2d.fillOval(40, 90, 220, 120);
        g2d.fillOval(560, 86, 190, 108);
        g2d.fillOval(250, 520, 320, 92);

        if (Images.background != null && Images.background.getIconWidth() > 0) {
            Composite oldComposite = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
            Images.background.paintIcon(this, g2d, 0, 90);
            g2d.setComposite(oldComposite);
        }
        g2d.dispose();
    }
}
