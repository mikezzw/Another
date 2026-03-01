package com.consume.ui;

import com.consume.dao.ConfigDAO;
import com.consume.util.JOptionPaneUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * 系统设置界面：配置本月预算和MySQL安装路径
 */
public class ConfigFrame extends JFrame {
    private final ConfigDAO configDAO = new ConfigDAO();

    // UI组件
    private JTextField txtBudget; // 预算输入框
    private JTextField txtMysqlPath; // MySQL路径输入框
    private JButton btnSelectMysqlPath; // 路径选择按钮

    public ConfigFrame() {
        initFrame();
        initComponents();
        loadConfigData(); // 加载现有配置到输入框
    }

    // 初始化窗口
    private void initFrame() {
        setTitle("预算与数据库配置");
        setSize(800, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    // 初始化UI组件
    private void initComponents() {
        // 中间面板（网格布局：2行3列，间距20）
        JPanel centerPanel = new JPanel(new GridLayout(2, 3, 20, 40));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(60, 80, 60, 80));

        // ① 本月预算配置
        JLabel lblBudget = new JLabel("本月预算（元）：");
        lblBudget.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        txtBudget = new JTextField();
        txtBudget.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        // 占位提示（默认3000元）
        JLabel lblBudgetTip = new JLabel("默认3000元，输入正整数");
        lblBudgetTip.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        lblBudgetTip.setForeground(Color.GRAY);
        centerPanel.add(lblBudget);
        centerPanel.add(txtBudget);
        centerPanel.add(lblBudgetTip);

        // ② MySQL安装路径配置
        JLabel lblMysqlPath = new JLabel("MySQL安装路径：");
        lblMysqlPath.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        txtMysqlPath = new JTextField();
        txtMysqlPath.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        btnSelectMysqlPath = new JButton("选择路径");
        btnSelectMysqlPath.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        // 路径选择按钮事件（打开文件夹选择器）
        btnSelectMysqlPath.addActionListener(e -> selectMysqlPath());
        centerPanel.add(lblMysqlPath);
        centerPanel.add(txtMysqlPath);
        centerPanel.add(btnSelectMysqlPath);

        add(centerPanel, BorderLayout.CENTER);

        // 底部保存按钮
        JButton btnSave = new JButton("保存配置");
        btnSave.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        btnSave.addActionListener(new SaveConfigListener());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
        bottomPanel.add(btnSave);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // 加载现有配置到输入框（如之前设置过预算，自动填充）
    private void loadConfigData() {
        // 加载预算（从config表读取，无则显示默认3000）
        String budgetStr = configDAO.getConfigValueByKey(ConfigDAO.KEY_MONTHLY_BUDGET);
        txtBudget.setText(budgetStr == null ? "3000" : budgetStr);

        // 加载MySQL路径（从config表读取，无则空）
        String mysqlPath = configDAO.getConfigValueByKey(ConfigDAO.KEY_MYSQL_PATH);
        if (mysqlPath != null) {
            txtMysqlPath.setText(mysqlPath);
        }
    }

    // 选择MySQL安装路径（打开文件夹选择器）
    private void selectMysqlPath() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择MySQL安装目录");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // 只允许选择文件夹
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();
            txtMysqlPath.setText(path); // 填充路径到输入框
        }
    }

    // 保存配置监听器（校验输入 + 调用DAO保存）
    private class SaveConfigListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // 1. 校验预算（必须是正整数）
            String budgetStr = txtBudget.getText().trim();
            if (budgetStr.isEmpty()) {
                JOptionPaneUtil.showError("请输入本月预算！");
                return;
            }
            if (!budgetStr.matches("\\d+")) { // 正则：仅允许数字
                JOptionPaneUtil.showError("预算必须是正整数！");
                return;
            }

            // 2. 校验MySQL路径（非空，且存在bin/mysql.exe）
            String mysqlPath = txtMysqlPath.getText().trim();
            if (mysqlPath.isEmpty()) {
                JOptionPaneUtil.showError("请选择MySQL安装路径！");
                return;
            }
            // 检查路径下是否有bin/mysql.exe（确保路径正确）
            File mysqlExe = new File(mysqlPath + "/bin/mysql.exe");
            if (!mysqlExe.exists()) {
                JOptionPaneUtil.showError("MySQL路径错误！未找到bin/mysql.exe");
                return;
            }

            // 3. 调用DAO保存配置（新增或更新）
            boolean saveBudget = configDAO.saveOrUpdateConfig(ConfigDAO.KEY_MONTHLY_BUDGET, budgetStr);
            boolean saveMysqlPath = configDAO.saveOrUpdateConfig(ConfigDAO.KEY_MYSQL_PATH, mysqlPath);

            if (saveBudget && saveMysqlPath) {
                JOptionPaneUtil.showInfo("配置保存成功！");
                dispose(); // 保存成功后关闭窗口
            } else {
                JOptionPaneUtil.showError("配置保存失败，请重试！");
            }
        }
    }
}
