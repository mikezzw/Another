package com.consume.dao;

import com.consume.entity.Config;
import com.consume.util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 系统配置DAO：处理config表的读写（预算、MySQL路径等）
 */
public class ConfigDAO {
    // 配置项的key（与config表的key_字段对应，避免硬编码）
    public static final String KEY_MONTHLY_BUDGET = "monthly_budget"; // 本月预算
    public static final String KEY_MYSQL_PATH = "mysql_path"; // MySQL安装路径

    /**
     * 1. 根据key查询配置值（如查“monthly_budget”获取预算）
     * @param key 配置项key（如KEY_MONTHLY_BUDGET）
     * @return 配置值（null=未找到）
     */
    public String getConfigValueByKey(String key) {
        String sql = "SELECT VALUE FROM config WHERE key_ = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, key); // 注意：表字段是key_，这里传key参数
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("VALUE"); // 返回配置值
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 2. 新增或更新配置（若key存在则更新，不存在则新增）
     * @param key 配置项key
     * @param value 配置值（如预算“5000”、MySQL路径“C:/MySQL”）
     * @return true=操作成功，false=失败
     */
    public boolean saveOrUpdateConfig(String key, String value) {
        Connection conn = null;
        PreparedStatement pstmt1 = null; // 查是否存在该key
        PreparedStatement pstmt2 = null; // 新增或更新

        try {
            conn = DBUtil.getConnection();
            // 步骤1：查询该key是否已存在
            String checkSql = "SELECT id FROM config WHERE key_ = ?";
            pstmt1 = conn.prepareStatement(checkSql);
            pstmt1.setString(1, key);
            ResultSet rs = pstmt1.executeQuery();

            if (rs.next()) {
                // 步骤2：存在则更新
                String updateSql = "UPDATE config SET VALUE = ? WHERE key_ = ?";
                pstmt2 = conn.prepareStatement(updateSql);
                pstmt2.setString(1, value);
                pstmt2.setString(2, key);
                pstmt2.executeUpdate();
            } else {
                // 步骤3：不存在则新增（id自增）
                String insertSql = "INSERT INTO config (key_, VALUE) VALUES (?, ?)";
                pstmt2 = conn.prepareStatement(insertSql);
                pstmt2.setString(1, key);
                pstmt2.setString(2, value);
                pstmt2.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.close(conn, pstmt1);
            DBUtil.close(null, pstmt2);
        }
    }

    /**
     * 3. 获取本月预算（封装方法，避免上层处理字符串转BigDecimal）
     * @return 本月预算（默认3000元，若配置表无则返回3000）
     */
    public BigDecimal getMonthlyBudget() {
        String budgetStr = getConfigValueByKey(KEY_MONTHLY_BUDGET);
        if (budgetStr == null || budgetStr.trim().isEmpty()) {
            return new BigDecimal("3000"); // 默认预算3000元
        }
        try {
            return new BigDecimal(budgetStr); // 字符串转BigDecimal（金额需精确）
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return new BigDecimal("3000"); // 格式错误时返回默认值
        }
    }
}