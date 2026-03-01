package com.example.accountapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.accountapp.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.DecimalFormat

/**
 * 记账应用的ViewModel
 * 负责管理记账数据和业务逻辑
 */
class AccountViewModel : ViewModel() {
    
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()
    
    private val _inputAmount = MutableStateFlow("")
    val inputAmount: StateFlow<String> = _inputAmount.asStateFlow()
    
    private val _inputDescription = MutableStateFlow("")
    val inputDescription: StateFlow<String> = _inputDescription.asStateFlow()
    
    private val _isIncome = MutableStateFlow(true)
    val isIncome: StateFlow<Boolean> = _isIncome.asStateFlow()
    
    val totalAmount: StateFlow<Double> = MutableStateFlow(0.0)
        .apply { 
            viewModelScope.launch {
                _transactions.collect { transactions ->
                    emit(transactions.sumOf { it.amount })
                }
            }
        }.asStateFlow()
    
    val incomeAmount: StateFlow<Double> = MutableStateFlow(0.0)
        .apply {
            viewModelScope.launch {
                _transactions.collect { transactions ->
                    emit(transactions.filter { it.isIncome }.sumOf { it.amount })
                }
            }
        }.asStateFlow()
    
    val expenseAmount: StateFlow<Double> = MutableStateFlow(0.0)
        .apply {
            viewModelScope.launch {
                _transactions.collect { transactions ->
                    emit(kotlin.math.abs(transactions.filter { it.isExpense }.sumOf { it.amount }))
                }
            }
        }.asStateFlow()
    
    init {
        loadSampleData()
    }
    
    /**
     * 更新输入金额
     */
    fun updateAmount(amount: String) {
        _inputAmount.value = amount
    }
    
    /**
     * 更新描述信息
     */
    fun updateDescription(description: String) {
        _inputDescription.value = description
    }
    
    /**
     * 切换收支类型
     */
    fun toggleTransactionType() {
        _isIncome.value = !_isIncome.value
    }
    
    /**
     * 添加交易记录
     */
    fun addTransaction() {
        val amountText = _inputAmount.value.trim()
        val description = _inputDescription.value.trim()
        
        if (amountText.isEmpty()) return
        
        val amount = amountText.toDoubleOrNull() ?: return
        if (amount <= 0) return
        
        val transaction = if (_isIncome.value) {
            Transaction.createIncome(amount, description)
        } else {
            Transaction.createExpense(amount, description)
        }
        
        _transactions.value = _transactions.value + transaction
        clearInputs()
    }
    
    /**
     * 删除交易记录
     */
    fun deleteTransaction(transactionId: String) {
        _transactions.value = _transactions.value.filter { it.id != transactionId }
    }
    
    /**
     * 清空输入框
     */
    fun clearInputs() {
        _inputAmount.value = ""
        _inputDescription.value = ""
    }
    
    /**
     * 加载示例数据
     */
    private fun loadSampleData() {
        val sampleTransactions = listOf(
            Transaction.createIncome(1000.0, "工资"),
            Transaction.createExpense(50.0, "午餐"),
            Transaction.createExpense(30.0, "交通费"),
            Transaction.createIncome(200.0, "兼职收入")
        )
        _transactions.value = sampleTransactions
    }
    
    companion object {
        val DECIMAL_FORMAT = DecimalFormat("#,##0.00")
    }
}