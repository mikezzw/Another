package com.consume.ui;

import com.consume.dao.ConfigDAO;
import com.consume.dao.RecordDAO;
import com.consume.util.JOptionPaneUtil;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Calendar;

/**
 * 消费一览界面：展示核心统计数据 + 环形进度条
 */
public class ConsumeOverviewFrame extends JFrame {
    private final RecordDAO recordDAO = new RecordDAO();
    private final ConfigDAO configDAO = new ConfigDAO();

    // 统计标签（展示数据）
    private JLabel lblMonthTotal, lblTodayTotal, lblDailyAvg;
    private JLabel lblMonthRemain, lblDailyAvailable, lblDaysLeft;

    public ConsumeOverviewFrame() {
        initFrame();
        initComponents();
        refreshStatData(); // 初始化统计数据
    }

    // 初始化窗口
    private void initFrame() {
        setTitle("消费一览");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    // 初始化UI组件
    private void initComponents() {
        // 1. 顶部统计面板（网格布局：2行3列，展示6个统计项）
        JPanel statPanel = new JPanel(new GridLayout(2, 3, 30, 20));
        statPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        // 初始化统计标签（统一样式）
        lblMonthTotal = createStatLabel("本月消费总额：");
        lblTodayTotal = createStatLabel("今日消费：");
        lblDailyAvg = createStatLabel("日均消费：");
        lblMonthRemain = createStatLabel("本月剩余：");
        lblDailyAvailable = createStatLabel("日均可用：");
        lblDaysLeft = createStatLabel("距离月末：");
        // 添加标签到面板
        statPanel.add(lblMonthTotal);
        statPanel.add(lblTodayTotal);
        statPanel.add(lblDailyAvg);
        statPanel.add(lblMonthRemain);
        statPanel.add(lblDailyAvailable);
        statPanel.add(lblDaysLeft);
        add(statPanel, BorderLayout.NORTH);

        // 2. 中间进度条面板（展示环形进度条）
        JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
        CircleProgressBar progressBar = new CircleProgressBar();
        progressPanel.add(progressBar);
        add(progressPanel, BorderLayout.CENTER);

        // 3. 底部刷新按钮（手动刷新数据）
        JButton btnRefresh = new JButton("刷新数据");
        btnRefresh.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        btnRefresh.addActionListener(e -> refreshStatData());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
        btnPanel.add(btnRefresh);
        add(btnPanel, BorderLayout.SOUTH);
    }

    // 创建统计标签（统一样式，避免重复代码）
    private JLabel createStatLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        return label;
    }

    // 刷新统计数据（核心：调用DAO获取数据并更新标签）
    private void refreshStatData() {
        try {
            // 1. 获取基础数据
            BigDecimal monthTotal = recordDAO.getMonthTotalSpend(); // 本月消费总额
            BigDecimal todayTotal = recordDAO.getTodayTotalSpend(); // 今日消费
            BigDecimal monthBudget = configDAO.getMonthlyBudget(); // 本月预算
            int daysPassed = getDaysPassedInMonth(); // 本月已过天数
            int daysLeft = getDaysLeftInMonth(); // 距离月末天数

            // 2. 计算衍生数据
            BigDecimal dailyAvg = daysPassed == 0 ? BigDecimal.ZERO :
                    monthTotal.divide(new BigDecimal(daysPassed), 2, BigDecimal.ROUND_HALF_UP); // 日均消费
            BigDecimal monthRemain = monthBudget.subtract(monthTotal).max(BigDecimal.ZERO); // 本月剩余（不允许为负）
            BigDecimal dailyAvailable = daysLeft == 0 ? BigDecimal.ZERO :
                    monthRemain.divide(new BigDecimal(daysLeft), 2, BigDecimal.ROUND_HALF_UP); // 日均可用

            // 3. 更新标签文本（保留2位小数，格式统一）
            lblMonthTotal.setText(String.format("本月消费总额：%.2f 元", monthTotal));
            lblTodayTotal.setText(String.format("今日消费：%.2f 元", todayTotal));
            lblDailyAvg.setText(String.format("日均消费：%.2f 元", dailyAvg));
            lblMonthRemain.setText(String.format("本月剩余：%.2f 元", monthRemain));
            lblDailyAvailable.setText(String.format("日均可用：%.2f 元", dailyAvailable));
            lblDaysLeft.setText(String.format("距离月末：%d 天", daysLeft));
        } catch (Exception e) {
            JOptionPaneUtil.showError("数据刷新失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    // 辅助：获取本月已过天数（如5号返回5）
    private int getDaysPassedInMonth() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    // 辅助：获取距离月末天数（如30天的月份，25号返回5）
    private int getDaysLeftInMonth() {
        Calendar cal = Calendar.getInstance();
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH); // 当月最后一天（如30、31）
        int today = cal.get(Calendar.DAY_OF_MONTH);
        return lastDay - today;
    }
}
