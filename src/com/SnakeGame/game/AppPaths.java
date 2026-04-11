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
       // 初始化数据目录和文件，迁移旧文件（如果存在）并确保新文件存在
    public static void bootstrap() {
        try {
            Files.createDirectories(DATA_DIR);
            migrateLegacyFileIfNeeded(RANKINGS_FILE, RANKINGS_PATH);
            migrateLegacyFileIfNeeded(USER_CONFIG_FILE, USER_CONFIG_PATH);
            initializePropertiesFile(RANKINGS_PATH);
            initializePropertiesFile(USER_CONFIG_PATH);
        } catch (IOException e) {
            throw new IllegalStateException("初始化数据目录失败: " + e.getMessage(), e);
        }
    }

    public static Path getRankingsPath() {
        return RANKINGS_PATH;
    }

    public static Path getUserConfigPath() {
        return USER_CONFIG_PATH;
    }

        // 作用：获取数据目录路径
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

        // 作用：初始化属性文件
    private static void initializePropertiesFile(Path path) throws IOException {
        if (Files.exists(path)) {
            return;
        }
        Files.createFile(path);
    }

            // 作用：迁移遗留文件到新位置
    private static void migrateLegacyFileIfNeeded(String legacyFileName, Path targetPath) throws IOException {
        Path legacyPath = Path.of(legacyFileName);
        if (Files.exists(legacyPath) && !Files.exists(targetPath)) {
            try (InputStream in = Files.newInputStream(legacyPath);
                 OutputStream out = Files.newOutputStream(targetPath)) {
                in.transferTo(out);
            }
        }
    }

         // 作用：原子性地写入属性文件，避免数据损坏
    public static void atomicStore(Properties props, Path targetPath, String comment) throws IOException {
        Path tempPath = targetPath.resolveSibling(targetPath.getFileName() + ".tmp");
        try (OutputStream out = Files.newOutputStream(tempPath)) {
            props.store(out, comment);
        }
        try {
            Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException atomicMoveError) {
            Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}




