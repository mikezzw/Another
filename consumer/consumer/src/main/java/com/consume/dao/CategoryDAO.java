package com.consume.dao;

import com.consume.entity.Category;
import com.consume.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 消费分类DAO：处理category表的CRUD操作
 */
public class CategoryDAO {

    /**
     * 1. 新增消费分类（如新增“娱乐”“医疗”分类）
     * @param category 分类实体（含name属性）
     * @return true=新增成功，false=失败
     */
    public boolean addCategory(Category category) {
        // SQL：插入分类名称（id自增，无需手动传）
        String sql = "INSERT INTO category (NAME) VALUES (?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection(); // 获取数据库连接
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, category.getName()); // 给SQL的“?”传值（分类名称）
            int rows = pstmt.executeUpdate(); // 执行插入，返回影响行数
            return rows > 0; // 影响行数>0说明插入成功
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // 异常时返回失败
        } finally {
            DBUtil.close(conn, pstmt); // 关闭资源（必须执行）
        }
    }

    /**
     * 2. 查询所有消费分类（用于“分类管理”界面显示、“记一笔”下拉框加载）
     * @return 分类列表（含所有Category对象）
     */
    public List<Category> getAllCategories() {
        List<Category> categoryList = new ArrayList<>();
        String sql = "SELECT id, NAME FROM category ORDER BY id ASC"; // 按id升序排列
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery(); // 执行查询，返回结果集

            // 遍历结果集，转换为Category对象
            while (rs.next()) {
                Category category = new Category();
                category.setId(rs.getInt("id")); // 给实体类赋值（对应表字段）
                category.setName(rs.getString("NAME"));
                categoryList.add(category); // 加入列表
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs); // 关闭结果集、连接等资源
        }
        return categoryList;
    }

    /**
     * 3. 根据ID修改分类名称（如把“购物”改为“线上购物”）
     * @param category 含id和新name的实体
     * @return true=修改成功，false=失败
     */
    public boolean updateCategory(Category category) {
        String sql = "UPDATE category SET NAME = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, category.getName()); // 第一个“?”：新分类名称
            pstmt.setInt(2, category.getId()); // 第二个“?”：分类ID（定位要修改的行）
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
     * 4. 根据ID删除分类（注意：record表有外键关联，需先删除该分类下的消费记录）
     * @param categoryId 分类ID
     * @return true=删除成功，false=失败
     */
    public boolean deleteCategory(int categoryId) {
        Connection conn = null;
        PreparedStatement pstmt1 = null; // 用于删除record表关联记录
        PreparedStatement pstmt2 = null; // 用于删除category表分类

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // 开启事务（确保两个删除操作要么都成功，要么都失败）

            // 步骤1：先删除record表中该分类的所有消费记录（外键约束不允许直接删分类）
            String sql1 = "DELETE FROM record WHERE cid = ?";
            pstmt1 = conn.prepareStatement(sql1);
            pstmt1.setInt(1, categoryId);
            pstmt1.executeUpdate();

            // 步骤2：再删除category表中的分类
            String sql2 = "DELETE FROM category WHERE id = ?";
            pstmt2 = conn.prepareStatement(sql2);
            pstmt2.setInt(1, categoryId);
            int rows = pstmt2.executeUpdate();

            conn.commit(); // 事务提交（两个操作都成功才提交）
            return rows > 0;
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback(); // 异常时回滚事务
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.close(conn, pstmt1);
            DBUtil.close(null, pstmt2); // 注意：conn已在第一个close中处理，这里传null
        }
    }

    /**
     * 5. 根据ID查询单个分类（用于“修改分类”时回显数据）
     * @param categoryId 分类ID
     * @return 分类实体（null=未找到）
     */
    public Category getCategoryById(int categoryId) {
        String sql = "SELECT id, NAME FROM category WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, categoryId);
            rs = pstmt.executeQuery();

            if (rs.next()) { // 若有结果，转换为实体
                Category category = new Category();
                category.setId(rs.getInt("id"));
                category.setName(rs.getString("NAME"));
                return category;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return null; // 未找到返回null
    }
}