package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.MeterInvoiceEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * نافذة معاينة وطباعة الفاتورة الحرارية (Thermal Receipt Preview)
 */
@Composable
fun ThermalInvoiceReceiptDialog(
    invoice: MeterInvoiceEntity,
    stationName: String,
    currency: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale("ar"))
    val dateStr = dateFormat.format(Date(invoice.invoiceDateTimestamp))

    val receiptText = """
        ================================
        ⚡ $stationName
        فاتورة استهلاك الطاقة الكهربائية
        ================================
        رقم الفاتورة: ${invoice.invoiceNumber}
        المشترك: ${invoice.subscriberName}
        رقم العداد: ${invoice.meterNumber}
        التاريخ: $dateStr
        --------------------------------
        القراءة السابقة: ${String.format("%,.0f", invoice.previousReading)} kWh
        القراءة الحالية: ${String.format("%,.0f", invoice.currentReading)} kWh
        الاستهلاك الفعلي: ${String.format("%,.0f", invoice.consumptionKwh)} kWh
        سعر الكيلو: ${String.format("%.0f", invoice.pricePerKwh)} $currency
        قيمة الطاقة: ${String.format("%,.0f", invoice.energyCost)} $currency
        رسوم الاشتراك: ${String.format("%,.0f", invoice.fixedFee)} $currency
        --------------------------------
        المبلغ الإجمالي: ${String.format("%,.0f", invoice.totalAmount)} $currency
        المسدد: ${String.format("%,.0f", invoice.paidAmount)} $currency
        المتبقي: ${String.format("%,.0f", (invoice.totalAmount - invoice.paidAmount).coerceAtLeast(0.0))} $currency
        حالة السداد: ${if (invoice.paymentStatus == "PAID") "مسدد بالكامل" else "غير مسدد"}
        ================================
        شكراً لتعاونكم وترشيدكم للاستهلاك
    """.trimIndent()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Print, contentDescription = null, tint = PolishBrandBlue)
                Text("معاينة الإيصال والطباعة الحرارية", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = PolishSurfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, PolishBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = receiptText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = PolishTextPrimary,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Text(
                    text = "متوافق مع جميع طابعات البلوتوث الميدانية (ESC/POS 58mm / 80mm)",
                    style = MaterialTheme.typography.labelSmall,
                    color = PolishTextSecondary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "فاتورة كهرباء - ${invoice.subscriberName}")
                        putExtra(Intent.EXTRA_TEXT, receiptText)
                    }
                    context.startActivity(Intent.createChooser(intent, "طباعة / إرسال الفاتورة"))
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = PolishBrandBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("طباعة / مشاركة", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", color = PolishTextSecondary)
            }
        }
    )
}
