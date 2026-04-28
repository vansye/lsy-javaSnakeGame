package com.SnakeGame.game;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/**
 * 统一管理可写数据目录，避免 JAR 运行时写入到只读位置。
 */
public final class AppPaths {
    private static final String APP_NAME = "SnakeGame";
    private static final String RANKINGS_FILE = "rankings.dat";
    private static final String USER_CONFIG_FILE = "user-config.properties";

    private static final Path DATA_DIR = resolveDataDir();
    private static final Path RANKINGS_PATH = DATA_DIR.resolve(RANKINGS_FILE);
    private static final Path USER_CONFIG_PATH = DATA_DIR.resolve(USER_CONFIG_FILE);

    private AppPaths() {
        throw new AssertionError("工具类不应被实例化");
    }

    // 初始化数据目录并迁移历史文件
    public static void bootstrap() {
        try {
            // 这是一个静态方法。它的作用是递归地创建目录。如果父目录不存在，它会自动连同父目录一起创建
            Files.createDirectories(DATA_DIR);
            // 这行代码执行了一个“检查并迁移”的逻辑。如果旧文件存在且新位置没有，就把数据复制过去。
            migrateLegacyFileIfNeeded(RANKINGS_FILE, RANKINGS_PATH);
            migrateLegacyFileIfNeeded(USER_CONFIG_FILE, USER_CONFIG_PATH);
            // 初始化属性文件, 保证后续读取配置文件时，不会因文件不存在而崩溃
            initializePropertiesFile(RANKINGS_PATH);
            initializePropertiesFile(USER_CONFIG_PATH);
        } catch (IOException e) {
            // 抛出一个异常，表示初始化数据目录失败，并附带原始异常信息，方便调试和用户反馈。
            throw new IllegalStateException("初始化数据目录失败: " + e.getMessage(), e);
        }
    }

    // 排行榜文件路径
    public static Path getRankingsPath() {
        return RANKINGS_PATH;
    }

    // 用户配置文件路径
    public static Path getUserConfigPath() {
        return USER_CONFIG_PATH;
    }

    // 解析可写数据目录：优先系统属性，其次 APPDATA，最后用户目录
    private static Path resolveDataDir() {
        String customDir = System.getProperty("snake.data.dir");
        if (customDir != null && !customDir.trim().isEmpty()) {
            return Path.of(customDir.trim());
        }

        String appData = System.getenv("APPDATA");
        if (appData != null && !appData.trim().isEmpty()) {
            return Path.of(appData, APP_NAME);
        }

        return Path.of(System.getProperty("user.home"), ".snakegame");
    }

    // 初始化空文件（若不存在）
    private static void initializePropertiesFile(Path path) throws IOException {
        if (Files.exists(path)) {
            return;
        }
        Files.createFile(path);
    }

    // 首次迁移旧版根目录文件到数据目录
    private static void migrateLegacyFileIfNeeded(String legacyFileName, Path targetPath) throws IOException {
        Path legacyPath = Path.of(legacyFileName);
        if (Files.exists(legacyPath) && !Files.exists(targetPath)) {
            try (InputStream in = Files.newInputStream(legacyPath);
                 OutputStream out = Files.newOutputStream(targetPath)) {
                in.transferTo(out);
            }
        }
    }

    // 原子写入属性文件，降低异常中断导致的数据损坏风险
    public static void atomicStore(Properties props, Path targetPath, String comment) throws IOException {
        // 创建临时文件路径
        Path tempPath = targetPath.resolveSibling(targetPath.getFileName() + ".tmp");
        // 将 Properties 数据写入临时文件
        try (OutputStream out = Files.newOutputStream(tempPath)) {
            props.store(out, comment);
        }
        // 原子移动替换原文件，失败时降级为普通移动
        try {
            Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException atomicMoveError) {
            Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}




