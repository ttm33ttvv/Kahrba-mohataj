@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.*
import com.example.data.model.ExpenseCategory
import java.util.Calendar

@Composable
fun AddExpenseDialog(
    generators: List<GeneratorEntity>,
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, amount: Double, category: ExpenseCategory, payee: String, paymentMethod: String, generatorId: Long?, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.MAINTENANCE) }
    var payee by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("نقدي") }
    var selectedGeneratorId by remember { mutableStateOf<Long?>(null) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تسجيل مصروف / نفقة جديدة",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Category Picker
                Text(
                    text = "نوع النفقة / البند:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                var expandedCat by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedCat,
                    onExpandedChange = { expandedCat = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCat) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCat,
                        onDismissRequest = { expandedCat = false }
                    ) {
                        ExpenseCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName) },
                                onClick = {
                                    selectedCategory = cat
                                    expandedCat = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("بيان المصروف / الوصف *") },
                    placeholder = { Text("مثال: صيانة سير المحرك، تبديل فلاتر...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("المبلغ الإجمالي ($currency) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_amount_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = payee,
                    onValueChange = { payee = it },
                    label = { Text("المستلم / الجهة أو الورشة") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Generator link
                if (generators.isNotEmpty()) {
                    var expandedGen by remember { mutableStateOf(false) }
                    val selectedGenName = generators.find { it.id == selectedGeneratorId }?.name ?: "عام / لكامل المحطة"
                    ExposedDropdownMenuBox(
                        expanded = expandedGen,
                        onExpandedChange = { expandedGen = it }
                    ) {
                        OutlinedTextField(
                            value = selectedGenName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("خاص بمولد معين (اختياري)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGen) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedGen,
                            onDismissRequest = { expandedGen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("عام / لكامل المحطة") },
                                onClick = {
                                    selectedGeneratorId = null
                                    expandedGen = false
                                }
                            )
                            generators.forEach { gen ->
                                DropdownMenuItem(
                                    text = { Text(gen.name) },
                                    onClick = {
                                        selectedGeneratorId = gen.id
                                        expandedGen = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amount > 0) {
                        onConfirm(title, amount, selectedCategory, payee, paymentMethod, selectedGeneratorId, notes)
                    }
                },
                enabled = title.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0,
                modifier = Modifier.testTag("confirm_add_expense_btn")
            ) {
                Text("حفظ المصروف")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun AddFuelRefillDialog(
    generators: List<GeneratorEntity>,
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (liters: Double, pricePerLiter: Double, supplier: String, invoiceNo: String, generatorId: Long?, notes: String) -> Unit
) {
    var litersText by remember { mutableStateOf("") }
    var pricePerLiterText by remember { mutableStateOf("") }
    var supplier by remember { mutableStateOf("") }
    var invoiceNo by remember { mutableStateOf("") }
    var selectedGeneratorId by remember { mutableStateOf<Long?>(null) }
    var notes by remember { mutableStateOf("") }

    val liters = litersText.toDoubleOrNull() ?: 0.0
    val pricePerLiter = pricePerLiterText.toDoubleOrNull() ?: 0.0
    val totalCost = liters * pricePerLiter

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تسجيل تزويد وقود / ديزل",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = litersText,
                    onValueChange = { litersText = it },
                    label = { Text("الكمية المضافة (لتر) *") },
                    placeholder = { Text("مثال: 3000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fuel_liters_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = pricePerLiterText,
                    onValueChange = { pricePerLiterText = it },
                    label = { Text("سعر اللتر الواحد ($currency) *") },
                    placeholder = { Text("مثال: 750") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fuel_price_input"),
                    singleLine = true
                )

                if (totalCost > 0) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("إجمالي تكلفة الوقود:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${String.format("%,.0f", totalCost)} $currency",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = supplier,
                    onValueChange = { supplier = it },
                    label = { Text("المورد / اسم شركة النفط أو الصهريج") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = invoiceNo,
                    onValueChange = { invoiceNo = it },
                    label = { Text("رقم الوصل / الفاتورة") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (liters > 0 && pricePerLiter > 0) {
                        onConfirm(liters, pricePerLiter, supplier, invoiceNo, selectedGeneratorId, notes)
                    }
                },
                enabled = liters > 0 && pricePerLiter > 0,
                modifier = Modifier.testTag("confirm_fuel_btn")
            ) {
                Text("تسجيل الوقود والمصروف")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun RecordMeterInvoiceDialog(
    subscribers: List<SubscriberEntity>,
    defaultKwhPrice: Double,
    defaultFixedFee: Double,
    currency: String,
    preselectedSubscriber: SubscriberEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (subscriber: SubscriberEntity, currentReading: Double, paidImmediately: Boolean, notes: String) -> Unit
) {
    var selectedSubscriber by remember {
        mutableStateOf(preselectedSubscriber ?: subscribers.firstOrNull())
    }
    var currentReadingText by remember { mutableStateOf("") }
    var paidImmediately by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }

    val sub = selectedSubscriber
    val prevReading = sub?.lastReadingValue ?: 0.0
    val currentReading = currentReadingText.toDoubleOrNull() ?: prevReading
    val consumptionKwh = (currentReading - prevReading).coerceAtLeast(0.0)
    val pricePerKwh = sub?.customPricePerKwh ?: defaultKwhPrice
    val fixedFee = sub?.customFixedFee ?: defaultFixedFee
    val energyCost = consumptionKwh * pricePerKwh
    val totalBill = energyCost + fixedFee

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تسجيل قراءة عداد وإصدار فاتورة",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Subscriber Picker
                if (preselectedSubscriber == null && subscribers.isNotEmpty()) {
                    var expandedSub by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedSub,
                        onExpandedChange = { expandedSub = it }
                    ) {
                        OutlinedTextField(
                            value = selectedSubscriber?.let { "${it.name} (${it.subscriberCode})" } ?: "اختر المشترك",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("المشترك *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSub) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedSub,
                            onDismissRequest = { expandedSub = false }
                        ) {
                            subscribers.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text("${item.name} - عداد: ${item.meterNumber}") },
                                    onClick = {
                                        selectedSubscriber = item
                                        expandedSub = false
                                    }
                                )
                            }
                        }
                    }
                } else if (sub != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(sub.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text("رقم العداد: ${sub.meterNumber} | كود: ${sub.subscriberCode}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Previous Reading Info Card
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("القراءة السابقة للعداد:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "${String.format("%,.1f", prevReading)} kWh",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                OutlinedTextField(
                    value = currentReadingText,
                    onValueChange = { currentReadingText = it },
                    label = { Text("القراءة الحالية الجديدة (kWh) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("meter_current_reading_input"),
                    singleLine = true
                )

                // Calculation Summary
                if (currentReadingText.isNotBlank() && currentReading >= prevReading) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("كمية الاستهلاك:", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "${String.format("%,.1f", consumptionKwh)} kWh",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("سعر الكيلو واط:", style = MaterialTheme.typography.bodySmall)
                                Text("${String.format("%,.0f", pricePerKwh)} $currency", style = MaterialTheme.typography.bodySmall)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("قيمة استهلاك الطاقة:", style = MaterialTheme.typography.bodySmall)
                                Text("${String.format("%,.0f", energyCost)} $currency", style = MaterialTheme.typography.bodySmall)
                            }
                            if (fixedFee > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("رسوم الاشتراك الثابتة:", style = MaterialTheme.typography.bodySmall)
                                    Text("${String.format("%,.0f", fixedFee)} $currency", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            HorizontalDivider()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("المبلغ المطلوب:", fontWeight = FontWeight.Bold)
                                Text(
                                    "${String.format("%,.0f", totalBill)} $currency",
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = paidImmediately,
                        onCheckedChange = { paidImmediately = it }
                    )
                    Text("تم استلام المبلغ نقداً الآن (مسدد)")
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (sub != null && currentReading >= prevReading) {
                        onConfirm(sub, currentReading, paidImmediately, notes)
                    }
                },
                enabled = sub != null && currentReadingText.isNotBlank() && currentReading >= prevReading,
                modifier = Modifier.testTag("confirm_meter_invoice_btn")
            ) {
                Text("إصدار وحفظ الفاتورة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun AddSubscriberDialog(
    defaultKwhPrice: Double,
    defaultFixedFee: Double,
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, code: String, phone: String, address: String, meterNumber: String, customPrice: Double?, customFixedFee: Double?, initialReading: Double, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("SUB-${System.currentTimeMillis().toString().takeLast(3)}") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var meterNumber by remember { mutableStateOf("MTR-${System.currentTimeMillis().toString().takeLast(4)}") }
    var customPriceText by remember { mutableStateOf("") }
    var customFixedFeeText by remember { mutableStateOf("") }
    var initialReadingText by remember { mutableStateOf("0") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "إضافة مشترك جديد للمحطة",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المشترك / المحل / المنزل *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sub_name_input"),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("كود المشترك") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = meterNumber,
                        onValueChange = { meterNumber = it },
                        label = { Text("رقم العداد *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("العنوان / القاطع / الزقاق") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = initialReadingText,
                    onValueChange = { initialReadingText = it },
                    label = { Text("قراءة العداد الحالية الافتتاحية (kWh)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customPriceText,
                        onValueChange = { customPriceText = it },
                        label = { Text("سعر خاص للكيلو ($currency)") },
                        placeholder = { Text("${String.format("%.0f", defaultKwhPrice)}") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = customFixedFeeText,
                        onValueChange = { customFixedFeeText = it },
                        label = { Text("رسوم ثابتة ($currency)") },
                        placeholder = { Text("${String.format("%.0f", defaultFixedFee)}") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && meterNumber.isNotBlank()) {
                        val cPrice = customPriceText.toDoubleOrNull()
                        val cFee = customFixedFeeText.toDoubleOrNull()
                        val initReading = initialReadingText.toDoubleOrNull() ?: 0.0
                        onConfirm(name, code, phone, address, meterNumber, cPrice, cFee, initReading, notes)
                    }
                },
                enabled = name.isNotBlank() && meterNumber.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_sub_btn")
            ) {
                Text("إضافة المشترك")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun RecordPayrollDialog(
    employees: List<EmployeeEntity>,
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (employee: EmployeeEntity, monthYear: String, baseSalary: Double, bonus: Double, deductions: Double, notes: String) -> Unit
) {
    var selectedEmployee by remember { mutableStateOf(employees.firstOrNull()) }
    val cal = Calendar.getInstance()
    var monthYear by remember {
        mutableStateOf(String.format("%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1))
    }
    var baseSalaryText by remember {
        mutableStateOf(selectedEmployee?.monthlySalary?.let { String.format("%.0f", it) } ?: "")
    }
    var bonusText by remember { mutableStateOf("0") }
    var deductionsText by remember { mutableStateOf("0") }
    var notes by remember { mutableStateOf("") }

    val base = baseSalaryText.toDoubleOrNull() ?: 0.0
    val bonus = bonusText.toDoubleOrNull() ?: 0.0
    val deductions = deductionsText.toDoubleOrNull() ?: 0.0
    val netSalary = (base + bonus - deductions).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "صرف مسير راتب موظف / عامل",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (employees.isNotEmpty()) {
                    var expandedEmp by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedEmp,
                        onExpandedChange = { expandedEmp = it }
                    ) {
                        OutlinedTextField(
                            value = selectedEmployee?.let { "${it.name} - ${it.jobTitle}" } ?: "اختر العامل",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("الموظف / العامل *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEmp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedEmp,
                            onDismissRequest = { expandedEmp = false }
                        ) {
                            employees.forEach { emp ->
                                DropdownMenuItem(
                                    text = { Text("${emp.name} (${emp.jobTitle})") },
                                    onClick = {
                                        selectedEmployee = emp
                                        baseSalaryText = String.format("%.0f", emp.monthlySalary)
                                        expandedEmp = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = monthYear,
                    onValueChange = { monthYear = it },
                    label = { Text("شهر الراتب (سنة-شهر)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = baseSalaryText,
                    onValueChange = { baseSalaryText = it },
                    label = { Text("الراتب الأساسي ($currency) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = bonusText,
                        onValueChange = { bonusText = it },
                        label = { Text("مكافآت وحوافز") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = deductionsText,
                        onValueChange = { deductionsText = it },
                        label = { Text("خصومات واستقطاع") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("صافي الراتب المصروف:", fontWeight = FontWeight.Bold)
                        Text(
                            "${String.format("%,.0f", netSalary)} $currency",
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val emp = selectedEmployee
                    if (emp != null && base > 0) {
                        onConfirm(emp, monthYear, base, bonus, deductions, notes)
                    }
                },
                enabled = selectedEmployee != null && base > 0,
                modifier = Modifier.testTag("confirm_payroll_btn")
            ) {
                Text("صرف وقيد كمصروف")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun StationConfigDialog(
    currentConfig: StationConfigEntity,
    onDismiss: () -> Unit,
    onConfirm: (StationConfigEntity) -> Unit
) {
    var name by remember { mutableStateOf(currentConfig.stationName) }
    var ownerName by remember { mutableStateOf(currentConfig.ownerName) }
    var phone by remember { mutableStateOf(currentConfig.phone) }
    var location by remember { mutableStateOf(currentConfig.location) }
    var currency by remember { mutableStateOf(currentConfig.currency) }
    var defaultKwhPriceText by remember { mutableStateOf(String.format("%.0f", currentConfig.defaultPricePerKwh)) }
    var defaultFixedFeeText by remember { mutableStateOf(String.format("%.0f", currentConfig.defaultFixedSubscriptionFee)) }
    var tankCapacityText by remember { mutableStateOf(String.format("%.0f", currentConfig.fuelTankCapacityLiters)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "إعدادات المحطة الكهربائية",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المحطة *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("اسم المالك / المدير") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = currency,
                        onValueChange = { currency = it },
                        label = { Text("رمز العملة") },
                        placeholder = { Text("د.ع أو $ أو ر.س") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم هاتف المحطة / الطوارئ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = defaultKwhPriceText,
                    onValueChange = { defaultKwhPriceText = it },
                    label = { Text("سعر بيع الكيلو واط الافتراضي ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = defaultFixedFeeText,
                    onValueChange = { defaultFixedFeeText = it },
                    label = { Text("رسوم الاشتراك الشهري الثابتة ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = tankCapacityText,
                    onValueChange = { tankCapacityText = it },
                    label = { Text("سعة خزان الوقود الإجمالية (لتر)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = currentConfig.copy(
                        stationName = name.ifBlank { "محطة الكهرباء الخاصة" },
                        ownerName = ownerName,
                        phone = phone,
                        location = location,
                        currency = currency.ifBlank { "د.ع" },
                        defaultPricePerKwh = defaultKwhPriceText.toDoubleOrNull() ?: 150.0,
                        defaultFixedSubscriptionFee = defaultFixedFeeText.toDoubleOrNull() ?: 5000.0,
                        fuelTankCapacityLiters = tankCapacityText.toDoubleOrNull() ?: 10000.0
                    )
                    onConfirm(updated)
                }
            ) {
                Text("حفظ التعديلات")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun PayInvoiceDialog(
    invoice: MeterInvoiceEntity,
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double) -> Unit
) {
    val remaining = (invoice.totalAmount - invoice.paidAmount).coerceAtLeast(0.0)
    var amountText by remember { mutableStateOf(String.format("%.0f", remaining)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تحصيل وسداد فاتورة كهرباء",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("المشترك: ${invoice.subscriberName}", fontWeight = FontWeight.Bold)
                Text("رقم الفاتورة: ${invoice.invoiceNumber}")
                Text("إجمالي الفاتورة: ${String.format("%,.0f", invoice.totalAmount)} $currency")
                Text("المسدد سابقاً: ${String.format("%,.0f", invoice.paidAmount)} $currency")
                Text("المتبقي للتحصيل: ${String.format("%,.0f", remaining)} $currency", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("المبلغ المدفوع حالياً ($currency) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onConfirm(amount)
                    }
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("تأكيد السداد")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun AddGeneratorDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, model: String, powerKva: Double, fuelRate: Double, oilInterval: Double, initialHours: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var powerKvaText by remember { mutableStateOf("500") }
    var fuelRateText by remember { mutableStateOf("45") }
    var oilIntervalText by remember { mutableStateOf("250") }
    var initialHoursText by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("إضافة وحدة توليد / مولد جديد", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المولد (مثال: كتربلر 500)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("الموديل / الرقم التسلسلي") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = powerKvaText,
                        onValueChange = { powerKvaText = it },
                        label = { Text("القدرة (kVA)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = fuelRateText,
                        onValueChange = { fuelRateText = it },
                        label = { Text("الاستهلاك (لتر/س)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = oilIntervalText,
                        onValueChange = { oilIntervalText = it },
                        label = { Text("تبديل الزيت (ساعة)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = initialHoursText,
                        onValueChange = { initialHoursText = it },
                        label = { Text("عداد الساعات الحالي") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val kva = powerKvaText.toDoubleOrNull() ?: 500.0
                        val fuelRate = fuelRateText.toDoubleOrNull() ?: 40.0
                        val interval = oilIntervalText.toDoubleOrNull() ?: 250.0
                        val initHours = initialHoursText.toDoubleOrNull() ?: 0.0
                        onConfirm(name, model, kva, fuelRate, interval, initHours)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("إضافة المولد")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
fun AddEmployeeDialog(
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, jobTitle: String, phone: String, monthlySalary: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var monthlySalaryText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("إضافة عامل / فني جديد بالمحطة", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الموظف أو الفني *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = jobTitle,
                    onValueChange = { jobTitle = it },
                    label = { Text("المسمى الوظيفي (مشغل، فني كهرباء، جابي...)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = monthlySalaryText,
                    onValueChange = { monthlySalaryText = it },
                    label = { Text("الراتب الشهري الأساسي ($currency) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sal = monthlySalaryText.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && sal > 0) {
                        onConfirm(name, jobTitle.ifBlank { "مشغل محطة" }, phone, sal)
                    }
                },
                enabled = name.isNotBlank() && (monthlySalaryText.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("إضافة الموظف")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
fun RecordProductionLogDialog(
    generators: List<GeneratorEntity>,
    onDismiss: () -> Unit,
    onConfirm: (generator: GeneratorEntity, runHours: Double, kwhProduced: Double, fuelConsumed: Double, notes: String) -> Unit
) {
    var selectedGenerator by remember { mutableStateOf(generators.firstOrNull()) }
    var runHoursText by remember { mutableStateOf("") }
    var kwhProducedText by remember { mutableStateOf("") }
    var fuelConsumedText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("تسجيل وردية تشغيل وإنتاج طاقة", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (generators.isNotEmpty()) {
                    var expandedGen by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedGen,
                        onExpandedChange = { expandedGen = it }
                    ) {
                        OutlinedTextField(
                            value = selectedGenerator?.name ?: "اختر المولد",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("المولد *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGen) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedGen,
                            onDismissRequest = { expandedGen = false }
                        ) {
                            generators.forEach { g ->
                                DropdownMenuItem(
                                    text = { Text(g.name) },
                                    onClick = {
                                        selectedGenerator = g
                                        expandedGen = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = runHoursText,
                    onValueChange = { runHoursText = it },
                    label = { Text("ساعات التشغيل بالوردية *") },
                    placeholder = { Text("مثال: 12") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = kwhProducedText,
                    onValueChange = { kwhProducedText = it },
                    label = { Text("كمية الطاقة المنتجة (kWh) *") },
                    placeholder = { Text("مثال: 4500") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = fuelConsumedText,
                    onValueChange = { fuelConsumedText = it },
                    label = { Text("كمية الوقود المستهلكة (لتر)") },
                    placeholder = { Text("مثال: 550") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val g = selectedGenerator
                    val hours = runHoursText.toDoubleOrNull() ?: 0.0
                    val kwh = kwhProducedText.toDoubleOrNull() ?: 0.0
                    val fuel = fuelConsumedText.toDoubleOrNull() ?: 0.0
                    if (g != null && hours > 0 && kwh > 0) {
                        onConfirm(g, hours, kwh, fuel, notes)
                    }
                },
                enabled = selectedGenerator != null && (runHoursText.toDoubleOrNull() ?: 0.0) > 0 && (kwhProducedText.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("تسجيل الوردية")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
