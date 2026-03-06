package com.SnakeGame.game;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class RankPanel extends JPanel {

    private static int[] normalRank = new int[10];
    private static int[] hardRank = new int[10];
    private  static int[] crazyRank =  new int[10];
    private static int[] rank = new int[10];
    private static LocalDateTime[] normalTime = new LocalDateTime[10];
    private static LocalDateTime[] hardTime = new LocalDateTime[10];
    private static LocalDateTime[] crazyTime = new LocalDateTime[10];
    private static LocalDateTime[] rankTime = new LocalDateTime[10];

    private static String currentModel = "normal";
    private JButton menuButton = new JButton("返回菜单");
    private static final String RANK_FILE = "rankings.dat";

    static {
        loadRankings();
    }

    public RankPanel() {
        setLayout(null);
        setupButton();
    }


    private void setupButton() {
        menuButton.setBounds(300, 600, 200, 40);
        menuButton.setBackground(new Color(0x36BBDD));
        menuButton.setFont(new Font("微软雅黑", Font.BOLD, 20));
        menuButton.setForeground(Color.WHITE);
        menuButton.addActionListener(e -> {
            Main.turnPage("menu");
            MenuPanel.resetMenu();
        });
        
        add(menuButton);
    }

    public static void showRank(String model){

        switch (model) {
            case "normal" -> {
                currentModel = "normal";
                System.arraycopy(normalRank, 0, rank, 0, normalRank.length);
                System.arraycopy(normalTime, 0, rankTime, 0, normalTime.length);
            }
            case "hard" -> {
                currentModel = "hard";
                System.arraycopy(hardRank, 0, rank, 0, hardRank.length);
                System.arraycopy(hardTime, 0, rankTime, 0, hardTime.length);
            }
            case "crazy" -> {
                currentModel = "crazy";
                System.arraycopy(crazyRank, 0, rank, 0, crazyRank.length);
                System.arraycopy(crazyTime, 0, rankTime, 0, crazyTime.length);
            }
        }
    }
    @Override
    protected void paintComponent(Graphics g) {
        try{
            super.paintComponent(g);
        setBackground(new Color(0x7ABCCC));

    }catch(Exception e){
        System.err.println("绘制排行榜失败: " + e.getMessage());
    }
        DrawTitle(g);
        drawRank(g,rank);
    }
    private static void drawRank(Graphics g, int[] rank){
        g.setColor(new Color(0x4007A1));
        g.setFont(new Font("微软雅黑", Font.BOLD, 20));
        for(int i = 0; i < rank.length; i++){
            g.drawString(i+1+" ", 60, 150+i*40);
            g.drawString(fomateTime(rankTime[i]), 330, 150+i*40);
            g.drawString(rank[i]+"", 600, 150+i*40);
        }
    }

    private void DrawTitle(Graphics g){
        g.setColor(new Color(0x0D5302));
        g.setFont(new Font("微软雅黑", Font.BOLD, 40));
        g.drawString(MenuPanel.getState()+" Ranking List", 200, 40);
        g.setColor(new Color(0xFAE03C));
        g.setFont(new Font("微软雅黑", Font.BOLD, 20));
        g.drawString("Ranking", 50, 100);
        g.drawString("EndedTime", 350, 100);
        g.drawString("Score", 600, 100);
    }
    public static void updateRank(int score, String model){
        if(model.equals("normal")){
            insertScore(normalRank,score,normalTime);
        }else if(model.equals("hard")){
            insertScore(hardRank,score,hardTime);
        }else if(model.equals("crazy")){
            insertScore(crazyRank,score,crazyTime);
        }
        saveRankings();
    }
    private static void insertScore(int[] rank,int score,LocalDateTime[] timeArray){
        for(int i = 0; i < rank.length; i++){
            if(score > rank[i]){
                for(int j = rank.length-1; j > i; j--){
                    rank[j] = rank[j-1];
                }
                for(int j = timeArray.length-1; j > i; j--){
                    timeArray[j] = timeArray[j-1];
                }
                timeArray[i] = LocalDateTime.now();
                rank[i] = score;
                break;
            }
        }
    }

    private static void loadRankings() {
        try {
            Properties props = new Properties();
            FileInputStream fis = new FileInputStream(RANK_FILE);
            props.load(fis);
            fis.close();

            for (int i = 0; i < 10; i++) {
                String scorekey = "normal" + i + "_score";
                String timekey = "normal" + i + "_time";
                if (props.containsKey(scorekey)) {
                    normalRank[i] = Integer.parseInt(props.getProperty(scorekey, "0"));
                }
                if (props.containsKey(timekey)) {
                    String timeStr = props.getProperty(timekey, "");
                    if (!timeStr.isEmpty()) {
                        normalTime[i] = LocalDateTime.parse(timeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    }
                }
            }
            for (int i = 0; i < 10; i++) {
                String scorekey = "normal" + i + "_score";
                String timekey = "normal" + i + "_time";
                if (props.containsKey(scorekey)) {
                    hardRank[i] = Integer.parseInt(props.getProperty(scorekey, "0"));
                }
                if (props.containsKey(timekey)) {
                    String timeStr = props.getProperty(timekey, "");
                    if (!timeStr.isEmpty()) {
                        hardTime[i] = LocalDateTime.parse(timeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    }
                }
            }
            for (int i = 0; i < 10; i++) {
                String scorekey = "normal" + i + "_score";
                String timekey = "normal" + i + "_time";
                if (props.containsKey(scorekey)) {
                    crazyRank[i] = Integer.parseInt(props.getProperty(scorekey, "0"));
                }
                if (props.containsKey(timekey)) {
                    String timeStr = props.getProperty(timekey, "");
                    if (!timeStr.isEmpty()) {
                        crazyTime[i] = LocalDateTime.parse(timeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("排行榜文件不存在，使用默认数据");
        } catch (IOException e) {
            System.err.println("读取排行榜文件失败: " + e.getMessage());
        }
    }

    private static void saveRankings() {
        try {
            Properties props = new Properties();
            
            for (int i = 0; i < 10; i++) {
                props.setProperty("normal" + i + "_score", String.valueOf(normalRank[i]));
                props.setProperty("normal" + i + "_time",normalTime[i] != null?normalTime[i].toString():"");
            }
            for (int i = 0; i < 10; i++) {
                props.setProperty("hard" + i + "_score", String.valueOf(hardRank[i]));
                props.setProperty("hard" + i + "_time",hardTime[i] != null?hardTime[i].toString():"");
            }
            for (int i = 0; i < 10; i++) {
                props.setProperty("crazy" + i + "_score", String.valueOf(crazyRank[i]));
                props.setProperty("crazy" + i + "_time",crazyTime[i] != null?crazyTime[i].toString():"");
            }

            FileOutputStream fos = new FileOutputStream(RANK_FILE);
            props.store(fos, "Snake Game Rankings");
            fos.close();
        } catch (IOException e) {
            System.err.println("保存排行榜文件失败: " + e.getMessage());
        }
    }
    private static String fomateTime(LocalDateTime  time){
        if(time == null){
            return "未记录";
        }
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
