package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DateFilterPeriod
import com.example.data.model.FinancialAnalytics
import com.example.ui.theme.*

@Composable
fun ReportsCalculatorScreen(
    analytics: FinancialAnalytics,
    stationName: String,
    selectedPeriod: DateFilterPeriod,
    onPeriodSelect: (DateFilterPeriod) -> Unit
) {
    val context = LocalContext.current
    val currency = analytics.currency
    var subTab by remember { mutableStateOf(0) } // 0: التقرير المحاسبي الشامل, 1: حاسبة تسعير وتكلفة الكيلو واط

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PolishBackground)
            .padding(horizontal = 16.dp)
    ) {
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
                        "📑 التقرير المالي الشامل",
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
                        "🧮 حاسبة التكلفة والتسعير",
                        fontWeight = if (subTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (subTab == 1) PolishBrandBlue else PolishTextSecondary
                    )
                }
            )
        }

        if (subTab == 0) {
            IncomeStatementReport(
                analytics = analytics,
                stationName = stationName,
                selectedPeriod = selectedPeriod,
                onPeriodSelect = onPeriodSelect,
                onShareClick = {
                    shareReport(context, analytics, stationName)
                }
            )
        } else {
            CostPricingCalculator(currency = currency)
        }
    }
}

@Composable
fun IncomeStatementReport(
    analytics: FinancialAnalytics,
    stationName: String,
    selectedPeriod: DateFilterPeriod,
    onPeriodSelect: (DateFilterPeriod) -> Unit,
    onShareClick: () -> Unit
) {
    val currency = analytics.currency
    val isProfit = analytics.netProfitOnBilled >= 0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "قائمة الأرباح والخسائر والحسابات الشاملة:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = PolishTextPrimary
                )

                OutlinedButton(
                    onClick = onShareClick,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, PolishBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PolishBrandBlue
                    )
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("مشاركة التقرير", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Period filter chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(DateFilterPeriod.THIS_MONTH, DateFilterPeriod.LAST_MONTH, DateFilterPeriod.THIS_YEAR, DateFilterPeriod.ALL).forEach { period ->
                    val isSelected = period == selectedPeriod
                    FilterChip(
                        selected = isSelected,
                        onClick = { onPeriodSelect(period) },
                        label = { Text(period.displayName, style = MaterialTheme.typography.labelSmall, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (isSelected) PolishBrandBlue else PolishBorder),
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

        // Summary Header Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                border = BorderStroke(1.dp, PolishBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "محطة: $stationName",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PolishBrandBlue
                    )
                    Text(
                        text = "تقرير الفترة المحاسبية: ${selectedPeriod.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = PolishTextSecondary
                    )

                    HorizontalDivider(color = PolishBorder)

                    // 1. الإيرادات
                    ReportRow(
                        title = "1. إجمالي إيرادات بيع الكهرباء (المفوترة)",
                        amount = analytics.totalBilledRevenue,
                        currency = currency,
                        color = PolishGreen,
                        isBold = true
                    )
                    ReportSubRow(title = "• مبيعات الطاقة بالكيلو واط:", value = "${String.format("%,.0f", analytics.totalKwhSold)} kWh")
                    ReportSubRow(title = "• متوسط سعر البيع للمشتركين:", value = "${String.format("%,.1f", analytics.averageSellingPricePerKwh)} $currency/kWh")
                    ReportSubRow(title = "• المبالغ المحصلة نقداً:", value = "${String.format("%,.0f", analytics.totalCollectedCash)} $currency")

                    HorizontalDivider(color = PolishBorder)

                    // 2. النفقات والمصروفات
                    ReportRow(
                        title = "2. إجمالي نفقات ومصروفات التشغيل",
                        amount = analytics.totalExpenses,
                        currency = currency,
                        color = PolishRed,
                        isBold = true
                    )
                    ReportSubRow(title = "• تكلفة وقود الديزل:", value = "${String.format("%,.0f", analytics.fuelExpenses)} $currency")
                    ReportSubRow(title = "• مرتبات وأجور العمال:", value = "${String.format("%,.0f", analytics.salaryExpenses)} $currency")
                    ReportSubRow(title = "• صيانة وأعطال المولدات:", value = "${String.format("%,.0f", analytics.maintenanceExpenses)} $currency")
                    ReportSubRow(title = "• زيوت المحركات والفلاتر:", value = "${String.format("%,.0f", analytics.oilFiltersExpenses)} $currency")
                    ReportSubRow(title = "• قطع غيار وشبكة:", value = "${String.format("%,.0f", analytics.sparePartsExpenses)} $currency")
                    ReportSubRow(title = "• إيجارات ونفقات أخرى:", value = "${String.format("%,.0f", analytics.otherExpenses)} $currency")

                    HorizontalDivider(color = PolishBorder)

                    // 3. مؤشرات الكيلو واط
                    Text("3. حسابات تكلفة وهامش ربح الكيلو واط:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = PolishTextPrimary)
                    ReportSubRow(title = "• تكلفة الكيلو واط على المحطة:", value = "${String.format("%,.1f", analytics.costPerKwh)} $currency/kWh", isHighlight = true, highlightColor = PolishRed)
                    ReportSubRow(title = "• سعر بيع الكيلو للمشتركين:", value = "${String.format("%,.1f", analytics.averageSellingPricePerKwh)} $currency/kWh", isHighlight = true, highlightColor = PolishBrandBlue)
                    ReportSubRow(title = "• صافي الربح في كل كيلو واط:", value = "${String.format("%,.1f", analytics.profitMarginPerKwh)} $currency/kWh", isHighlight = true, highlightColor = PolishGreen)

                    HorizontalDivider(color = PolishBorder)

                    // 4. صافي الربح النهائي
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isProfit) PolishGreenContainer else PolishRedContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("صافي الربح النهائي:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = if (isProfit) PolishGreenOnContainer else PolishRedOnContainer)
                                Text("نسبة هامش الربح: ${String.format("%.1f", analytics.profitMarginPercentage)}%", style = MaterialTheme.typography.bodySmall, color = if (isProfit) PolishGreenOnContainer.copy(alpha = 0.8f) else PolishRedOnContainer.copy(alpha = 0.8f))
                            }
                            Text(
                                "${String.format("%,.0f", analytics.netProfitOnBilled)} $currency",
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.headlineSmall,
                                color = if (isProfit) PolishGreenOnContainer else PolishRedOnContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CostPricingCalculator(currency: String) {
    var expectedMonthlyKwhText by remember { mutableStateOf("30000") }
    var dieselPricePerLiterText by remember { mutableStateOf("750") }
    var dieselConsumptionRateText by remember { mutableStateOf("0.16") } // Liters per kWh
    var monthlySalariesText by remember { mutableStateOf("2000000") }
    var monthlyMaintenanceAndMiscText by remember { mutableStateOf("1000000") }
    var targetProfitMarginPercentageText by remember { mutableStateOf("25") }

    val monthlyKwh = expectedMonthlyKwhText.toDoubleOrNull() ?: 1.0
    val dieselPrice = dieselPricePerLiterText.toDoubleOrNull() ?: 0.0
    val consumptionRate = dieselConsumptionRateText.toDoubleOrNull() ?: 0.16
    val salaries = monthlySalariesText.toDoubleOrNull() ?: 0.0
    val maintenanceMisc = monthlyMaintenanceAndMiscText.toDoubleOrNull() ?: 0.0
    val targetMarginPct = targetProfitMarginPercentageText.toDoubleOrNull() ?: 20.0

    // Calculations
    val totalFuelLitersNeeded = monthlyKwh * consumptionRate
    val totalEstimatedFuelCost = totalFuelLitersNeeded * dieselPrice
    val totalEstimatedExpenses = totalEstimatedFuelCost + salaries + maintenanceMisc

    val estimatedCostPerKwh = if (monthlyKwh > 0) totalEstimatedExpenses / monthlyKwh else 0.0
    val fuelCostPerKwh = if (monthlyKwh > 0) totalEstimatedFuelCost / monthlyKwh else 0.0
    val overheadCostPerKwh = if (monthlyKwh > 0) (salaries + maintenanceMisc) / monthlyKwh else 0.0

    val recommendedSellingPricePerKwh = if (targetMarginPct < 100) estimatedCostPerKwh * (1 + targetMarginPct / 100.0) else estimatedCostPerKwh
    val profitPerKwh = recommendedSellingPricePerKwh - estimatedCostPerKwh
    val expectedTotalMonthlyRevenue = monthlyKwh * recommendedSellingPricePerKwh
    val expectedTotalMonthlyNetProfit = expectedTotalMonthlyRevenue - totalEstimatedExpenses

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Text(
                text = "حاسبة تخطيط وتسعير تكلفة الكيلو واط الافتراضية:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = PolishTextPrimary
            )
            Text(
                text = "استخدم هذه الحاسبة لتحديد سعر بيع الكيلو المناسب لمحطتك وفقاً لأسعار الوقود والمصاريف",
                style = MaterialTheme.typography.bodySmall,
                color = PolishTextSecondary
            )
        }

        // Inputs Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                border = BorderStroke(1.dp, PolishBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("مدخلات وبيانات التشغيل المتوقعة:", fontWeight = FontWeight.Bold, color = PolishTextPrimary)

                    OutlinedTextField(
                        value = expectedMonthlyKwhText,
                        onValueChange = { expectedMonthlyKwhText = it },
                        label = { Text("كمية الكهرباء المتوقع بيعها شهرياً (kWh) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calc_kwh_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = PolishSurface,
                            unfocusedContainerColor = PolishSurface,
                            focusedBorderColor = PolishBrandBlue,
                            unfocusedBorderColor = PolishBorder
                        )
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = dieselPricePerLiterText,
                            onValueChange = { dieselPricePerLiterText = it },
                            label = { Text("سعر لتر الديزل ($currency)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = PolishSurface,
                                unfocusedContainerColor = PolishSurface,
                                focusedBorderColor = PolishBrandBlue,
                                unfocusedBorderColor = PolishBorder
                            )
                        )
                        OutlinedTextField(
                            value = dieselConsumptionRateText,
                            onValueChange = { dieselConsumptionRateText = it },
                            label = { Text("استهلاك اللتر/kWh") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = PolishSurface,
                                unfocusedContainerColor = PolishSurface,
                                focusedBorderColor = PolishBrandBlue,
                                unfocusedBorderColor = PolishBorder
                            )
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = monthlySalariesText,
                            onValueChange = { monthlySalariesText = it },
                            label = { Text("إجمالي الرواتب ($currency)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = PolishSurface,
                                unfocusedContainerColor = PolishSurface,
                                focusedBorderColor = PolishBrandBlue,
                                unfocusedBorderColor = PolishBorder
                            )
                        )
                        OutlinedTextField(
                            value = monthlyMaintenanceAndMiscText,
                            onValueChange = { monthlyMaintenanceAndMiscText = it },
                            label = { Text("الصيانة والنفقات ($currency)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = PolishSurface,
                                unfocusedContainerColor = PolishSurface,
                                focusedBorderColor = PolishBrandBlue,
                                unfocusedBorderColor = PolishBorder
                            )
                        )
                    }

                    OutlinedTextField(
                        value = targetProfitMarginPercentageText,
                        onValueChange = { targetProfitMarginPercentageText = it },
                        label = { Text("نسبة هامش الربح الصافي المستهدف (%) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = PolishSurface,
                            unfocusedContainerColor = PolishSurface,
                            focusedBorderColor = PolishBrandBlue,
                            unfocusedBorderColor = PolishBorder
                        )
                    )
                }
            }
        }

        // Calculation Results Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PolishPrimaryContainer),
                border = BorderStroke(1.dp, PolishBrandBlue.copy(alpha = 0.2f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "💡 النتائج والتسعير الموصى به للمحطة:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PolishOnPrimaryContainer
                    )

                    HorizontalDivider(color = PolishOnPrimaryContainer.copy(alpha = 0.15f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("تكلفة الكيلو واط على المحطة:", fontWeight = FontWeight.Medium, color = PolishOnPrimaryContainer)
                        Text(
                            "${String.format("%,.1f", estimatedCostPerKwh)} $currency/kWh",
                            fontWeight = FontWeight.Bold,
                            color = PolishRed
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("• منها تكلفة الوقود لكل كيلو:", style = MaterialTheme.typography.bodySmall, color = PolishOnPrimaryContainer.copy(alpha = 0.8f))
                        Text("${String.format("%,.1f", fuelCostPerKwh)} $currency/kWh", style = MaterialTheme.typography.bodySmall, color = PolishOnPrimaryContainer)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("• منها الرواتب والصيانة لكل كيلو:", style = MaterialTheme.typography.bodySmall, color = PolishOnPrimaryContainer.copy(alpha = 0.8f))
                        Text("${String.format("%,.1f", overheadCostPerKwh)} $currency/kWh", style = MaterialTheme.typography.bodySmall, color = PolishOnPrimaryContainer)
                    }

                    HorizontalDivider(color = PolishOnPrimaryContainer.copy(alpha = 0.15f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("سعر البيع المقترح للمشتركين:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = PolishOnPrimaryContainer)
                        Text(
                            "${String.format("%,.0f", recommendedSellingPricePerKwh)} $currency/kWh",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge,
                            color = PolishBrandBlue
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("هامش الربح في كل كيلو واط:", color = PolishOnPrimaryContainer)
                        Text("${String.format("%,.1f", profitPerKwh)} $currency/kWh", fontWeight = FontWeight.Bold, color = PolishGreen)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("صافي الربح الشهري المتوقع للمحطة:", fontWeight = FontWeight.Bold, color = PolishOnPrimaryContainer)
                        Text(
                            "${String.format("%,.0f", expectedTotalMonthlyNetProfit)} $currency",
                            fontWeight = FontWeight.ExtraBold,
                            color = PolishGreen,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReportRow(title: String, amount: Double, currency: String, color: Color, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal, style = MaterialTheme.typography.bodyMedium, color = PolishTextPrimary)
        Text(
            text = "${String.format("%,.0f", amount)} $currency",
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = color,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ReportSubRow(
    title: String,
    value: String,
    isHighlight: Boolean = false,
    highlightColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = MaterialTheme.typography.bodySmall, color = PolishTextSecondary)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlight) highlightColor else PolishTextPrimary
        )
    }
}

fun shareReport(context: Context, analytics: FinancialAnalytics, stationName: String) {
    val currency = analytics.currency
    val text = """
        📊 تقرير الحسابات الشاملة - $stationName
        الفترة: ${analytics.period.displayName}
        ==================================
        ⚡ مبيعات الطاقة: ${String.format("%,.0f", analytics.totalKwhSold)} kWh
        💰 إجمالي الإيرادات: ${String.format("%,.0f", analytics.totalBilledRevenue)} $currency
        💸 إجمالي النفقات: ${String.format("%,.0f", analytics.totalExpenses)} $currency
        - وقود ديزل: ${String.format("%,.0f", analytics.fuelExpenses)} $currency
        - مرتبات العمال: ${String.format("%,.0f", analytics.salaryExpenses)} $currency
        - صيانة وأعطال: ${String.format("%,.0f", analytics.maintenanceExpenses)} $currency
        - زيوت وفلاتر: ${String.format("%,.0f", analytics.oilFiltersExpenses)} $currency
        - أخرى: ${String.format("%,.0f", analytics.otherExpenses)} $currency
        ==================================
        🔍 حسابات تكلفة الكيلو واط:
        • تكلفة الكيلو على المحطة: ${String.format("%,.1f", analytics.costPerKwh)} $currency/kWh
        • متوسط سعر بيع الكيلو: ${String.format("%,.1f", analytics.averageSellingPricePerKwh)} $currency/kWh
        • صافي ربح الكيلو: ${String.format("%,.1f", analytics.profitMarginPerKwh)} $currency/kWh (${String.format("%.1f", analytics.profitMarginPercentage)}%)
        ==================================
        🏆 صافي الربح النهائي: ${String.format("%,.0f", analytics.netProfitOnBilled)} $currency
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "تقرير حسابات محطة الكهرباء")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "مشاركة تقرير المحطة"))
}
