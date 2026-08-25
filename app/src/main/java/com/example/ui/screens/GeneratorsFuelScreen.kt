package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.local.entity.FuelRecordEntity
import com.example.data.local.entity.GeneratorEntity
import com.example.data.local.entity.ProductionLogEntity
import com.example.data.model.GeneratorStatus
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GeneratorsFuelScreen(
    generators: List<GeneratorEntity>,
    fuelRecords: List<FuelRecordEntity>,
    productionLogs: List<ProductionLogEntity>,
    currentFuelInTank: Double,
    fuelTankCapacity: Double,
    currency: String,
    onOpenAddGenerator: () -> Unit,
    onOpenAddFuel: () -> Unit,
    onOpenRecordProduction: () -> Unit,
    onToggleGeneratorStatus: (GeneratorEntity, String) -> Unit,
    onResetOilChange: (GeneratorEntity) -> Unit,
    onDeleteGenerator: (GeneratorEntity) -> Unit,
    onDeleteFuelRecord: (FuelRecordEntity) -> Unit
) {
    var subTab by remember { mutableStateOf(0) } // 0: المولدات والتشغيل, 1: سجل الوقود والخزان, 2: سجلات الإنتاج

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PolishBackground)
            .padding(horizontal = 16.dp)
    ) {
        // Tab Switcher
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
                        "⚙️ المولدات (${generators.size})",
                        fontWeight = if (subTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (subTab == 0) PolishBrandBlue else PolishTextSecondary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            )
            Tab(
                selected = subTab == 1,
                onClick = { subTab = 1 },
                text = {
                    Text(
                        "⛽ سجل الوقود (${fuelRecords.size})",
                        fontWeight = if (subTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (subTab == 1) PolishBrandBlue else PolishTextSecondary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            )
            Tab(
                selected = subTab == 2,
                onClick = { subTab = 2 },
                text = {
                    Text(
                        "📈 سجل الإنتاج (${productionLogs.size})",
                        fontWeight = if (subTab == 2) FontWeight.Bold else FontWeight.Normal,
                        color = if (subTab == 2) PolishBrandBlue else PolishTextSecondary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            )
        }

        when (subTab) {
            0 -> GeneratorsListSection(
                generators = generators,
                onAddGeneratorClick = onOpenAddGenerator,
                onToggleStatus = onToggleGeneratorStatus,
                onResetOilChange = onResetOilChange,
                onDeleteGenerator = onDeleteGenerator
            )
            1 -> FuelRecordsSection(
                fuelRecords = fuelRecords,
                currentFuelInTank = currentFuelInTank,
                fuelTankCapacity = fuelTankCapacity,
                currency = currency,
                onAddFuelClick = onOpenAddFuel,
                onDeleteFuelRecord = onDeleteFuelRecord
            )
            2 -> ProductionLogsSection(
                productionLogs = productionLogs,
                onRecordProductionClick = onOpenRecordProduction
            )
        }
    }
}

@Composable
fun GeneratorsListSection(
    generators: List<GeneratorEntity>,
    onAddGeneratorClick: () -> Unit,
    onToggleStatus: (GeneratorEntity, String) -> Unit,
    onResetOilChange: (GeneratorEntity) -> Unit,
    onDeleteGenerator: (GeneratorEntity) -> Unit
) {
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
                    text = "وحدات توليد الطاقة الكهربائية بالمحطة:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = PolishTextPrimary
                )

                Button(
                    onClick = onAddGeneratorClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PolishBrandBlue),
                    modifier = Modifier.testTag("add_generator_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة مولد", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(generators, key = { it.id }) { gen ->
            val isRunning = gen.status == "RUNNING"
            val hoursSinceLastService = gen.totalRunHours - gen.lastMaintenanceHours
            val serviceInterval = if (gen.oilChangeIntervalHours > 0) gen.oilChangeIntervalHours else 250.0
            val serviceRatio = (hoursSinceLastService / serviceInterval).toFloat().coerceIn(0f, 1f)
            val serviceDueSoon = serviceRatio >= 0.85f

            Card(
                shape = RoundedCornerShape(20.dp),
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isRunning) PolishGreenContainer else PolishSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PrecisionManufacturing,
                                    contentDescription = null,
                                    tint = if (isRunning) PolishGreen else PolishTextSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column {
                                Text(gen.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = PolishTextPrimary)
                                Text("${gen.model} | قدرة: ${String.format("%.0f", gen.powerKva)} kVA", style = MaterialTheme.typography.bodySmall, color = PolishTextSecondary)
                            }
                        }

                        // Status Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (gen.status) {
                                "RUNNING" -> PolishGreenContainer
                                "STOPPED" -> PolishSurfaceVariant
                                else -> PolishRedContainer
                            }
                        ) {
                            Text(
                                text = when (gen.status) {
                                    "RUNNING" -> "يعمل حالياً"
                                    "STOPPED" -> "متوقف"
                                    else -> "صيانة"
                                },
                                color = when (gen.status) {
                                    "RUNNING" -> PolishGreenOnContainer
                                    "STOPPED" -> PolishTextSecondary
                                    else -> PolishRedOnContainer
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Generator Metrics Row
                    Surface(
                        color = PolishSurfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("ساعات التشغيل:", style = MaterialTheme.typography.labelSmall, color = PolishTextSecondary)
                                Text("${String.format("%,.1f", gen.totalRunHours)} ساعة", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = PolishTextPrimary)
                            }
                            Column {
                                Text("استهلاك الديزل:", style = MaterialTheme.typography.labelSmall, color = PolishTextSecondary)
                                Text("${String.format("%.0f", gen.estimatedFuelLitersPerHour)} لتر/ساعة", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = PolishTextPrimary)
                            }
                            Column {
                                Text("القدرة الكهربائية:", style = MaterialTheme.typography.labelSmall, color = PolishTextSecondary)
                                Text("${String.format("%.0f", gen.powerKva * 0.8)} kW", fontWeight = FontWeight.Bold, color = PolishBrandBlue, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    // Oil Change Interval Progress
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "مؤشر الصيانة وتغيير الزيت (كل ${String.format("%.0f", serviceInterval)} ساعة):",
                                style = MaterialTheme.typography.labelSmall,
                                color = PolishTextSecondary
                            )
                            Text(
                                text = "${String.format("%.0f", hoursSinceLastService)} / ${String.format("%.0f", serviceInterval)} ساعة",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (serviceDueSoon) PolishRed else PolishGreen
                            )
                        }
                        LinearProgressIndicator(
                            progress = { serviceRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (serviceDueSoon) PolishRed else PolishAmber,
                            trackColor = PolishSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(
                            onClick = { onResetOilChange(gen) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = PolishPrimaryContainer,
                                contentColor = PolishOnPrimaryContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تم تغيير الزيت والصيانة", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedButton(
                                onClick = {
                                    val nextStatus = if (isRunning) "STOPPED" else "RUNNING"
                                    onToggleStatus(gen, nextStatus)
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, PolishBorder),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(if (isRunning) "إيقاف المولد" else "تشغيل المولد", style = MaterialTheme.typography.labelSmall, color = PolishTextPrimary)
                            }

                            IconButton(
                                onClick = { onDeleteGenerator(gen) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = PolishRed.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FuelRecordsSection(
    fuelRecords: List<FuelRecordEntity>,
    currentFuelInTank: Double,
    fuelTankCapacity: Double,
    currency: String,
    onAddFuelClick: () -> Unit,
    onDeleteFuelRecord: (FuelRecordEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Tank Status Card
        item {
            val fuelRatio = if (fuelTankCapacity > 0) (currentFuelInTank / fuelTankCapacity).toFloat() else 0f
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("خزان الوقود الرئيسي بالمحطة:", style = MaterialTheme.typography.labelMedium, color = PolishTextSecondary)
                            Text(
                                "${String.format("%,.0f", currentFuelInTank)} لتر متبقي",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                                fontWeight = FontWeight.Bold,
                                color = PolishBrandBlue
                            )
                        }

                        Button(
                            onClick = onAddFuelClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PolishBrandBlue),
                            modifier = Modifier.testTag("add_fuel_refill_btn")
                        ) {
                            Icon(Icons.Default.LocalGasStation, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تزويد وقود", fontWeight = FontWeight.Bold)
                        }
                    }

                    LinearProgressIndicator(
                        progress = { fuelRatio.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = if (fuelRatio < 0.25f) PolishRed else PolishBrandBlue,
                        trackColor = PolishSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("السعة الكاملة: ${String.format("%,.0f", fuelTankCapacity)} لتر", style = MaterialTheme.typography.labelSmall, color = PolishTextSecondary)
                        Text("نسبة الامتلاء: ${String.format("%.0f", fuelRatio * 100)}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = PolishTextPrimary)
                    }
                }
            }
        }

        item {
            Text(
                text = "📜 سجل عمليات شراء وتزويد الوقود:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = PolishTextPrimary
            )
        }

        if (fuelRecords.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = PolishSurface),
                    border = BorderStroke(1.dp, PolishBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("لم يتم تسجيل أي عمليات تزويد وقود بعد", style = MaterialTheme.typography.bodyMedium, color = PolishTextSecondary)
                    }
                }
            }
        } else {
            items(fuelRecords, key = { it.id }) { record ->
                val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale("ar"))
                val dateStr = dateFormat.format(Date(record.dateTimestamp))

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
                                    .background(PolishAmberContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = PolishAmber, modifier = Modifier.size(22.dp))
                            }
                            Column {
                                Text(
                                    text = "+ ${String.format("%,.0f", record.litersAdded)} لتر ديزل",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PolishTextPrimary
                                )
                                Text(
                                    text = "سعر اللتر: ${String.format("%,.0f", record.costPerLiter)} $currency | التاريخ: $dateStr",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PolishTextSecondary
                                )
                                if (record.supplier.isNotBlank()) {
                                    Text(
                                        text = "المورد: ${record.supplier} ${if (record.invoiceNumber.isNotBlank()) "(وصل: ${record.invoiceNumber})" else ""}",
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
                                text = "${String.format("%,.0f", record.totalCost)} $currency",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = PolishRed
                            )
                            IconButton(
                                onClick = { onDeleteFuelRecord(record) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = PolishRed.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductionLogsSection(
    productionLogs: List<ProductionLogEntity>,
    onRecordProductionClick: () -> Unit
) {
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
                    text = "سجل التوليد وساعات تشغيل المولدات:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = PolishTextPrimary
                )

                Button(
                    onClick = onRecordProductionClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PolishBrandBlue)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تسجيل وردية تشغيل", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (productionLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = PolishSurface),
                    border = BorderStroke(1.dp, PolishBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("لم يتم تسجيل ورديات إنتاج بعد", style = MaterialTheme.typography.bodyMedium, color = PolishTextSecondary)
                    }
                }
            }
        } else {
            items(productionLogs, key = { it.id }) { log ->
                val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale("ar"))
                val dateStr = dateFormat.format(Date(log.logDateTimestamp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PolishSurface),
                    border = BorderStroke(1.dp, PolishBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(log.generatorName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = PolishTextPrimary)
                            Text(dateStr, style = MaterialTheme.typography.labelSmall, color = PolishTextTertiary)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("ساعات التشغيل: ${String.format("%.1f", log.runHours)} س", style = MaterialTheme.typography.bodySmall, color = PolishTextSecondary)
                            Text("الإنتاج: ${String.format("%,.0f", log.kwhProduced)} kWh", fontWeight = FontWeight.Bold, color = PolishBrandBlue, style = MaterialTheme.typography.bodySmall)
                            Text("الوقود المستهلك: ${String.format("%,.0f", log.fuelConsumedLiters)} لتر", style = MaterialTheme.typography.bodySmall, color = PolishTextSecondary)
                        }
                    }
                }
            }
        }
    }
}
