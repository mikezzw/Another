package com.consume.util;

import com.consume.dao.ConfigDAO;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;

/**
 * 数据库备份/恢复工具类：调用MySQL命令实现数据备份与还原
 */
public class DBBackupRestoreUtil {
    // 数据库基础配置（需与DBUtil中的配置一致！）
    private static final String DB_NAME = "hutubill"; // 你的数据库名
    private static final String DB_USER = "root";     // 你的MySQL用户名
    private static final String DB_PWD = "123456";    // 你的MySQL密码
    private static final ConfigDAO configDAO = new ConfigDAO(); // 用于获取MySQL安装路径

    /**
     * 备份数据到指定SQL文件
     * @param parentFrame 父窗口（用于弹窗居中）
     */
    public static void backup(JFrame parentFrame) {
        // 1. 先获取配置的MySQL安装路径
        String mysqlPath = configDAO.getConfigValueByKey(ConfigDAO.KEY_MYSQL_PATH);
        if (mysqlPath == null || mysqlPath.trim().isEmpty()) {
            JOptionPaneUtil.showError("请先到【系统设置】→【预算与数据库配置】中设置MySQL安装路径！");
            return;
        }

        // 2. 打开文件选择器，让用户选择备份文件保存路径
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择备份文件保存路径");
        // 默认文件名（含时间戳，避免重复）
        String defaultFileName = "hutubill_backup_" + System.currentTimeMillis() + ".sql";
        fileChooser.setSelectedFile(new File(defaultFileName));
        // 只允许选择文件（不允许选择文件夹）
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // 3. 用户选择路径后执行备份
        int result = fileChooser.showSaveDialog(parentFrame);
        if (result == JFileChooser.APPROVE_OPTION) {
            String savePath = fileChooser.getSelectedFile().getAbsolutePath();
            // 处理路径中的反斜杠（Windows路径需转义）
            savePath = savePath.replace("\\", "/");
            mysqlPath = mysqlPath.replace("\\", "/");

            // 4. 拼接mysqldump命令（备份核心命令）
            // 格式：mysql安装路径/bin/mysqldump -u用户名 -p密码 数据库名 > 备份文件路径
            String cmd = String.format(
                    "\"%s/bin/mysqldump.exe\" -u%s -p%s %s > \"%s\"",
                    mysqlPath, DB_USER, DB_PWD, DB_NAME, savePath
            );

            try {
                // 执行命令（启动新进程执行mysqldump）
                Process process = Runtime.getRuntime().exec(cmd);
                int exitCode = process.waitFor(); // 等待命令执行完成
                if (exitCode == 0) { // 0表示命令执行成功
                    JOptionPaneUtil.showInfo("备份成功！文件保存路径：\n" + savePath);
                } else {
                    JOptionPaneUtil.showError("备份失败！请检查MySQL路径和权限。");
                }
            } catch (IOException | InterruptedException e) {
                JOptionPaneUtil.showError("备份异常：" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 从SQL文件恢复数据
     * @param parentFrame 父窗口（用于弹窗居中）
     */
    public static void restore(JFrame parentFrame) {
        // 1. 先获取配置的MySQL安装路径
        String mysqlPath = configDAO.getConfigValueByKey(ConfigDAO.KEY_MYSQL_PATH);
        if (mysqlPath == null || mysqlPath.trim().isEmpty()) {
            JOptionPaneUtil.showError("请先到【系统设置】→【预算与数据库配置】中设置MySQL安装路径！");
            return;
        }

        // 2. 确认恢复（危险操作，避免误删数据）
        if (!JOptionPaneUtil.showConfirm("恢复数据会覆盖现有数据，是否确认？")) {
            return;
        }

        // 3. 打开文件选择器，让用户选择SQL备份文件
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择SQL备份文件");
        // 只允许选择.sql文件（过滤其他格式）
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".sql");
            }

            @Override
            public String getDescription() {
                return "SQL备份文件 (*.sql)";
            }
        });

        // 4. 用户选择文件后执行恢复
        int result = fileChooser.showOpenDialog(parentFrame);
        if (result == JFileChooser.APPROVE_OPTION) {
            String sqlPath = fileChooser.getSelectedFile().getAbsolutePath();
            sqlPath = sqlPath.replace("\\", "/");
            mysqlPath = mysqlPath.replace("\\", "/");

            // 5. 拼接mysql命令（恢复核心命令）
            // 格式：mysql安装路径/bin/mysql -u用户名 -p密码 数据库名 < SQL文件路径
            String cmd = String.format(
                    "\"%s/bin/mysql.exe\" -u%s -p%s %s < \"%s\"",
                    mysqlPath, DB_USER, DB_PWD, DB_NAME, sqlPath
            );

            try {
                Process process = Runtime.getRuntime().exec(cmd);
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    JOptionPaneUtil.showInfo("恢复成功！请刷新数据查看。");
                } else {
                    JOptionPaneUtil.showError("恢复失败！请检查SQL文件格式和MySQL权限。");
                }
            } catch (IOException | InterruptedException e) {
                JOptionPaneUtil.showError("恢复异常：" + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}