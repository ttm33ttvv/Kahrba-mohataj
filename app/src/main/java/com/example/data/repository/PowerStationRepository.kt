package com.example.data.repository

import com.example.data.local.dao.PowerStationDao
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class PowerStationRepository(private val dao: PowerStationDao) {

    val stationConfig: Flow<StationConfigEntity?> = dao.getStationConfig()
    val allGenerators: Flow<List<GeneratorEntity>> = dao.getAllGenerators()
    val allExpenses: Flow<List<ExpenseEntity>> = dao.getAllExpenses()
    val allEmployees: Flow<List<EmployeeEntity>> = dao.getAllEmployees()
    val allPayrolls: Flow<List<PayrollEntity>> = dao.getAllPayrolls()
    val allSubscribers: Flow<List<SubscriberEntity>> = dao.getAllSubscribers()
    val allInvoices: Flow<List<MeterInvoiceEntity>> = dao.getAllInvoices()
    val allFuelRecords: Flow<List<FuelRecordEntity>> = dao.getAllFuelRecords()
    val allProductionLogs: Flow<List<ProductionLogEntity>> = dao.getAllProductionLogs()

    suspend fun updateStationConfig(config: StationConfigEntity) {
        dao.insertOrUpdateConfig(config)
    }

    // Generator Operations
    suspend fun addGenerator(generator: GeneratorEntity): Long {
        return dao.insertGenerator(generator)
    }

    suspend fun updateGenerator(generator: GeneratorEntity) {
        dao.updateGenerator(generator)
    }

    suspend fun deleteGenerator(generator: GeneratorEntity) {
        dao.deleteGenerator(generator)
    }

    // Expense Operations
    suspend fun addExpense(expense: ExpenseEntity): Long {
        return dao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: ExpenseEntity) {
        dao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        dao.deleteExpense(expense)
    }

    // Fuel Refill with automatic expense logging & tank update
    suspend fun recordFuelRefill(fuelRecord: FuelRecordEntity, createExpense: Boolean = true) {
        dao.insertFuelRecord(fuelRecord)
        if (createExpense) {
            val expense = ExpenseEntity(
                title = "شراء وقود ديزل (${String.format("%.0f", fuelRecord.litersAdded)} لتر)",
                amount = fuelRecord.totalCost,
                category = "FUEL",
                dateTimestamp = fuelRecord.dateTimestamp,
                payee = fuelRecord.supplier.ifBlank { "مورد الوقود" },
                paymentMethod = "نقدي",
                generatorId = fuelRecord.generatorId,
                notes = fuelRecord.notes,
                referenceId = fuelRecord.invoiceNumber
            )
            dao.insertExpense(expense)
        }

        // Update tank capacity
        val currentConfig = dao.getStationConfig().firstOrNull() ?: StationConfigEntity()
        val newFuel = (currentConfig.currentFuelInTankLiters + fuelRecord.litersAdded).coerceAtMost(currentConfig.fuelTankCapacityLiters)
        dao.insertOrUpdateConfig(currentConfig.copy(currentFuelInTankLiters = newFuel))
    }

    suspend fun deleteFuelRecord(fuelRecord: FuelRecordEntity) {
        dao.deleteFuelRecord(fuelRecord)
    }

    // Employee Operations
    suspend fun addEmployee(employee: EmployeeEntity): Long {
        return dao.insertEmployee(employee)
    }

    suspend fun updateEmployee(employee: EmployeeEntity) {
        dao.updateEmployee(employee)
    }

    suspend fun deleteEmployee(employee: EmployeeEntity) {
        dao.deleteEmployee(employee)
    }

    // Payroll Operations with optional auto-expense logging
    suspend fun recordPayroll(payroll: PayrollEntity, createExpense: Boolean = true) {
        dao.insertPayroll(payroll)
        if (createExpense && payroll.isPaid) {
            val expense = ExpenseEntity(
                title = "مرتب الموظف: ${payroll.employeeName} (${payroll.monthYear})",
                amount = payroll.netSalary,
                category = "SALARY",
                dateTimestamp = payroll.paymentDateTimestamp,
                payee = payroll.employeeName,
                paymentMethod = "نقدي",
                notes = payroll.notes
            )
            dao.insertExpense(expense)
        }
    }

    suspend fun updatePayroll(payroll: PayrollEntity) {
        dao.updatePayroll(payroll)
    }

    suspend fun deletePayroll(payroll: PayrollEntity) {
        dao.deletePayroll(payroll)
    }

    // Subscriber Operations
    suspend fun addSubscriber(subscriber: SubscriberEntity): Long {
        return dao.insertSubscriber(subscriber)
    }

    suspend fun updateSubscriber(subscriber: SubscriberEntity) {
        dao.updateSubscriber(subscriber)
    }

    suspend fun deleteSubscriber(subscriber: SubscriberEntity) {
        dao.deleteSubscriber(subscriber)
    }

    // Meter Invoices & Electricity Sales
    suspend fun recordMeterInvoice(invoice: MeterInvoiceEntity) {
        dao.insertInvoice(invoice)
        // Advance subscriber last reading and update balance if unpaid
        val subscriber = dao.getSubscriberById(invoice.subscriberId)
        if (subscriber != null) {
            val unpaidPortion = (invoice.totalAmount - invoice.paidAmount).coerceAtLeast(0.0)
            val updatedSubscriber = subscriber.copy(
                lastReadingValue = invoice.currentReading,
                lastReadingDateTimestamp = invoice.invoiceDateTimestamp,
                balance = subscriber.balance + unpaidPortion
            )
            dao.updateSubscriber(updatedSubscriber)
        }
    }

    suspend fun updateInvoice(invoice: MeterInvoiceEntity) {
        dao.updateInvoice(invoice)
    }

    suspend fun markInvoicePaid(invoice: MeterInvoiceEntity, amountPaid: Double) {
        val newPaid = (invoice.paidAmount + amountPaid).coerceAtMost(invoice.totalAmount)
        val status = if (newPaid >= invoice.totalAmount) "PAID" else "PARTIAL"
        val updated = invoice.copy(
            paidAmount = newPaid,
            paymentStatus = status,
            paymentDateTimestamp = System.currentTimeMillis()
        )
        dao.updateInvoice(updated)

        // Adjust subscriber balance
        val subscriber = dao.getSubscriberById(invoice.subscriberId)
        if (subscriber != null) {
            val newBalance = (subscriber.balance - amountPaid).coerceAtLeast(0.0)
            dao.updateSubscriber(subscriber.copy(balance = newBalance))
        }
    }

    suspend fun deleteInvoice(invoice: MeterInvoiceEntity) {
        dao.deleteInvoice(invoice)
    }

    // Production Logs
    suspend fun recordProductionLog(log: ProductionLogEntity) {
        dao.insertProductionLog(log)
        // Update generator run hours
        // and reduce fuel in tank
        val currentConfig = dao.getStationConfig().firstOrNull() ?: StationConfigEntity()
        val newFuel = (currentConfig.currentFuelInTankLiters - log.fuelConsumedLiters).coerceAtLeast(0.0)
        dao.insertOrUpdateConfig(currentConfig.copy(currentFuelInTankLiters = newFuel))
    }

    suspend fun deleteProductionLog(log: ProductionLogEntity) {
        dao.deleteProductionLog(log)
    }
}
