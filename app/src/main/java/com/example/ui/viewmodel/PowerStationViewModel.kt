package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.*
import com.example.data.model.*
import com.example.data.repository.PowerStationRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class PowerStationViewModel(
    private val repository: PowerStationRepository
) : ViewModel() {

    // Feature Control State (Admin / Owner Permissions)
    private val _featureControlState = MutableStateFlow(FeatureControlState())
    val featureControlState: StateFlow<FeatureControlState> = _featureControlState.asStateFlow()

    fun updateEngineeringFeatures(enabledIds: Set<String>, masterPin: String) {
        _featureControlState.value = _featureControlState.value.copy(
            enabledFeatures = enabledIds,
            masterPin = masterPin
        )
    }

    // Active Navigation Tab
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    // Filter period
    private val _selectedPeriod = MutableStateFlow(DateFilterPeriod.THIS_MONTH)
    val selectedPeriod: StateFlow<DateFilterPeriod> = _selectedPeriod.asStateFlow()

    fun setPeriod(period: DateFilterPeriod) {
        _selectedPeriod.value = period
    }

    fun setPeriodFilter(period: DateFilterPeriod) {
        _selectedPeriod.value = period
    }

    // Search queries
    private val _subscriberSearchQuery = MutableStateFlow("")
    val subscriberSearchQuery: StateFlow<String> = _subscriberSearchQuery.asStateFlow()
    val searchQuery: StateFlow<String> = _subscriberSearchQuery.asStateFlow()

    fun setSubscriberSearch(query: String) {
        _subscriberSearchQuery.value = query
    }

    fun setSearchQuery(query: String) {
        _subscriberSearchQuery.value = query
    }

    private val _expenseCategoryFilter = MutableStateFlow<ExpenseCategory?>(null)
    val expenseCategoryFilter: StateFlow<ExpenseCategory?> = _expenseCategoryFilter.asStateFlow()
    val selectedCategoryFilter: StateFlow<ExpenseCategory?> = _expenseCategoryFilter.asStateFlow()

    fun setExpenseCategoryFilter(category: ExpenseCategory?) {
        _expenseCategoryFilter.value = category
    }

    fun setCategoryFilter(category: ExpenseCategory?) {
        _expenseCategoryFilter.value = category
    }

    // Repository Flows
    val config = repository.stationConfig.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StationConfigEntity()
    )
    val stationConfig: StateFlow<StationConfigEntity?> = config

    val generators = repository.allGenerators.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val allGenerators: StateFlow<List<GeneratorEntity>> = generators

    val allExpenses = repository.allExpenses.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allEmployees = repository.allEmployees.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allPayrolls = repository.allPayrolls.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allSubscribers = repository.allSubscribers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allInvoices = repository.allInvoices.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allFuelRecords = repository.allFuelRecords.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allProductionLogs = repository.allProductionLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current Fuel In Tank calculation
    val currentFuelInTank: StateFlow<Double> = combine(
        config,
        allFuelRecords,
        allProductionLogs
    ) { cfg, fuels, logs ->
        val base = cfg?.currentFuelInTankLiters ?: 6800.0
        val added = fuels.sumOf { it.litersAdded }
        val consumed = logs.sumOf { it.fuelConsumedLiters }
        (base + added - consumed).coerceAtLeast(0.0)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 6800.0
    )

    // Filtered Invoices based on Period
    val filteredInvoices = combine(allInvoices, _selectedPeriod) { invoices, period ->
        invoices.filter { isTimestampInPeriod(it.invoiceDateTimestamp, period) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered Expenses based on Period and Category
    val filteredExpenses = combine(allExpenses, _selectedPeriod, _expenseCategoryFilter) { expenses, period, catFilter ->
        expenses.filter { expense ->
            val matchPeriod = isTimestampInPeriod(expense.dateTimestamp, period)
            val matchCat = catFilter == null || expense.category.equals(catFilter.name, ignoreCase = true)
            matchPeriod && matchCat
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered Subscribers based on search
    val filteredSubscribers = combine(allSubscribers, _subscriberSearchQuery) { subscribers, query ->
        if (query.isBlank()) subscribers
        else {
            subscribers.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.subscriberCode.contains(query, ignoreCase = true) ||
                it.meterNumber.contains(query, ignoreCase = true) ||
                it.phone.contains(query, ignoreCase = true) ||
                it.sectorAddress.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Comprehensive Financial & Accounting Analytics Engine
    val financialAnalytics: StateFlow<FinancialAnalytics> = combine(
        listOf(
            config,
            _selectedPeriod,
            allInvoices,
            allExpenses,
            allFuelRecords,
            allProductionLogs
        )
    ) { args ->
        val currentConfig = args[0] as? StationConfigEntity ?: StationConfigEntity()
        val period = args[1] as? DateFilterPeriod ?: DateFilterPeriod.THIS_MONTH
        @Suppress("UNCHECKED_CAST")
        val invoices = args[2] as? List<MeterInvoiceEntity> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val expenses = args[3] as? List<ExpenseEntity> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val fuelRecords = args[4] as? List<FuelRecordEntity> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val productionLogs = args[5] as? List<ProductionLogEntity> ?: emptyList()

        computeAnalytics(
            config = currentConfig,
            period = period,
            invoices = invoices.filter { isTimestampInPeriod(it.invoiceDateTimestamp, period) },
            expenses = expenses.filter { isTimestampInPeriod(it.dateTimestamp, period) },
            fuelRecords = fuelRecords.filter { isTimestampInPeriod(it.dateTimestamp, period) },
            productionLogs = productionLogs.filter { isTimestampInPeriod(it.logDateTimestamp, period) }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FinancialAnalytics()
    )

    private fun computeAnalytics(
        config: StationConfigEntity,
        period: DateFilterPeriod,
        invoices: List<MeterInvoiceEntity>,
        expenses: List<ExpenseEntity>,
        fuelRecords: List<FuelRecordEntity>,
        productionLogs: List<ProductionLogEntity>
    ): FinancialAnalytics {
        val currency = config.currency

        // 1. Power Sales & Production
        val totalKwhSold = invoices.sumOf { it.consumptionKwh }
        val totalKwhGeneratedFromLogs = productionLogs.sumOf { it.kwhProduced }
        val totalKwhGenerated = if (totalKwhGeneratedFromLogs > 0) totalKwhGeneratedFromLogs else (totalKwhSold * 1.08) // fallback loss estimation
        val unbilledKwhLoss = (totalKwhGenerated - totalKwhSold).coerceAtLeast(0.0)
        val networkLossPercentage = if (totalKwhGenerated > 0) (unbilledKwhLoss / totalKwhGenerated) * 100.0 else 0.0

        // 2. Revenues
        val totalBilledRevenue = invoices.sumOf { it.totalAmount }
        val totalCollectedCash = invoices.sumOf { it.paidAmount }
        val totalPendingReceivables = (totalBilledRevenue - totalCollectedCash).coerceAtLeast(0.0)
        val paidInvoicesCount = invoices.count { it.paymentStatus == "PAID" }

        // 3. Expenses Breakdown
        var fuelExpenses = 0.0
        var salaryExpenses = 0.0
        var maintenanceExpenses = 0.0
        var oilFiltersExpenses = 0.0
        var sparePartsExpenses = 0.0
        var otherExpenses = 0.0

        for (exp in expenses) {
            when (exp.category.uppercase()) {
                "FUEL" -> fuelExpenses += exp.amount
                "SALARY" -> salaryExpenses += exp.amount
                "MAINTENANCE" -> maintenanceExpenses += exp.amount
                "OIL_FILTERS" -> oilFiltersExpenses += exp.amount
                "SPARE_PARTS" -> sparePartsExpenses += exp.amount
                else -> otherExpenses += exp.amount
            }
        }
        val totalExpenses = expenses.sumOf { it.amount }

        // 4. Fuel Metrics
        val totalFuelLitersPurchased = fuelRecords.sumOf { it.litersAdded }
        val totalFuelLitersConsumedFromLogs = productionLogs.sumOf { it.fuelConsumedLiters }
        val totalFuelLitersConsumed = if (totalFuelLitersConsumedFromLogs > 0) totalFuelLitersConsumedFromLogs else totalFuelLitersPurchased
        val litersPerKwhGenerated = if (totalKwhGenerated > 0) totalFuelLitersConsumed / totalKwhGenerated else 0.0
        val kwhGeneratedPerLiter = if (totalFuelLitersConsumed > 0) totalKwhGenerated / totalFuelLitersConsumed else 0.0
        val fuelCostPerKwh = if (totalKwhSold > 0) fuelExpenses / totalKwhSold else 0.0

        // 5. Key Unit Cost & Margin Metrics
        // Unit Cost per kWh on the station = Total Expenses / Total kWh Sold
        val costPerKwh = if (totalKwhSold > 0) totalExpenses / totalKwhSold else 0.0

        // Average Selling Price per kWh
        val averageSellingPricePerKwh = if (totalKwhSold > 0) {
            invoices.sumOf { it.energyCost } / totalKwhSold
        } else {
            config.defaultPricePerKwh
        }

        // Profit Margin per kWh
        val profitMarginPerKwh = averageSellingPricePerKwh - costPerKwh
        val profitMarginPercentage = if (averageSellingPricePerKwh > 0) (profitMarginPerKwh / averageSellingPricePerKwh) * 100.0 else 0.0

        // 6. Net Profit
        val netProfitOnBilled = totalBilledRevenue - totalExpenses
        val netProfitOnCollected = totalCollectedCash - totalExpenses

        return FinancialAnalytics(
            period = period,
            currency = currency,
            totalKwhGenerated = totalKwhGenerated,
            totalKwhSold = totalKwhSold,
            unbilledKwhLoss = unbilledKwhLoss,
            networkLossPercentage = networkLossPercentage,
            totalBilledRevenue = totalBilledRevenue,
            totalCollectedCash = totalCollectedCash,
            totalPendingReceivables = totalPendingReceivables,
            totalInvoicesCount = invoices.size,
            paidInvoicesCount = paidInvoicesCount,
            totalExpenses = totalExpenses,
            fuelExpenses = fuelExpenses,
            salaryExpenses = salaryExpenses,
            maintenanceExpenses = maintenanceExpenses,
            oilFiltersExpenses = oilFiltersExpenses,
            sparePartsExpenses = sparePartsExpenses,
            otherExpenses = otherExpenses,
            totalFuelLitersPurchased = totalFuelLitersPurchased,
            totalFuelLitersConsumed = totalFuelLitersConsumed,
            litersPerKwhGenerated = litersPerKwhGenerated,
            kwhGeneratedPerLiter = kwhGeneratedPerLiter,
            fuelCostPerKwh = fuelCostPerKwh,
            costPerKwh = costPerKwh,
            averageSellingPricePerKwh = averageSellingPricePerKwh,
            profitMarginPerKwh = profitMarginPerKwh,
            profitMarginPercentage = profitMarginPercentage,
            netProfitOnBilled = netProfitOnBilled,
            netProfitOnCollected = netProfitOnCollected
        )
    }

    // Actions
    fun updateConfig(config: StationConfigEntity) {
        viewModelScope.launch {
            repository.updateStationConfig(config)
        }
    }

    fun updateStationConfig(config: StationConfigEntity) {
        updateConfig(config)
    }

    fun addExpense(
        title: String,
        amount: Double,
        category: ExpenseCategory,
        payee: String,
        paymentMethod: String,
        generatorId: Long?,
        notes: String
    ) {
        viewModelScope.launch {
            repository.addExpense(
                ExpenseEntity(
                    title = title,
                    amount = amount,
                    category = category.name,
                    dateTimestamp = System.currentTimeMillis(),
                    payee = payee,
                    paymentMethod = paymentMethod,
                    generatorId = generatorId,
                    notes = notes
                )
            )
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun addFuelRecord(
        litersAdded: Double,
        costPerLiter: Double,
        supplier: String,
        invoiceNumber: String,
        generatorId: Long?,
        notes: String
    ) {
        viewModelScope.launch {
            val totalCost = litersAdded * costPerLiter
            repository.recordFuelRefill(
                FuelRecordEntity(
                    litersAdded = litersAdded,
                    costPerLiter = costPerLiter,
                    totalCost = totalCost,
                    supplier = supplier,
                    invoiceNumber = invoiceNumber,
                    generatorId = generatorId,
                    dateTimestamp = System.currentTimeMillis(),
                    notes = notes
                ),
                createExpense = true
            )
        }
    }

    fun addFuelRefill(
        litersAdded: Double,
        costPerLiter: Double,
        supplier: String,
        invoiceNumber: String,
        generatorId: Long?,
        notes: String
    ) {
        addFuelRecord(litersAdded, costPerLiter, supplier, invoiceNumber, generatorId, notes)
    }

    fun deleteFuelRecord(fuelRecord: FuelRecordEntity) {
        viewModelScope.launch {
            repository.deleteFuelRecord(fuelRecord)
        }
    }

    fun addGenerator(
        name: String,
        model: String,
        powerKva: Double,
        estimatedFuelLitersPerHour: Double,
        oilChangeIntervalHours: Double,
        initialHours: Double = 0.0,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.addGenerator(
                GeneratorEntity(
                    name = name,
                    model = model,
                    powerKva = powerKva,
                    estimatedFuelLitersPerHour = estimatedFuelLitersPerHour,
                    oilChangeIntervalHours = oilChangeIntervalHours,
                    totalRunHours = initialHours,
                    lastMaintenanceHours = initialHours,
                    status = "RUNNING",
                    notes = notes
                )
            )
        }
    }

    fun updateGeneratorStatus(generator: GeneratorEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateGenerator(generator.copy(status = newStatus))
        }
    }

    fun recordGeneratorMaintenance(generator: GeneratorEntity, currentHours: Double) {
        viewModelScope.launch {
            repository.updateGenerator(
                generator.copy(
                    totalRunHours = currentHours,
                    lastMaintenanceHours = currentHours
                )
            )
        }
    }

    fun recordOilChange(generator: GeneratorEntity) {
        recordGeneratorMaintenance(generator, generator.totalRunHours)
    }

    fun deleteGenerator(generator: GeneratorEntity) {
        viewModelScope.launch {
            repository.deleteGenerator(generator)
        }
    }

    fun addEmployee(
        name: String,
        jobTitle: String,
        phone: String,
        monthlySalary: Double,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.addEmployee(
                EmployeeEntity(
                    name = name,
                    jobTitle = jobTitle,
                    phone = phone,
                    monthlySalary = monthlySalary,
                    notes = notes
                )
            )
        }
    }

    fun deleteEmployee(employee: EmployeeEntity) {
        viewModelScope.launch {
            repository.deleteEmployee(employee)
        }
    }

    fun recordPayroll(
        employee: EmployeeEntity,
        monthYear: String,
        baseSalary: Double,
        bonus: Double,
        deductions: Double,
        notes: String
    ) {
        viewModelScope.launch {
            val net = (baseSalary + bonus - deductions).coerceAtLeast(0.0)
            repository.recordPayroll(
                PayrollEntity(
                    employeeId = employee.id,
                    employeeName = employee.name,
                    monthYear = monthYear,
                    baseSalary = baseSalary,
                    bonus = bonus,
                    deductions = deductions,
                    netSalary = net,
                    isPaid = true,
                    paymentDateTimestamp = System.currentTimeMillis(),
                    notes = notes
                ),
                createExpense = true
            )
        }
    }

    fun deletePayroll(payroll: PayrollEntity) {
        viewModelScope.launch {
            repository.deletePayroll(payroll)
        }
    }

    fun addSubscriber(
        name: String,
        code: String,
        phone: String,
        sectorAddress: String,
        meterNumber: String,
        customPricePerKwh: Double?,
        customFixedFee: Double?,
        initialReading: Double,
        notes: String
    ) {
        viewModelScope.launch {
            repository.addSubscriber(
                SubscriberEntity(
                    name = name,
                    subscriberCode = code,
                    phone = phone,
                    sectorAddress = sectorAddress,
                    meterNumber = meterNumber,
                    customPricePerKwh = customPricePerKwh,
                    customFixedFee = customFixedFee,
                    lastReadingValue = initialReading,
                    balance = 0.0,
                    status = "ACTIVE",
                    notes = notes
                )
            )
        }
    }

    fun updateSubscriber(subscriber: SubscriberEntity) {
        viewModelScope.launch {
            repository.updateSubscriber(subscriber)
        }
    }

    fun deleteSubscriber(subscriber: SubscriberEntity) {
        viewModelScope.launch {
            repository.deleteSubscriber(subscriber)
        }
    }

    fun recordMeterInvoice(
        subscriber: SubscriberEntity,
        currentReading: Double,
        paidImmediately: Boolean,
        notes: String
    ) {
        viewModelScope.launch {
            val cfg = config.value ?: StationConfigEntity()
            val previousReading = subscriber.lastReadingValue
            val consumptionKwh = (currentReading - previousReading).coerceAtLeast(0.0)
            val pricePerKwh = subscriber.customPricePerKwh ?: cfg.defaultPricePerKwh
            val fixedFee = subscriber.customFixedFee ?: cfg.defaultFixedSubscriptionFee
            val energyCost = consumptionKwh * pricePerKwh
            val totalAmount = energyCost + fixedFee
            val paidAmount = if (paidImmediately) totalAmount else 0.0
            val status = if (paidImmediately) "PAID" else "UNPAID"

            val cal = Calendar.getInstance()
            val currentMonth = String.format("%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
            val invoiceNumber = "INV-${System.currentTimeMillis().toString().takeLast(6)}"

            val invoice = MeterInvoiceEntity(
                subscriberId = subscriber.id,
                subscriberName = subscriber.name,
                meterNumber = subscriber.meterNumber,
                invoiceNumber = invoiceNumber,
                previousReading = previousReading,
                currentReading = currentReading,
                consumptionKwh = consumptionKwh,
                pricePerKwh = pricePerKwh,
                fixedFee = fixedFee,
                energyCost = energyCost,
                totalAmount = totalAmount,
                paidAmount = paidAmount,
                paymentStatus = status,
                invoiceDateTimestamp = System.currentTimeMillis(),
                paymentDateTimestamp = if (paidImmediately) System.currentTimeMillis() else null,
                periodMonthYear = currentMonth,
                notes = notes
            )
            repository.recordMeterInvoice(invoice)
        }
    }

    fun recordMeterReading(
        subscriber: SubscriberEntity,
        currentReading: Double,
        paidImmediately: Boolean,
        notes: String
    ) {
        recordMeterInvoice(subscriber, currentReading, paidImmediately, notes)
    }

    fun payInvoice(invoice: MeterInvoiceEntity, amountToPay: Double) {
        viewModelScope.launch {
            repository.markInvoicePaid(invoice, amountToPay)
        }
    }

    fun deleteInvoice(invoice: MeterInvoiceEntity) {
        viewModelScope.launch {
            repository.deleteInvoice(invoice)
        }
    }

    fun recordProductionLog(
        generator: GeneratorEntity,
        runHours: Double,
        kwhProduced: Double,
        fuelConsumedLiters: Double,
        notes: String
    ) {
        viewModelScope.launch {
            repository.recordProductionLog(
                ProductionLogEntity(
                    generatorId = generator.id,
                    generatorName = generator.name,
                    runHours = runHours,
                    kwhProduced = kwhProduced,
                    fuelConsumedLiters = fuelConsumedLiters,
                    logDateTimestamp = System.currentTimeMillis(),
                    notes = notes
                )
            )
            // Update generator total run hours
            repository.updateGenerator(
                generator.copy(
                    totalRunHours = generator.totalRunHours + runHours
                )
            )
        }
    }

    companion object {
        fun isTimestampInPeriod(timestamp: Long, period: DateFilterPeriod): Boolean {
            if (period == DateFilterPeriod.ALL) return true
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply { timeInMillis = timestamp }

            return when (period) {
                DateFilterPeriod.ALL -> true
                DateFilterPeriod.TODAY -> {
                    now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
                }
                DateFilterPeriod.THIS_WEEK -> {
                    now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                    now.get(Calendar.WEEK_OF_YEAR) == target.get(Calendar.WEEK_OF_YEAR)
                }
                DateFilterPeriod.THIS_MONTH -> {
                    now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                    now.get(Calendar.MONTH) == target.get(Calendar.MONTH)
                }
                DateFilterPeriod.LAST_MONTH -> {
                    val prevMonth = (now.get(Calendar.MONTH) - 1 + 12) % 12
                    val expectedYear = if (now.get(Calendar.MONTH) == 0) now.get(Calendar.YEAR) - 1 else now.get(Calendar.YEAR)
                    target.get(Calendar.YEAR) == expectedYear && target.get(Calendar.MONTH) == prevMonth
                }
                DateFilterPeriod.THIS_YEAR -> {
                    now.get(Calendar.YEAR) == target.get(Calendar.YEAR)
                }
            }
        }
    }
}

class PowerStationViewModelFactory(
    private val repository: PowerStationRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PowerStationViewModel::class.java)) {
            return PowerStationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
