package com.consume.dao;

import com.consume.entity.Record;
import com.consume.util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 消费记录DAO：处理record表的新增、统计、查询操作
 */
public class RecordDAO {

    /**
     * 1. 新增消费记录（“记一笔”功能核心方法）
     * @param record 消费记录实体（含spend、cid、comment、date）
     * @return true=新增成功，false=失败
     */
    public boolean addRecord(Record record) {
        // SQL：插入消费记录（id自增）
        String sql = "INSERT INTO record (spend, cid, COMMENT, DATE) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, record.getSpend()); // 消费金额
            pstmt.setInt(2, record.getCid()); // 关联分类ID
            pstmt.setString(3, record.getComment()); // 备注（允许null，传null即可）
            pstmt.setDate(4, new java.sql.Date(record.getDate().getTime())); // 消费日期（转换为SQL的Date）
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.close(conn, pstmt);
        }
    }

    /**
     * 2. 统计本月消费总额（“消费一览”：本月消费总数）
     * @return 本月消费总额（单位：元）
     */
    public BigDecimal getMonthTotalSpend() {
        // SQL：按年月分组，求和spend（若本月无记录，返回0）
        String sql = "SELECT IFNULL(SUM(spend), 0) AS total " +
                "FROM record " +
                "WHERE DATE_FORMAT(DATE, '%Y-%m') = DATE_FORMAT(NOW(), '%Y-%m')";
        return getSumFromSql(sql); // 调用工具方法执行求和
    }

    /**
     * 3. 统计今日消费总额（“消费一览”：今日消费）
     * @return 今日消费总额
     */
    public BigDecimal getTodayTotalSpend() {
        String sql = "SELECT IFNULL(SUM(spend), 0) AS total " +
                "FROM record " +
                "WHERE DATE(DATE) = CURDATE()"; // CURDATE()是MySQL的“今日日期”函数
        return getSumFromSql(sql);
    }

    /**
     * 4. 获取本月每日消费数据（“月度报表”：柱状图数据）
     * @return Map<日期（如“1”“2”）, 当日消费总额>（按日期升序）
     */
    public Map<String, BigDecimal> getDailySpendInMonth() {
        Map<String, BigDecimal> dailySpendMap = new TreeMap<>(); // TreeMap自动按key升序
        Calendar cal = Calendar.getInstance();
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH); // 本月总天数

        // 步骤1：初始化本月所有日期的消费为0（避免漏填无消费的日期）
        for (int i = 1; i <= daysInMonth; i++) {
            dailySpendMap.put(String.valueOf(i), BigDecimal.ZERO);
        }

        // 步骤2：查询本月每日实际消费总额
        String sql = "SELECT DAY(DATE) AS day, IFNULL(SUM(spend), 0) AS total " +
                "FROM record " +
                "WHERE DATE_FORMAT(DATE, '%Y-%m') = DATE_FORMAT(NOW(), '%Y-%m') " +
                "GROUP BY DAY(DATE)"; // 按日期分组求和

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            // 遍历结果，更新map中对应日期的消费额
            while (rs.next()) {
                String day = rs.getString("day"); // 日期（如“5”表示5号）
                BigDecimal total = rs.getBigDecimal("total"); // 当日消费总额
                dailySpendMap.put(day, total);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return dailySpendMap;
    }

    /**
     * 5. 查询某分类下的消费记录数（“分类管理”：显示分类使用次数）
     * @param categoryId 分类ID
     * @return 该分类的消费记录数
     */
    public int getRecordCountByCategory(int categoryId) {
        String sql = "SELECT COUNT(*) AS count FROM record WHERE cid = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, categoryId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count"); // 返回记录数
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return 0;
    }

    /**
     * 辅助方法：执行SUM查询，返回求和结果（避免重复代码）
     */
    private BigDecimal getSumFromSql(String sql) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getBigDecimal("total"); // 返回求和结果
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return BigDecimal.ZERO; // 异常或无结果时返回0
    }
    /**
     * 新增：查询所有消费记录（按日期倒序，最新记录在前面）
     * @return 所有消费记录列表
     */
    public List<Record> getAllRecords() {
        List<Record> recordList = new ArrayList<>();
        // SQL：查询所有记录，按消费日期倒序（最新的在前面）
        String sql = "SELECT id, spend, cid, COMMENT, DATE " +
                "FROM record " +
                "ORDER BY DATE DESC, id DESC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            // 遍历结果集，转换为Record对象
            while (rs.next()) {
                Record record = new Record();
                record.setId(rs.getInt("id"));
                record.setSpend(rs.getInt("spend"));
                record.setCid(rs.getInt("cid"));
                record.setComment(rs.getString("COMMENT"));
                record.setDate(rs.getDate("DATE")); // 数据库DATE类型→Java Date类型
                recordList.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("查询所有消费记录失败", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return recordList;
    }
}
