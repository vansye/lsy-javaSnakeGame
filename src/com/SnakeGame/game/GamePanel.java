package com.SnakeGame.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 游戏主面板：负责核心循环、输入处理与场景渲染
 */
public class GamePanel extends JPanel {
    // 游戏状态变量（实例字段）
    private boolean started = false;
    private boolean dead = false;
    private int score = 0;
    public Timer timer;
    private int speed = 250;
    private int time = 180;
    private int elapsedTime = 0;
    private boolean timedMode = false;
    private boolean scoreUpdate = false;
    private String gameMode = "normal";

    // 游戏实体（实例字段）
    private Snake snake;
    private Food food;
    private Obstacle obstacle;

    private static final int SPEED_BOOST_DELTA_MS = 50;
    private static final long SPEED_BOOST_MAX_HOLD_MS = 5000L;
    private static final long SPEED_BOOST_COOLDOWN_MS = 3000L;
    private static final long GOLD_MAX_HOLD_MS = 2000L;
    private static final long GOLD_COOLDOWN_MS = 5000L;
    private static final int FRAME_DELAY_MS = 16;

    private boolean speedBoostHolding = false;
    private long speedBoostPressStart = 0L;
    private long speedBoostCooldownUntil = 0L;
    private boolean speedBoostKeyPressed = false;
    private boolean speedBoostMousePressed = false;

    private boolean goldHolding = false;
    private boolean goldActive = false;
    private long goldPressStart = 0L;
    private long goldCooldownUntil = 0L;
    private boolean goldKeyPressed = false;
    private boolean goldMousePressed = false;

    private static final Color SPEED_SKILL_COLOR = new Color(0xEFA14E);
    private static final Color GOLD_SKILL_COLOR = new Color(0x6DBE4A);
    private static final Color COOLDOWN_TEXT_COLOR = new Color(0x8B6A36);

    private long lastSecondMark = 0;
    private long lastFrameMark = 0;
    private long logicAccumulatorMs = 0;
    private float renderAlpha = 0f;

    private final JButton backButton = new JButton("<");
    private final JButton settingButton = new JButton("⚙");
    private final JButton startButton = new JButton("开始游戏");
    private final JButton menu_1Button = new JButton("返回菜单");
    private final JButton rankButton = new JButton("排行榜");
    private final JButton menuButton = new JButton("返回菜单");

    private static final Font SCORE_FONT = new Font("幼圆", Font.BOLD, 20);
    private static final Font SKILL_HINT_FONT = new Font("微软雅黑", Font.BOLD, 12);
    private static final Font STATUS_FONT = new Font("幼圆", Font.BOLD, 40);
    private static final Color INFO_CARD_FILL = new Color(255, 255, 255, 170);
    private static final Color INFO_CARD_BORDER = new Color(0xA3D3F7);
    private static final Color INFO_TEXT_COLOR = new Color(0x1B5877);
    private static final Color SKILL_READY_COLOR = new Color(0x3E8E5E);

    // ========== 构造器 ==========

    public GamePanel() {
        snake = new Snake();
        food = new Food();
        obstacle = new Obstacle();

        setupPanelProperties();
        setupButtons();
        addKeyboardListener();
        addMouseControlListener();
        initializeTimer();
        if (timer != null) {
            timer.start();
        }
    }

    // ========== 状态访问器 ==========

    public boolean isStarted() { return started; }
    public void setStarted(boolean started) { this.started = started; }

    public boolean isDead() { return dead; }
    public void setDead(boolean dead) { this.dead = dead; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = Math.max(speed, 30); }

    public boolean isGoldActive() { return goldActive; }

    public Obstacle getObstacle() { return obstacle; }

    public String getCurrentGameMode() { return gameMode; }

    public boolean isScoreUpdate() { return scoreUpdate; }
    public void setScoreUpdate(boolean scoreUpdate) { this.scoreUpdate = scoreUpdate; }

    // ========== 模式准备与重置 ==========

    public void prepareGameByMode(String mode) {
        if (mode != null && !mode.isEmpty()) {
            gameMode = mode;
        }
        GameConfig.setCurrentMode(gameMode);
        GameConfig.ModeSetting setting = GameConfig.getModeSetting(gameMode);
        speed = setting.getStartSpeed();
        timedMode = setting.isTimedMode();
        time = setting.getTimeLimitSec();
        elapsedTime = 0;
        score = 0;
        started = false;
        dead = false;
        scoreUpdate = false;
        resetSkillStates();
        food.setSpeedChangeRate(setting.getSpeedChangeRate());
        snake.init();
        obstacle.init(setting.getObstacleCount(), snake);
        food.init(snake, obstacle);
        if (timer != null) {
            timer.setDelay(FRAME_DELAY_MS);
        }
    }

    private void resetSkillStates() {
        speedBoostHolding = false;
        speedBoostPressStart = 0L;
        speedBoostCooldownUntil = 0L;
        speedBoostKeyPressed = false;
        speedBoostMousePressed = false;
        goldHolding = false;
        goldActive = false;
        goldPressStart = 0L;
        goldCooldownUntil = 0L;
        goldKeyPressed = false;
        goldMousePressed = false;
    }

    // 重置游戏状态并回到待开始界面
    private void resetGame() {
        dead = false;
        started = false;
        score = 0;
        elapsedTime = 0;
        scoreUpdate = false;
        resetSkillStates();
        GameConfig.ModeSetting setting = GameConfig.getModeSetting(gameMode);
        speed = setting.getStartSpeed();
        timedMode = setting.isTimedMode();
        time = setting.getTimeLimitSec();
        food.setSpeedChangeRate(setting.getSpeedChangeRate());
        snake.init();
        obstacle.init(setting.getObstacleCount(), snake);
        food.init(snake, obstacle);
        logicAccumulatorMs = 0;
        renderAlpha = 0f;
        lastFrameMark = System.currentTimeMillis();
        if (timer != null) {
            timer.setDelay(FRAME_DELAY_MS);
        }
        updateOverlayButtons();
    }

    // ========== UI 初始化 ==========

    private void setupPanelProperties() {
        this.setLayout(null);
        this.setFocusable(true);
        this.setPreferredSize(new Dimension(814, 685));
    }

    private void setupButtons() {
        backButton.setBounds(12, 8, 34, 34);
        settingButton.setBounds(764, 8, 34, 34);
        startButton.setBounds(317, 482, 180, 44);
        menuButton.setBounds(227, 538, 160, 40);
        menu_1Button.setBounds(317, 546, 180, 44);
        rankButton.setBounds(427, 538, 160, 40);

        UIFactory.styleIconButton(backButton, new Color(0x4A88D0), Color.WHITE);
        UIFactory.styleIconButton(settingButton, new Color(0x4A88D0), Color.WHITE);
        UIFactory.styleMainButton(startButton, new Color(0x5EBB7E), Color.WHITE);
        UIFactory.styleMainButton(menu_1Button, new Color(0x5EBB7E), Color.WHITE);
        UIFactory.styleMainButton(menuButton, new Color(0x59A5C6), Color.WHITE);
        UIFactory.styleMainButton(rankButton, new Color(0x7D9CF5), Color.WHITE);

        backButton.addActionListener(e -> Main.goBack());
        settingButton.addActionListener(e -> Main.turnPage("setting"));
        startButton.addActionListener(e -> {
            if (dead) {
                resetGame();
            }
            started = true;
            lastSecondMark = System.currentTimeMillis();
            updateOverlayButtons();
            requestFocusInWindow();
        });
        rankButton.addActionListener(e -> {
            RankPanel.showRank(gameMode);
            Main.turnPage("rank");
        });
        menuButton.addActionListener(e -> Main.turnPage("menu"));
        menu_1Button.addActionListener(e -> Main.turnPage("menu"));

        add(backButton);
        add(settingButton);
        add(startButton);
        add(menuButton);
        add(menu_1Button);
        add(rankButton);
        updateOverlayButtons();
    }

    // ========== 输入监听 ==========

    private void addKeyboardListener() {
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e == null) return;

                super.keyPressed(e);
                snake.keyPressed(e);

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

    private void addMouseControlListener() {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e == null || !started || dead || !isInGameBoard(e.getX(), e.getY())) {
                    return;
                }
                requestFocusInWindow();
                if (SwingUtilities.isRightMouseButton(e)) {
                    handleSkillMousePressed(MouseEvent.BUTTON3);
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    handleSkillMousePressed(MouseEvent.BUTTON1);
                }
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e == null) return;

                if (SwingUtilities.isRightMouseButton(e)) {
                    handleSkillMouseReleased(MouseEvent.BUTTON3);
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    handleSkillMouseReleased(MouseEvent.BUTTON1);
                }
                repaint();
            }
        });
    }

    private boolean isInGameBoard(int x, int y) {
        return x >= 0 && x <= 800 && y >= 50 && y <= 650;
    }

    // ========== 游戏控制 ==========

    private void handleSpaceKey() {
        if (dead) {
            resetGame();
            return;
        }

        started = !started;
        if (started) {
            lastSecondMark = System.currentTimeMillis();
        } else {
            resetSkillStates();
            if (timer != null) {
                timer.setDelay(FRAME_DELAY_MS);
            }
        }
        updateOverlayButtons();
    }

    // ========== 技能系统 ==========

    private void handleSkillKeyPressed(int keyCode) {
        if (!started || dead) return;

        long now = System.currentTimeMillis();
        if (keyCode == KeyEvent.VK_Q) {
            pressSpeedBoostSource(false, now);
        } else if (keyCode == KeyEvent.VK_F) {
            pressGoldSource(false, now);
        }
    }

    private void handleSkillKeyReleased(int keyCode) {
        long now = System.currentTimeMillis();
        if (keyCode == KeyEvent.VK_Q) {
            releaseSpeedBoostSource(false, now);
        } else if (keyCode == KeyEvent.VK_F) {
            releaseGoldSource(false, now);
        }
    }

    private void handleSkillMousePressed(int button) {
        if (!started || dead) return;

        long now = System.currentTimeMillis();
        if (button == MouseEvent.BUTTON3) {
            pressSpeedBoostSource(true, now);
        } else if (button == MouseEvent.BUTTON1) {
            pressGoldSource(true, now);
        }
    }

    private void handleSkillMouseReleased(int button) {
        long now = System.currentTimeMillis();
        if (button == MouseEvent.BUTTON3) {
            releaseSpeedBoostSource(true, now);
        } else if (button == MouseEvent.BUTTON1) {
            releaseGoldSource(true, now);
        }
    }

    private void pressSpeedBoostSource(boolean fromMouse, long now) {
        if (fromMouse) {
            speedBoostMousePressed = true;
        } else {
            speedBoostKeyPressed = true;
        }
        if (!speedBoostHolding && now >= speedBoostCooldownUntil) {
            speedBoostHolding = true;
            speedBoostPressStart = now;
        }
    }

    private void releaseSpeedBoostSource(boolean fromMouse, long now) {
        if (fromMouse) {
            speedBoostMousePressed = false;
        } else {
            speedBoostKeyPressed = false;
        }
        if (!speedBoostHolding) return;
        if (speedBoostKeyPressed || speedBoostMousePressed) return;
        speedBoostHolding = false;
        speedBoostPressStart = 0L;
        speedBoostCooldownUntil = now + SPEED_BOOST_COOLDOWN_MS;
    }

    private void pressGoldSource(boolean fromMouse, long now) {
        if (fromMouse) {
            goldMousePressed = true;
        } else {
            goldKeyPressed = true;
        }
        if (!goldHolding && now >= goldCooldownUntil) {
            goldHolding = true;
            goldActive = true;
            goldPressStart = now;
        }
    }

    private void releaseGoldSource(boolean fromMouse, long now) {
        if (fromMouse) {
            goldMousePressed = false;
        } else {
            goldKeyPressed = false;
        }
        if (!goldHolding) return;
        if (goldKeyPressed || goldMousePressed) return;
        goldHolding = false;
        goldActive = false;
        goldPressStart = 0L;
        goldCooldownUntil = now + GOLD_COOLDOWN_MS;
    }

    private void updateSkillStates() {
        long now = System.currentTimeMillis();

        if (speedBoostHolding && speedBoostPressStart > 0L && now - speedBoostPressStart >= SPEED_BOOST_MAX_HOLD_MS) {
            speedBoostHolding = false;
            speedBoostPressStart = 0L;
            speedBoostCooldownUntil = now + SPEED_BOOST_COOLDOWN_MS;
            speedBoostKeyPressed = false;
            speedBoostMousePressed = false;
        }

        if (goldHolding && goldPressStart > 0L && now - goldPressStart >= GOLD_MAX_HOLD_MS) {
            goldHolding = false;
            goldActive = false;
            goldPressStart = 0L;
            goldCooldownUntil = now + GOLD_COOLDOWN_MS;
            goldKeyPressed = false;
            goldMousePressed = false;
        }
    }

    private int getCurrentLogicDelay() {
        return speedBoostHolding ? Math.max(speed - SPEED_BOOST_DELTA_MS, 30) : Math.max(speed, 30);
    }

    // ========== 定时器与游戏循环 ==========

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

                    if (started && !dead) {
                        updateSkillStates();
                        logicAccumulatorMs += delta;
                        int logicDelay = getCurrentLogicDelay();
                        try {
                            while (logicAccumulatorMs >= logicDelay && !dead) {
                                snake.move(obstacle, GamePanel.this);
                                food.eat(snake, GamePanel.this);
                                logicAccumulatorMs -= logicDelay;
                            }
                            renderAlpha = Math.max(0f, Math.min(1f, logicAccumulatorMs / (float) Math.max(1, logicDelay)));
                            updateGameTime();
                            if (dead) {
                                endGame();
                            }
                        } catch (Exception ex) {
                            System.err.println("游戏逻辑执行错误: " + ex.getMessage());
                            dead = true;
                            started = false;
                            endGame();
                        }
                    } else {
                        logicAccumulatorMs = 0L;
                        renderAlpha = 0f;
                    }
                    repaint();
                }
            });
        } catch (Exception e) {
            System.err.println("定时器初始化失败: " + e.getMessage());
            timer = null;
        }
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

            if (timedMode) {
                time -= step;
                if (time <= 0) {
                    time = 0;
                    dead = true;
                    started = false;
                }
            }
        }
    }

    private void endGame() {
        if (scoreUpdate) return;
        resetSkillStates();
        if (timer != null) {
            timer.setDelay(FRAME_DELAY_MS);
        }
        RankPanel.updateRank(score, gameMode, elapsedTime);
        scoreUpdate = true;
    }

    // ========== 绘制 ==========

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        try {
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
            drawErrorMessage(g, "绘制错误: " + e.getMessage());
        }
    }

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

    private void drawGameArea(Graphics g) {
        g.setColor(GameConfig.getBoardColor());
        g.fillRect(0, 50, 800, 600);
    }

    private void drawFood(Graphics g) {
        for (int i = 0; i < food.getFoodCount(); i++) {
            Images.food.paintIcon(this, g, food.getFoodX(i), food.getFoodY(i));
        }
    }

    private void drawGrid(Graphics g) {
        g.setColor(GameConfig.getGridColor());
        for (int x = 0; x <= 32; x++) {
            g.drawLine(x * 25, 50, x * 25, 650);
        }
        for (int y = 0; y <= 24; y++) {
            g.drawLine(0, y * 25 + 50, 800, y * 25 + 50);
        }
    }

    private void drawObstacle(Graphics g) {
        obstacle.draw(g, goldActive);
    }

    private void drawSnake(Graphics g) {
        int snakeLength = snake.getLength();
        if (snakeLength <= 0) return;
        float alpha = (started && !dead) ? renderAlpha : 1f;
        drawSnakeHead(g, alpha);
        drawSnakeBody(g, snakeLength, alpha);
    }

    private void drawSnakeHead(Graphics g, float alpha) {
        String direction = snake.getDirection();
        int headX = snake.getRenderX(0, alpha);
        int headY = snake.getRenderY(0, alpha);

        ImageIcon headIcon = switch (direction) {
            case "R" -> Images.right;
            case "L" -> Images.left;
            case "U" -> Images.up;
            case "D" -> Images.down;
            default -> Images.right;
        };

        headIcon.paintIcon(this, g, headX, headY);
    }

    private void drawSnakeBody(Graphics g, int snakeLength, float alpha) {
        for (int i = 1; i < snakeLength && i < 200; i++) {
            int bodyX = snake.getRenderX(i, alpha);
            int bodyY = snake.getRenderY(i, alpha);
            if (bodyX >= -25 && bodyX <= 800 && bodyY >= 25 && bodyY <= 650) {
                Images.body.paintIcon(this, g, bodyX, bodyY);
            }
        }
    }

    private void drawGameInfo(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        UIFactory.drawRoundedPanel(g2d, 50, 6, 700, 34, 12, INFO_CARD_FILL, INFO_CARD_BORDER);

        g2d.setColor(INFO_TEXT_COLOR);
        g2d.setFont(SCORE_FONT);

        g2d.drawString("得分：" + score, 70, 31);
        g2d.drawString("长度：" + snake.getLength(), 240, 31);
        g2d.setColor(INFO_TEXT_COLOR);
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
        drawSingleSkillIcon(g2d, x, y, "Q/右", speedBoostHolding, speedBoostPressStart, speedBoostCooldownUntil, SPEED_BOOST_MAX_HOLD_MS, SPEED_SKILL_COLOR, true, now);
        drawSingleSkillIcon(g2d, x + 110, y, "F/左", goldHolding, goldPressStart, goldCooldownUntil, GOLD_MAX_HOLD_MS, GOLD_SKILL_COLOR, false, now);
    }

    private void drawSingleSkillIcon(Graphics2D g2d, int x, int y, String hotkeyLabel,
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
        g2d.drawString(hotkeyLabel, x + 40, y + 14);
        if (!secondsHint.isEmpty()) {
            int secondsX = x + 46 + g2d.getFontMetrics().stringWidth(hotkeyLabel);
            g2d.setColor(stateColor);
            g2d.drawString(secondsHint + "s", secondsX, y + 14);
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

    private void drawGameStateMessage(Graphics g) {
        if (!started && !dead) {
            drawStartMessage(g);
            updateOverlayButtons();
        } else if (dead && !started) {
            drawGameOverMessage(g);
            updateOverlayButtons();
        } else {
            updateOverlayButtons();
        }
    }

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

        RankPanel.showRank(gameMode);
        g2d.dispose();
    }

    private void updateOverlayButtons() {
        if (started && !dead) {
            startButton.setVisible(false);
            menuButton.setVisible(false);
            rankButton.setVisible(false);
            menu_1Button.setVisible(false);
            return;
        }

        startButton.setVisible(true);
        menu_1Button.setVisible(!started && !dead);
        menuButton.setVisible(dead);
        rankButton.setVisible(dead);
    }

    private void drawErrorMessage(Graphics g, String errorMessage) {
        if (g == null) return;
        g.setColor(Color.RED);
        g.setFont(SCORE_FONT);
        g.drawString("错误: " + errorMessage, 20, 665);
    }
}
