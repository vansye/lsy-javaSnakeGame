package com.SnakeGame.game;

import javax.swing.*;
import java.awt.*;

/**
 * 设置面板 - 配置皮肤、模式参数与限时模式
 */
public class SettingsPanel extends JPanel {
    // 模式配置按当前选中模式读写，皮肤配置为全局设置
    // JFrame组件下拉框，进行初始化配置
    private final JComboBox<String> modeSelector = new JComboBox<>(new String[]{"normal", "hard", "crazy"});
    private final JComboBox<String> skinSelector = new JComboBox<>(new String[]{"classic", "fresh", "night","custom"});
    private final JTextField speedField = new JTextField();
    private final JTextField speedRateField = new JTextField();
    private final JTextField timeField = new JTextField();
    private final JTextField obstacleField = new JTextField();
    private final JButton boardColorButton = new JButton("选择棋盘颜色");
    private final JButton gridColorButton = new JButton("选择网格颜色");
    // 复选框
    private final JCheckBox timedModeCheckBox = new JCheckBox("开启限时模式");

    private final JButton saveButton = new JButton("保存设置");
    private final JButton backButton = new JButton("<");
    private static final Color CARD_FILL = new Color(255, 255, 255, 180);
    private static final Color CARD_BORDER = new Color(0x9CD0F5);
    private static final Color PREVIEW_DEFAULT_BG = new Color(0x7E95B8);
    private Color selectedBoardColor;
    private Color selectedGridColor;

    public SettingsPanel() {
        setupPanel();
        setupControls();
        bindEvents();
        refreshModeFields();
    }

    private void setupPanel() {
        setLayout(null);
        setFocusable(true);
        setBackground(new Color(0xEAF8FF));
    }

    private void setupControls() {
        JLabel title = new JLabel("游戏设置");
        title.setBounds(340, 28, 200, 40);
        title.setFont(new Font("幼圆", Font.BOLD, 36));
        title.setForeground(new Color(0x1D4E89));
        add(title);

        addLabel("模式：", 190, 108);
        modeSelector.setBounds(300, 108, 260, 35);
        modeSelector.setSelectedItem(GameConfig.getCurrentMode());
        UIFactory.styleComboBox(modeSelector);
        add(modeSelector);

        addLabel("皮肤：", 190, 156);
        skinSelector.setBounds(300, 156, 260, 35);
        UIFactory.styleComboBox(skinSelector);
        add(skinSelector);

        addLabel("棋盘颜色：", 170, 204);
        setupButton(boardColorButton, 300, 204, new Color(0x3F89C9));
        boardColorButton.setSize(178, 35);

        addLabel("网格颜色：", 170, 252);
        setupButton(gridColorButton, 300, 252, new Color(0x4CA67E));
        gridColorButton.setSize(178, 35);

        addLabel("初始速度(ms)：", 150, 300);
        speedField.setBounds(300, 300, 260, 35);
        UIFactory.styleTextField(speedField);
        add(speedField);

        addLabel("加速变化值：", 150, 348);
        speedRateField.setBounds(300, 348, 260, 35);
        UIFactory.styleTextField(speedRateField);
        add(speedRateField);

        addLabel("限时秒数：", 170, 396);
        timeField.setBounds(300, 396, 260, 35);
        UIFactory.styleTextField(timeField);
        add(timeField);

        addLabel("障碍数量：", 170, 444);
        obstacleField.setBounds(300, 444, 260, 35);
        UIFactory.styleTextField(obstacleField);
        add(obstacleField);

        timedModeCheckBox.setBounds(300, 492, 260, 35);
        UIFactory.styleCheckBox(timedModeCheckBox, CARD_FILL);
        add(timedModeCheckBox);

        setupButton(saveButton, 318, 536, new Color(0x2D8C4B));
        setupIconButton(backButton, 12, 12, new Color(0x4A88D0));

        refreshModeFields();
    }

    private void addLabel(String text, int x, int y) {
        JLabel label = new JLabel(text);
        label.setBounds(x, y, 140, 35);
        label.setFont(new Font("幼圆", Font.BOLD, 20));
        label.setForeground(new Color(0x1C3A64));
        add(label);
    }

    private void setupButton(JButton button, int x, int y, Color bgColor) {
        button.setBounds(x, y, 170, 44);
        UIFactory.styleMainButton(button, bgColor, Color.WHITE);
        add(button);
    }

    private void setupIconButton(JButton button, int x, int y, Color bgColor) {
        button.setBounds(x, y, 34, 34);
        UIFactory.styleIconButton(button, bgColor, Color.WHITE);
        add(button);
    }

    private void bindEvents() {
        // 切换模式时，右侧参数输入框实时切到该模式的当前配置
        modeSelector.addActionListener(e -> {
            String mode = (String) modeSelector.getSelectedItem();
            GameConfig.setCurrentMode(mode);
            refreshModeFields();
        });

        skinSelector.addActionListener(e -> updateCustomColorControls());

        boardColorButton.addActionListener(e -> {
            if (!"custom".equals(skinSelector.getSelectedItem())) {
                return;
            }
            Color chosen = JColorChooser.showDialog(this, "选择棋盘颜色", selectedBoardColor);
            if (chosen != null) {
                selectedBoardColor = chosen;
                refreshColorButtonView(boardColorButton, chosen);
            }
        });

        gridColorButton.addActionListener(e -> {
            if (!"custom".equals(skinSelector.getSelectedItem())) {
                return;
            }
            Color chosen = JColorChooser.showDialog(this, "选择网格颜色", selectedGridColor);
            if (chosen != null) {
                selectedGridColor = chosen;
                refreshColorButtonView(gridColorButton, chosen);
            }
        });

        saveButton.addActionListener(e -> {
            // 先保存当前模式参数，再保存全局皮肤，避免跨模式覆盖
            saveCurrentModeSetting();
            String mode = (String) modeSelector.getSelectedItem();
            GameConfig.setCurrentMode(mode);

            String theme = (String) skinSelector.getSelectedItem();
            GameConfig.setCurrentSkin(mode, theme);

            if ("custom".equals(theme)) {
                GameConfig.setCustomBoardColor(mode, selectedBoardColor);
                GameConfig.setCustomGridColor(mode, selectedGridColor);
            }

            GameConfig.saveUserPreferences();


            JOptionPane.showMessageDialog(this, "设置已保存", "提示", JOptionPane.INFORMATION_MESSAGE);
        });

        backButton.addActionListener(e -> Main.goBack());
    }

    private void refreshModeFields() {
        // 回填选中模式的参数，便于可视化编辑
        String mode = (String) modeSelector.getSelectedItem();
        GameConfig.ModeSetting setting = GameConfig.getModeSetting(mode);
        speedField.setText(String.valueOf(setting.getStartSpeed()));
        speedRateField.setText(String.valueOf(setting.getSpeedChangeRate()));
        timeField.setText(String.valueOf(setting.getTimeLimitSec()));
        obstacleField.setText(String.valueOf(setting.getObstacleCount()));
        timedModeCheckBox.setSelected(setting.isTimedMode());

        skinSelector.setSelectedItem(GameConfig.getSkinForMode(mode));
        selectedBoardColor = GameConfig.getCustomBoardColor(mode);
        selectedGridColor = GameConfig.getCustomGridColor(mode);
        updateCustomColorControls();
    }

    private void saveCurrentModeSetting() {
        String mode = (String) modeSelector.getSelectedItem();
        // 输入异常时使用默认值兜底，避免界面输入导致配置崩溃
        int speed = parseInt(speedField.getText(), 250);
        int speedRate = parseInt(speedRateField.getText(), 0);
        int limitTime = parseInt(timeField.getText(), 180);
        int obstacleCount = parseInt(obstacleField.getText(), 0);
        GameConfig.updateModeSetting(mode, speed, speedRate, timedModeCheckBox.isSelected(), limitTime, obstacleCount);
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private void updateCustomColorControls() {
        boolean isCustom = "custom".equals(skinSelector.getSelectedItem());
        boardColorButton.setEnabled(isCustom);
        gridColorButton.setEnabled(isCustom);

        refreshColorButtonView(boardColorButton, isCustom ? selectedBoardColor : null);
        refreshColorButtonView(gridColorButton, isCustom ? selectedGridColor : null);
    }

    private void refreshColorButtonView(JButton button, Color color) {
        if (color == null) {
            button.setText("跟随主题默认");
            button.setBackground(PREVIEW_DEFAULT_BG);
            button.setForeground(Color.WHITE);
            return;
        }
        button.setText(String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()));
        button.setBackground(color);
        int luminance = color.getRed() * 3 + color.getGreen() * 6 + color.getBlue();
        button.setForeground(luminance >= 1020 ? Color.BLACK : Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        UIFactory.paintSoftGradient(g2d, getWidth(), getHeight(), new Color(0xEEFAFF), new Color(0xDFF5EC));
        UIFactory.drawRoundedPanel(g2d, 120, 82, 560, 518, 26, CARD_FILL, CARD_BORDER);
        UIFactory.drawRoundedPanel(g2d, 280, 530, 250, 56, 24, new Color(255, 255, 255, 190), CARD_BORDER);
        g2d.dispose();
    }
}

