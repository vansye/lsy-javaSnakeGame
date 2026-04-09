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
    private static int time = 180;
    private static int elapsedTime = 0;
    private static boolean timedMode = false;
    private static boolean scoreUpdate = false;
    private static String gameMode = "normal";

    private static final int SPEED_BOOST_DELTA_MS = 20; // 加速模式每次降低的延迟（ms）
    private static final long SPEED_BOOST_MAX_HOLD_MS = 5000L;
    private static final long SPEED_BOOST_COOLDOWN_MS = 5000L;
    private static final long GOLD_MAX_HOLD_MS = 2000L;
    private static final long GOLD_COOLDOWN_MS = 10000L;
    private static final int FRAME_DELAY_MS = 16;

    private static boolean speedBoostHolding = false;
    private static long speedBoostPressStart = 0L;
    private static long speedBoostCooldownUntil = 0L;

    private static boolean goldHolding = false;
    private static boolean goldActive = false;
    private static long goldPressStart = 0L;
    private static long goldCooldownUntil = 0L;

    private static final Color SPEED_SKILL_COLOR = new Color(0xEFA14E);
    private static final Color GOLD_SKILL_COLOR = new Color(0x6DBE4A);
    private static final Color COOLDOWN_TEXT_COLOR = new Color(0x8B6A36);

    private long lastSecondMark = 0;
    private long lastFrameMark = 0;
    private long logicAccumulatorMs = 0;

    private final JButton backButton = new JButton("<");
    private final JButton settingButton = new JButton("⚙");
    // 仅在开局前/结束后显示的大按钮，避免游戏中遮挡网格
    private final JButton startButton = new JButton("开始游戏");
    private final JButton rankButton = new JButton("排行榜");
    private final JButton menuButton = new JButton("返回菜单");

    private static final Font SCORE_FONT = new Font("幼圆", Font.BOLD, 20);
    private static final Font SKILL_HINT_FONT = new Font("微软雅黑", Font.BOLD, 12);
    private static final Font STATUS_FONT = new Font("幼圆", Font.BOLD, 40);
    private static final Color INFO_CARD_FILL = new Color(255, 255, 255, 170);
    private static final Color INFO_CARD_BORDER = new Color(0xA3D3F7);
    private static final Color INFO_TEXT_COLOR = new Color(0x1B5877);
    private static final Color SKILL_READY_COLOR = new Color(0x3E8E5E);

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
        GamePanel.speed = Math.max(speed, 30);
    }

    public static void setTimedMode(boolean timedMode) {
        GamePanel.timedMode = timedMode;
    }

    public static void setTimeLimit(int timeLimit) {
        GamePanel.time = Math.max(30, timeLimit);
    }

    public static void setGameMode(String mode) {
        if (mode != null && !mode.isEmpty()) {
            gameMode = mode;
            GameConfig.setCurrentMode(mode);
        }
    }

    public static boolean isGoldActive() {
        return goldActive;
    }

    public static void prepareGameByMode(String mode) {
        if (mode != null && !mode.isEmpty()) {
            gameMode = mode;
        }
        GameConfig.ModeSetting setting = GameConfig.getModeSetting(gameMode);
        speed = setting.getStartSpeed();
        timedMode = setting.isTimedMode();
        time = setting.getTimeLimitSec();
        elapsedTime = 0;
        score = 0;
        isStart = false;
        isDead = false;
        scoreUpdate = false;
        speedBoostHolding = false;
        speedBoostPressStart = 0L;
        speedBoostCooldownUntil = 0L;
        goldHolding = false;
        goldActive = false;
        goldPressStart = 0L;
        goldCooldownUntil = 0L;
        Food.setSpeedChangeRate(setting.getSpeedChangeRate());
        Obstacle.init(setting.getObstacleCount());
        if (timer != null) {
            timer.setDelay(FRAME_DELAY_MS);
        }
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

        // 设置按钮
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
        gameMode = MenuPanel.getState();
        GameConfig.setCurrentMode(gameMode);
        applyModeSetting();
        isStart = false;
        isDead = false;
        score = 0;
        elapsedTime = 0;
        scoreUpdate = false;
        lastSecondMark = System.currentTimeMillis();
        speedBoostHolding = false;
        speedBoostPressStart = 0L;
        speedBoostCooldownUntil = 0L;
        goldHolding = false;
        goldActive = false;
        goldPressStart = 0L;
        goldCooldownUntil = 0L;
    }

    private void applyModeSetting() {
        GameConfig.setCurrentMode(gameMode);
        GameConfig.ModeSetting setting = GameConfig.getModeSetting(gameMode);
        speed = setting.getStartSpeed();
        timedMode = setting.isTimedMode();
        time = setting.getTimeLimitSec();
        Food.setSpeedChangeRate(setting.getSpeedChangeRate());
        Obstacle.init(setting.getObstacleCount());
    }

//   设置面板属性
    private void setupPanelProperties() {
        this.setLayout(null);
        this.setFocusable(true);
        this.setPreferredSize(new Dimension(814, 685));
    }

//    设置按钮
    private void setupButtons() {
        backButton.setBounds(12, 8, 34, 34);
        settingButton.setBounds(764, 8, 34, 34);
        // 结束态按钮区域采用上下两行，视觉更居中，不遮挡结算文字
        startButton.setBounds(317, 482, 180, 44);
        menuButton.setBounds(227, 538, 160, 40);
        rankButton.setBounds(427, 538, 160, 40);

        UIFactory.styleIconButton(backButton, new Color(0x4A88D0), Color.WHITE);
        UIFactory.styleIconButton(settingButton, new Color(0x4A88D0), Color.WHITE);
        UIFactory.styleMainButton(startButton, new Color(0x5EBB7E), Color.WHITE);
        UIFactory.styleMainButton(menuButton, new Color(0x59A5C6), Color.WHITE);
        UIFactory.styleMainButton(rankButton, new Color(0x7D9CF5), Color.WHITE);

        // 左上角返回：回到上一个页面（主菜单不会显示该按钮）
        backButton.addActionListener(e -> Main.goBack());
        settingButton.addActionListener(e -> Main.turnPage("setting"));
        startButton.addActionListener(e -> {
            if (isDead) {
                resetGame();
            }
            // 点击开始后切入运行态，并重置秒级计时起点
            isStart = true;
            lastSecondMark = System.currentTimeMillis();
            updateOverlayButtons();
            requestFocusInWindow();
        });
        rankButton.addActionListener(e -> {
            RankPanel.showRank(MenuPanel.getState());
            Main.turnPage("rank");
        });
        menuButton.addActionListener(e -> {
            Main.turnPage("menu");
            MenuPanel.resetMenu();
        });

        add(backButton);
        add(settingButton);
        add(startButton);
        add(menuButton);
        add(rankButton);
        updateOverlayButtons();
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

                handleSkillKeyPressed(e.getKeyCode());

                repaint();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e == null) return;

                handleSkillKeyReleased(e.getKeyCode());

                repaint();
            }
        });
    }

//    处理空格键事件
    private void handleSpaceKey() {
        if (isDead) {
            resetGame();
            return;
        }

        isStart = !isStart;
        if (isStart) {
            lastSecondMark = System.currentTimeMillis();
        } else {
            speedBoostHolding = false;
            speedBoostPressStart = 0L;
            goldHolding = false;
            goldActive = false;
            goldPressStart = 0L;
            if (timer != null) {
                timer.setDelay(FRAME_DELAY_MS);
            }
        }
        updateOverlayButtons();
    }

    private void handleSkillKeyPressed(int keyCode) {
        if (!isStart || isDead) {
            return;
        }

        long now = System.currentTimeMillis();
        if (keyCode == KeyEvent.VK_Q && now >= speedBoostCooldownUntil) {
            if (!speedBoostHolding) {
                speedBoostHolding = true;
                speedBoostPressStart = now;
            }
            return;
        }

        if (keyCode == KeyEvent.VK_F && now >= goldCooldownUntil) {
            if (!goldHolding) {
                goldHolding = true;
                goldActive = true;
                goldPressStart = now;
            }
        }
    }

    private void handleSkillKeyReleased(int keyCode) {
        long now = System.currentTimeMillis();
        if (keyCode == KeyEvent.VK_Q && speedBoostHolding) {
            speedBoostHolding = false;
            speedBoostPressStart = 0L;
            speedBoostCooldownUntil = now + SPEED_BOOST_COOLDOWN_MS;
            return;
        }

        if (keyCode == KeyEvent.VK_F && goldHolding) {
            goldHolding = false;
            goldActive = false;
            goldPressStart = 0L;
            goldCooldownUntil = now + GOLD_COOLDOWN_MS;
        }
    }

    private void updateSkillStates() {
        long now = System.currentTimeMillis();

        if (speedBoostHolding && speedBoostPressStart > 0L && now - speedBoostPressStart >= SPEED_BOOST_MAX_HOLD_MS) {
            speedBoostHolding = false;
            speedBoostPressStart = 0L;
            speedBoostCooldownUntil = now + SPEED_BOOST_COOLDOWN_MS;
        }

        if (goldHolding && goldPressStart > 0L && now - goldPressStart >= GOLD_MAX_HOLD_MS) {
            goldHolding = false;
            goldActive = false;
            goldPressStart = 0L;
            goldCooldownUntil = now + GOLD_COOLDOWN_MS;
        }

    }

    private int getCurrentLogicDelay() {
        return speedBoostHolding ? Math.max(speed - SPEED_BOOST_DELTA_MS, 30) : Math.max(speed, 30);
    }

//     重置游戏状态
    private void resetGame() {
        isDead = false;
        isStart = false;
        score = 0;
        elapsedTime = 0;
        gameMode = MenuPanel.getState();
        applyModeSetting();
        Snake.init();
        Food.init();
        scoreUpdate = false;
        lastSecondMark = System.currentTimeMillis();
        speedBoostHolding = false;
        speedBoostPressStart = 0L;
        speedBoostCooldownUntil = 0L;
        goldHolding = false;
        goldActive = false;
        goldPressStart = 0L;
        goldCooldownUntil = 0L;
        logicAccumulatorMs = 0;
        lastFrameMark = System.currentTimeMillis();
        if (timer != null) {
            timer.setDelay(FRAME_DELAY_MS);
        }
        // 重置后回到“待开始”界面，保留开始按钮
        updateOverlayButtons();
    }

//     初始化定时器
    private void initializeTimer() {
        try {
            timer = new Timer(FRAME_DELAY_MS, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    long now = System.currentTimeMillis();
                    if (lastFrameMark == 0L) {
                        lastFrameMark = now;
                    }
                    long delta = Math.max(0L, Math.min(120L, now - lastFrameMark));
                    lastFrameMark = now;

                    if (isStart && !isDead) {
                        updateSkillStates();
                        logicAccumulatorMs += delta;
                        int logicDelay = getCurrentLogicDelay();
                        try {
                            while (logicAccumulatorMs >= logicDelay && !isDead) {
                                Snake.move();
                                Food.eat();
                                logicAccumulatorMs -= logicDelay;
                            }
                            updateGameTime();
                            if (isDead) {
                                endGame();
                            }
                        } catch (Exception ex) {
                            System.err.println("游戏逻辑执行错误: " + ex.getMessage());
                            isDead = true;
                            isStart = false;
                            endGame();
                        }
                    } else {
                        logicAccumulatorMs = 0L;
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
            drawGameArea(g);
            drawFood(g);
            drawGrid(g);
            drawObstacle(g);
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
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        UIFactory.paintSoftGradient(g2d, getWidth(), getHeight(), new Color(0xECFAFF), new Color(0xDDF4EA));
        g2d.setColor(new Color(0xD7E6F3));
        g2d.fillRect(0, 0, 800, 50);
        g2d.setColor(new Color(0x94BDD8));
        g2d.drawLine(0, 49, 800, 49);
        g2d.dispose();
    }

//      绘制游戏区域
    private void drawGameArea(Graphics g) {
        g.setColor(GameConfig.getBoardColor());
        g.fillRect(0, 50, 800, 600);
    }

//    绘制食物
    private void drawFood(Graphics g) {
        Images.food.paintIcon(this, g, Food.getFx(), Food.getFy());
    }

//      绘制网格
    private void drawGrid(Graphics g) {
        g.setColor(GameConfig.getGridColor());

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

    private void drawObstacle(Graphics g) {
        Obstacle.draw(g);
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

        ImageIcon headIcon = switch (direction) {
            case "R" -> Images.right;
            case "L" -> Images.left;
            case "U" -> Images.up;
            case "D" -> Images.down;
            default -> Images.right;
        };

        headIcon.paintIcon(this, g, headX, headY);
    }

//     绘制蛇身
    private void drawSnakeBody(Graphics g, int snakeLength) {
        for (int i = 1; i < snakeLength && i < 200; i++) {
            int bodyX = Snake.skx[i];
            int bodyY = Snake.sky[i];
            if (bodyX >= 0 && bodyX <= 775 && bodyY >= 50 && bodyY <= 625) {
                Images.body.paintIcon(this, g, bodyX, bodyY);
            }
        }
    }

//      绘制游戏信息
    private void drawGameInfo(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        UIFactory.drawRoundedPanel(g2d, 50, 6, 700, 34, 12, INFO_CARD_FILL, INFO_CARD_BORDER);

        Color infoTextColor = GameConfig.getInfoColor();
        g2d.setColor(infoTextColor);
        g2d.setFont(SCORE_FONT);

        g2d.drawString("得分：" + score, 70, 31);
        g2d.drawString("长度：" + Snake.getLength(), 240, 31);
        g2d.setColor(infoTextColor);
        g2d.drawString("速度：" + (speedBoostHolding ? Math.max(speed - SPEED_BOOST_DELTA_MS, 30) : speed) + "ms", 400, 31);
        
        if (timedMode) {
            g2d.drawString("剩余：" + time + "s", 630, 31);
        } else {
            g2d.drawString("用时：" + elapsedTime + "s", 630, 31);
        }

        drawSkillIcons(g2d, 560, 28, System.currentTimeMillis());
        g2d.dispose();
    }

    private void drawSkillIcons(Graphics2D g2d, int x, int y, long now) {
        drawSingleSkillIcon(
                g2d,
                x,
                y,
                'Q',
                speedBoostHolding,
                speedBoostPressStart,
                speedBoostCooldownUntil,
                SPEED_BOOST_MAX_HOLD_MS,
                SPEED_SKILL_COLOR,
                true,
                now
        );

        drawSingleSkillIcon(
                g2d,
            x + 110,
                y,
                'F',
                goldHolding,
                goldPressStart,
                goldCooldownUntil,
                GOLD_MAX_HOLD_MS,
                GOLD_SKILL_COLOR,
                false,
                now
        );
    }

    private void drawSingleSkillIcon(Graphics2D g2d, int x, int y, char hotkey,
                                     boolean active, long pressStart, long cooldownUntil,
                                     long maxHoldMs, Color activeColor, boolean lightning, long now) {
        Color stateColor = SKILL_READY_COLOR;
        String secondsHint = "";
        if (active && pressStart > 0L) {
            long leftMs = Math.max(0L, maxHoldMs - (now - pressStart));
            stateColor = activeColor;
            secondsHint = String.valueOf((int) Math.ceil(leftMs / 1000.0));
        } else if (now < cooldownUntil) {
            long cdMs = cooldownUntil - now;
            stateColor = COOLDOWN_TEXT_COLOR;
            secondsHint = String.valueOf((int) Math.ceil(cdMs / 1000.0));
        }

        g2d.setColor(stateColor);
        g2d.fillOval(x + 2, y + 4, 10, 10);

        if (lightning) {
            drawLightningIcon(g2d, x + 18, y + 2, stateColor);
        } else {
            drawShieldIcon(g2d, x + 18, y + 2, stateColor);
        }

        g2d.setFont(SKILL_HINT_FONT);
        g2d.setColor(new Color(0x2C567C));
        g2d.drawString(String.valueOf(hotkey), x + 40, y + 14);
        if (!secondsHint.isEmpty()) {
            g2d.setColor(stateColor);
            g2d.drawString(secondsHint + "s", x + 56, y + 14);
        }
    }

    private void drawLightningIcon(Graphics2D g2d, int x, int y, Color color) {
        Polygon bolt = new Polygon();
        bolt.addPoint(x + 4, y);
        bolt.addPoint(x + 10, y);
        bolt.addPoint(x + 7, y + 7);
        bolt.addPoint(x + 13, y + 7);
        bolt.addPoint(x + 5, y + 16);
        bolt.addPoint(x + 7, y + 9);
        bolt.addPoint(x + 2, y + 9);
        g2d.setColor(color);
        g2d.fillPolygon(bolt);
    }

    private void drawShieldIcon(Graphics2D g2d, int x, int y, Color color) {
        Polygon shield = new Polygon();
        shield.addPoint(x + 1, y + 2);
        shield.addPoint(x + 11, y + 2);
        shield.addPoint(x + 11, y + 8);
        shield.addPoint(x + 6, y + 15);
        shield.addPoint(x + 1, y + 8);
        g2d.setColor(color);
        g2d.fillPolygon(shield);
    }

//     绘制游戏状态消息
    private void drawGameStateMessage(Graphics g) {
        // 状态提示与按钮显隐绑定，保证界面元素与当前状态一致
        if (!isStart && !isDead) {
            drawStartMessage(g);
            updateOverlayButtons();
        } else if (isDead && !isStart) {
            drawGameOverMessage(g);
            updateOverlayButtons();
        } else {
            updateOverlayButtons();
        }
    }

//     绘制开始游戏消息
    private void drawStartMessage(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        UIFactory.drawRoundedPanel(g2d, 278, 250, 256, 72, 20, new Color(255, 255, 255, 180), new Color(0x9FD0F4));
        g2d.setColor(new Color(0x1D4F7D));
        g2d.setFont(new Font("幼圆", Font.BOLD, 36));
        String message = "开始游戏";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (814 - fm.stringWidth(message)) / 2;
        g2d.drawString(message, x, 298);
        g2d.dispose();
    }

//     绘制游戏结束消息
    private void drawGameOverMessage(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        UIFactory.drawRoundedPanel(g2d, 238, 210, 338, 200, 20, new Color(255, 255, 255, 200), new Color(0x9FD0F4));

        g2d.setColor(new Color(0xA01A2F));
        g2d.setFont(new Font("幼圆", Font.BOLD, 40));
        g2d.drawString("游戏结束", 307, 266);

        g2d.setColor(new Color(0x2A577E));
        g2d.setFont(new Font("幼圆", Font.BOLD, 26));
        g2d.drawString("项目", 284, 320);
        g2d.drawString("结果", 450, 320);
        g2d.drawLine(274, 334, 534, 334);

        g2d.setFont(new Font("幼圆", Font.BOLD, 30));
        g2d.drawString("最终得分", 284, 370);
        g2d.drawString(String.valueOf(score), 468, 370);

        g2d.drawString("本局用时", 284, 402);
        g2d.drawString(elapsedTime + "s", 468, 402);

        /*g.setColor(new Color(0xFFEAEAEA, true));
        g.setFont(new Font("幼圆", Font.BOLD, 24));
        String tipMessage = "按空格重新开始，或点击左上角返回";
        int x4 = (814 - g.getFontMetrics().stringWidth(tipMessage)) / 2;
        g.drawString(tipMessage, x4, 450);*/

        RankPanel.showRank(MenuPanel.getState());
        g2d.dispose();
    }

    private void updateOverlayButtons() {
        // 游戏进行中不显示大按钮，避免遮挡网格
        if (isStart && !isDead) {
            startButton.setVisible(false);
            menuButton.setVisible(false);
            rankButton.setVisible(false);
            return;
        }

        // 待开始：仅显示开始；结束后：显示开始+返回菜单+排行榜
        startButton.setVisible(true);
        menuButton.setVisible(isDead);
        rankButton.setVisible(isDead);
    }

    private void drawErrorMessage(Graphics g, String errorMessage) {
        if (g == null) return;

        g.setColor(Color.RED);
        g.setFont(SCORE_FONT);
        g.drawString("错误: " + errorMessage, 20, 665);
    }

    private void updateGameTime() {
        long now = System.currentTimeMillis();
        if (lastSecondMark == 0) {
            lastSecondMark = now;
            return;
        }

        if (now - lastSecondMark >= 1000) {
            int step = (int) ((now - lastSecondMark) / 1000);
            elapsedTime += step;
            lastSecondMark += step * 1000L;

            // 限时模式下同步扣减剩余时间，到 0 直接判负
            if (timedMode) {
                time -= step;
                if (time <= 0) {
                    time = 0;
                    isDead = true;
                    isStart = false;
                }
            }
        }
    }

    private void endGame() {
        if (scoreUpdate) {
            return;
        }
        speedBoostHolding = false;
        speedBoostPressStart = 0L;
        goldHolding = false;
        goldActive = false;
        goldPressStart = 0L;
        if (timer != null) {
            timer.setDelay(FRAME_DELAY_MS);
        }
        // 只在首次结算时写入排行榜，避免重复触发
        RankPanel.updateRank(score, gameMode, elapsedTime);
        scoreUpdate = true;
        //initializeGameState();
    }
}


