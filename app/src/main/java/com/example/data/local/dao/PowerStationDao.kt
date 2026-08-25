package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PowerStationDao {

    // Station Config
    @Query("SELECT * FROM station_config WHERE id = 1")
    fun getStationConfig(): Flow<StationConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConfig(config: StationConfigEntity)

    // Generators
    @Query("SELECT * FROM generators ORDER BY id ASC")
    fun getAllGenerators(): Flow<List<GeneratorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenerator(generator: GeneratorEntity): Long

    @Update
    suspend fun updateGenerator(generator: GeneratorEntity)

    @Delete
    suspend fun deleteGenerator(generator: GeneratorEntity)

    // Expenses
    @Query("SELECT * FROM expenses ORDER BY dateTimestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE dateTimestamp BETWEEN :startTime AND :endTime ORDER BY dateTimestamp DESC")
    fun getExpensesBetween(startTime: Long, endTime: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY dateTimestamp DESC")
    fun getExpensesByCategory(category: String): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    // Employees
    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getAllEmployees(): Flow<List<EmployeeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: EmployeeEntity): Long

    @Update
    suspend fun updateEmployee(employee: EmployeeEntity)

    @Delete
    suspend fun deleteEmployee(employee: EmployeeEntity)

    // Payrolls
    @Query("SELECT * FROM payrolls ORDER BY paymentDateTimestamp DESC")
    fun getAllPayrolls(): Flow<List<PayrollEntity>>

    @Query("SELECT * FROM payrolls WHERE monthYear = :monthYear ORDER BY employeeName ASC")
    fun getPayrollsByMonth(monthYear: String): Flow<List<PayrollEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayroll(payroll: PayrollEntity): Long

    @Update
    suspend fun updatePayroll(payroll: PayrollEntity)

    @Delete
    suspend fun deletePayroll(payroll: PayrollEntity)

    // Subscribers
    @Query("SELECT * FROM subscribers ORDER BY name ASC")
    fun getAllSubscribers(): Flow<List<SubscriberEntity>>

    @Query("SELECT * FROM subscribers WHERE id = :id")
    suspend fun getSubscriberById(id: Long): SubscriberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscriber(subscriber: SubscriberEntity): Long

    @Update
    suspend fun updateSubscriber(subscriber: SubscriberEntity)

    @Delete
    suspend fun deleteSubscriber(subscriber: SubscriberEntity)

    // Meter Invoices
    @Query("SELECT * FROM meter_invoices ORDER BY invoiceDateTimestamp DESC")
    fun getAllInvoices(): Flow<List<MeterInvoiceEntity>>

    @Query("SELECT * FROM meter_invoices WHERE subscriberId = :subscriberId ORDER BY invoiceDateTimestamp DESC")
    fun getInvoicesForSubscriber(subscriberId: Long): Flow<List<MeterInvoiceEntity>>

    @Query("SELECT * FROM meter_invoices WHERE invoiceDateTimestamp BETWEEN :startTime AND :endTime ORDER BY invoiceDateTimestamp DESC")
    fun getInvoicesBetween(startTime: Long, endTime: Long): Flow<List<MeterInvoiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: MeterInvoiceEntity): Long

    @Update
    suspend fun updateInvoice(invoice: MeterInvoiceEntity)

    @Delete
    suspend fun deleteInvoice(invoice: MeterInvoiceEntity)

    // Fuel Records
    @Query("SELECT * FROM fuel_records ORDER BY dateTimestamp DESC")
    fun getAllFuelRecords(): Flow<List<FuelRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuelRecord(record: FuelRecordEntity): Long

    @Delete
    suspend fun deleteFuelRecord(record: FuelRecordEntity)

    // Production Logs
    @Query("SELECT * FROM production_logs ORDER BY logDateTimestamp DESC")
    fun getAllProductionLogs(): Flow<List<ProductionLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductionLog(log: ProductionLogEntity): Long

    @Delete
    suspend fun deleteProductionLog(log: ProductionLogEntity)
}
