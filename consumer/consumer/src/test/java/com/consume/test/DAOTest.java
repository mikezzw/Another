package com.consume.test;

import com.consume.dao.CategoryDAO;
import com.consume.dao.ConfigDAO;
import com.consume.dao.RecordDAO;
import com.consume.entity.Category;
import com.consume.entity.Record;
import com.consume.util.DBUtil;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * DAO层测试类：验证所有DAO方法是否正常工作
 */
public class DAOTest {
    // 初始化DAO对象
    private static CategoryDAO categoryDAO = new CategoryDAO();
    private static RecordDAO recordDAO = new RecordDAO();
    private static ConfigDAO configDAO = new ConfigDAO();

    public static void main(String[] args) {
        // 测试1：新增分类
        testAddCategory();
        // 测试2：查询所有分类
        testGetAllCategories();
        // 测试3：新增消费记录（需先有分类，用测试1新增的分类ID）
        testAddRecord();
        // 测试4：统计本月消费
        testGetMonthTotalSpend();
        // 测试5：设置并查询预算
        testSaveAndGetBudget();

        System.out.println("所有DAO测试完成！");
    }

    // 测试新增分类
    private static void testAddCategory() {
        Category category = new Category();
        category.setName("测试分类"); // 新增“测试分类”
        boolean success = categoryDAO.addCategory(category);
        System.out.println("新增分类结果：" + (success ? "成功" : "失败"));
    }

    // 测试查询所有分类
    private static void testGetAllCategories() {
        List<Category> categories = categoryDAO.getAllCategories();
        System.out.println("\n所有分类：");
        for (Category c : categories) {
            System.out.println("ID：" + c.getId() + "，名称：" + c.getName());
        }
    }

    // 测试新增消费记录（注意：cid需替换为testAddCategory新增的分类ID）
    private static void testAddRecord() {
        Record record = new Record();
        record.setSpend(100); // 消费100元
        record.setCid(1); // 替换为实际新增的分类ID（如testAddCategory返回的ID）
        record.setComment("测试消费"); // 备注
        record.setDate(new Date()); // 今日日期
        boolean success = recordDAO.addRecord(record);
        System.out.println("\n新增消费记录结果：" + (success ? "成功" : "失败"));
    }

    // 测试统计本月消费
    private static void testGetMonthTotalSpend() {
        BigDecimal monthTotal = recordDAO.getMonthTotalSpend();
        System.out.println("\n本月消费总额：" + monthTotal + "元");
    }

    // 测试设置并查询预算
    private static void testSaveAndGetBudget() {
        // 设置预算为5000元
        boolean saveSuccess = configDAO.saveOrUpdateConfig(ConfigDAO.KEY_MONTHLY_BUDGET, "5000");
        System.out.println("\n设置预算结果：" + (saveSuccess ? "成功" : "失败"));
        // 查询预算
        BigDecimal budget = configDAO.getMonthlyBudget();
        System.out.println("当前本月预算：" + budget + "元");
    }
}