package com.SnakeGame.game;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * 排行榜面板：按模式展示与持久化前十成绩
 */
public class RankPanel extends JPanel {
    private static int[] normalRank = new int[10];
    private static int[] hardRank = new int[10];
    private static int[] crazyRank = new int[10];
    private static int[] rank = new int[10];
    private static int[] normalDuration = new int[10];
    private static int[] hardDuration = new int[10];
    private static int[] crazyDuration = new int[10];
    private static int[] rankDuration = new int[10];
    private static LocalDateTime[] normalTime = new LocalDateTime[10];
    private static LocalDateTime[] hardTime = new LocalDateTime[10];
    private static LocalDateTime[] crazyTime = new LocalDateTime[10];
    private static LocalDateTime[] rankTime = new LocalDateTime[10];

    private static String currentModel = "normal";
    private final JButton backButton = new JButton("<");
    private final JButton settingButton = new JButton("⚙");
    private final JButton normalButton = new JButton("普通");
    private final JButton hardButton = new JButton("困难");
    private final JButton crazyButton = new JButton("狂暴");
    private final JButton menuButton = new JButton("返回菜单");

    private static final Path RANK_FILE = AppPaths.getRankingsPath();
    private static final Color CARD_FILL = new Color(255, 255, 255, 185);
    private static final Color CARD_BORDER = new Color(0x9CCEF5);

    static {
        // 类加载时先尝试读取本地榜单
        loadRankings();
    }

    // 初始化页面组件并默认展示 normal 榜
    public RankPanel() {
        setLayout(null);
        setupButtons();
        // 打开排行榜页时默认显示普通模式，和主菜单入口行为一致
        showRank("normal");
    }

    // 按钮布局与导航事件绑定
    private void setupButtons() {
        backButton.setBounds(12, 12, 34, 34);
        settingButton.setBounds(764, 12, 34, 34);
        // 底部按钮统一尺寸与间距，保证视觉更和谐
        normalButton.setBounds(140, 598, 125, 40);
        hardButton.setBounds(278, 598, 125, 40);
        crazyButton.setBounds(416, 598, 125, 40);
        menuButton.setBounds(554, 598, 125, 40);

        UIFactory.styleIconButton(backButton, new Color(0x4A88D0), Color.WHITE);
        UIFactory.styleIconButton(settingButton, new Color(0x4A88D0), Color.WHITE);
        UIFactory.styleMainButton(normalButton, new Color(0x58C6A9), Color.WHITE);
        UIFactory.styleMainButton(hardButton, new Color(0xEA8A72), Color.WHITE);
        UIFactory.styleMainButton(crazyButton, new Color(0xA682F0), Color.WHITE);
        UIFactory.styleMainButton(menuButton, new Color(0x59A5C6), Color.WHITE);

        backButton.addActionListener(e -> Main.goBack());
        settingButton.addActionListener(e -> Main.turnPage("setting"));
        normalButton.addActionListener(e -> {
            showRank("normal");
            repaint();
        });
        hardButton.addActionListener(e -> {
            showRank("hard");
            repaint();
        });
        crazyButton.addActionListener(e -> {
            showRank("crazy");
            repaint();
        });
        menuButton.addActionListener(e -> Main.turnPage("menu"));

        add(backButton);
        add(settingButton);
        add(normalButton);
        add(hardButton);
        add(crazyButton);
        add(menuButton);
    }

    // 切换当前展示模式并拷贝对应榜单到通用展示缓冲区
    public static void showRank(String model) {
        // 将“当前模式数据”拷贝到统一展示数组，绘制代码只关心 rank/rankTime/rankDuration
        // 这样绘制层无需关心模式分支，降低 UI 逻辑复杂度。
        switch (model) {
            case "normal" -> {
                currentModel = "normal";
                System.arraycopy(normalRank, 0, rank, 0, normalRank.length);
                System.arraycopy(normalTime, 0, rankTime, 0, normalTime.length);
                System.arraycopy(normalDuration, 0, rankDuration, 0, normalDuration.length);
            }
            case "hard" -> {
                currentModel = "hard";
                System.arraycopy(hardRank, 0, rank, 0, hardRank.length);
                System.arraycopy(hardTime, 0, rankTime, 0, hardTime.length);
                System.arraycopy(hardDuration, 0, rankDuration, 0, hardDuration.length);
            }
            case "crazy" -> {
                currentModel = "crazy";
                System.arraycopy(crazyRank, 0, rank, 0, crazyRank.length);
                System.arraycopy(crazyTime, 0, rankTime, 0, crazyTime.length);
                System.arraycopy(crazyDuration, 0, rankDuration, 0, crazyDuration.length);
            }
        }
    }

    @Override
    // 绘制排行榜底板，再叠加标题与行数据
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        UIFactory.paintSoftGradient(g2d, getWidth(), getHeight(), new Color(0xE9F8FF), new Color(0xD8F2EA));
        UIFactory.drawRoundedPanel(g2d, 34, 78, 740, 490, 24, CARD_FILL, CARD_BORDER);
        g2d.dispose();
        drawTitle(g);
        drawRank(g, rank);
    }

    // 绘制 10 行成绩数据
    private static void drawRank(Graphics g, int[] rank) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color rankTextColor = new Color(0x2A4A75);
        Color secondaryTextColor = new Color(0x355A85);
        g2d.setFont(new Font("幼圆", Font.BOLD, 18));

        for (int i = 0; i < rank.length; i++) {
            int rowY = 122 + i * 40;
            Color rowColor = (i % 2 == 0) ? new Color(255, 255, 255, 165) : new Color(235, 247, 255, 165);
            UIFactory.drawRoundedPanel(g2d, 54, rowY, 700, 32, 14, rowColor, new Color(0xC5E2F8));
            int textY = 145 + i * 40;

            // drawRoundedPanel 会改写画笔颜色，这里显式恢复深色文字，确保可读性
            g2d.setColor(rankTextColor);
            g2d.drawString(String.valueOf(i + 1), 70, textY);
            g2d.setColor(secondaryTextColor);
            g2d.drawString(formatTime(rankTime[i]), 190, textY);
            g2d.drawString(rankDuration[i] + "s", 530, textY);
            g2d.setColor(rankTextColor);
            g2d.drawString(String.valueOf(rank[i]), 680, textY);
        }
        g2d.dispose();
    }

    // 绘制页面主标题与列标题
    private void drawTitle(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        UIFactory.drawRoundedPanel(g2d, 170, 18, 470, 52, 24, new Color(255, 255, 255, 175), new Color(0x9CCEF5));
        UIFactory.drawCenteredTitle(
                g2d,
                currentModel + " 模式排行榜",
                405,
                54,
                new Font("幼圆", Font.BOLD, 36),
                new Color(0x2A4A75),
                new Color(255, 255, 255, 180)
        );

        g2d.setColor(new Color(0x2F6296));
        g2d.setFont(new Font("幼圆", Font.BOLD, 20));
        g2d.drawString("名次", 70, 104);
        g2d.drawString("结束时间", 210, 104);
        g2d.drawString("用时", 525, 104);
        g2d.drawString("分数", 675, 104);
        g2d.dispose();
    }

    // 更新成绩：插入对应模式榜单并保存
    public static void updateRank(int score, String model, int durationSec) {
        // 按模式写入对应榜单，并落盘保存
        if (model.equals("normal")) {
            insertScore(normalRank, score, normalTime, normalDuration, durationSec);
        } else if (model.equals("hard")) {
            insertScore(hardRank, score, hardTime, hardDuration, durationSec);
        } else if (model.equals("crazy")) {
            insertScore(crazyRank, score, crazyTime, crazyDuration, durationSec);
        }
        saveRankings();
    }

    // 插入算法：分数降序，分数相同按用时升序
    private static void insertScore(int[] rank, int score, LocalDateTime[] timeArray, int[] durationArray, int durationSec) {
        // 先按分数降序，再按用时升序进行插入
        for (int i = 0; i < rank.length; i++) {
            if (score > rank[i] || (score == rank[i] && (durationArray[i] == 0 || durationSec < durationArray[i]))) {
                // 经典“插入一位”写法：从尾到 i+1 依次后移，给第 i 位腾出空槽。
                for (int j = rank.length - 1; j > i; j--) {
                    rank[j] = rank[j - 1];
                }
                for (int j = timeArray.length - 1; j > i; j--) {
                    timeArray[j] = timeArray[j - 1];
                }
                for (int j = durationArray.length - 1; j > i; j--) {
                    durationArray[j] = durationArray[j - 1];
                }
                timeArray[i] = LocalDateTime.now();
                rank[i] = score;
                durationArray[i] = Math.max(0, durationSec);
                // 只插入一次，完成后立即结束，避免同一成绩被重复写入。
                break;
            }
        }
    }

    // 从 rankings 文件读取三个模式的榜单数据
    private static void loadRankings() {
        if (!Files.exists(RANK_FILE)) {
            System.out.println("排行榜文件不存在，使用默认数据");
            return;
        }

        try {
            Properties props = new Properties();
            try (InputStream fis = Files.newInputStream(RANK_FILE)) {
                props.load(fis);
            }

            for (int i = 0; i < 10; i++) {
                // 键名采用 mode + index + 字段名 的扁平结构，便于 Properties 直接存取。
                String scorekey = "normal" + i + "_score";
                String timekey = "normal" + i + "_time";
                String durationKey = "normal" + i + "_duration";
                if (props.containsKey(scorekey)) {
                    normalRank[i] = Integer.parseInt(props.getProperty(scorekey, "0"));
                }
                if (props.containsKey(timekey)) {
                    String timeStr = props.getProperty(timekey, "");
                    if (!timeStr.isEmpty()) {
                        normalTime[i] = LocalDateTime.parse(timeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    }
                }
                normalDuration[i] = Integer.parseInt(props.getProperty(durationKey, "0"));
            }
            for (int i = 0; i < 10; i++) {
                String scorekey = "hard" + i + "_score";
                String timekey = "hard" + i + "_time";
                String durationKey = "hard" + i + "_duration";
                if (props.containsKey(scorekey)) {
                    hardRank[i] = Integer.parseInt(props.getProperty(scorekey, "0"));
                }
                if (props.containsKey(timekey)) {
                    String timeStr = props.getProperty(timekey, "");
                    if (!timeStr.isEmpty()) {
                        hardTime[i] = LocalDateTime.parse(timeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    }
                }
                hardDuration[i] = Integer.parseInt(props.getProperty(durationKey, "0"));
            }
            for (int i = 0; i < 10; i++) {
                String scorekey = "crazy" + i + "_score";
                String timekey = "crazy" + i + "_time";
                String durationKey = "crazy" + i + "_duration";
                if (props.containsKey(scorekey)) {
                    crazyRank[i] = Integer.parseInt(props.getProperty(scorekey, "0"));
                }
                if (props.containsKey(timekey)) {
                    String timeStr = props.getProperty(timekey, "");
                    if (!timeStr.isEmpty()) {
                        crazyTime[i] = LocalDateTime.parse(timeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    }
                }
                crazyDuration[i] = Integer.parseInt(props.getProperty(durationKey, "0"));
            }
        } catch (IOException e) {
            System.err.println("读取排行榜文件失败: " + e.getMessage());
        }
    }

    // 将三个模式榜单一次性写回磁盘
    private static void saveRankings() {
        try {
            Properties props = new Properties();

            for (int i = 0; i < 10; i++) {
                // 固定写入前十，保证文件结构稳定，读取时无需探测长度。
                props.setProperty("normal" + i + "_score", String.valueOf(normalRank[i]));
                props.setProperty("normal" + i + "_time", normalTime[i] != null ? normalTime[i].toString() : "");
                props.setProperty("normal" + i + "_duration", String.valueOf(normalDuration[i]));
            }
            for (int i = 0; i < 10; i++) {
                props.setProperty("hard" + i + "_score", String.valueOf(hardRank[i]));
                props.setProperty("hard" + i + "_time", hardTime[i] != null ? hardTime[i].toString() : "");
                props.setProperty("hard" + i + "_duration", String.valueOf(hardDuration[i]));
            }
            for (int i = 0; i < 10; i++) {
                props.setProperty("crazy" + i + "_score", String.valueOf(crazyRank[i]));
                props.setProperty("crazy" + i + "_time", crazyTime[i] != null ? crazyTime[i].toString() : "");
                props.setProperty("crazy" + i + "_duration", String.valueOf(crazyDuration[i]));
            }

            AppPaths.atomicStore(props, RANK_FILE, "Snake Game Rankings");
        } catch (IOException e) {
            System.err.println("保存排行榜文件失败: " + e.getMessage());
        }
    }

    // 将结束时间格式化为可读字符串
    private static String formatTime(LocalDateTime time) {
        if (time == null) {
            return "未记录";
        }
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
