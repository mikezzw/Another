package com.consume.ui;

import com.consume.dao.CategoryDAO;
import com.consume.dao.RecordDAO;
import com.consume.entity.Category;
import com.consume.util.JOptionPaneUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * 消费分类管理界面：实现分类的CRUD
 */
public class CategoryManageFrame extends JFrame {
    // 依赖的DAO
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final RecordDAO recordDAO = new RecordDAO();

    // UI组件
    private JTable categoryTable; // 分类列表表格
    private DefaultTableModel tableModel; // 表格数据模型
    private JTextField txtCategoryName; // 新增/修改分类的名称输入框
    private Integer selectedCategoryId; // 选中的分类ID（用于修改/删除）

    public CategoryManageFrame() {
        initFrame(); // 初始化窗口基本属性
        initComponents(); // 初始化UI组件
        loadCategoryData(); // 加载分类数据到表格
    }

    // 1. 初始化窗口（标题、大小、布局等）
    private void initFrame() {
        setTitle("消费分类管理");
        setSize(800, 500);
        setLocationRelativeTo(null); // 居中显示
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // 关闭时释放窗口，不退出整个程序
        setLayout(new BorderLayout()); // 边界布局（分上、中、下区域）
    }

    // 2. 初始化UI组件（分区域组装）
    private void initComponents() {
        // ---------------------- 顶部操作区（新增/修改/删除按钮 + 输入框） ----------------------
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        // 分类名称输入框
        JLabel lblName = new JLabel("分类名称：");
        txtCategoryName = new JTextField(20); // 输入框宽度20字符
        topPanel.add(lblName);
        topPanel.add(txtCategoryName);

        // 操作按钮
        JButton btnAdd = new JButton("新增分类");
        JButton btnUpdate = new JButton("修改分类");
        JButton btnDelete = new JButton("删除分类");
        topPanel.add(btnAdd);
        topPanel.add(btnUpdate);
        topPanel.add(btnDelete);

        // 添加顶部区域到窗口
        add(topPanel, BorderLayout.NORTH);

        // ---------------------- 中间表格区（展示分类列表 + 消费次数） ----------------------
        // 表格列名（分类ID、分类名称、消费次数）
        String[] columnNames = {"分类ID", "分类名称", "消费次数"};
        tableModel = new DefaultTableModel(null, columnNames) {
            // 表格单元格不可编辑（仅展示）
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        categoryTable = new JTable(tableModel);
        // 表格添加鼠标点击事件（选中行时获取分类ID和名称）
        categoryTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = categoryTable.getSelectedRow();
                if (selectedRow != -1) { // 选中有效行
                    // 获取选中行的分类ID（第一列，转换为Integer）
                    selectedCategoryId = (Integer) tableModel.getValueAt(selectedRow, 0);
                    // 获取选中行的分类名称（第二列），填充到输入框（用于修改）
                    String categoryName = (String) tableModel.getValueAt(selectedRow, 1);
                    txtCategoryName.setText(categoryName);
                }
            }
        });
        // 为表格添加滚动条（数据过多时可滚动）
        JScrollPane scrollPane = new JScrollPane(categoryTable);
        add(scrollPane, BorderLayout.CENTER);

        // ---------------------- 按钮点击事件绑定 ----------------------
        // 新增分类
        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String categoryName = txtCategoryName.getText().trim();
                if (categoryName.isEmpty()) {
                    JOptionPaneUtil.showError("请输入分类名称！");
                    return;
                }
                // 调用DAO新增分类
                Category category = new Category();
                category.setName(categoryName);
                if (categoryDAO.addCategory(category)) {
                    JOptionPaneUtil.showInfo("新增分类成功！");
                    loadCategoryData(); // 重新加载表格数据
                    txtCategoryName.setText(""); // 清空输入框
                } else {
                    JOptionPaneUtil.showError("新增分类失败，请重试！");
                }
            }
        });

        // 修改分类
        btnUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedCategoryId == null) { // 未选中分类
                    JOptionPaneUtil.showError("请先选中要修改的分类！");
                    return;
                }
                String newName = txtCategoryName.getText().trim();
                if (newName.isEmpty()) {
                    JOptionPaneUtil.showError("请输入新的分类名称！");
                    return;
                }
                // 调用DAO修改分类
                Category category = new Category();
                category.setId(selectedCategoryId);
                category.setName(newName);
                if (categoryDAO.updateCategory(category)) {
                    JOptionPaneUtil.showInfo("修改分类成功！");
                    loadCategoryData(); // 重新加载表格
                    txtCategoryName.setText(""); // 清空输入框
                    selectedCategoryId = null; // 重置选中ID
                } else {
                    JOptionPaneUtil.showError("修改分类失败，请重试！");
                }
            }
        });

        // 删除分类
        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedCategoryId == null) {
                    JOptionPaneUtil.showError("请先选中要删除的分类！");
                    return;
                }
                // 先查询该分类的消费次数，提示用户（避免误删）
                int recordCount = recordDAO.getRecordCountByCategory(selectedCategoryId);
                String tip = String.format("该分类下有%d条消费记录，删除后记录也会被删除，是否确认？", recordCount);
                if (!JOptionPaneUtil.showConfirm(tip)) {
                    return; // 用户取消删除
                }
                // 调用DAO删除分类
                if (categoryDAO.deleteCategory(selectedCategoryId)) {
                    JOptionPaneUtil.showInfo("删除分类成功！");
                    loadCategoryData(); // 重新加载表格
                    txtCategoryName.setText("");
                    selectedCategoryId = null;
                } else {
                    JOptionPaneUtil.showError("删除分类失败，请重试！");
                }
            }
        });
    }

    // 3. 加载分类数据到表格（核心方法：调用DAO获取数据，更新表格模型）
    private void loadCategoryData() {
        // 清空表格原有数据
        tableModel.setRowCount(0);

        // 调用DAO获取所有分类
        List<Category> categoryList = categoryDAO.getAllCategories();
        for (Category category : categoryList) {
            // 获取该分类的消费次数（调用RecordDAO）
            int recordCount = recordDAO.getRecordCountByCategory(category.getId());
            // 组装表格行数据（对应列名：分类ID、分类名称、消费次数）
            Object[] rowData = {
                    category.getId(),
                    category.getName(),
                    recordCount
            };
            tableModel.addRow(rowData); // 添加行到表格
        }
    }
}
