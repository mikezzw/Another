package com.consume.ui;

import com.consume.dao.CategoryDAO;
import com.consume.dao.RecordDAO;
import com.consume.entity.Category;
import com.consume.entity.Record;
import com.consume.util.JOptionPaneUtil;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 记一笔界面：录入消费记录
 */
public class AddRecordFrame extends JFrame {

    private MainFrame mainFrame;
    // 依赖的DAO
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final RecordDAO recordDAO = new RecordDAO();

    // UI组件
    private JComboBox<Category> cboCategory; // 分类下拉框（显示Category对象）
    private JTextField txtAmount; // 消费金额输入框
    private JDateChooser dateChooser; // 日期选择器（默认今日）

    // 修改构造方法：接收主窗口引用
    public AddRecordFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initFrame();
        initComponents();
        loadCategoryToComboBox();

        // 新增：窗口关闭时刷新主窗口表格
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (mainFrame != null) {
                    mainFrame.refreshRecordTable(); // 调用主窗口的刷新方法
                }
                dispose();
            }
        });
    }

    // 1. 初始化窗口
    private void initFrame() {
        setTitle("记一笔");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    // 2. 初始化UI组件（网格布局：3行2列，整齐排列输入项）
    private void initComponents() {
        // 中间面板（网格布局：3行2列，间距10）
        JPanel centerPanel = new JPanel(new GridLayout(3, 2, 20, 30));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 80, 50, 80)); // 内边距

        // ① 消费分类选择
        JLabel lblCategory = new JLabel("消费分类：");
        lblCategory.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        cboCategory = new JComboBox<>();
        // 下拉框显示分类名称（需重写Category的toString方法，或自定义渲染器）
        cboCategory.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Category) {
                    setText(((Category) value).getName()); // 显示分类名称
                }
                return this;
            }
        });
        centerPanel.add(lblCategory);
        centerPanel.add(cboCategory);

        // ② 消费金额输入
        JLabel lblAmount = new JLabel("消费金额（元）：");
        lblAmount.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        txtAmount = new JTextField();
        txtAmount.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        centerPanel.add(lblAmount);
        centerPanel.add(txtAmount);

        // ③ 消费日期选择
        JLabel lblDate = new JLabel("消费日期：");
        lblDate.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        dateChooser = new JDateChooser();
        dateChooser.setDate(new Date()); // 默认选中今日
        dateChooser.setDateFormatString("yyyy-MM-dd"); // 日期格式
        dateChooser.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        centerPanel.add(lblDate);
        centerPanel.add(dateChooser);

        // 添加中间面板到窗口
        add(centerPanel, BorderLayout.CENTER);

        // 底部提交按钮
        JButton btnSubmit = new JButton("提交消费记录");
        btnSubmit.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        btnSubmit.addActionListener(new SubmitListener());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
        bottomPanel.add(btnSubmit);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // 3. 加载分类到下拉框（调用CategoryDAO）
    private void loadCategoryToComboBox() {
        cboCategory.removeAllItems(); // 清空原有选项
        List<Category> categoryList = categoryDAO.getAllCategories();
        for (Category category : categoryList) {
            cboCategory.addItem(category); // 添加分类到下拉框
        }
        // 默认选中第一个分类（若有）
        if (categoryList.size() > 0) {
            cboCategory.setSelectedIndex(0);
        } else {
            JOptionPaneUtil.showError("暂无消费分类，请先到【分类管理】新增分类！");
            dispose(); // 关闭当前窗口
        }
    }

    // 4. 提交按钮监听器（校验输入 + 调用DAO新增记录）
    private class SubmitListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // ① 校验消费金额
            String amountStr = txtAmount.getText().trim();
            if (amountStr.isEmpty()) {
                JOptionPaneUtil.showError("请输入消费金额！");
                return;
            }
            Integer amount;
            try {
                amount = Integer.parseInt(amountStr); // 转换为整数（若需小数，可改为BigDecimal）
                if (amount <= 0) {
                    throw new NumberFormatException(); // 金额必须为正数
                }
            } catch (NumberFormatException ex) {
                JOptionPaneUtil.showError("请输入有效的正整数金额！");
                return;
            }

            // ② 校验分类选择
            Category selectedCategory = (Category) cboCategory.getSelectedItem();
            if (selectedCategory == null) {
                JOptionPaneUtil.showError("请选择消费分类！");
                return;
            }

            // ③ 校验日期选择
            Date consumeDate = dateChooser.getDate();
            if (consumeDate == null) {
                JOptionPaneUtil.showError("请选择消费日期！");
                return;
            }

            // ④ 调用DAO新增消费记录
            Record record = new Record();
            record.setSpend(amount);
            record.setCid(selectedCategory.getId());
            record.setDate(consumeDate);
            record.setComment("");

            if (recordDAO.addRecord(record)) {
                JOptionPaneUtil.showInfo("记账成功！");
                // 新增：提交成功后刷新主窗口表格并关闭当前窗口
                if (mainFrame != null) {
                    mainFrame.refreshRecordTable();
                }
                dispose(); // 关闭“记一笔”窗口
            } else {
                JOptionPaneUtil.showError("记账失败，请重试！");
            }
        }
    }
}