package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "station_config")
data class StationConfigEntity(
    @PrimaryKey val id: Int = 1,
    val stationName: String = "محطة النور لتوليد وتوزيع الطاقة الكهربائية",
    val ownerName: String = "المدير العام",
    val phone: String = "07700000000",
    val location: String = "المنطقة الرئيسية",
    val currency: String = "د.ع",
    val defaultPricePerKwh: Double = 150.0,
    val defaultFixedSubscriptionFee: Double = 5000.0,
    val fuelTankCapacityLiters: Double = 10000.0,
    val currentFuelInTankLiters: Double = 4500.0,
    val alertFuelLevelLiters: Double = 1000.0
)

@Entity(tableName = "generators")
data class GeneratorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val model: String = "",
    val powerKva: Double = 500.0,
    val estimatedFuelLitersPerHour: Double = 35.0,
    val totalRunHours: Double = 0.0,
    val lastMaintenanceHours: Double = 0.0,
    val oilChangeIntervalHours: Double = 250.0,
    val status: String = "RUNNING", // RUNNING, STOPPED, MAINTENANCE
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String, // FUEL, SALARY, MAINTENANCE, OIL_FILTERS, SPARE_PARTS, RENT_UTILITY, ADMIN_MISC
    val dateTimestamp: Long = System.currentTimeMillis(),
    val payee: String = "", // المستلم / الجهة
    val paymentMethod: String = "نقدي", // نقدي, تحويل, آجل
    val generatorId: Long? = null, // إذا كان المصروف خاص بمولد معين
    val notes: String = "",
    val referenceId: String = "" // رقم الفاتورة أو الإيصال
)

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val jobTitle: String, // مشغل محطة, فني كهربائي, قارئ ومحصل عدادات, حارس, ميكانيكي, محاسب
    val phone: String = "",
    val monthlySalary: Double,
    val isActive: Boolean = true,
    val hireDateTimestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "payrolls")
data class PayrollEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val employeeName: String,
    val monthYear: String, // e.g. "2026-08"
    val baseSalary: Double,
    val bonus: Double = 0.0,
    val deductions: Double = 0.0,
    val netSalary: Double,
    val isPaid: Boolean = true,
    val paymentDateTimestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "subscribers")
data class SubscriberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val subscriberCode: String,
    val phone: String = "",
    val sectorAddress: String = "",
    val meterNumber: String = "",
    val customPricePerKwh: Double? = null, // null means use station default
    val customFixedFee: Double? = null,
    val lastReadingValue: Double = 0.0,
    val lastReadingDateTimestamp: Long = System.currentTimeMillis(),
    val balance: Double = 0.0, // الرصيد المطلوب / الديون السابقة
    val status: String = "ACTIVE", // ACTIVE, SUSPENDED, DISCONNECTED
    val registeredDateTimestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "meter_invoices")
data class MeterInvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subscriberId: Long,
    val subscriberName: String,
    val meterNumber: String,
    val invoiceNumber: String,
    val previousReading: Double,
    val currentReading: Double,
    val consumptionKwh: Double, // current - previous
    val pricePerKwh: Double,
    val fixedFee: Double = 0.0,
    val energyCost: Double, // consumptionKwh * pricePerKwh
    val totalAmount: Double, // energyCost + fixedFee
    val paidAmount: Double = 0.0,
    val paymentStatus: String = "UNPAID", // PAID, PARTIAL, UNPAID
    val invoiceDateTimestamp: Long = System.currentTimeMillis(),
    val paymentDateTimestamp: Long? = null,
    val periodMonthYear: String = "", // e.g. "2026-08"
    val notes: String = ""
)

@Entity(tableName = "fuel_records")
data class FuelRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val litersAdded: Double,
    val costPerLiter: Double,
    val totalCost: Double,
    val supplier: String = "",
    val invoiceNumber: String = "",
    val generatorId: Long? = null,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "production_logs")
data class ProductionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val generatorId: Long,
    val generatorName: String,
    val runHours: Double,
    val kwhProduced: Double,
    val fuelConsumedLiters: Double,
    val logDateTimestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)
