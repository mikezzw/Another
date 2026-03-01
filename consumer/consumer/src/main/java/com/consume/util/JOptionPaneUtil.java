package com.consume.util;

import javax.swing.JOptionPane;

/**
 * 自定义弹窗工具类：封装Swing的JOptionPane，简化弹窗调用
 */
public class JOptionPaneUtil {

    /**
     * 1. 信息提示弹窗（绿色对勾图标）
     * @param msg 要显示的提示内容（如“新增成功！”）
     */
    public static void showInfo(String msg) {
        JOptionPane.showMessageDialog(
                null,        // 父窗口（null表示弹窗居中显示）
                msg,         // 弹窗内容
                "提示",       // 弹窗标题
                JOptionPane.INFORMATION_MESSAGE // 图标类型（信息图标）
        );
    }

    /**
     * 2. 错误提示弹窗（红色叉号图标）
     * @param msg 要显示的错误内容（如“请输入金额！”）
     */
    public static void showError(String msg) {
        JOptionPane.showMessageDialog(
                null,
                msg,
                "错误",       // 弹窗标题
                JOptionPane.ERROR_MESSAGE // 图标类型（错误图标）
        );
    }

    /**
     * 3. 确认弹窗（Yes/No选项，用于删除、恢复等危险操作）
     * @param msg 确认提示内容（如“确定要删除吗？”）
     * @return true=用户点击“Yes”，false=用户点击“No”或关闭弹窗
     */
    public static boolean showConfirm(String msg) {
        int result = JOptionPane.showConfirmDialog(
                null,
                msg,
                "确认",       // 弹窗标题
                JOptionPane.YES_NO_OPTION // 按钮类型（Yes/No）
        );
        // 若用户点击“Yes”，返回true；否则返回false
        return result == JOptionPane.YES_OPTION;
    }

    /**
     * 4. 输入弹窗（让用户输入内容，如修改分类名称）
     * @param msg 输入提示内容（如“请输入新的分类名称：”）
     * @return 用户输入的字符串（null=用户关闭弹窗或点击“取消”）
     */
    public static String showInput(String msg) {
        return JOptionPane.showInputDialog(
                null,
                msg,
                "输入",       // 弹窗标题
                JOptionPane.PLAIN_MESSAGE // 无图标
        );
    }
}