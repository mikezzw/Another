package com.example.accountapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.accountapp.model.Transaction
import com.example.accountapp.ui.theme.AccountAppTheme
import com.example.accountapp.viewmodel.AccountViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AccountAppTheme {
                AccountApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountApp(
    viewModel: AccountViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.transactions.collectAsState()
    val inputAmount by viewModel.inputAmount.collectAsState()
    val inputDescription by viewModel.inputDescription.collectAsState()
    val isIncome by viewModel.isIncome.collectAsState()
    val totalAmount by viewModel.totalAmount.collectAsState()
    val incomeAmount by viewModel.incomeAmount.collectAsState()
    val expenseAmount by viewModel.expenseAmount.collectAsState()
    
    val isAddEnabled = inputAmount.isNotEmpty() && 
        inputAmount.toDoubleOrNull()?.let { it > 0 } == true
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("记账本") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            val isAddEnabled = inputAmount.isNotEmpty() && 
                inputAmount.toDoubleOrNull()?.let { it > 0 } == true
            
            if (isAddEnabled) {
                FloatingActionButton(
                    onClick = { viewModel.addTransaction() }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "添加记录")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 统计卡片
            StatisticsCards(
                totalAmount = totalAmount,
                incomeAmount = incomeAmount,
                expenseAmount = expenseAmount
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 输入区域
            InputSection(
                amount = inputAmount,
                description = inputDescription,
                isIncome = isIncome,
                isAddEnabled = isAddEnabled,
                onAmountChange = viewModel::updateAmount,
                onDescriptionChange = viewModel::updateDescription,
                onTypeToggle = viewModel::toggleTransactionType,
                onAddClick = { viewModel.addTransaction() }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 记录列表
            Text(
                text = "交易记录",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无记录",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            } else {
                TransactionList(
                    transactions = transactions,
                    onDeleteTransaction = viewModel::deleteTransaction
                )
            }
        }
    }
}

@Composable
fun StatisticsCards(
    totalAmount: Double,
    incomeAmount: Double,
    expenseAmount: Double,
    modifier: Modifier = Modifier
) {
    val decimalFormat = AccountViewModel.DECIMAL_FORMAT
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 总金额卡片
        StatCard(
            title = "总金额",
            amount = totalAmount,
            color = if (totalAmount >= 0) Color.Green else Color.Red,
            modifier = Modifier.weight(1f)
        )
        
        // 收入卡片
        StatCard(
            title = "收入",
            amount = incomeAmount,
            color = Color.Green,
            modifier = Modifier.weight(1f)
        )
        
        // 支出卡片
        StatCard(
            title = "支出",
            amount = expenseAmount,
            color = Color.Red,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    title: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    val decimalFormat = AccountViewModel.DECIMAL_FORMAT
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "¥${decimalFormat.format(kotlin.math.abs(amount))}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun InputSection(
    amount: String,
    description: String,
    isIncome: Boolean,
    isAddEnabled: Boolean,
    onAmountChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTypeToggle: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 类型切换按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onTypeToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isIncome) Color.Green else Color.Red
                    )
                ) {
                    Text(
                        text = if (isIncome) "收入" else "支出",
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 金额输入
            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChange,
                label = { Text("金额") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 描述输入
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("描述（可选）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 添加按钮
            Button(
                onClick = onAddClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = isAddEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isIncome) Color.Green else Color.Red
                )
            ) {
                Text(
                    text = "添加${if (isIncome) "收入" else "支出"}",
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun TransactionList(
    transactions: List<Transaction>,
    onDeleteTransaction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(transactions.reversed()) { transaction ->
            TransactionItem(
                transaction = transaction,
                onDeleteClick = { onDeleteTransaction(transaction.id) }
            )
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    val decimalFormat = AccountViewModel.DECIMAL_FORMAT
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (transaction.isIncome) Color.Green.copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description.ifEmpty { if (transaction.isIncome) "收入" else "支出" },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = dateFormat.format(transaction.date),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¥${decimalFormat.format(kotlin.math.abs(transaction.amount))}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.isIncome) Color.Green else Color.Red
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AccountAppPreview() {
    AccountAppTheme {
        AccountApp()
    }
}