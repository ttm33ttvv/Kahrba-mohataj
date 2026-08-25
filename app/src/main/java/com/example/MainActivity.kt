package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.PowerStationDatabase
import com.example.data.local.entity.*
import com.example.data.model.*
import com.example.data.repository.PowerStationRepository
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.PowerStationViewModel
import com.example.ui.viewmodel.PowerStationViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: PowerStationViewModel by viewModels {
        val db = PowerStationDatabase.getDatabase(applicationContext)
        val repo = PowerStationRepository(db.dao())
        PowerStationViewModelFactory(repo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PowerStationApp(viewModel = viewModel)
        }
    }
}

enum class NavigationDestination(
    val title: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
) {
    DASHBOARD(
        title = "الرئيسية",
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard,
        testTag = "nav_dashboard"
    ),
    EXPENSES(
        title = "المصروفات والرواتب",
        selectedIcon = Icons.Filled.AccountBalanceWallet,
        unselectedIcon = Icons.Outlined.AccountBalanceWallet,
        testTag = "nav_expenses"
    ),
    SALES(
        title = "المشتركين والمبيعات",
        selectedIcon = Icons.Filled.ElectricMeter,
        unselectedIcon = Icons.Outlined.ElectricMeter,
        testTag = "nav_sales"
    ),
    GENERATORS(
        title = "المولدات والوقود",
        selectedIcon = Icons.Filled.PrecisionManufacturing,
        unselectedIcon = Icons.Outlined.PrecisionManufacturing,
        testTag = "nav_generators"
    ),
    REPORTS(
        title = "التقارير والحاسبة",
        selectedIcon = Icons.Filled.Calculate,
        unselectedIcon = Icons.Outlined.Calculate,
        testTag = "nav_reports"
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PowerStationApp(viewModel: PowerStationViewModel) {
    PowerStationTheme {
        var currentDestination by remember { mutableStateOf(NavigationDestination.DASHBOARD) }

        // State flows from ViewModel
        val analytics by viewModel.financialAnalytics.collectAsStateWithLifecycle()
        val stationConfig by viewModel.stationConfig.collectAsStateWithLifecycle()
        val generators by viewModel.allGenerators.collectAsStateWithLifecycle()
        val expenses by viewModel.filteredExpenses.collectAsStateWithLifecycle()
        val employees by viewModel.allEmployees.collectAsStateWithLifecycle()
        val payrolls by viewModel.allPayrolls.collectAsStateWithLifecycle()
        val subscribers by viewModel.allSubscribers.collectAsStateWithLifecycle()
        val invoices by viewModel.filteredInvoices.collectAsStateWithLifecycle()
        val fuelRecords by viewModel.allFuelRecords.collectAsStateWithLifecycle()
        val productionLogs by viewModel.allProductionLogs.collectAsStateWithLifecycle()
        val currentFuelInTank by viewModel.currentFuelInTank.collectAsStateWithLifecycle()
        val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
        val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
        val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
        val featureControlState by viewModel.featureControlState.collectAsStateWithLifecycle()

        // Dialog Visibility States
        var showAddExpenseDialog by remember { mutableStateOf(false) }
        var showAddFuelDialog by remember { mutableStateOf(false) }
        var showRecordMeterDialog by remember { mutableStateOf(false) }
        var meterPreselectedSubscriber by remember { mutableStateOf<SubscriberEntity?>(null) }
        var showAddSubscriberDialog by remember { mutableStateOf(false) }
        var showRecordPayrollDialog by remember { mutableStateOf(false) }
        var showAddEmployeeDialog by remember { mutableStateOf(false) }
        var showAddGeneratorDialog by remember { mutableStateOf(false) }
        var showRecordProductionDialog by remember { mutableStateOf(false) }
        var showStationConfigDialog by remember { mutableStateOf(false) }
        var showEngineeringControlDialog by remember { mutableStateOf(false) }
        var payingInvoice by remember { mutableStateOf<MeterInvoiceEntity?>(null) }

        val cfg = stationConfig ?: StationConfigEntity(
            stationName = "محطة الطاقة الكهربائية الخاصة",
            currency = "د.ع"
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = PolishBackground,
            topBar = {
                Surface(
                    color = PolishSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
                ) {
                    TopAppBar(
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
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = cfg.stationName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = PolishTextPrimary
                                    )
                                    Text(
                                        text = if (cfg.location.isNotBlank()) cfg.location else "إدارة محطة التوليد والشبكة",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = PolishTextSecondary
                                    )
                                }
                            }
                        },
                        actions = {
                            // Fuel tank quick chip
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = PolishSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalGasStation,
                                        contentDescription = null,
                                        tint = PolishAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${String.format("%,.0f", currentFuelInTank)} لتر",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = PolishTextPrimary
                                    )
                                }
                            }

                            IconButton(
                                onClick = { showStationConfigDialog = true },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(PolishSurfaceVariant)
                                    .testTag("open_settings_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "إعدادات المحطة",
                                    tint = PolishTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = PolishSurface
                        )
                    )
                }
            },
            bottomBar = {
                Surface(
                    color = PolishSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
                ) {
                    NavigationBar(
                        containerColor = PolishSurfaceVariant,
                        tonalElevation = 0.dp
                    ) {
                        NavigationDestination.entries.forEach { destination ->
                            val selected = currentDestination == destination
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentDestination = destination },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                                        contentDescription = destination.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = destination.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PolishOnPrimaryContainer,
                                    selectedTextColor = PolishOnPrimaryContainer,
                                    indicatorColor = PolishPrimaryContainer,
                                    unselectedIconColor = PolishTextSecondary,
                                    unselectedTextColor = PolishTextSecondary
                                ),
                                modifier = Modifier.testTag(destination.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentDestination) {
                    NavigationDestination.DASHBOARD -> DashboardScreen(
                        analytics = analytics,
                        currentFuelInTank = currentFuelInTank,
                        fuelTankCapacity = cfg.fuelTankCapacityLiters,
                        generators = generators,
                        productionLogs = productionLogs,
                        featureState = featureControlState,
                        selectedPeriod = selectedPeriod,
                        onPeriodSelect = { viewModel.setPeriodFilter(it) },
                        onOpenAddExpense = { showAddExpenseDialog = true },
                        onOpenAddFuel = { showAddFuelDialog = true },
                        onOpenRecordMeter = {
                            meterPreselectedSubscriber = null
                            showRecordMeterDialog = true
                        },
                        onOpenAddSubscriber = { showAddSubscriberDialog = true },
                        onOpenSettings = { showStationConfigDialog = true },
                        onOpenFeatureControl = { showEngineeringControlDialog = true }
                    )

                    NavigationDestination.EXPENSES -> ExpensesPayrollScreen(
                        expenses = expenses,
                        employees = employees,
                        payrolls = payrolls,
                        currency = cfg.currency,
                        selectedCategoryFilter = selectedCategoryFilter,
                        onSelectCategoryFilter = { viewModel.setCategoryFilter(it) },
                        selectedPeriod = selectedPeriod,
                        onPeriodSelect = { viewModel.setPeriodFilter(it) },
                        onAddExpenseClick = { showAddExpenseDialog = true },
                        onAddEmployeeClick = { showAddEmployeeDialog = true },
                        onRecordPayrollClick = { showRecordPayrollDialog = true },
                        onDeleteExpense = { viewModel.deleteExpense(it) },
                        onDeleteEmployee = { viewModel.deleteEmployee(it) },
                        onDeletePayroll = { viewModel.deletePayroll(it) }
                    )

                    NavigationDestination.SALES -> SubscribersSalesScreen(
                        subscribers = subscribers,
                        invoices = invoices,
                        currency = cfg.currency,
                        stationName = cfg.stationName,
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onOpenAddSubscriber = { showAddSubscriberDialog = true },
                        onOpenRecordMeter = { sub ->
                            meterPreselectedSubscriber = sub
                            showRecordMeterDialog = true
                        },
                        onOpenPayInvoice = { invoice ->
                            payingInvoice = invoice
                        },
                        onDeleteSubscriber = { viewModel.deleteSubscriber(it) },
                        onDeleteInvoice = { viewModel.deleteInvoice(it) }
                    )

                    NavigationDestination.GENERATORS -> GeneratorsFuelScreen(
                        generators = generators,
                        fuelRecords = fuelRecords,
                        productionLogs = productionLogs,
                        currentFuelInTank = currentFuelInTank,
                        fuelTankCapacity = cfg.fuelTankCapacityLiters,
                        currency = cfg.currency,
                        onOpenAddGenerator = { showAddGeneratorDialog = true },
                        onOpenAddFuel = { showAddFuelDialog = true },
                        onOpenRecordProduction = { showRecordProductionDialog = true },
                        onToggleGeneratorStatus = { gen, newStatus ->
                            viewModel.updateGeneratorStatus(gen, newStatus)
                        },
                        onResetOilChange = { gen ->
                            viewModel.recordOilChange(gen)
                        },
                        onDeleteGenerator = { viewModel.deleteGenerator(it) },
                        onDeleteFuelRecord = { viewModel.deleteFuelRecord(it) }
                    )

                    NavigationDestination.REPORTS -> ReportsCalculatorScreen(
                        analytics = analytics,
                        stationName = cfg.stationName,
                        selectedPeriod = selectedPeriod,
                        onPeriodSelect = { viewModel.setPeriodFilter(it) }
                    )
                }
            }
        }

        // Dialogs Rendering
        if (showAddExpenseDialog) {
            AddExpenseDialog(
                generators = generators,
                currency = cfg.currency,
                onDismiss = { showAddExpenseDialog = false },
                onConfirm = { title, amount, category, payee, method, genId, notes ->
                    viewModel.addExpense(title, amount, category, payee, method, genId, notes)
                    showAddExpenseDialog = false
                }
            )
        }

        if (showAddFuelDialog) {
            AddFuelRefillDialog(
                generators = generators,
                currency = cfg.currency,
                onDismiss = { showAddFuelDialog = false },
                onConfirm = { liters, pricePerLiter, supplier, invoiceNo, genId, notes ->
                    viewModel.addFuelRefill(liters, pricePerLiter, supplier, invoiceNo, genId, notes)
                    showAddFuelDialog = false
                }
            )
        }

        if (showRecordMeterDialog) {
            RecordMeterInvoiceDialog(
                subscribers = subscribers,
                defaultKwhPrice = cfg.defaultPricePerKwh,
                defaultFixedFee = cfg.defaultFixedSubscriptionFee,
                currency = cfg.currency,
                preselectedSubscriber = meterPreselectedSubscriber,
                onDismiss = { showRecordMeterDialog = false },
                onConfirm = { subscriber, currentReading, paidImmediately, notes ->
                    viewModel.recordMeterReading(subscriber, currentReading, paidImmediately, notes)
                    showRecordMeterDialog = false
                }
            )
        }

        if (showAddSubscriberDialog) {
            AddSubscriberDialog(
                defaultKwhPrice = cfg.defaultPricePerKwh,
                defaultFixedFee = cfg.defaultFixedSubscriptionFee,
                currency = cfg.currency,
                onDismiss = { showAddSubscriberDialog = false },
                onConfirm = { name, code, phone, address, meterNumber, customPrice, customFee, initialReading, notes ->
                    viewModel.addSubscriber(name, code, phone, address, meterNumber, customPrice, customFee, initialReading, notes)
                    showAddSubscriberDialog = false
                }
            )
        }

        if (showRecordPayrollDialog) {
            RecordPayrollDialog(
                employees = employees,
                currency = cfg.currency,
                onDismiss = { showRecordPayrollDialog = false },
                onConfirm = { employee, monthYear, base, bonus, deductions, notes ->
                    viewModel.recordPayroll(employee, monthYear, base, bonus, deductions, notes)
                    showRecordPayrollDialog = false
                }
            )
        }

        if (showAddEmployeeDialog) {
            AddEmployeeDialog(
                currency = cfg.currency,
                onDismiss = { showAddEmployeeDialog = false },
                onConfirm = { name, jobTitle, phone, monthlySalary ->
                    viewModel.addEmployee(name, jobTitle, phone, monthlySalary)
                    showAddEmployeeDialog = false
                }
            )
        }

        if (showAddGeneratorDialog) {
            AddGeneratorDialog(
                onDismiss = { showAddGeneratorDialog = false },
                onConfirm = { name, model, powerKva, fuelRate, interval, initialHours ->
                    viewModel.addGenerator(name, model, powerKva, fuelRate, interval, initialHours)
                    showAddGeneratorDialog = false
                }
            )
        }

        if (showRecordProductionDialog) {
            RecordProductionLogDialog(
                generators = generators,
                onDismiss = { showRecordProductionDialog = false },
                onConfirm = { generator, runHours, kwhProduced, fuelConsumed, notes ->
                    viewModel.recordProductionLog(generator, runHours, kwhProduced, fuelConsumed, notes)
                    showRecordProductionDialog = false
                }
            )
        }

        if (showStationConfigDialog) {
            StationConfigDialog(
                currentConfig = cfg,
                onDismiss = { showStationConfigDialog = false },
                onConfirm = { updatedConfig ->
                    viewModel.updateStationConfig(updatedConfig)
                    showStationConfigDialog = false
                }
            )
        }

        if (showEngineeringControlDialog) {
            EngineeringFeaturesControlDialog(
                featureControlState = featureControlState,
                onDismiss = { showEngineeringControlDialog = false },
                onSaveFeatures = { enabledIds, newPin ->
                    viewModel.updateEngineeringFeatures(enabledIds, newPin)
                }
            )
        }

        payingInvoice?.let { inv ->
            PayInvoiceDialog(
                invoice = inv,
                currency = cfg.currency,
                onDismiss = { payingInvoice = null },
                onConfirm = { amount ->
                    viewModel.payInvoice(inv, amount)
                    payingInvoice = null
                }
            )
        }
    }
}
