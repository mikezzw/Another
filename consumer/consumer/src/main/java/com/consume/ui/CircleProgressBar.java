package com.consume.ui;

import com.consume.dao.ConfigDAO;
import com.consume.dao.RecordDAO;
import com.consume.util.JOptionPaneUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.math.BigDecimal;

/**
 * 自定义环形进度条：展示消费进度（绿色→黄色→红色渐变）
 */
public class CircleProgressBar extends JComponent {
    private final RecordDAO recordDAO = new RecordDAO();
    private final ConfigDAO configDAO = new ConfigDAO();
    private double progress; // 进度（0.0~1.0，1.0表示超预算）

    public CircleProgressBar() {
        // 定时刷新进度（每秒1次，确保数据实时）
        Timer timer = new Timer(1000, e -> {
            updateProgress(); // 更新进度
            repaint(); // 重绘进度条
        });
        timer.start();
    }

    // 更新消费进度（本月消费 / 本月预算）
    private void updateProgress() {
        try {
            BigDecimal monthTotal = recordDAO.getMonthTotalSpend(); // 本月消费总额
            BigDecimal monthBudget = configDAO.getMonthlyBudget(); // 本月预算
            if (monthBudget.compareTo(BigDecimal.ZERO) == 0) {
                progress = 0.0; // 预算为0时进度为0
                return;
            }
            // 计算进度（超过1.0按1.0算，避免进度条超圈）
            progress = monthTotal.divide(monthBudget, 4, BigDecimal.ROUND_HALF_UP).doubleValue();
            progress = Math.min(progress, 1.0);
        } catch (Exception e) {
            JOptionPaneUtil.showError("进度计算异常：" + e.getMessage());
            progress = 0.0;
        }
    }

    // 重绘进度条（核心：绘制环形和文字）
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // 抗锯齿（让环形更平滑）

        // 1. 计算环形尺寸（取组件宽高中的较小值，确保圆形）
        int size = Math.min(getWidth(), getHeight());
        int x = (getWidth() - size) / 2; // 环形x坐标（居中）
        int y = (getHeight() - size) / 2; // 环形y坐标（居中）
        int ringWidth = 15; // 环形宽度
        int arcSize = size - 2 * ringWidth; // 环形内径（减去两侧宽度）

        // 2. 绘制背景环形（灰色，未使用的进度）
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setStroke(new BasicStroke(ringWidth)); // 画笔宽度=环形宽度
        // 绘制360°环形（Arc2D.OPEN表示开口环形，非填充）
        g2d.draw(new Arc2D.Double(x + ringWidth, y + ringWidth, arcSize, arcSize, -90, 360, Arc2D.OPEN));

        // 3. 绘制进度环形（渐变颜色：绿→黄→红）
        g2d.setColor(getProgressColor());
        int arcAngle = (int) (progress * 360); // 进度对应的角度（360°为满）
        g2d.draw(new Arc2D.Double(x + ringWidth, y + ringWidth, arcSize, arcSize, -90, arcAngle, Arc2D.OPEN));

        // 4. 绘制中心进度文字（如“60%”）
        String progressText = String.format("%.0f%%", progress * 100);
        Font font = new Font("微软雅黑", Font.BOLD, size / 6);
        g2d.setFont(font);
        g2d.setColor(Color.BLACK);
        // 文字居中（计算文字坐标）
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(progressText);
        int textHeight = fm.getAscent();
        g2d.drawString(progressText, x + size/2 - textWidth/2, y + size/2 + textHeight/2);
    }

    // 根据进度获取渐变颜色（0%→50%：绿；50%→80%：黄；80%→100%：红）
    private Color getProgressColor() {
        if (progress <= 0.5) {
            // 绿色渐变（0.0→0.5：#00FF00→#FFFF00）
            int green = 255;
            int red = (int) (progress * 2 * 255); // 0→255
            return new Color(red, green, 0);
        } else if (progress <= 0.8) {
            // 黄色渐变（0.5→0.8：#FFFF00→#FFA500）
            int red = 255;
            int green = (int) ((1 - (progress - 0.5) / 0.3) * 255); // 255→100
            return new Color(red, green, 0);
        } else {
            // 红色渐变（0.8→1.0：#FFA500→#FF0000）
            int red = 255;
            int green = (int) ((1 - (progress - 0.8) / 0.2) * 100); // 100→0
            return new Color(red, green, 0);
        }
    }

    // 设置进度条默认大小（避免显示过小）
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, 200);
    }
}