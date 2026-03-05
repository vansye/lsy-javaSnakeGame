package com.SnakeGame.game;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.MouseAdapter;
import java.awt.Graphics;

public class MenuPanel extends JPanel {
    private static String state;
    private static boolean isStart = false;
    private static boolean rankingMode = false;
    JButton normalButton = new JButton("Normal Model");
    JButton hardButton = new JButton("Hard Model");
    JButton crazyButton = new JButton("Crazy Model");
    static JButton rankingButton = new JButton("Ranking List");
    //JButton startButton = new JButton("Start Game");

    public MenuPanel() {
            setupProperties();
            addButtons();
            setAddMouseListener();
    }
     private void setupProperties() {
        this.setLayout(null);
        this.setBackground(new Color(0x0D5302));
        this.setVisible( true);
        this.setFocusable(true);
    }

       private void addButtons() {
        normalButton.setBounds(300, 100, 200, 50);
        hardButton.setBounds(300, 200, 200, 50);
        crazyButton.setBounds(300, 300, 200, 50);
        rankingButton.setBounds(300, 400, 200, 50);
        //startButton.setBounds(300, 500, 200, 50);
        normalButton.setForeground(new Color(0x31A6C1));
        hardButton.setForeground(new Color(0xAA0417));
        crazyButton.setForeground(new Color(0xF421D1));
        rankingButton.setForeground(new Color(0x19C194));
        normalButton.setFont( new Font("微软雅黑", Font.BOLD, 20));
        hardButton.setFont( new Font("微软雅黑", Font.BOLD, 20));
        crazyButton.setFont( new Font("微软雅黑", Font.BOLD, 20));
        rankingButton.setFont( new Font("微软雅黑", Font.BOLD, 20));
        //startButton.setForeground(new Color(0xE6DB0A));
        setButtonProperties(normalButton);
        setButtonProperties(hardButton);
        setButtonProperties(crazyButton);
        setButtonProperties(rankingButton);
        //setButtonProperties(startButton);
        add(normalButton);
        add(hardButton);
        add(crazyButton);
        add(rankingButton);
        //add(startButton);
    }

    private void setAddMouseListener() {
        normalButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (rankingMode) {
                    state = "normal";
                    RankPanel.showRank(state);
                    Main.turnPage("rank");
                } else {
                    state = "normal";
                    GamePanel.setSpeed(250);
                    Food.setSpeedChangeRate(0);
                    Main.turnPage("game");
                }
                repaint();
            }
        });
        hardButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (rankingMode) {
                    state = "hard";
                    RankPanel.showRank(state);
                    Main.turnPage("rank");
                } else {
                    state = "hard";
                    Food.setSpeedChangeRate(0);
                    GamePanel.setSpeed(100);
                    Main.turnPage("game");
                }
                repaint();
            }
        });
        crazyButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (rankingMode) {
                    state = "crazy";
                    RankPanel.showRank(state);
                    Main.turnPage("rank");
                } else {
                    state = "crazy";
                    Food.setSpeedChangeRate(10);
                    GamePanel.setSpeed(250);
                    Main.turnPage("game");
                }
                repaint();
            }
        });
        rankingButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                rankingButton.setVisible(false);
                rankingMode = true;
                state = "rankingList";
                isStart = true;
                repaint();
            }
        });
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        try{
            drawBackGround(g);
            setMyLabel(g);
    } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private void setButtonProperties(JButton button) {
        button.setBackground(new Color(0xCAE8F1));

    }
    private void setMyLabel(Graphics g) {
        g.setColor(new Color(4, 34, 106));
        g.setFont(new Font("微软雅黑", Font.BOLD, 40));
        g.drawString("Welcome to MySnakeGame By Vansye", 20, 40);
    }
    public static String getState() {
        return MenuPanel.state;
    }

    public static void resetRankingMode() {
        rankingMode = false;
    }

    public static void resetMenu() {
        rankingMode = false;
        rankingButton.setVisible(true);
    }
    private void drawBackGround(Graphics g) {
        setBackground(new Color(0xE9FBE3));
       Images.background.paintIcon(this, g, 0, 70);

    }

}
