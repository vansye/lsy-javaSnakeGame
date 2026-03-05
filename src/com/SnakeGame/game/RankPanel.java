package com.SnakeGame.game;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Properties;

public class RankPanel extends JPanel {

    private static int[] normalRank = new int[10];
    private static int[] hardRank = new int[10];
    private  static int[] crazyRank =  new int[10];
    private static int[] rank = new int[10];
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
            }
            case "hard" -> {
                currentModel = "hard";
                System.arraycopy(hardRank, 0, rank, 0, hardRank.length);
            }
            case "crazy" -> {
                currentModel = "crazy";
                System.arraycopy(crazyRank, 0, rank, 0, crazyRank.length);
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
            g.drawString(i+1+" ", 50, 150+i*40);
            g.drawString(currentModel, 350, 150+i*40);
            g.drawString(rank[i]+"", 600, 150+i*40);
        }
    }

    private void DrawTitle(Graphics g){
        g.setColor(new Color(0x0D5302));
        g.setFont(new Font("微软雅黑", Font.BOLD, 40));
        g.drawString("Ranking List", 250, 40);
        g.setColor(new Color(0xFAE03C));
        g.setFont(new Font("微软雅黑", Font.BOLD, 20));
        g.drawString("Ranking", 50, 100);
        g.drawString("model", 350, 100);
        g.drawString("Score", 600, 100);
    }
    public static void updateRank(int score, String model){
        if(model.equals("normal")){
            insertScore(normalRank,score);
        }else if(model.equals("hard")){
            insertScore(hardRank,score);
        }else if(model.equals("crazy")){
            insertScore(crazyRank,score);
        }
        saveRankings();
    }
    private static void insertScore(int[] rank,int score){
        for(int i = 0; i < rank.length; i++){
            if(score > rank[i]){
                for(int j = rank.length-1; j > i; j--){
                    rank[j] = rank[j-1];
                }
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
                String key = "normal" + i;
                if (props.containsKey(key)) {
                    normalRank[i] = Integer.parseInt(props.getProperty(key, "0"));
                }
            }
            for (int i = 0; i < 10; i++) {
                String key = "hard" + i;
                if (props.containsKey(key)) {
                    hardRank[i] = Integer.parseInt(props.getProperty(key, "0"));
                }
            }
            for (int i = 0; i < 10; i++) {
                String key = "crazy" + i;
                if (props.containsKey(key)) {
                    crazyRank[i] = Integer.parseInt(props.getProperty(key, "0"));
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
                props.setProperty("normal" + i, String.valueOf(normalRank[i]));
            }
            for (int i = 0; i < 10; i++) {
                props.setProperty("hard" + i, String.valueOf(hardRank[i]));
            }
            for (int i = 0; i < 10; i++) {
                props.setProperty("crazy" + i, String.valueOf(crazyRank[i]));
            }

            FileOutputStream fos = new FileOutputStream(RANK_FILE);
            props.store(fos, "Snake Game Rankings");
            fos.close();
        } catch (IOException e) {
            System.err.println("保存排行榜文件失败: " + e.getMessage());
        }
    }
}
