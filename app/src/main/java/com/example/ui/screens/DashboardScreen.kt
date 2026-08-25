package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.GeneratorEntity
import com.example.data.local.entity.ProductionLogEntity
import com.example.data.model.DateFilterPeriod
import com.example.data.model.EngineeringFeature
import com.example.data.model.FeatureControlState
import com.example.data.model.FinancialAnalytics
import com.example.ui.components.EngineeringFeaturesCard
import com.example.ui.components.MetricCard
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    analytics: FinancialAnalytics,
    currentFuelInTank: Double,
    fuelTankCapacity: Double,
    generators: List<GeneratorEntity> = emptyList(),
    productionLogs: List<ProductionLogEntity> = emptyList(),
    featureState: FeatureControlState = FeatureControlState(),
    selectedPeriod: DateFilterPeriod,
    onPeriodSelect: (DateFilterPeriod) -> Unit,
    onOpenAddExpense: () -> Unit,
    onOpenAddFuel: () -> Unit,
    onOpenRecordMeter: () -> Unit,
    onOpenAddSubscriber: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenFeatureControl: () -> Unit = {}
) {
    val currency = analytics.currency

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PolishBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        // Period Selector Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "الفترة الزمنية للحسابات:",
                    style = MaterialTheme.typography.labelMedium,
                    color = PolishTextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        DateFilterPeriod.THIS_MONTH,
                        DateFilterPeriod.LAST_MONTH,
                        DateFilterPeriod.THIS_WEEK,
                        DateFilterPeriod.TODAY,
                        DateFilterPeriod.ALL
                    ).forEach { period ->
                        val selected = period == selectedPeriod
                        FilterChip(
                            selected = selected,
                            onClick = { onPeriodSelect(period) },
                            label = {
                                Text(
                                    period.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (selected) PolishBrandBlue else PolishBorder),
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
        }

        // Hero Net Profit Card - Styled strictly per Professional Polish Theme
        item {
            val isProfit = analytics.netProfitOnBilled >= 0
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_net_profit_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isProfit) PolishPrimaryContainer else PolishRedContainer
                ),
                border = BorderStroke(1.dp, if (isProfit) PolishBorder else PolishRed.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "صافي ربح المحطة (${selectedPeriod.displayName})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isProfit) PolishOnPrimaryContainer else PolishRedOnContainer
                            )
                            Text(
                                text = "بعد خصم الوقود والرواتب والصيانة",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isProfit) PolishOnPrimaryContainer.copy(alpha = 0.7f) else PolishRedOnContainer.copy(alpha = 0.7f)
                            )
                        }

                        // Status Pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            shadowElevation = 0.5.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (isProfit) "مكسب وتشغيل رابح" else "عجز مالي",
                                    color = if (isProfit) PolishBrandBlue else PolishRed,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Large Net Profit Value
                    Text(
                        text = "${if (isProfit) "+" else ""}${String.format("%,.0f", analytics.netProfitOnBilled)} $currency",
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isProfit) PolishOnPrimaryContainer else PolishRed
                    )

                    HorizontalDivider(
                        color = if (isProfit) PolishOnPrimaryContainer.copy(alpha = 0.12f) else PolishRed.copy(alpha = 0.15f)
                    )

                    // Secondary Metrics Row inside Hero Card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "إجمالي الإيرادات",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isProfit) PolishOnPrimaryContainer.copy(alpha = 0.7f) else PolishRedOnContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                "${String.format("%,.0f", analytics.totalBilledRevenue)} $currency",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isProfit) PolishOnPrimaryContainer else PolishRedOnContainer
                            )
                        }
                        Column {
                            Text(
                                "إجمالي المصروفات",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isProfit) PolishOnPrimaryContainer.copy(alpha = 0.7f) else PolishRedOnContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                "${String.format("%,.0f", analytics.totalExpenses)} $currency",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = PolishRed
                            )
                        }
                        Column {
                            Text(
                                "هامش الربح",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isProfit) PolishOnPrimaryContainer.copy(alpha = 0.7f) else PolishRedOnContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                "${String.format("%.1f", analytics.profitMarginPercentage)}%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isProfit) PolishGreen else PolishRed
                            )
                        }
                    }
                }
            }
        }

        // Section Title: Key Unit Power Calculations
        item {
            Text(
                text = "⚡ حسابات الطاقة وتكلفة الكيلو واط الشاملة:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = PolishTextPrimary
            )
        }

        // 4 Core Required Calculations Cards Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. كم باعت المحطة كهرباء بالكيلو
                    MetricCard(
                        title = "الكهرباء المباعة",
                        value = "${String.format("%,.0f", analytics.totalKwhSold)} kWh",
                        subtitle = "المنتج: ${String.format("%,.0f", analytics.totalKwhGenerated)} kWh",
                        icon = Icons.Default.ElectricMeter,
                        iconTint = PolishBrandBlue,
                        badgeText = "الطاقة",
                        badgeColor = PolishPrimaryContainer,
                        badgeTextColor = PolishOnPrimaryContainer,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("metric_kwh_sold")
                    )

                    // 2. كم طلعت تكلفة الكيلو على المحطة
                    MetricCard(
                        title = "تكلفة الكيلو",
                        value = "${String.format("%,.1f", analytics.costPerKwh)} $currency",
                        subtitle = "وقود+رواتب+صيانة/kWh",
                        icon = Icons.Default.PriceCheck,
                        iconTint = PolishRed,
                        badgeText = "التكلفة",
                        badgeColor = PolishRedContainer,
                        badgeTextColor = PolishRedOnContainer,
                        contentColor = PolishRed,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("metric_cost_per_kwh")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 3. بكم باعت سعر الكيلو
                    MetricCard(
                        title = "سعر بيع الكيلو",
                        value = "${String.format("%,.1f", analytics.averageSellingPricePerKwh)} $currency",
                        subtitle = "متوسط تعرفة المشتركين",
                        icon = Icons.Default.Sell,
                        iconTint = PolishGreen,
                        badgeText = "سعر البيع",
                        badgeColor = PolishGreenContainer,
                        badgeTextColor = PolishGreenOnContainer,
                        contentColor = PolishGreen,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("metric_selling_price_kwh")
                    )

                    // 4. كم الربح الصافي في كل كيلو واط
                    val profitPerKwhPositive = analytics.profitMarginPerKwh >= 0
                    MetricCard(
                        title = "ربح الكيلو الصافي",
                        value = "${String.format("%,.1f", analytics.profitMarginPerKwh)} $currency",
                        subtitle = "هامش ربح لكل 1 kWh",
                        icon = Icons.Default.TrendingUp,
                        iconTint = if (profitPerKwhPositive) PolishGreen else PolishRed,
                        badgeText = "${String.format("%.1f", analytics.profitMarginPercentage)}%",
                        badgeColor = if (profitPerKwhPositive) PolishGreenContainer else PolishRedContainer,
                        badgeTextColor = if (profitPerKwhPositive) PolishGreenOnContainer else PolishRedOnContainer,
                        contentColor = if (profitPerKwhPositive) PolishGreen else PolishRed,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("metric_profit_margin_kwh")
                    )
                }
            }
        }

        // Quick Action Buttons Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                border = BorderStroke(1.dp, PolishBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "الإجراءات السريعة:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextPrimary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ActionIconButton(
                            label = "قراءة عداد",
                            icon = Icons.Default.ReceiptLong,
                            iconContainerColor = PolishPrimaryContainer,
                            tint = PolishBrandBlue,
                            onClick = onOpenRecordMeter
                        )
                        ActionIconButton(
                            label = "إضافة مصروف",
                            icon = Icons.Default.AccountBalanceWallet,
                            iconContainerColor = PolishRedContainer,
                            tint = PolishRed,
                            onClick = onOpenAddExpense
                        )
                        ActionIconButton(
                            label = "تزويد وقود",
                            icon = Icons.Default.LocalGasStation,
                            iconContainerColor = PolishAmberContainer,
                            tint = PolishAmber,
                            onClick = onOpenAddFuel
                        )
                        ActionIconButton(
                            label = "مشترك جديد",
                            icon = Icons.Default.PersonAdd,
                            iconContainerColor = PolishGreenContainer,
                            tint = PolishGreen,
                            onClick = onOpenAddSubscriber
                        )
                    }
                }
            }
        }

        // Detailed Cost Structure / Expenses Breakdown - Styled per theme item cards
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ملخص النفقات والمصروفات",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = PolishTextPrimary
                        )
                        Text(
                            text = "${String.format("%,.0f", analytics.totalExpenses)} $currency",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = PolishRed
                        )
                    }

                    HorizontalDivider(color = PolishBorder)

                    val totalExp = if (analytics.totalExpenses > 0) analytics.totalExpenses else 1.0

                    // 1. الوقود والديزل
                    CostBreakdownRow(
                        categoryName = "قيمة الوقود والزيوت",
                        subtitle = "ديزل المولدات والتشغيل",
                        amount = analytics.fuelExpenses,
                        total = totalExp,
                        currency = currency,
                        iconContainerColor = PolishRedContainer,
                        tintColor = PolishRed,
                        icon = Icons.Default.LocalGasStation,
                        unitCost = if (analytics.totalKwhSold > 0) analytics.fuelExpenses / analytics.totalKwhSold else 0.0
                    )

                    // 2. مرتبات العمال
                    CostBreakdownRow(
                        categoryName = "مرتبات الموظفين والعمال",
                        subtitle = "كادر التشغيل والصيانة",
                        amount = analytics.salaryExpenses,
                        total = totalExp,
                        currency = currency,
                        iconContainerColor = PolishPurpleContainer,
                        tintColor = PolishPurple,
                        icon = Icons.Default.Badge,
                        unitCost = if (analytics.totalKwhSold > 0) analytics.salaryExpenses / analytics.totalKwhSold else 0.0
                    )

                    // 3. صيانة وأعطال
                    CostBreakdownRow(
                        categoryName = "نفقات أعطال وصيانة",
                        subtitle = "صيانة المولدات والشبكة",
                        amount = analytics.maintenanceExpenses,
                        total = totalExp,
                        currency = currency,
                        iconContainerColor = PolishAmberContainer,
                        tintColor = PolishAmber,
                        icon = Icons.Default.Build,
                        unitCost = if (analytics.totalKwhSold > 0) analytics.maintenanceExpenses / analytics.totalKwhSold else 0.0
                    )

                    // 4. زيوت وفلاتر
                    CostBreakdownRow(
                        categoryName = "زيوت محركات وفلاتر دورية",
                        subtitle = "تبديل الزيت الدوري",
                        amount = analytics.oilFiltersExpenses,
                        total = totalExp,
                        currency = currency,
                        iconContainerColor = PolishAmberContainer,
                        tintColor = PolishAmber,
                        icon = Icons.Default.Opacity,
                        unitCost = if (analytics.totalKwhSold > 0) analytics.oilFiltersExpenses / analytics.totalKwhSold else 0.0
                    )

                    // 5. قطع غيار ومستلزمات شبكة
                    CostBreakdownRow(
                        categoryName = "قطع غيار وتمديدات شبكة",
                        subtitle = "كابلات وقواطع ومستلزمات",
                        amount = analytics.sparePartsExpenses,
                        total = totalExp,
                        currency = currency,
                        iconContainerColor = PolishPurpleContainer,
                        tintColor = PolishPurple,
                        icon = Icons.Default.Handyman,
                        unitCost = if (analytics.totalKwhSold > 0) analytics.sparePartsExpenses / analytics.totalKwhSold else 0.0
                    )

                    // 6. نفقات إدارية وإيجارات وأخرى
                    CostBreakdownRow(
                        categoryName = "إيجارات ونفقات أخرى",
                        subtitle = "نفقات إدارية ومصاريف عامة",
                        amount = analytics.otherExpenses,
                        total = totalExp,
                        currency = currency,
                        iconContainerColor = PolishSurfaceVariant,
                        tintColor = PolishTextSecondary,
                        icon = Icons.Default.ReceiptLong,
                        unitCost = if (analytics.totalKwhSold > 0) analytics.otherExpenses / analytics.totalKwhSold else 0.0
                    )
                }
            }
        }

        // Fuel Tank & Generation Efficiency Card
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
                        text = "⛽ كفاءة الوقود ومستوى الخزان",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextPrimary
                    )

                    val fuelPct = if (fuelTankCapacity > 0) (currentFuelInTank / fuelTankCapacity).toFloat() else 0f
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("خزان الوقود الرئيسي:", style = MaterialTheme.typography.bodySmall, color = PolishTextSecondary)
                            Text(
                                "${String.format("%,.0f", currentFuelInTank)} / ${String.format("%,.0f", fuelTankCapacity)} لتر (${String.format("%.0f", fuelPct * 100)}%)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = PolishTextPrimary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { fuelPct.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (fuelPct < 0.25f) PolishRed else PolishBrandBlue,
                            trackColor = PolishSurfaceVariant
                        )
                    }

                    HorizontalDivider(color = PolishBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("معدل استهلاك اللتر", style = MaterialTheme.typography.labelSmall, color = PolishTextSecondary)
                            Text(
                                "${String.format("%.2f", analytics.litersPerKwhGenerated)} لتر / kWh",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = PolishTextPrimary
                            )
                        }
                        Column {
                            Text("إنتاج اللتر الواحد", style = MaterialTheme.typography.labelSmall, color = PolishTextSecondary)
                            Text(
                                "${String.format("%.2f", analytics.kwhGeneratedPerLiter)} kWh / لتر",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = PolishBrandBlue
                            )
                        }
                        Column {
                            Text("الفاقد في الشبكة", style = MaterialTheme.typography.labelSmall, color = PolishTextSecondary)
                            Text(
                                "${String.format("%.1f", analytics.networkLossPercentage)}%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (analytics.networkLossPercentage > 15) PolishRed else PolishGreen
                            )
                        }
                    }
                }
            }
        }

        // Engineering & Advanced Technical Features Module
        item {
            EngineeringFeaturesCard(
                featureState = featureState,
                generators = generators,
                productionLogs = productionLogs,
                totalFuelLitersUsed = analytics.fuelExpenses / 750.0, // approx liters or actual
                totalKwhSold = analytics.totalKwhSold,
                totalKwhProduced = analytics.totalKwhGenerated,
                currency = currency,
                onOpenControlDialog = onOpenFeatureControl
            )
        }
    }
}

@Composable
fun ActionIconButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconContainerColor: Color,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(iconContainerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = PolishTextPrimary
        )
    }
}

@Composable
fun CostBreakdownRow(
    categoryName: String,
    subtitle: String,
    amount: Double,
    total: Double,
    currency: String,
    iconContainerColor: Color,
    tintColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    unitCost: Double
) {
    val percentage = ((amount / total) * 100).toFloat()

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = PolishSurface,
        border = BorderStroke(1.dp, PolishBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                            .background(iconContainerColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = tintColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = categoryName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = PolishTextPrimary
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = PolishTextSecondary
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "-${String.format("%,.0f", amount)} $currency",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = PolishRed
                    )
                    Text(
                        text = "${String.format("%.1f", unitCost)} $currency/kWh",
                        style = MaterialTheme.typography.labelSmall,
                        color = PolishTextTertiary
                    )
                }
            }

            LinearProgressIndicator(
                progress = { (percentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = tintColor,
                trackColor = PolishSurfaceVariant
            )
        }
    }
}
