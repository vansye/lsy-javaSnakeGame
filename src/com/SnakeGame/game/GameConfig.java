package com.SnakeGame.game;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 游戏配置中心：统一管理模式参数、皮肤与限时设置
 */
public final class GameConfig {
    private static final Map<String, ModeSetting> MODE_SETTINGS = new HashMap<>();
    private static final Map<String, String> MODE_SKINS = new HashMap<>();
    private static final Map<String, Color> MODE_CUSTOM_BOARD_COLORS = new HashMap<>();
    private static final Map<String, Color> MODE_CUSTOM_GRID_COLORS = new HashMap<>();
    private static final List<String> SUPPORTED_MODES = List.of("normal", "hard", "crazy");
    private static final List<String> SUPPORTED_SKINS = List.of("classic", "fresh", "night", "custom");
    private static final String CONFIG_FILE = "user-config.properties";

    private static String currentMode = "normal";

    static {
        // 静态初始化块：类第一次被加载时执行一次，用来放默认模式参数。
        MODE_SETTINGS.put("normal", new ModeSetting(250, 0, false, 180, 0));
        MODE_SETTINGS.put("hard", new ModeSetting(120, 0, false, 150, 6));
        MODE_SETTINGS.put("crazy", new ModeSetting(250, 10, true, 120, 10));

        MODE_SKINS.put("normal", "classic");
        MODE_SKINS.put("hard", "classic");
        MODE_SKINS.put("crazy", "classic");

        loadUserPreferences();
    }

    private GameConfig() {
        throw new AssertionError("工具类不应被实例化");
    }

    public static ModeSetting getModeSetting(String mode) {
        ModeSetting setting = MODE_SETTINGS.getOrDefault(mode, MODE_SETTINGS.get("normal"));
        // 返回副本而不是原对象，避免外部误改配置中心里的数据。
        return setting.copy();
    }

    public static void updateModeSetting(String mode, int startSpeed, int speedChangeRate,
                                         boolean timedMode, int timeLimitSec, int obstacleCount) {
        if (!isValidMode(mode)) {
            return;
        }
        int safeSpeed = Math.max(60, Math.min(startSpeed, 400));
        int safeRate = Math.max(0, Math.min(speedChangeRate, 30));
        int safeTime = Math.max(30, Math.min(timeLimitSec, 600));
        int safeObstacle = Math.max(0, Math.min(obstacleCount, 40));
        MODE_SETTINGS.put(mode, new ModeSetting(safeSpeed, safeRate, timedMode, safeTime, safeObstacle));
    }

    public static void setCurrentMode(String mode) {
        if (!isValidMode(mode)) {
            return;
        }
        currentMode = mode;
    }

    public static String getCurrentMode() {
        return currentMode;
    }

    public static void setCurrentSkin(String skin) {
        setCurrentSkin(currentMode, skin);
    }

    public static void setCurrentSkin(String mode, String skin) {
        if (!isValidMode(mode) || !isValidSkin(skin)) {
            return;
        }
        MODE_SKINS.put(mode, skin);
    }

    public static String getCurrentSkin() {
        return getSkinForMode(currentMode);
    }

    public static String getSkinForMode(String mode) {
        if (!isValidMode(mode)) {
            return "classic";
        }
        return MODE_SKINS.getOrDefault(mode, "classic");
    }

    public static Color getPanelColor() {
        String skin = getCurrentSkin();
        return switch (skin) {
            case "custom" -> new Color(0xE3EAE1);
            case "fresh" -> new Color(0x059526);
            case "night" -> new Color(0x101B2D);
            default -> new Color(0xE3EAE1);
        };
    }

    public static Color getBoardColor() {
        String skin = getCurrentSkin();
        if ("custom".equals(skin)) {
            Color custom = getCustomBoardColor();
            if (custom != null) {
                return custom;
            }
            return new Color(236, 151, 151, 255);
        }
        return switch (skin) {
            case "fresh" -> new Color(0x070707);
            case "night" -> new Color(0x0C0C16);
            default -> new Color(255, 255, 255, 255);
        };
    }

    public static Color getGridColor() {
        String skin = getCurrentSkin();
        if ("custom".equals(skin)) {
            Color custom = getCustomGridColor();
            if (custom != null) {
                return custom;
            }
            return new Color(222, 232, 220, 58);
        }
        return switch (skin) {
            case "fresh" -> new Color(0x2CC50B);
            case "night" -> new Color(0x2E4E86);
            default -> new Color(214, 230, 211, 58);
        };
    }

    public static void setCustomBoardColor(Color color) {
        setCustomBoardColor(currentMode, color);
    }

    public static void setCustomBoardColor(String mode, Color color) {
        if (!isValidMode(mode)) {
            return;
        }
        if (color == null) {
            MODE_CUSTOM_BOARD_COLORS.remove(mode);
            return;
        }
        MODE_CUSTOM_BOARD_COLORS.put(mode, color);
    }

    public static void setCustomGridColor(Color color) {
        setCustomGridColor(currentMode, color);
    }

    public static void setCustomGridColor(String mode, Color color) {
        if (!isValidMode(mode)) {
            return;
        }
        if (color == null) {
            MODE_CUSTOM_GRID_COLORS.remove(mode);
            return;
        }
        MODE_CUSTOM_GRID_COLORS.put(mode, color);
    }

    public static Color getCustomBoardColor() {
        return getCustomBoardColor(currentMode);
    }

    public static Color getCustomBoardColor(String mode) {
        if (!isValidMode(mode)) {
            return null;
        }
        return MODE_CUSTOM_BOARD_COLORS.get(mode);
    }

    public static Color getCustomGridColor() {
        return getCustomGridColor(currentMode);
    }

    public static Color getCustomGridColor(String mode) {
        if (!isValidMode(mode)) {
            return null;
        }
        return MODE_CUSTOM_GRID_COLORS.get(mode);
    }

    public static void clearCustomGameColors() {
        clearCustomGameColors(currentMode);
    }

    public static void clearCustomGameColors(String mode) {
        if (!isValidMode(mode)) {
            return;
        }
        MODE_CUSTOM_BOARD_COLORS.remove(mode);
        MODE_CUSTOM_GRID_COLORS.remove(mode);
    }

    public static String getCurrentSnakeSkin() {
        return "classic";
    }

    public static boolean isSnakeSkinManualOverride() {
        return false;
    }

    public static void setCurrentSnakeSkin(String skinName, boolean manualOverride) {
        // 蛇皮肤切换功能已移除，固定使用 classic
    }

    /*public static void resetSnakeSkinToThemeDefault() {
        // 蛇皮肤切换功能已移除，固定使用 classic。
    }
*/
    public static String getDefaultSnakeSkinForTheme(String theme) {
        return "classic";
    }

    public static Color getInfoColor() {
        String skin = getCurrentSkin();
        return switch (skin) {
            case "custom" -> new Color(0xFF38E4B6, true);
            case "fresh" -> new Color(0x1B8F57);
            case "night" -> new Color(0x64C5FF);
            default -> new Color(0xFF38E4B6, true);
        };
    }

    public static Color getObstacleColor() {
        String skin = getCurrentSkin();
        return switch (skin) {
            case "custom" -> new Color(0x5E2E0A);
            case "fresh" -> new Color(0x754827);
            case "night" -> new Color(0x6A83B7);
            default -> new Color(0xF48593);
        };
    }

    public static void saveUserPreferences() {
        Properties props = new Properties();
        props.setProperty("current.mode", currentMode);

        for (String mode : SUPPORTED_MODES) {
            ModeSetting setting = MODE_SETTINGS.get(mode);
            if (setting == null) {
                continue;
            }
            props.setProperty(mode + ".speed", String.valueOf(setting.getStartSpeed()));
            props.setProperty(mode + ".speedRate", String.valueOf(setting.getSpeedChangeRate()));
            props.setProperty(mode + ".timed", String.valueOf(setting.isTimedMode()));
            props.setProperty(mode + ".timeLimit", String.valueOf(setting.getTimeLimitSec()));
            props.setProperty(mode + ".obstacle", String.valueOf(setting.getObstacleCount()));

            props.setProperty(mode + ".skin", getSkinForMode(mode));

            Color board = getCustomBoardColor(mode);
            Color grid = getCustomGridColor(mode);
            if (board != null) {
                props.setProperty(mode + ".custom.board", toHex(board));
            }
            if (grid != null) {
                props.setProperty(mode + ".custom.grid", toHex(grid));
            }
        }

        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "Snake Game User Preferences");
        } catch (IOException e) {
            System.err.println("保存配置失败: " + e.getMessage());
        }
    }

    private static void loadUserPreferences() {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) {
            return;
        }

        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(file)) {
            props.load(in);
        } catch (IOException e) {
            System.err.println("读取配置失败: " + e.getMessage());
            return;
        }

        String modeValue = props.getProperty("current.mode");
        if (isValidMode(modeValue)) {
            currentMode = modeValue;
        }

        for (String mode : SUPPORTED_MODES) {
            int speed = parseInt(props.getProperty(mode + ".speed"), MODE_SETTINGS.get(mode).getStartSpeed());
            int speedRate = parseInt(props.getProperty(mode + ".speedRate"), MODE_SETTINGS.get(mode).getSpeedChangeRate());
            boolean timed = parseBoolean(props.getProperty(mode + ".timed"), MODE_SETTINGS.get(mode).isTimedMode());
            int timeLimit = parseInt(props.getProperty(mode + ".timeLimit"), MODE_SETTINGS.get(mode).getTimeLimitSec());
            int obstacle = parseInt(props.getProperty(mode + ".obstacle"), MODE_SETTINGS.get(mode).getObstacleCount());
            updateModeSetting(mode, speed, speedRate, timed, timeLimit, obstacle);

            String skin = props.getProperty(mode + ".skin");
            if (isValidSkin(skin)) {
                MODE_SKINS.put(mode, skin);
            }

            Color board = parseHexColor(props.getProperty(mode + ".custom.board"));
            Color grid = parseHexColor(props.getProperty(mode + ".custom.grid"));
            if (board != null) {
                MODE_CUSTOM_BOARD_COLORS.put(mode, board);
            }
            if (grid != null) {
                MODE_CUSTOM_GRID_COLORS.put(mode, grid);
            }
        }
    }

    private static boolean isValidMode(String mode) {
        return mode != null && SUPPORTED_MODES.contains(mode);
    }

    private static boolean isValidSkin(String skin) {
        return skin != null && SUPPORTED_SKINS.contains(skin);
    }

    private static int parseInt(String value, int defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static boolean parseBoolean(String value, boolean defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }

    private static Color parseHexColor(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Color.decode(value);
        } catch (Exception e) {
            return null;
        }
    }

    private static String toHex(Color color) {
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static final class ModeSetting {
        private final int startSpeed;
        private final int speedChangeRate;
        private final boolean timedMode;
        private final int timeLimitSec;
        private final int obstacleCount;

        private ModeSetting(int startSpeed, int speedChangeRate, boolean timedMode, int timeLimitSec, int obstacleCount) {
            this.startSpeed = startSpeed;
            this.speedChangeRate = speedChangeRate;
            this.timedMode = timedMode;
            this.timeLimitSec = timeLimitSec;
            this.obstacleCount = obstacleCount;
        }

        private ModeSetting copy() {
            // 不可变对象 + copy：常见配置对象写法，便于安全共享。
            return new ModeSetting(startSpeed, speedChangeRate, timedMode, timeLimitSec, obstacleCount);
        }

        public int getStartSpeed() {
            return startSpeed;
        }

        public int getSpeedChangeRate() {
            return speedChangeRate;
        }

        public boolean isTimedMode() {
            return timedMode;
        }

        public int getTimeLimitSec() {
            return timeLimitSec;
        }

        public int getObstacleCount() {
            return obstacleCount;
        }
    }
}

