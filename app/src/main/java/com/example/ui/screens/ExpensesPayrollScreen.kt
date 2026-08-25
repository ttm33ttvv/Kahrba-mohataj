package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.EmployeeEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.PayrollEntity
import com.example.data.model.DateFilterPeriod
import com.example.data.model.ExpenseCategory
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExpensesPayrollScreen(
    expenses: List<ExpenseEntity>,
    employees: List<EmployeeEntity>,
    payrolls: List<PayrollEntity>,
    currency: String,
    selectedCategoryFilter: ExpenseCategory?,
    onSelectCategoryFilter: (ExpenseCategory?) -> Unit,
    selectedPeriod: DateFilterPeriod,
    onPeriodSelect: (DateFilterPeriod) -> Unit,
    onAddExpenseClick: () -> Unit,
    onAddEmployeeClick: () -> Unit,
    onRecordPayrollClick: () -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit,
    onDeleteEmployee: (EmployeeEntity) -> Unit,
    onDeletePayroll: (PayrollEntity) -> Unit
) {
    var subTab by remember { mutableStateOf(0) } // 0: المصروفات والنفقات, 1: الرواتب وكادر المحطة

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PolishBackground)
            .padding(horizontal = 16.dp)
    ) {
        // Sub-Tab Switcher
        TabRow(
            selectedTabIndex = subTab,
            containerColor = PolishSurface,
            contentColor = PolishBrandBlue,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[subTab]),
                    color = PolishBrandBlue
                )
            },
            modifier = Modifier
                .padding(vertical = 10.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(PolishSurface)
        ) {
            Tab(
                selected = subTab == 0,
                onClick = { subTab = 0 },
                text = {
                    Text(
                        "💸 المصروفات والنفقات (${expenses.size})",
                        fontWeight = if (subTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (subTab == 0) PolishBrandBlue else PolishTextSecondary
                    )
                }
            )
            Tab(
                selected = subTab == 1,
                onClick = { subTab = 1 },
                text = {
                    Text(
                        "👥 كادر المحطة والرواتب (${employees.size})",
                        fontWeight = if (subTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (subTab == 1) PolishBrandBlue else PolishTextSecondary
                    )
                }
            )
        }

        if (subTab == 0) {
            // Expenses Tab View
            ExpensesListSection(
                expenses = expenses,
                currency = currency,
                selectedCategoryFilter = selectedCategoryFilter,
                onSelectCategoryFilter = onSelectCategoryFilter,
                onAddExpenseClick = onAddExpenseClick,
                onDeleteExpense = onDeleteExpense
            )
        } else {
            // Employees & Payroll View
            PayrollListSection(
                employees = employees,
                payrolls = payrolls,
                currency = currency,
                onAddEmployeeClick = onAddEmployeeClick,
                onRecordPayrollClick = onRecordPayrollClick,
                onDeleteEmployee = onDeleteEmployee,
                onDeletePayroll = onDeletePayroll
            )
        }
    }
}

@Composable
fun ExpensesListSection(
    expenses: List<ExpenseEntity>,
    currency: String,
    selectedCategoryFilter: ExpenseCategory?,
    onSelectCategoryFilter: (ExpenseCategory?) -> Unit,
    onAddExpenseClick: () -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit
) {
    val totalFilteredExpenses = expenses.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Summary & Add Action Header
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                border = BorderStroke(1.dp, PolishBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "إجمالي النفقات المعروضة:",
                            style = MaterialTheme.typography.labelSmall,
                            color = PolishTextSecondary
                        )
                        Text(
                            text = "${String.format("%,.0f", totalFilteredExpenses)} $currency",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                            fontWeight = FontWeight.Black,
                            color = PolishRed
                        )
                    }

                    Button(
                        onClick = onAddExpenseClick,
                        colors = ButtonDefaults.buttonColors(containerColor = PolishBrandBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_expense_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة مصروف", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    val isAllSelected = selectedCategoryFilter == null
                    FilterChip(
                        selected = isAllSelected,
                        onClick = { onSelectCategoryFilter(null) },
                        label = { Text("جميع البنود", fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal) },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (isAllSelected) PolishBrandBlue else PolishBorder),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PolishBrandBlue,
                            selectedLabelColor = Color.White,
                            containerColor = PolishSurface,
                            labelColor = PolishTextSecondary
                        )
                    )
                }
                items(ExpenseCategory.entries) { cat ->
                    val isCatSelected = selectedCategoryFilter == cat
                    FilterChip(
                        selected = isCatSelected,
                        onClick = { onSelectCategoryFilter(cat) },
                        label = { Text(cat.displayName, fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal) },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (isCatSelected) PolishBrandBlue else PolishBorder),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PolishBrandBlue,
                            selectedLabelColor = Color.White,
                            containerColor = PolishSurface,
                            labelColor = PolishTextSecondary
                        )
                    )
                }
            }
        }

        if (expenses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = PolishSurface),
                    border = BorderStroke(1.dp, PolishBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = PolishTextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            "لا توجد مصروفات مسجلة في هذه الفترة أو التصنيف",
                            fontWeight = FontWeight.Bold,
                            color = PolishTextPrimary
                        )
                        Text(
                            "انقر على 'إضافة مصروف' لتسجيل النفقات",
                            style = MaterialTheme.typography.bodySmall,
                            color = PolishTextSecondary
                        )
                    }
                }
            }
        } else {
            items(expenses, key = { it.id }) { expense ->
                val cat = ExpenseCategory.fromString(expense.category)
                val dateFormat = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar"))
                val dateStr = dateFormat.format(Date(expense.dateTimestamp))

                val (iconContainerColor, tintColor) = when (cat) {
                    ExpenseCategory.FUEL -> Pair(PolishRedContainer, PolishRed)
                    ExpenseCategory.SALARY -> Pair(PolishPurpleContainer, PolishPurple)
                    ExpenseCategory.MAINTENANCE -> Pair(PolishAmberContainer, PolishAmber)
                    ExpenseCategory.OIL_FILTERS -> Pair(PolishAmberContainer, PolishAmber)
                    ExpenseCategory.SPARE_PARTS -> Pair(PolishPurpleContainer, PolishPurple)
                    else -> Pair(PolishSurfaceVariant, PolishTextSecondary)
                }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PolishSurface),
                    border = BorderStroke(1.dp, PolishBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(iconContainerColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (cat) {
                                        ExpenseCategory.FUEL -> Icons.Default.LocalGasStation
                                        ExpenseCategory.SALARY -> Icons.Default.Badge
                                        ExpenseCategory.MAINTENANCE -> Icons.Default.Build
                                        ExpenseCategory.OIL_FILTERS -> Icons.Default.Opacity
                                        ExpenseCategory.SPARE_PARTS -> Icons.Default.Handyman
                                        else -> Icons.Default.ReceiptLong
                                    },
                                    contentDescription = null,
                                    tint = tintColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = expense.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PolishTextPrimary
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = PolishSurfaceVariant
                                    ) {
                                        Text(
                                            text = cat.displayName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PolishTextSecondary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    if (expense.payee.isNotBlank()) {
                                        Text(
                                            text = "• ${expense.payee}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PolishTextSecondary
                                        )
                                    }
                                }
                                Text(
                                    text = dateStr,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PolishTextTertiary
                                )
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "- ${String.format("%,.0f", expense.amount)} $currency",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = PolishRed
                            )

                            IconButton(
                                onClick = { onDeleteExpense(expense) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "حذف",
                                    tint = PolishRed.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PayrollListSection(
    employees: List<EmployeeEntity>,
    payrolls: List<PayrollEntity>,
    currency: String,
    onAddEmployeeClick: () -> Unit,
    onRecordPayrollClick: () -> Unit,
    onDeleteEmployee: (EmployeeEntity) -> Unit,
    onDeletePayroll: (PayrollEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Actions & Employees Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👷 كادر وعمال المحطة (${employees.size}):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = PolishTextPrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = onAddEmployeeClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = PolishPrimaryContainer,
                            contentColor = PolishOnPrimaryContainer
                        )
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة عامل", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onRecordPayrollClick,
                        colors = ButtonDefaults.buttonColors(containerColor = PolishBrandBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("record_payroll_btn")
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("صرف راتب", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Staff Cards
        if (employees.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = PolishSurface),
                    border = BorderStroke(1.dp, PolishBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("لا يوجد موظفون مسجلون حالياً", style = MaterialTheme.typography.bodyMedium, color = PolishTextSecondary)
                    }
                }
            }
        } else {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    employees.forEach { emp ->
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = PolishSurface),
                            border = BorderStroke(1.dp, PolishBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(PolishPurpleContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = PolishPurple,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            emp.name,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = PolishTextPrimary
                                        )
                                        Text(
                                            "${emp.jobTitle} ${if (emp.phone.isNotBlank()) "• ${emp.phone}" else ""}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PolishTextSecondary
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("الراتب الشهري", style = MaterialTheme.typography.labelSmall, color = PolishTextSecondary)
                                        Text(
                                            "${String.format("%,.0f", emp.monthlySalary)} $currency",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = PolishBrandBlue
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteEmployee(emp) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "حذف",
                                            tint = PolishRed.copy(alpha = 0.7f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Historical Payroll Records Section
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "📜 سجل مسيرات الرواتب المصروفة:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = PolishTextPrimary
            )
        }

        if (payrolls.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = PolishSurface),
                    border = BorderStroke(1.dp, PolishBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("لم يتم صرف أي مسير رواتب حتى الآن", style = MaterialTheme.typography.bodyMedium, color = PolishTextSecondary)
                    }
                }
            }
        } else {
            items(payrolls, key = { it.id }) { payroll ->
                val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale("ar"))
                val dateStr = dateFormat.format(Date(payroll.paymentDateTimestamp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PolishSurface),
                    border = BorderStroke(1.dp, PolishBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PolishGreenContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PolishGreen, modifier = Modifier.size(22.dp))
                            }
                            Column {
                                Text(payroll.employeeName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = PolishTextPrimary)
                                Text("مسير شهر: ${payroll.monthYear} | التاريخ: $dateStr", style = MaterialTheme.typography.labelSmall, color = PolishTextSecondary)
                                if (payroll.bonus > 0 || payroll.deductions > 0) {
                                    Text(
                                        "أساسي: ${String.format("%,.0f", payroll.baseSalary)} | حافز: +${String.format("%,.0f", payroll.bonus)} | خصم: -${String.format("%,.0f", payroll.deductions)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PolishTextTertiary
                                    )
                                }
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "${String.format("%,.0f", payroll.netSalary)} $currency",
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.titleSmall,
                                color = PolishGreen
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PolishGreenContainer
                            ) {
                                Text(
                                    "مدفوع نقداً",
                                    color = PolishGreenOnContainer,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
