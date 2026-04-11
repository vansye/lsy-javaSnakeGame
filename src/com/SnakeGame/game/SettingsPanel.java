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
    private final JButton obstacleColorButton = new JButton("选择障碍颜色");
    // 复选框
    private final JCheckBox timedModeCheckBox = new JCheckBox("开启限时模式");

    private final JButton saveButton = new JButton("保存设置");
    private final JButton backButton = new JButton("<");
    private final JPanel contentPanel = new JPanel(null);
    private JScrollPane scrollPane;
    private static final Color CARD_FILL = new Color(255, 255, 255, 180);
    private static final Color CARD_BORDER = new Color(0x9CD0F5);
    private static final Color PREVIEW_DEFAULT_BG = new Color(0x7E95B8);
    private Color selectedBoardColor;
    private Color selectedGridColor;
    private Color selectedObstacleColor;

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
        contentPanel.setOpaque(false);
    }

    private void setupControls() {
        JLabel title = new JLabel("游戏设置");
        title.setBounds(286, 24, 200, 40);
        title.setFont(new Font("幼圆", Font.BOLD, 36));
        title.setForeground(new Color(0x1D4E89));
        contentPanel.add(title);

        addLabel("模式：", 130, 92);
        modeSelector.setBounds(240, 92, 260, 35);
        modeSelector.setSelectedItem(GameConfig.getCurrentMode());
        UIFactory.styleComboBox(modeSelector);
        contentPanel.add(modeSelector);

        addLabel("皮肤：", 130, 140);
        skinSelector.setBounds(240, 140, 260, 35);
        UIFactory.styleComboBox(skinSelector);
        contentPanel.add(skinSelector);

        addLabel("棋盘颜色：", 110, 188);
        setupButton(boardColorButton, 240, 188, new Color(0x3F89C9));
        boardColorButton.setSize(178, 35);

        addLabel("网格颜色：", 110, 236);
        setupButton(gridColorButton, 240, 236, new Color(0x4CA67E));
        gridColorButton.setSize(178, 35);

        addLabel("障碍颜色：", 110, 284);
        setupButton(obstacleColorButton, 240, 284, new Color(0xB17A32));
        obstacleColorButton.setSize(178, 35);

        addLabel("初始速度ms：", 90, 332);
        speedField.setBounds(240, 332, 260, 35);
        UIFactory.styleTextField(speedField);
        contentPanel.add(speedField);

        addLabel("加速变化值：", 90, 380);
        speedRateField.setBounds(240, 380, 260, 35);
        UIFactory.styleTextField(speedRateField);
        contentPanel.add(speedRateField);

        addLabel("限时秒数：", 110, 428);
        timeField.setBounds(240, 428, 260, 35);
        UIFactory.styleTextField(timeField);
        contentPanel.add(timeField);

        addLabel("障碍数量：", 110, 476);
        obstacleField.setBounds(240, 476, 260, 35);
        UIFactory.styleTextField(obstacleField);
        contentPanel.add(obstacleField);

        timedModeCheckBox.setBounds(240, 524, 260, 35);
        UIFactory.styleCheckBox(timedModeCheckBox, CARD_FILL);
        contentPanel.add(timedModeCheckBox);

        setupButton(saveButton, 258, 584, new Color(0x2D8C4B));
        setupIconButton(backButton, 12, 12, new Color(0x4A88D0));

        contentPanel.setPreferredSize(new Dimension(640, 680));
        scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBounds(65, 78, 670, 560);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0x9CD0F5), 0, true));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        add(scrollPane);

        refreshModeFields();
    }

    private void addLabel(String text, int x, int y) {
        JLabel label = new JLabel(text);
        label.setBounds(x, y, 140, 35);
        label.setFont(new Font("幼圆", Font.BOLD, 20));
        label.setForeground(new Color(0x1C3A64));
        contentPanel.add(label);
    }

    private void setupButton(JButton button, int x, int y, Color bgColor) {
        button.setBounds(x, y, 170, 44);
        UIFactory.styleMainButton(button, bgColor, Color.WHITE);
        contentPanel.add(button);
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

        obstacleColorButton.addActionListener(e -> {
            if (!"custom".equals(skinSelector.getSelectedItem())) {
                return;
            }
            Color chosen = JColorChooser.showDialog(this, "选择障碍颜色", selectedObstacleColor);
            if (chosen != null) {
                selectedObstacleColor = chosen;
                refreshColorButtonView(obstacleColorButton, chosen);
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
                GameConfig.setCustomObstacleColor(mode, selectedObstacleColor);
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
        selectedObstacleColor = GameConfig.getCustomObstacleColor(mode);
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
        obstacleColorButton.setEnabled(isCustom);

        refreshColorButtonView(boardColorButton, isCustom ? selectedBoardColor : null);
        refreshColorButtonView(gridColorButton, isCustom ? selectedGridColor : null);
        refreshColorButtonView(obstacleColorButton, isCustom ? selectedObstacleColor : null);
    }

    public void onPageEnter(String preferredMode) {
        String mode = normalizeMode(preferredMode);
        modeSelector.setSelectedItem(mode);
        GameConfig.setCurrentMode(mode);
        refreshModeFields();
        if (scrollPane != null) {
            scrollPane.getVerticalScrollBar().setValue(0);
        }
    }

    private String normalizeMode(String mode) {
        if ("hard".equals(mode) || "crazy".equals(mode)) {
            return mode;
        }
        return "normal";
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
        UIFactory.drawRoundedPanel(g2d, 74, 64, 664, 584, 26, CARD_FILL, CARD_BORDER);
        g2d.dispose();
    }
}

