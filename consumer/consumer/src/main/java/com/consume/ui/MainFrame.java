package com.consume.ui;

import com.consume.dao.CategoryDAO;
import com.consume.dao.RecordDAO;
import com.consume.entity.Category;
import com.consume.entity.Record;
import com.consume.util.DBBackupRestoreUtil;
import com.consume.util.JOptionPaneUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 主窗口：整合所有功能菜单 + 显示消费记录表格
 */
public class MainFrame extends JFrame {
    // 新增：依赖的DAO（用于加载消费记录和分类）
    private final RecordDAO recordDAO = new RecordDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    // 新增：表格相关组件
    private JTable recordTable; // 消费记录表格
    private DefaultTableModel tableModel; // 表格数据模型
    private Map<Integer, String> categoryMap; // 缓存分类ID→名称（避免重复查询数据库）

    public MainFrame() {
        initFrame();
        initMenuBar(); // 保留原有菜单
        initRecordTable(); // 新增：初始化消费记录表格
    }

    // 1. 初始化主窗口（保留原有逻辑，无需修改）
    private void initFrame() {
        setTitle("消费管理系统");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    // 2. 初始化菜单条（保留原有逻辑，无需修改）
    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // ① 消费管理菜单（分类管理、记一笔）
        JMenu consumeMenu = new JMenu("消费管理");
        JMenuItem itemCategory = new JMenuItem("分类管理");
        JMenuItem itemAddRecord = new JMenuItem("记一笔");
        itemCategory.addActionListener(e -> new CategoryManageFrame().setVisible(true));
// 修改：打开“记一笔”时传入MainFrame引用
        itemAddRecord.addActionListener(e -> new AddRecordFrame(MainFrame.this).setVisible(true));
        consumeMenu.add(itemCategory);
        consumeMenu.add(itemAddRecord);

        // ② 统计报表菜单（消费一览、月度报表）
        JMenu reportMenu = new JMenu("统计报表");
        JMenuItem itemOverview = new JMenuItem("消费一览");
        JMenuItem itemMonthlyReport = new JMenuItem("月度报表");
        itemOverview.addActionListener(e -> new ConsumeOverviewFrame().setVisible(true));
        itemMonthlyReport.addActionListener(e -> new MonthlyReportFrame().setVisible(true));
        reportMenu.add(itemOverview);
        reportMenu.add(itemMonthlyReport);

        // ③ 系统设置菜单（预算配置、备份、恢复）
        JMenu systemMenu = new JMenu("系统设置");
        JMenuItem itemConfig = new JMenuItem("预算与数据库配置");
        JMenuItem itemBackup = new JMenuItem("备份数据");
        JMenuItem itemRestore = new JMenuItem("恢复数据");
        itemConfig.addActionListener(e -> new ConfigFrame().setVisible(true));
        itemBackup.addActionListener(e -> DBBackupRestoreUtil.backup(MainFrame.this));
        itemRestore.addActionListener(e -> {
            DBBackupRestoreUtil.restore(MainFrame.this);
            // 新增：恢复数据后自动刷新表格
            refreshRecordTable();
        });
        systemMenu.add(itemConfig);
        systemMenu.add(itemBackup);
        systemMenu.add(itemRestore);

        menuBar.add(consumeMenu);
        menuBar.add(reportMenu);
        menuBar.add(systemMenu);
        setJMenuBar(menuBar);
    }

    // 3. 新增：初始化消费记录表格（核心方法）
    private void initRecordTable() {
        // ① 初始化分类缓存（ID→名称，避免每次查分类都访问数据库）
        loadCategoryCache();

        // ② 定义表格列名（对应消费记录的字段，可根据需求调整）
        String[] columnNames = {"记录ID", "消费金额（元）", "消费分类", "消费日期", "备注"};

        // ③ 创建表格数据模型（设置列名，初始无数据；设置单元格不可编辑）
        tableModel = new DefaultTableModel(null, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 表格仅展示，不允许直接编辑
            }
        };

        // ④ 创建表格并配置样式
        recordTable = new JTable(tableModel);
        recordTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        recordTable.setRowHeight(25); // 行高
        // 设置表格列宽（自适应或固定宽度）
        recordTable.getColumnModel().getColumn(0).setPreferredWidth(80); // 记录ID
        recordTable.getColumnModel().getColumn(1).setPreferredWidth(120); // 消费金额
        recordTable.getColumnModel().getColumn(2).setPreferredWidth(100); // 消费分类
        recordTable.getColumnModel().getColumn(3).setPreferredWidth(120); // 消费日期
        recordTable.getColumnModel().getColumn(4).setPreferredWidth(300); // 备注

        // ⑤ 为表格添加滚动条（数据过多时可滚动）
        JScrollPane scrollPane = new JScrollPane(recordTable);
        scrollPane.setPreferredSize(new Dimension(1150, 600));

        // ⑥ 底部添加“刷新”按钮（手动刷新表格数据）
        JButton btnRefresh = new JButton("刷新消费记录");
        btnRefresh.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        btnRefresh.addActionListener(e -> refreshRecordTable());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        btnPanel.add(btnRefresh);

        // ⑦ 组装主窗口（表格+滚动条在中心，刷新按钮在底部）
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        // ⑧ 初始加载消费记录
        refreshRecordTable();
    }

    // 4. 新增：加载分类缓存（ID→名称，优化性能）
    private void loadCategoryCache() {
        categoryMap = new HashMap<>();
        List<Category> categoryList = categoryDAO.getAllCategories();
        for (Category category : categoryList) {
            categoryMap.put(category.getId(), category.getName());
        }
    }

    // 5. 新增：刷新消费记录表格（核心：调用DAO获取数据并更新表格）
    public void refreshRecordTable() {
        try {
            // ① 清空表格原有数据
            tableModel.setRowCount(0);

            // ② 调用DAO获取所有消费记录（这里需先在RecordDAO中新增查询所有记录的方法！）
            List<Record> recordList = recordDAO.getAllRecords();

            // ③ 遍历记录，转换为表格行数据
            for (Record record : recordList) {
                // 转换分类ID为名称（从缓存中取，避免重复查询）
                String categoryName = categoryMap.getOrDefault(record.getCid(), "未知分类");
                // 转换日期格式（Date→String，如“2024-05-20”）
                String dateStr = new java.text.SimpleDateFormat("yyyy-MM-dd").format(record.getDate());
                // 转换消费金额（int→BigDecimal，确保显示小数）
                BigDecimal amount = new BigDecimal(record.getSpend());

                // 组装表格行数据（顺序与columnNames对应）
                Object[] rowData = {
                        record.getId(),
                        amount.setScale(2, BigDecimal.ROUND_HALF_UP), // 保留2位小数
                        categoryName,
                        dateStr,
                        record.getComment() == null ? "" : record.getComment() // 备注为空时显示空字符串
                };
                tableModel.addRow(rowData); // 添加行到表格
            }

        } catch (Exception e) {
            JOptionPaneUtil.showError("刷新消费记录失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    // 程序入口（保留原有逻辑）
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new MainFrame().setVisible(true);
        });
    }
}