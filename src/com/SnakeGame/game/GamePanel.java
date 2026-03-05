package com.SnakeGame.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

//     游戏主面板类 - 负责游戏的主要逻辑和渲染

public class GamePanel extends JPanel {
    // 游戏状态变量
    private static boolean isStart = false;
    private static boolean isDead = false;
    private static int score = 0;
    public static Timer timer;
    private static int speed = 250;
    private static boolean scoreUpdate = false;



    private JButton menuButton = new JButton("返回菜单");
    private JButton rankButton = new JButton("查看排名");
    
    // 字体缓存
    private static final Font SCORE_FONT = new Font("微软雅黑", Font.BOLD, 20);
    private static final Font STATUS_FONT = new Font("微软雅黑", Font.BOLD, 40);

    public static boolean isIsStart() {
        return isStart;
    }

    public static void setIsStart(boolean isStart) {
        GamePanel.isStart = isStart;
    }

    public static boolean isIsDead() {
        return isDead;
    }

    public static void setIsDead(boolean isDead) {
        GamePanel.isDead = isDead;
    }

    public static int getScore() {
        return score;
    }

    public static void setScore(int score) {
        GamePanel.score = score;
    }

    public static int getSpeed() {
        return speed;
    }

    public static void setSpeed(int speed) {
        GamePanel.speed = speed;
    }

    public static boolean isScoreUpdate() {
        return scoreUpdate;
    }

    public static void setScoreUpdate(boolean scoreUpdate) {
        GamePanel.scoreUpdate = scoreUpdate;
    }

    public GamePanel() {
        // 初始化游戏状态
        initializeGameState();
        
        // 设置面板属性
        setupPanelProperties();
        
        // 初始化按钮
        setupButtons();
        
        // 添加键盘监听器
        addKeyboardListener();
        
        // 初始化定时器
        initializeTimer();
        
        // 启动定时器
        if (timer != null) {
            timer.start();
        }
    }
//    游戏初始化
    private void initializeGameState() {
        Snake.init();
        Food.init();
        isStart = false;
        isDead = false;
        score = 0;
        speed = 250;
        scoreUpdate = false;
        hideButtons();
    }
    
//   设置面板属性
    private void setupPanelProperties() {
        this.setLayout(null);
        this.setFocusable(true);
        this.setPreferredSize(new Dimension(814, 685));
    }

//    设置按钮
    private void setupButtons() {

        menuButton.setBounds(307, 370, 200, 50);
        rankButton.setBounds(332, 450, 150, 50);
        
        menuButton.setBackground(new Color(0x18C16D));
        rankButton.setBackground(new Color(0x21AE4C));

        menuButton.setFont(SCORE_FONT);
        rankButton.setFont(SCORE_FONT);
        
        menuButton.setForeground(Color.WHITE);
        rankButton.setForeground(Color.WHITE);
        
        menuButton.setVisible(false);
        rankButton.setVisible(false);
        
        menuButton.addActionListener(e -> {
            Main.turnPage("menu");
            MenuPanel.resetMenu();
            hideButtons();
        });
        
        rankButton.addActionListener(e -> {
            Main.turnPage("rank");
            hideButtons();
        });
        
        add(menuButton);
        add(rankButton);
    }

    private void showButtons() {
        menuButton.setVisible(true);
        rankButton.setVisible(true);
    }

    private void hideButtons() {
        menuButton.setVisible(false);
        rankButton.setVisible(false);
    }

//     添加键盘监听器

    private void addKeyboardListener() {
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e == null) return;
                
                super.keyPressed(e);
                Snake.keyPressed(e);

                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    handleSpaceKey();
                }
                
                repaint();
            }
        });
    }

//    处理空格键事件

    private void handleSpaceKey() {
        isStart = !isStart;
        
        if (isDead) {
            // 重新开始游戏
            resetGame();
        }
    }
    

//     重置游戏状态

    private void resetGame() {
        isDead = false;
        score = 0;
        speed = 250;
        Snake.init();
        Food.init();
        scoreUpdate = false;
        
        hideButtons();
        
        if (timer != null) {
            timer.setDelay(speed);
        }
    }

//     初始化定时器

    private void initializeTimer() {
        try {
            timer = new Timer(speed, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (isStart && !isDead) {
                        try {
                            Snake.move();
                            Food.eat();
                        } catch (Exception ex) {
                            System.err.println("游戏逻辑执行错误: " + ex.getMessage());
                            // 发生错误时停止游戏
                            isDead = true;
                            isStart = false;
                        }
                    }
                    repaint();
                }
            });
        } catch (Exception e) {
            System.err.println("定时器初始化失败: " + e.getMessage());
            timer = null;
        }
    }

//      绘制游戏面板

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        try {
            // 检查图形上下文
            if (g == null) return;
            
            drawBackground(g);
            drawTitle(g);
            drawGameArea(g);
            drawFood(g);
            drawGrid(g);
            drawSnake(g);
            drawGameInfo(g);
            drawGameStateMessage(g);
        } catch (Exception e) {
            System.err.println("绘制过程中发生错误: " + e.getMessage());
            // 绘制错误信息
            drawErrorMessage(g, "绘制错误: " + e.getMessage());
        }
    }

//     绘制背景
    private void drawBackground(Graphics g) {
        this.setBackground(new Color(80, 220, 60));
    }

//      绘制标题
    private void drawTitle(Graphics g) {
        if (Images.title != null) {
            Images.title.paintIcon(this, g, 0, 0);
        }
    }

//      绘制游戏区域
    private void drawGameArea(Graphics g) {
        g.setColor(new Color(5, 0, 5, 255));
        g.fillRect(0, 50, 800, 600);
    }
    
//    绘制食物
    private void drawFood(Graphics g) {
        if (Images.food != null) {
            Images.food.paintIcon(this, g, Food.getFx(), Food.getFy());
        }
    }

//      绘制网格
    private void drawGrid(Graphics g) {
        g.setColor(new Color(146, 241, 133, 255));
        
        // 绘制垂直线
        for (int x = 0; x <= 32; x++) {
            int pX = x * 25;
            g.drawLine(pX, 50, pX, 650);
        }
        
        // 绘制水平线
        for (int y = 0; y <= 24; y++) {
            int pY = y * 25 + 50;
            g.drawLine(0, pY, 800, pY);
        }

    }
//    绘制蛇

    private void drawSnake(Graphics g) {
        int snakeLength = Snake.getLength();
        if (snakeLength <= 0) return;
        
        // 绘制蛇头
        drawSnakeHead(g);
        
        // 绘制蛇身
        drawSnakeBody(g, snakeLength);
    }
    
//    绘制蛇头

    private void drawSnakeHead(Graphics g) {
        String direction = Snake.getDirection();
        int headX = Snake.skx[0];
        int headY = Snake.sky[0];
        
        ImageIcon headIcon = null;
        switch (direction) {
            case "R":
                headIcon = Images.right;
                break;
            case "L":
                headIcon = Images.left;
                break;
            case "U":
                headIcon = Images.up;
                break;
            case "D":
                headIcon = Images.down;
                break;
        }
        
        if (headIcon != null) {
            headIcon.paintIcon(this, g, headX, headY);
        }
    }

//     绘制蛇身
    private void drawSnakeBody(Graphics g, int snakeLength) {
        if (Images.body == null) return;
        
        for (int i = 1; i < snakeLength && i < 200; i++) {
            int bodyX = Snake.skx[i];
            int bodyY = Snake.sky[i];
            
            // 检查坐标有效性
            if (bodyX >= 0 && bodyX <= 775 && bodyY >= 50 && bodyY <= 625) {
                Images.body.paintIcon(this, g, bodyX, bodyY);
            }
        }
    }

//      绘制游戏信息

    private void drawGameInfo(Graphics g) {
        g.setColor(new Color(0xFF38E4B6, true));
        g.setFont(SCORE_FONT);
        
        g.drawString("得分为：" + score, 20, 20);
        g.drawString("长度为：" + Snake.getLength(), 20, 45);
        g.drawString("速度为：" + speed + "帧", 600, 20);
    }

//     绘制游戏状态消息
    private void drawGameStateMessage(Graphics g) {
        if (!isStart && !isDead) {
            drawStartMessage(g);
            menuButton.setVisible(true);
            rankButton.setVisible(false);
        } else if (isDead && !isStart) {
            drawGameOverMessage(g);
        } else if (isStart && !isDead) {
            menuButton.setVisible(false);
            rankButton.setVisible(false);
        }
    }
    
//     绘制开始游戏消息
    private void drawStartMessage(Graphics g) {
        g.setColor(new Color(0xFF0A27E6, true));
        g.setFont(STATUS_FONT);
        String message = "点击空格开始游戏";
        FontMetrics fm = g.getFontMetrics();
        int x = (814 - fm.stringWidth(message)) / 2;
        g.drawString(message, x, 300);
    }
    
//     绘制游戏结束消息
    private void drawGameOverMessage(Graphics g) {
        g.setColor(new Color(0xFFAA0417, true));
        g.setFont(STATUS_FONT);
        String gameOverMessage = "游戏结束";
        FontMetrics fm = g.getFontMetrics();
        int x1 = (814 - fm.stringWidth(gameOverMessage)) / 2;
        g.drawString(gameOverMessage, x1, 300);
        
        g.setColor(new Color(0xFFE6DB0A, true));
        String scoreMessage = "最终得分为：" + score;
        int x2 = (814 - fm.stringWidth(scoreMessage)) / 2;
        g.drawString(scoreMessage, x2, 350);
        
        if(!scoreUpdate){
            RankPanel.updateRank(score, MenuPanel.getState());
            scoreUpdate = true;
        }
        RankPanel.showRank(MenuPanel.getState());
        
        menuButton.setVisible(true);
        rankButton.setVisible(true);
    }
    
//     绘制错误消息
    private void drawErrorMessage(Graphics g, String errorMessage) {
        if (g == null) return;
        
        g.setColor(Color.RED);
        g.setFont(SCORE_FONT);
        g.drawString("错误: " + errorMessage, 20, 665);
    }
}