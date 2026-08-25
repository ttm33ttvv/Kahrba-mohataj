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
import com.example.data.local.entity.MeterInvoiceEntity
import com.example.data.local.entity.SubscriberEntity
import com.example.data.model.PaymentStatus
import com.example.ui.components.ThermalInvoiceReceiptDialog
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SubscribersSalesScreen(
    subscribers: List<SubscriberEntity>,
    invoices: List<MeterInvoiceEntity>,
    currency: String,
    stationName: String = "محطة الطاقة الكهربائية",
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onOpenAddSubscriber: () -> Unit,
    onOpenRecordMeter: (SubscriberEntity?) -> Unit,
    onOpenPayInvoice: (MeterInvoiceEntity) -> Unit,
    onDeleteSubscriber: (SubscriberEntity) -> Unit,
    onDeleteInvoice: (MeterInvoiceEntity) -> Unit
) {
    var subTab by remember { mutableStateOf(0) } // 0: فواتير ومبيعات الكهرباء, 1: دليل المشتركين والعدادات

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
                        "⚡ فواتير ومبيعات الطاقة (${invoices.size})",
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
                        "👥 المشتركين والعدادات (${subscribers.size})",
                        fontWeight = if (subTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (subTab == 1) PolishBrandBlue else PolishTextSecondary
                    )
                }
            )
        }

        if (subTab == 0) {
            // Invoices & Electricity Sales Tab
            InvoicesSection(
                invoices = invoices,
                currency = currency,
                stationName = stationName,
                onRecordMeterClick = { onOpenRecordMeter(null) },
                onOpenPayInvoice = onOpenPayInvoice,
                onDeleteInvoice = onDeleteInvoice
            )
        } else {
            // Subscribers & Meters Directory Tab
            SubscribersDirectorySection(
                subscribers = subscribers,
                currency = currency,
                searchQuery = searchQuery,
                onSearchChange = onSearchChange,
                onOpenAddSubscriber = onOpenAddSubscriber,
                onOpenRecordMeter = onOpenRecordMeter,
                onDeleteSubscriber = onDeleteSubscriber
            )
        }
    }
}

@Composable
fun InvoicesSection(
    invoices: List<MeterInvoiceEntity>,
    currency: String,
    stationName: String = "محطة الطاقة الكهربائية",
    onRecordMeterClick: () -> Unit,
    onOpenPayInvoice: (MeterInvoiceEntity) -> Unit,
    onDeleteInvoice: (MeterInvoiceEntity) -> Unit
) {
    var selectedInvoiceForPrint by remember { mutableStateOf<MeterInvoiceEntity?>(null) }
    val totalSoldKwh = invoices.sumOf { it.consumptionKwh }
    val totalBilled = invoices.sumOf { it.totalAmount }
    val totalCollected = invoices.sumOf { it.paidAmount }
    val totalPending = (totalBilled - totalCollected).coerceAtLeast(0.0)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
        // Sales Summary Banner
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
                        Column {
                            Text("إجمالي مبيعات الطاقة:", style = MaterialTheme.typography.labelSmall, color = PolishTextSecondary)
                            Text(
                                "${String.format("%,.0f", totalSoldKwh)} kWh",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                                fontWeight = FontWeight.Black,
                                color = PolishBrandBlue
                            )
                        }

                        Button(
                            onClick = onRecordMeterClick,
                            colors = ButtonDefaults.buttonColors(containerColor = PolishBrandBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("record_meter_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("قراءة عداد جديدة", fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = PolishBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("المفوتر الإجمالي", style = MaterialTheme.typography.labelSmall, color = PolishTextSecondary)
                            Text(
                                "${String.format("%,.0f", totalBilled)} $currency",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = PolishTextPrimary
                            )
                        }
                        Column {
                            Text("المحصل نقداً", style = MaterialTheme.typography.labelSmall, color = PolishTextSecondary)
                            Text(
                                "${String.format("%,.0f", totalCollected)} $currency",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = PolishGreen
                            )
                        }
                        Column {
                            Text("الديون المتبقية", style = MaterialTheme.typography.labelSmall, color = PolishTextSecondary)
                            Text(
                                "${String.format("%,.0f", totalPending)} $currency",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (totalPending > 0) PolishRed else PolishGreen
                            )
                        }
                    }
                }
            }
        }

        if (invoices.isEmpty()) {
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
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(48.dp), tint = PolishTextSecondary)
                        Text("لا توجد فواتير ومبيعات مسجلة في هذه الفترة", fontWeight = FontWeight.Bold, color = PolishTextPrimary)
                        Text("انقر على 'قراءة عداد جديدة' لتسجيل استهلاك المشتركين", style = MaterialTheme.typography.bodySmall, color = PolishTextSecondary)
                    }
                }
            }
        } else {
            items(invoices, key = { it.id }) { inv ->
                val isPaid = inv.paymentStatus == "PAID"
                val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale("ar"))
                val dateStr = dateFormat.format(Date(inv.invoiceDateTimestamp))

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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(PolishPrimaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ElectricMeter, contentDescription = null, tint = PolishBrandBlue, modifier = Modifier.size(20.dp))
                                }
                                Column {
                                    Text(inv.subscriberName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = PolishTextPrimary)
                                    Text("عداد: ${inv.meterNumber} | فاتورة: ${inv.invoiceNumber}", style = MaterialTheme.typography.labelSmall, color = PolishTextSecondary)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isPaid) PolishGreenContainer else PolishRedContainer
                            ) {
                                Text(
                                    text = if (isPaid) "مسدد بالكامل" else "غير مسدد",
                                    color = if (isPaid) PolishGreenOnContainer else PolishRedOnContainer,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Meter Reading Details Row
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
                                    Text("السابقة:", style = MaterialTheme.typography.labelSmall, color = PolishTextSecondary)
                                    Text("${String.format("%,.0f", inv.previousReading)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = PolishTextPrimary)
                                }
                                Column {
                                    Text("الحالية:", style = MaterialTheme.typography.labelSmall, color = PolishTextSecondary)
                                    Text("${String.format("%,.0f", inv.currentReading)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = PolishTextPrimary)
                                }
                                Column {
                                    Text("الاستهلاك:", style = MaterialTheme.typography.labelSmall, color = PolishTextSecondary)
                                    Text("${String.format("%,.0f", inv.consumptionKwh)} kWh", fontWeight = FontWeight.Bold, color = PolishBrandBlue, style = MaterialTheme.typography.bodySmall)
                                }
                                Column {
                                    Text("سعر الكيلو:", style = MaterialTheme.typography.labelSmall, color = PolishTextSecondary)
                                    Text("${String.format("%.0f", inv.pricePerKwh)} $currency", style = MaterialTheme.typography.bodySmall, color = PolishTextPrimary)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("المبلغ المطلوب: ${String.format("%,.0f", inv.totalAmount)} $currency", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.bodyMedium, color = PolishTextPrimary)
                                Text("التاريخ: $dateStr", style = MaterialTheme.typography.labelSmall, color = PolishTextTertiary)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = { selectedInvoiceForPrint = inv },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(Icons.Default.Print, contentDescription = "طباعة حرارية", tint = PolishBrandBlue, modifier = Modifier.size(18.dp))
                                }

                                if (!isPaid) {
                                    Button(
                                        onClick = { onOpenPayInvoice(inv) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PolishGreen, contentColor = Color.White)
                                    ) {
                                        Text("تحصيل المبلغ", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                                IconButton(
                                    onClick = { onDeleteInvoice(inv) },
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

    selectedInvoiceForPrint?.let { inv ->
        ThermalInvoiceReceiptDialog(
            invoice = inv,
            stationName = stationName,
            currency = currency,
            onDismiss = { selectedInvoiceForPrint = null }
        )
    }
}
}

@Composable
fun SubscribersDirectorySection(
    subscribers: List<SubscriberEntity>,
    currency: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onOpenAddSubscriber: () -> Unit,
    onOpenRecordMeter: (SubscriberEntity?) -> Unit,
    onDeleteSubscriber: (SubscriberEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Search & Add Subscriber Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("بحث عن مشترك بالاسم، العداد، الهاتف...", style = MaterialTheme.typography.bodySmall) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PolishTextSecondary) },
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

                Button(
                    onClick = onOpenAddSubscriber,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PolishBrandBlue),
                    modifier = Modifier.testTag("add_subscriber_btn")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة مشترك", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (subscribers.isEmpty()) {
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
                        Text("لا يوجد مشتركين مطابقين للبحث", fontWeight = FontWeight.Bold, color = PolishTextPrimary)
                    }
                }
            }
        } else {
            items(subscribers, key = { it.id }) { sub ->
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
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PolishGreenContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = PolishGreen, modifier = Modifier.size(22.dp))
                            }
                            Column {
                                Text(sub.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = PolishTextPrimary)
                                Text("عداد: ${sub.meterNumber} | كود: ${sub.subscriberCode}", style = MaterialTheme.typography.labelSmall, color = PolishTextSecondary)
                                if (sub.sectorAddress.isNotBlank()) {
                                    Text("العنوان: ${sub.sectorAddress}", style = MaterialTheme.typography.labelSmall, color = PolishTextSecondary)
                                }
                                Text("آخر قراءة مسجلة: ${String.format("%,.1f", sub.lastReadingValue)} kWh", style = MaterialTheme.typography.labelSmall, color = PolishBrandBlue)
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { onOpenRecordMeter(sub) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PolishBrandBlue),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.ElectricMeter, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("قراءة العداد", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }

                            IconButton(
                                onClick = { onDeleteSubscriber(sub) },
                                modifier = Modifier.size(24.dp)
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
