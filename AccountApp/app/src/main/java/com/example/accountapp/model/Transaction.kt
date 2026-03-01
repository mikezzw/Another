package com.example.accountapp.model

import java.util.Date
import java.util.UUID

/**
 * 记账交易数据模型
 * @param id 交易唯一标识符
 * @param amount 交易金额（正数表示收入，负数表示支出）
 * @param description 交易描述
 * @param date 交易时间
 */
data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val description: String = "",
    val date: Date = Date()
) {
    /**
     * 判断是否为收入
     */
    val isIncome: Boolean
        get() = amount > 0
    
    /**
     * 判断是否为支出
     */
    val isExpense: Boolean
        get() = amount < 0
    
    companion object {
        /**
         * 创建收入记录
         */
        fun createIncome(amount: Double, description: String = ""): Transaction {
            return Transaction(
                amount = kotlin.math.abs(amount),
                description = description
            )
        }
        
        /**
         * 创建支出记录
         */
        fun createExpense(amount: Double, description: String = ""): Transaction {
            return Transaction(
                amount = -kotlin.math.abs(amount),
                description = description
            )
        }
    }
}