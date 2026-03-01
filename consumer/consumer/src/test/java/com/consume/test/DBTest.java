package com.consume.test;

import com.consume.util.DBUtil;
import java.sql.Connection;

/**
 * 数据库连接测试类（验证DBUtil是否正常工作）
 */
public class DBTest {
    public static void main(String[] args) {
        // 调用DBUtil获取连接，若不抛异常则说明连接成功
        Connection conn = DBUtil.getConnection();
        System.out.println("数据库连接成功！连接对象：" + conn);

        // 关闭连接（避免资源占用）
        DBUtil.close(conn, null);
    }
}