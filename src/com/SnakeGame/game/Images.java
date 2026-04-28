package com.SnakeGame.game;
import javax.swing.*;

import java.net.URL;

/**
 * 图像资源管理类：统一加载游戏贴图
 */
public final class Images {
    
    // 私有构造函数防止实例化
    private Images() {
        throw new AssertionError("工具类不应被实例化");
    }
    
    // 图像资源URL
    public static final URL foodURL = getResource("/images/food.png");
    public static final URL titleURL = getResource("/images/title.jpg");
    public static final URL backgroundURL = getResource("/images/background.jpg");
    
    // 图像图标
    public static ImageIcon body;
    public static final ImageIcon food = createImageIcon(foodURL, "food");
    public static ImageIcon left;
    public static ImageIcon right;
    public static ImageIcon up;
    public static ImageIcon down;
    public static final ImageIcon title = createImageIcon(titleURL, "title");
    public static final ImageIcon background = createImageIcon(backgroundURL, "background");

    static {
        loadSnakeSkin("classic");
    }
    

    // 按 classpath 路径获取资源 URL
    private static URL getResource(String path) {
        URL url = Images.class.getResource(path);
        if (url == null) {
            System.err.println("警告: 找不到资源文件: " + path);
        }
        return url;
    }

    // 创建图标并在缺失时返回空图标兜底
    private static ImageIcon createImageIcon(URL url, String name) {
        if (url == null) {
            System.err.println("错误: 无法加载图像: " + name);
            // 返回一个默认的空白图标作为降级方案
            return new ImageIcon();
        }
        return new ImageIcon(url);
    }

    // 皮肤入口：当前版本固定加载 classic 素材
    public static void loadSnakeSkin(String skinName) {
        if (!loadOriginalSnakeSprites()) {
            System.err.println("警告: 原始蛇图片加载失败");
        }
        GameConfig.setCurrentSnakeSkin("classic", false);
    }

    // 读取原始蛇头/蛇身方向贴图
    private static boolean loadOriginalSnakeSprites() {
        URL bodyUrl = getResource("/images/body.png");
        URL leftUrl = getResource("/images/left.png");
        URL rightUrl = getResource("/images/right.png");
        URL upUrl = getResource("/images/up.png");
        URL downUrl = getResource("/images/down.png");
        if (bodyUrl == null || leftUrl == null || rightUrl == null || upUrl == null || downUrl == null) {
            return false;
        }
        body = createImageIcon(bodyUrl, "body");
        left = createImageIcon(leftUrl, "left");
        right = createImageIcon(rightUrl, "right");
        up = createImageIcon(upUrl, "up");
        down = createImageIcon(downUrl, "down");
        return true;
    }

}
