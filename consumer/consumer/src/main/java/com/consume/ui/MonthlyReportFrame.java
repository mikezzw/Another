package com.consume.ui;

import com.consume.dao.RecordDAO;
import com.consume.util.JOptionPaneUtil;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Map;

/**
 * 月度消费报表界面：用柱状图展示本月每日消费趋势
 */
public class MonthlyReportFrame extends JFrame {
    private final RecordDAO recordDAO = new RecordDAO();
    private ChartPanel chartPanel; // 图表面板（承载柱状图）

    public MonthlyReportFrame() {
        initFrame();
        initChart(); // 初始化柱状图
        add(chartPanel, BorderLayout.CENTER); // 添加图表到窗口
    }

    // 初始化窗口
    private void initFrame() {
        setTitle("月度消费报表");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    // 初始化柱状图（核心：用JFreeChart生成图表）
    private void initChart() {
        try {
            // 1. 获取本月每日消费数据（Map<日期（如“1”）, 消费额>）
            Map<String, BigDecimal> dailySpendMap = recordDAO.getDailySpendInMonth();

            // 2. 构建时间序列数据（JFreeChart需要的格式）
            TimeSeries series = new TimeSeries("每日消费（元）"); // 序列名称（图表图例）
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH); // 注意：JFreeChart的Month是0-based（1月=0）

            // 遍历每日数据，添加到序列
            for (Map.Entry<String, BigDecimal> entry : dailySpendMap.entrySet()) {
                int day = Integer.parseInt(entry.getKey()); // 日期（如“5”→5号）
                double spend = entry.getValue().doubleValue(); // 消费额（转换为double，JFreeChart兼容）
                // 添加数据点（年、月、日，消费额）
                series.add(new Day(day, month + 1, year), spend); // 这里month+1是因为JFreeChart的Day是1-based
            }

            // 3. 构建数据集（承载时间序列）
            TimeSeriesCollection dataset = new TimeSeriesCollection(series);

            // 4. 创建柱状图（时间序列柱状图）
            JFreeChart chart = ChartFactory.createTimeSeriesChart(
                    String.format("%d年%d月消费趋势", year, month + 1), // 图表标题（含年月）
                    "日期", // X轴标签（日期）
                    "消费金额（元）", // Y轴标签（金额）
                    dataset, // 数据集
                    true, // 是否显示图例
                    true, // 是否显示工具提示（鼠标悬停显示具体值）
                    false // 是否显示URL链接（一般用不到）
            );

            // 5. 美化图表（调整X轴日期格式、字体等）
            XYPlot plot = (XYPlot) chart.getPlot();
            DateAxis dateAxis = (DateAxis) plot.getDomainAxis(); // X轴（日期轴）
            // 设置X轴日期格式（只显示“日”，如“5”）
            dateAxis.setDateFormatOverride(new java.text.SimpleDateFormat("d"));
            // 设置轴标签字体（避免中文乱码）
            dateAxis.setLabelFont(new Font("微软雅黑", Font.PLAIN, 12));
            dateAxis.setTickLabelFont(new Font("微软雅黑", Font.PLAIN, 10));
            plot.getRangeAxis().setLabelFont(new Font("微软雅黑", Font.PLAIN, 12));
            plot.getRangeAxis().setTickLabelFont(new Font("微软雅黑", Font.PLAIN, 10));
            // 设置图表标题字体
            chart.getTitle().setFont(new Font("微软雅黑", Font.BOLD, 16));

            // 6. 构建图表面板（用于嵌入Swing窗口）
            chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(950, 500)); // 设置面板大小

        } catch (Exception e) {
            JOptionPaneUtil.showError("报表生成失败：" + e.getMessage());
            e.printStackTrace();
            // 失败时显示空面板，避免窗口崩溃
            chartPanel = new ChartPanel(null);
        }
    }
}