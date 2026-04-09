package com.SnakeGame.game;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * 统一按钮样式工具，保证各页面风格一致
 */
public final class UIFactory {
    private static final Font MAIN_FONT = new Font("幼圆", Font.BOLD, 20);
    private static final Font ICON_FONT = new Font("微软雅黑", Font.BOLD, 18);
    private static final Font FORM_FONT = new Font("微软雅黑", Font.PLAIN, 17);

    private static final Color DARK_TEXT = new Color(0x1A3A61);
    private static final Color LIGHT_BORDER = new Color(0xA5CFF4);
    private static final Color BUTTON_THEME_BASE = new Color(0x6EA8D7);
    private static final Color CARET_BLUE = new Color(0x2F8ACB);
    private static final Color CHECKBOX_TEXT = new Color(0x1E4D80);

    private UIFactory() {
        throw new AssertionError("工具类不应被实例化");
    }

    public static void styleMainButton(JButton button, Color bg, Color fg) {
        applyButtonStyle(button, bg, fg, MAIN_FONT, new Color(255, 255, 255, 160), 2,
                new EmptyBorder(8, 16, 8, 16));
    }

    public static void styleIconButton(JButton button, Color bg, Color fg) {
        applyButtonStyle(button, bg, fg, ICON_FONT, new Color(255, 255, 255, 190), 1,
                new EmptyBorder(2, 2, 2, 2));
    }

    private static void applyButtonStyle(JButton button, Color bg, Color fg, Font font,
                                         Color borderColor, int borderWidth, EmptyBorder padding) {
        button.setFont(font);
        button.setBackground(harmonizeButtonColor(bg));
        // 简化：不再动态计算对比度，直接用传入前景色，未传时默认白色。
        button.setForeground(fg != null ? fg : Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(borderColor, borderWidth, 14),
                padding));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private static Color harmonizeButtonColor(Color input) {
        if (input == null) {
            return BUTTON_THEME_BASE;
        }
        int r = (input.getRed() * 3 + BUTTON_THEME_BASE.getRed() * 5) / 8;
        int g = (input.getGreen() * 3 + BUTTON_THEME_BASE.getGreen() * 5) / 8;
        int b = (input.getBlue() * 3 + BUTTON_THEME_BASE.getBlue() * 5) / 8;
        return new Color(r, g, b);
    }

    private static final class RoundedLineBorder extends AbstractBorder {
        private final Color color;
        private final int thickness;
        private final int radius;

        private RoundedLineBorder(Color color, int thickness, int radius) {
            this.color = color;
            this.thickness = thickness;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(thickness));
            int inset = Math.max(1, thickness / 2);
            g2d.drawRoundRect(x + inset, y + inset, width - thickness, height - thickness, radius, radius);
            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness + 1, thickness + 1, thickness + 1, thickness + 1);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            Insets target = getBorderInsets(c);
            insets.top = target.top;
            insets.left = target.left;
            insets.bottom = target.bottom;
            insets.right = target.right;
            return insets;
        }
    }

    public static void styleTextField(JTextField textField) {
        // RGBA 第 4 个参数是透明度(0-255)，这里用轻微透明提升层次感。
        textField.setFont(FORM_FONT);
        textField.setBackground(new Color(255, 255, 255, 235));
        textField.setForeground(DARK_TEXT);
        textField.setCaretColor(CARET_BLUE);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LIGHT_BORDER, 2, true),
                // 复合边框=外层描边+内层留白，避免文字贴边。
                new EmptyBorder(4, 10, 4, 10)
        ));
    }

    public static void styleComboBox(JComboBox<?> comboBox) {
        // 与文本框统一配色，保持表单控件风格一致。
        comboBox.setFont(FORM_FONT);
        comboBox.setBackground(new Color(255, 255, 255, 235));
        comboBox.setForeground(DARK_TEXT);
        comboBox.setBorder(BorderFactory.createLineBorder(LIGHT_BORDER, 2, true));
    }

    public static void styleCheckBox(JCheckBox checkBox, Color background) {
        checkBox.setOpaque(true);
        checkBox.setBackground(background);
        checkBox.setForeground(CHECKBOX_TEXT);
        checkBox.setFont(new Font("幼圆", Font.BOLD, 18));
        checkBox.setFocusPainted(false);
    }

    public static void paintSoftGradient(Graphics2D g2d, int width, int height, Color topColor, Color bottomColor) {
        GradientPaint paint = new GradientPaint(0, 0, topColor, 0, Math.max(height, 1), bottomColor);
        Paint oldPaint = g2d.getPaint();
        g2d.setPaint(paint);
        g2d.fillRect(0, 0, width, height);
        g2d.setPaint(oldPaint);
    }

    public static void drawRoundedPanel(Graphics2D g2d, int x, int y, int width, int height,
                                        int arc, Color fillColor, Color borderColor) {
        // arc 同时用于水平/垂直圆角半径，值越大圆角越明显。
        RoundRectangle2D panel = new RoundRectangle2D.Float(x, y, width, height, arc, arc);
        g2d.setColor(fillColor);
        g2d.fill(panel);
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(2f));
        g2d.draw(panel);
    }

    public static void drawCenteredTitle(Graphics2D g2d, String text, int centerX, int baselineY,
                                         Font font, Color textColor, Color shadowColor) {
        // FontMetrics 用像素宽度做精确居中，避免不同字体下“视觉不居中”。
        Font oldFont = g2d.getFont();
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int x = centerX - fm.stringWidth(text) / 2;

        g2d.setColor(shadowColor != null ? shadowColor : new Color(255, 255, 255, 180));
        g2d.drawString(text, x + 1, baselineY + 1);
        g2d.setColor(textColor);
        g2d.drawString(text, x, baselineY);
        g2d.setFont(oldFont);
    }
}
