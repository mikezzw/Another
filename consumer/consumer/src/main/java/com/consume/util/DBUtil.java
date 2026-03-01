package com.consume.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 数据库连接工具类（适配hutubill数据库）
 */
public class DBUtil {
    // 1. 数据库配置（根据你的MySQL环境修改！）
    private static final String URL = "jdbc:mysql://localhost:3306/hutubill?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";          // 你的MySQL用户名（默认root）
    private static final String PASSWORD = "123456";    // 你的MySQL密码（安装时设置的密码）

    // 2. 静态代码块：加载MySQL驱动（程序启动时执行一次）
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");  // MySQL 8.0+ 驱动
            // 若用MySQL 5.7，替换为：Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // 驱动加载失败直接抛出异常（程序无法继续，必须解决）
            throw new RuntimeException("MySQL驱动加载失败！请检查依赖是否正确", e);
        }
    }

    /**
     * 3. 获取数据库连接（核心方法，后续DAO层都会调用）
     * @return Connection 数据库连接对象
     */
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("数据库连接失败！请检查配置（URL/用户名/密码）", e);
        }
    }

    /**
     * 4. 关闭数据库资源（避免内存泄漏，必须调用）
     * @param conn 连接对象
     * @param pstmt 预处理语句对象
     * @param rs 结果集对象
     */
    public static void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            // 注意关闭顺序：先关ResultSet，再关PreparedStatement，最后关Connection
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();  // 关闭资源失败仅打印日志，不影响程序后续执行
        }
    }

    /**
     * 重载方法：无ResultSet时调用（如新增、修改、删除操作，无需结果集）
     */
    public static void close(Connection conn, PreparedStatement pstmt) {
        close(conn, pstmt, null);
    }
}