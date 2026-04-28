package com.SnakeGame.game;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 游戏配置中心：统一管理模式参数、皮肤与限时设置
 */
public final class GameConfig {
    // final + static 的 Map 代表“引用不可变”，但 Map 内容仍可 put/remove。
    // 这里利用它作为全局配置缓存容器。
    private static final Map<String, ModeSetting> MODE_SETTINGS = new HashMap<>();
    private static final Map<String, String> MODE_SKINS = new HashMap<>();
    private static final Map<String, Color> MODE_CUSTOM_BOARD_COLORS = new HashMap<>();
    private static final Map<String, Color> MODE_CUSTOM_GRID_COLORS = new HashMap<>();
    private static final Map<String, Color> MODE_CUSTOM_OBSTACLE_COLORS = new HashMap<>();
    // List.of(...) 返回不可变列表，适合做“枚举型白名单”，避免运行时被误改。
    private static final List<String> SUPPORTED_MODES = List.of("normal", "hard", "crazy");
    private static final List<String> SUPPORTED_SKINS = List.of("classic", "fresh", "night", "custom");
    private static final Path CONFIG_FILE = AppPaths.getUserConfigPath();

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

    // 读取指定模式配置（返回副本，避免外部直接修改内部状态）
    public static ModeSetting getModeSetting(String mode) {
        // getOrDefault: key 不存在时返回默认值，避免 null 分支扩散。
        ModeSetting setting = MODE_SETTINGS.getOrDefault(mode, MODE_SETTINGS.get("normal"));
        // 返回副本而不是原对象，避免外部误改配置中心里的数据。
        return setting.copy();
    }

    // 更新模式参数并做安全边界校验
    public static void updateModeSetting(String mode, int startSpeed, int speedChangeRate,
                                         boolean timedMode, int timeLimitSec, int obstacleCount) {
        if (!isValidMode(mode)) {
            return;
        }
        // 统一在配置中心做边界裁剪，避免 UI 输入或配置文件脏数据破坏运行时逻辑。
        int safeSpeed = Math.max(60, Math.min(startSpeed, 400));
        int safeRate = Math.max(0, Math.min(speedChangeRate, 30));
        int safeTime = Math.max(30, Math.min(timeLimitSec, 600));
        int safeObstacle = Math.max(0, Math.min(obstacleCount, 40));
        MODE_SETTINGS.put(mode, new ModeSetting(safeSpeed, safeRate, timedMode, safeTime, safeObstacle));
    }

    // 切换当前活动模式
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
        // 重载（overload）写法：这个便捷方法把当前模式自动作为第一个参数传给完整版本。
        setCurrentSkin(currentMode, skin);
    }

    public static void setCurrentSkin(String mode, String skin) {
        if (!isValidMode(mode) || !isValidSkin(skin)) {
            return;
        }
        MODE_SKINS.put(mode, skin);
    }

    // 获取当前模式对应场景皮肤
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
        // switch 表达式（->）比传统 switch-case 更紧凑，且天然返回一个值。
        return switch (skin) {
            case "custom" -> new Color(0xE3EAE1);
            case "fresh" -> new Color(0x059526);
            case "night" -> new Color(0x101B2D);
            default -> new Color(0xE3EAE1);
        };
    }

    // 根据皮肤返回棋盘底色（custom 优先读取用户颜色）
    public static Color getBoardColor() {
        String skin = getCurrentSkin();
        if ("custom".equals(skin)) {
            Color custom = getCustomBoardColor();
            if (custom != null) {
                return custom;
            }
            return new Color(236, 151, 151, 255);
        }
        // “字符串常量.equals(变量)”可避免变量为 null 时触发 NPE。
        return switch (skin) {
            case "fresh" -> new Color(0x070707);
            case "night" -> new Color(0x0C0C16);
            default -> new Color(255, 255, 255, 255);
        };
    }

    // 根据皮肤返回网格线颜色（custom 优先读取用户颜色）
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

    public static void setCustomObstacleColor(Color color) {
        setCustomObstacleColor(currentMode, color);
    }

    public static void setCustomObstacleColor(String mode, Color color) {
        if (!isValidMode(mode)) {
            return;
        }
        if (color == null) {
            MODE_CUSTOM_OBSTACLE_COLORS.remove(mode);
            return;
        }
        MODE_CUSTOM_OBSTACLE_COLORS.put(mode, color);
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

    public static Color getCustomObstacleColor() {
        return getCustomObstacleColor(currentMode);
    }

    public static Color getCustomObstacleColor(String mode) {
        if (!isValidMode(mode)) {
            return null;
        }
        return MODE_CUSTOM_OBSTACLE_COLORS.get(mode);
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
        MODE_CUSTOM_OBSTACLE_COLORS.remove(mode);
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
        if ("custom".equals(skin)) {
            Color custom = getCustomObstacleColor();
            if (custom != null) {
                return custom;
            }
            return new Color(0x5E2E0A);
        }
        return switch (skin) {
            case "fresh" -> new Color(0x754827);
            case "night" -> new Color(0x6A83B7);
            default -> new Color(0xF48593);
        };
    }

    // 保存用户偏好到可写配置文件
    public static void saveUserPreferences() {
        Properties props = new Properties();
        // Properties 的值本质是字符串，所以数值/布尔都要 String.valueOf(...)。
        props.setProperty("current.mode", currentMode);

        for (String mode : SUPPORTED_MODES) {
            // 每个模式保存一套完整参数，切模式时可独立恢复，不会互相覆盖。
            ModeSetting setting = MODE_SETTINGS.get(mode);
            if (setting == null) {
                continue;
            }
            // props.setProperty()将一个字符串类型的“键”（Key）和一个字符串类型的“值”（Value）存入内存中的 Properties 对象。
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
            Color obstacle = getCustomObstacleColor(mode);
            if (obstacle != null) {
                props.setProperty(mode + ".custom.obstacle", toHex(obstacle));
            }
        }

        try {
            AppPaths.atomicStore(props, CONFIG_FILE, "Snake Game User Preferences");
        } catch (IOException e) {
            System.err.println("保存配置失败: " + e.getMessage());
        }
    }

    // 启动时加载用户偏好并覆盖默认配置
    private static void loadUserPreferences() {
        if (!Files.exists(CONFIG_FILE)) {
            return;
        }

        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(CONFIG_FILE)) {
            // props.load()从输入流中读取 Properties 格式的数据，解析为键值对存入 props 对象。
            props.load(in);
        } catch (IOException e) {
            System.err.println("读取配置失败: " + e.getMessage());
            return;
        }

        // props.getProperty()根据键名获取对应的字符串值，如果键不存在则返回 null。
        String modeValue = props.getProperty("current.mode");
        if (isValidMode(modeValue)) {
            currentMode = modeValue;
        }

        for (String mode : SUPPORTED_MODES) {
            // 逐字段解析 + 默认值回退：保证缺失字段时仍可用默认模式参数启动。
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
            Color obstacleColor = parseHexColor(props.getProperty(mode + ".custom.obstacle"));
            if (board != null) {
                MODE_CUSTOM_BOARD_COLORS.put(mode, board);
            }
            if (grid != null) {
                MODE_CUSTOM_GRID_COLORS.put(mode, grid);
            }
            if (obstacleColor != null) {
                MODE_CUSTOM_OBSTACLE_COLORS.put(mode, obstacleColor);
            }
        }
    }

    // 模式合法性校验
    private static boolean isValidMode(String mode) {
        return mode != null && SUPPORTED_MODES.contains(mode);
    }

    // 皮肤合法性校验
    private static boolean isValidSkin(String skin) {
        return skin != null && SUPPORTED_SKINS.contains(skin);
    }

    // 整型解析兜底
    private static int parseInt(String value, int defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            // 配置文件可能被手改或损坏，解析失败时兜底，避免启动期崩溃。
            return defaultValue;
        }
    }

    // 布尔解析兜底
    private static boolean parseBoolean(String value, boolean defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        // Boolean.parseBoolean 仅当文本忽略大小写等于 "true" 时返回 true，
        // 其他任意值（包括 "1"、"yes"）都会返回 false。
        return Boolean.parseBoolean(value.trim());
    }

    // 解析 #RRGGBB 颜色字符串
    private static Color parseHexColor(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Color.decode(value);
        } catch (Exception e) {
            // 自定义颜色写错格式时忽略该字段，继续使用主题默认色。
            return null;
        }
    }
    // 将颜色转换为 #RRGGBB 字符串
    private static String toHex(Color color) {
        // %02X: 十六进制大写，不足两位前补 0，最终得到固定长度 #RRGGBB。
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    // 模式配置不可变对象
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

