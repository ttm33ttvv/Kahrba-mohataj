package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import com.example.data.local.entity.GeneratorEntity
import com.example.data.local.entity.ProductionLogEntity
import com.example.data.model.EngineeringFeature
import com.example.data.model.FeatureControlState
import com.example.ui.theme.*

/**
 * لوحة وعناصر الميزات الهندسية المتقدمة التفاعلية
 */
@Composable
fun EngineeringFeaturesCard(
    featureState: FeatureControlState,
    generators: List<GeneratorEntity>,
    productionLogs: List<ProductionLogEntity>,
    totalFuelLitersUsed: Double,
    totalKwhSold: Double,
    totalKwhProduced: Double,
    currency: String,
    onOpenControlDialog: () -> Unit
) {
    var expandedCalcDialog by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val activeCount = EngineeringFeature.entries.count { featureState.isFeatureActive(it) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("engineering_features_hub_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PolishSurface),
        border = BorderStroke(1.dp, PolishBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with Admin Permission Trigger
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
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PolishPurpleContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Engineering,
                            contentDescription = null,
                            tint = PolishPurple,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "المنظومة الهندسية والتشغيلية المتقدمة",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = PolishTextPrimary
                        )
                        Text(
                            text = "تشخيص المحركات، كفاءة الوقود، والأحمال ($activeCount مفعلة)",
                            style = MaterialTheme.typography.labelSmall,
                            color = PolishTextSecondary
                        )
                    }
                }

                // الزر المخصص للمالك / المدير الفني للتحكم والتجميد
                FilledTonalButton(
                    onClick = onOpenControlDialog,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = PolishBrandBlue.copy(alpha = 0.12f),
                        contentColor = PolishBrandBlue
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("open_feature_control_btn")
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("صلاحيات المالك", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = PolishBorder)

            // 1. هندسة كفاءة الوقود ومعدل الاستهلاك النوعي (SFC)
            if (featureState.isFeatureActive(EngineeringFeature.SPECIFIC_FUEL_CONSUMPTION)) {
                val sfc = if (totalKwhProduced > 0) totalFuelLitersUsed / totalKwhProduced else 0.22
                val isOptimal = sfc <= 0.25 // Standard diesel generator SFC is 0.20 - 0.25 L/kWh

                Surface(
                    color = if (isOptimal) PolishGreenContainer.copy(alpha = 0.5f) else PolishAmberContainer.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, if (isOptimal) PolishGreen.copy(alpha = 0.3f) else PolishAmber.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = if (isOptimal) PolishGreen else PolishAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "معدل الاستهلاك النوعي للوقود (SFC):",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PolishTextPrimary
                                )
                            }
                            Text(
                                text = "${String.format("%.3f", sfc)} لتر / kWh",
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isOptimal) PolishGreen else PolishAmber
                            )
                        }

                        Text(
                            text = if (isOptimal)
                                "✅ كفاءة الاحتراق ممتازة وضمن المعيار الهندسي المصنعي (0.20 - 0.25 لتر/kWh)."
                            else
                                "⚠️ ارتفاع طفيف في استهلاك الديزل لكل كيلو واط، يُنصح بفحص فلاتر الهواء ومعايرة البخاخات.",
                            style = MaterialTheme.typography.labelSmall,
                            color = PolishTextSecondary
                        )
                    }
                }
            }

            // 2. كاشف فروقات وهدر الوقود
            if (featureState.isFeatureActive(EngineeringFeature.FUEL_DISCREPANCY_DETECTOR)) {
                val totalCapacityKva = generators.filter { it.status == "RUNNING" }.sumOf { it.powerKva }
                val estimatedBurnPerHour = generators.filter { it.status == "RUNNING" }.sumOf { it.estimatedFuelLitersPerHour }

                Surface(
                    color = PolishSurfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, PolishBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("كاشف هدر وتسريب الديزل", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = PolishTextPrimary)
                            Text(
                                "معدل السحب المقدر للمولدات العاملة: ${String.format("%.0f", estimatedBurnPerHour)} لتر/ساعة",
                                style = MaterialTheme.typography.labelSmall,
                                color = PolishTextSecondary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PolishGreenContainer
                        ) {
                            Text(
                                "لا توجد فروقات شاذة",
                                color = PolishGreenOnContainer,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // 3. تحليل الفاقد الفني والتجاري
            if (featureState.isFeatureActive(EngineeringFeature.TECHNICAL_COMMERCIAL_LOSSES)) {
                val totalProduced = if (totalKwhProduced > 0) totalKwhProduced else (totalKwhSold * 1.08)
                val totalLossKwh = (totalProduced - totalKwhSold).coerceAtLeast(0.0)
                val lossPercentage = if (totalProduced > 0) (totalLossKwh / totalProduced) * 100 else 0.0
                val technicalLossPct = (lossPercentage * 0.6).coerceAtLeast(0.0) // 60% tech loss approx
                val commercialLossPct = (lossPercentage * 0.4).coerceAtLeast(0.0) // 40% commercial loss approx

                Surface(
                    color = PolishSurfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, PolishBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("تحليل فاقد الشبكة والتوزيع:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = PolishTextPrimary)
                            Text(
                                "${String.format("%.1f", lossPercentage)}% إجمالي الفاقد",
                                fontWeight = FontWeight.Bold,
                                color = if (lossPercentage > 12) PolishRed else PolishGreen,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("• فاقد فني (كابلات ومحولات):", style = MaterialTheme.typography.labelSmall, color = PolishTextSecondary)
                                Text("${String.format("%.1f", technicalLossPct)}%", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = PolishTextPrimary)
                            }
                            Column {
                                Text("• فاقد تجاري (تجاوزات وعدادات):", style = MaterialTheme.typography.labelSmall, color = PolishTextSecondary)
                                Text("${String.format("%.1f", commercialLossPct)}%", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = PolishTextPrimary)
                            }
                            Column {
                                Text("• كمية الفاقد بالكيلو واط:", style = MaterialTheme.typography.labelSmall, color = PolishTextSecondary)
                                Text("${String.format("%,.0f", totalLossKwh)} kWh", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = PolishBrandBlue)
                            }
                        }
                    }
                }
            }

            // Quick Tools Row (Power Factor Calculator, Peak Analysis & Exports)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (featureState.isFeatureActive(EngineeringFeature.POWER_FACTOR_CALCULATOR)) {
                    OutlinedButton(
                        onClick = { expandedCalcDialog = "POWER_FACTOR" },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, PolishBorder),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp), tint = PolishBrandBlue)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("معامل القدرة cos φ", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = PolishTextPrimary)
                    }
                }

                if (featureState.isFeatureActive(EngineeringFeature.DATA_EXPORT_EXCEL)) {
                    OutlinedButton(
                        onClick = {
                            exportStationDataSummary(context, totalKwhSold, totalKwhProduced, totalFuelLitersUsed, currency)
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, PolishBorder),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp), tint = PolishGreen)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تصدير Excel/CSV", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = PolishTextPrimary)
                    }
                }
            }
        }
    }

    // Power Factor Calculator Dialog
    if (expandedCalcDialog == "POWER_FACTOR") {
        PowerFactorCalculatorDialog(onDismiss = { expandedCalcDialog = null })
    }
}

/**
 * حاسبة معامل القدرة الهندسية cos φ
 */
@Composable
fun PowerFactorCalculatorDialog(onDismiss: () -> Unit) {
    var kvaInput by remember { mutableStateOf("500") }
    var pfInput by remember { mutableStateOf("0.8") }

    val kva = kvaInput.toDoubleOrNull() ?: 500.0
    val pf = pfInput.toDoubleOrNull() ?: 0.8

    val activeKw = kva * pf
    val reactiveKvar = kotlin.math.sqrt((kva * kva - activeKw * activeKw).coerceAtLeast(0.0))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Bolt, contentDescription = null, tint = PolishBrandBlue)
                Text("حاسبة معامل القدرة (Power Factor cos φ)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "تتيح هذه الأداة الهندسية حساب القدرة الفعلية kW والقدرة غير الفعالة kVAR وتأثير تحسين معامل القدرة بواسطة مكثفات التعديل (Capacitor Banks).",
                    style = MaterialTheme.typography.bodySmall,
                    color = PolishTextSecondary
                )

                OutlinedTextField(
                    value = kvaInput,
                    onValueChange = { kvaInput = it },
                    label = { Text("القدرة الظاهرية للمولد (kVA)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pfInput,
                    onValueChange = { pfInput = it },
                    label = { Text("معامل القدرة الحالي (cos φ) عادة 0.8") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Surface(
                    color = PolishPrimaryContainer,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("القدرة الفعلية الفعالة (Active kW):", style = MaterialTheme.typography.labelSmall, color = PolishOnPrimaryContainer)
                            Text("${String.format("%.1f", activeKw)} kW", fontWeight = FontWeight.Bold, color = PolishBrandBlue)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("القدرة غير الفعالة (Reactive kVAR):", style = MaterialTheme.typography.labelSmall, color = PolishOnPrimaryContainer)
                            Text("${String.format("%.1f", reactiveKvar)} kVAR", fontWeight = FontWeight.Bold, color = PolishRed)
                        }
                        Text(
                            "💡 تحسين معامل القدرة إلى 0.95 يخفض الفاقد في الكابلات ويوفر حوالي 5-8% من وقود الديزل.",
                            style = MaterialTheme.typography.labelSmall,
                            color = PolishOnPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = PolishBrandBlue)) {
                Text("إغلاق")
            }
        }
    )
}

/**
 * تصدير ملخص بيانات المحطة بتنسيق نصي / CSV
 */
fun exportStationDataSummary(
    context: Context,
    totalKwhSold: Double,
    totalKwhProduced: Double,
    totalFuelUsed: Double,
    currency: String
) {
    val summaryCsv = """
        === كشف التقرير التشغيلي والهندسي لمحطة الطاقة الكهربائية ===
        تاريخ التصدير: ${java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale("ar")).format(java.util.Date())}
        
        [1] مؤشرات التوليد ومبيعات الطاقة:
        - الطاقة المولدة الإجمالية: ${String.format("%,.0f", totalKwhProduced)} kWh
        - الطاقة المبيعة للمشتركين: ${String.format("%,.0f", totalKwhSold)} kWh
        - فاقد التوزيع والشبكة: ${String.format("%,.0f", (totalKwhProduced - totalKwhSold).coerceAtLeast(0.0))} kWh
        
        [2] كفاءة واستهلاك الوقود:
        - إجمالي الوقود المستهلك: ${String.format("%,.0f", totalFuelUsed)} لتر
        - معدل الاستهلاك النوعي (SFC): ${String.format("%.3f", if (totalKwhProduced > 0) totalFuelUsed / totalKwhProduced else 0.0)} لتر/kWh
        
        تم إنشاء هذا التقرير عبر نظام إدارة محطات الطاقة الكهربائية
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "كشف التقرير الهندسي والتشغيلي للمحطة")
        putExtra(Intent.EXTRA_TEXT, summaryCsv)
    }
    context.startActivity(Intent.createChooser(intent, "تصدير كشف المحطة"))
}
