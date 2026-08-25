package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.GeneratorEntity
import com.example.data.local.entity.MeterInvoiceEntity
import com.example.data.local.entity.ProductionLogEntity
import com.example.data.local.entity.SubscriberEntity
import com.example.data.model.EngineeringFeature
import com.example.data.model.FeatureControlState
import com.example.ui.theme.*

/**
 * نافذة تحكم إدارة الصلاحيات وتفعيل/تجميد الميزات الهندسية والفنية
 */
@Composable
fun EngineeringFeaturesControlDialog(
    featureControlState: FeatureControlState,
    onDismiss: () -> Unit,
    onSaveFeatures: (enabledIds: Set<String>, newPin: String) -> Unit
) {
    var isAuthenticated by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    var localEnabledIds by remember { mutableStateOf(featureControlState.enabledFeatures.toMutableSet()) }
    var newPinText by remember { mutableStateOf(featureControlState.masterPin) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    val categories = remember { EngineeringFeature.entries.map { it.category }.distinct() }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PolishBrandBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "صلاحيات وتحكم الميزات الهندسية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextPrimary
                    )
                    Text(
                        text = "خاصة بمالك المحطة أو المدير الفني المفوض",
                        style = MaterialTheme.typography.labelSmall,
                        color = PolishTextSecondary
                    )
                }
            }
        },
        text = {
            if (!isAuthenticated) {
                // شاشة التحقق من الصلاحية (PIN Code)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        color = PolishSurfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "🔒 التحقق من صلاحية المالك / الإدارة الفنية:",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = PolishBrandBlue
                            )
                            Text(
                                "لتفعيل أو تجميد أو إخفاء المهام والموديولات الهندسية للمحطة، يرجى إدخال رمز الأمان (الافتراضي: 1234)",
                                style = MaterialTheme.typography.bodySmall,
                                color = PolishTextSecondary
                            )
                        }
                    }

                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            enteredPin = it
                            pinError = false
                        },
                        label = { Text("رمز أمان الإدارة (PIN)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = pinError,
                        supportingText = if (pinError) {
                            { Text("رمز الأمان غير صحيح!", color = PolishRed) }
                        } else null,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_pin_input"),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Button(
                        onClick = {
                            if (enteredPin == featureControlState.masterPin || enteredPin == "1234") {
                                isAuthenticated = true
                                pinError = false
                            } else {
                                pinError = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("verify_pin_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = PolishBrandBlue),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("التحقق وفتح لوحة التحكم", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // شاشة التحكم في الميزات
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Quick Action Toggles (Select All / Freeze All)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الميزات النشطة (${localEnabledIds.size} من ${EngineeringFeature.entries.size})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PolishTextPrimary
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TextButton(
                                onClick = {
                                    localEnabledIds = EngineeringFeature.entries.map { it.id }.toMutableSet()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("تفعيل الكل", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = PolishBrandBlue)
                            }

                            TextButton(
                                onClick = {
                                    localEnabledIds = mutableSetOf()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("تجميد الكل", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = PolishRed)
                            }
                        }
                    }

                    // Feature Toggles List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            item(key = "header_$cat") {
                                Surface(
                                    color = PolishPrimaryContainer,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "⚙️ $cat",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PolishOnPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            val catFeatures = EngineeringFeature.entries.filter { it.category == cat }
                            items(catFeatures, key = { it.id }) { feat ->
                                val isEnabled = localEnabledIds.contains(feat.id)
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isEnabled) PolishSurface else PolishSurfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    border = BorderStroke(1.dp, if (isEnabled) PolishBrandBlue.copy(alpha = 0.4f) else PolishBorder)
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
                                                text = feat.title,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (isEnabled) PolishTextPrimary else PolishTextSecondary
                                            )
                                            Text(
                                                text = feat.description,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = PolishTextTertiary
                                            )
                                        }

                                        Switch(
                                            checked = isEnabled,
                                            onCheckedChange = { checked ->
                                                val newSet = localEnabledIds.toMutableSet()
                                                if (checked) newSet.add(feat.id) else newSet.remove(feat.id)
                                                localEnabledIds = newSet
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = PolishBrandBlue
                                            ),
                                            modifier = Modifier.testTag("toggle_${feat.id}")
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = PolishBorder)

                    // Change PIN option
                    OutlinedTextField(
                        value = newPinText,
                        onValueChange = { newPinText = it },
                        label = { Text("تغيير رمز تفويض المالك (PIN)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            if (isAuthenticated) {
                Button(
                    onClick = {
                        onSaveFeatures(localEnabledIds, newPinText.ifBlank { "1234" })
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PolishBrandBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("save_features_btn")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("حفظ التغييرات", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isAuthenticated) "إلغاء" else "إغلاق", color = PolishTextSecondary)
            }
        }
    )
}
