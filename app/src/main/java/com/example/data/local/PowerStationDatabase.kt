package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.PowerStationDao
import com.example.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [
        StationConfigEntity::class,
        GeneratorEntity::class,
        ExpenseEntity::class,
        EmployeeEntity::class,
        PayrollEntity::class,
        SubscriberEntity::class,
        MeterInvoiceEntity::class,
        FuelRecordEntity::class,
        ProductionLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PowerStationDatabase : RoomDatabase() {
    abstract fun powerStationDao(): PowerStationDao
    fun dao(): PowerStationDao = powerStationDao()

    companion object {
        @Volatile
        private var INSTANCE: PowerStationDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
        ): PowerStationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PowerStationDatabase::class.java,
                    "power_station_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(PowerStationDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class PowerStationDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialStationData(database.powerStationDao())
                    }
                }
            }
        }

        private suspend fun populateInitialStationData(dao: PowerStationDao) {
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance()
            
            // 1. Station Config
            dao.insertOrUpdateConfig(
                StationConfigEntity(
                    id = 1,
                    stationName = "محطة الرافدين لتوليد وتوزيع الكهرباء",
                    ownerName = "أبو فهد المهندس",
                    phone = "07801234567",
                    location = "المنطقة الصناعية - المحطة المركزية",
                    currency = "د.ع",
                    defaultPricePerKwh = 150.0,
                    defaultFixedSubscriptionFee = 5000.0,
                    fuelTankCapacityLiters = 12000.0,
                    currentFuelInTankLiters = 6800.0,
                    alertFuelLevelLiters = 2000.0
                )
            )

            // 2. Generators
            val gen1Id = dao.insertGenerator(
                GeneratorEntity(
                    name = "المولد الرئيسي - كاتربيلر CAT C18",
                    model = "Caterpillar 600 kVA",
                    powerKva = 600.0,
                    estimatedFuelLitersPerHour = 42.0,
                    totalRunHours = 1840.0,
                    lastMaintenanceHours = 1750.0,
                    oilChangeIntervalHours = 250.0,
                    status = "RUNNING",
                    notes = "توليد الحمل الرئيسي للحي السكني والمحلات"
                )
            )

            val gen2Id = dao.insertGenerator(
                GeneratorEntity(
                    name = "المولد الاحتياطي - بيركنز Perkins 350",
                    model = "Perkins 350 kVA",
                    powerKva = 350.0,
                    estimatedFuelLitersPerHour = 26.0,
                    totalRunHours = 960.0,
                    lastMaintenanceHours = 900.0,
                    oilChangeIntervalHours = 250.0,
                    status = "STOPPED",
                    notes = "جاهز للتشغيل في ساعات الذروة المسائية"
                )
            )

            // 3. Employees
            val emp1Id = dao.insertEmployee(
                EmployeeEntity(
                    name = "م. حيدر العراقي",
                    jobTitle = "مشغل رئيسي وفني ميكانيك",
                    phone = "07711223344",
                    monthlySalary = 750000.0,
                    isActive = true
                )
            )

            val emp2Id = dao.insertEmployee(
                EmployeeEntity(
                    name = "علي كاظم",
                    jobTitle = "قارئ ومحصل عدادات المشتركين",
                    phone = "07722334455",
                    monthlySalary = 500000.0,
                    isActive = true
                )
            )

            val emp3Id = dao.insertEmployee(
                EmployeeEntity(
                    name = "حسين جاسم",
                    jobTitle = "فني شبكات وتمديدات كهربائية",
                    phone = "07733445566",
                    monthlySalary = 600000.0,
                    isActive = true
                )
            )

            // 4. Current Month Payrolls
            val currentMonth = String.format("%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
            dao.insertPayroll(
                PayrollEntity(
                    employeeId = emp1Id,
                    employeeName = "م. حيدر العراقي",
                    monthYear = currentMonth,
                    baseSalary = 750000.0,
                    bonus = 50000.0,
                    deductions = 0.0,
                    netSalary = 800000.0,
                    isPaid = true,
                    paymentDateTimestamp = now - 86400000L * 5,
                    notes = "راتب الشهر الحالي مع مكافأة وردية إضافية"
                )
            )

            dao.insertPayroll(
                PayrollEntity(
                    employeeId = emp2Id,
                    employeeName = "علي كاظم",
                    monthYear = currentMonth,
                    baseSalary = 500000.0,
                    bonus = 25000.0,
                    deductions = 0.0,
                    netSalary = 525000.0,
                    isPaid = true,
                    paymentDateTimestamp = now - 86400000L * 5,
                    notes = "راتب الشهر الحالي مع حافز تحصيل ممتاز"
                )
            )

            dao.insertPayroll(
                PayrollEntity(
                    employeeId = emp3Id,
                    employeeName = "حسين جاسم",
                    monthYear = currentMonth,
                    baseSalary = 600000.0,
                    bonus = 0.0,
                    deductions = 0.0,
                    netSalary = 600000.0,
                    isPaid = true,
                    paymentDateTimestamp = now - 86400000L * 5,
                    notes = "راتب الشهر الحالي"
                )
            )

            // 5. Fuel Refill Records
            dao.insertFuelRecord(
                FuelRecordEntity(
                    litersAdded = 4000.0,
                    costPerLiter = 750.0,
                    totalCost = 3000000.0,
                    supplier = "شركة النفط الوطنية - صهريج 104",
                    invoiceNumber = "FL-8921",
                    generatorId = gen1Id,
                    dateTimestamp = now - 86400000L * 12,
                    notes = "تفريغ ديزل أصلي عالي النقاوة بالخزان الرئيسي"
                )
            )

            dao.insertFuelRecord(
                FuelRecordEntity(
                    litersAdded = 3000.0,
                    costPerLiter = 760.0,
                    totalCost = 2280000.0,
                    supplier = "محطة التجهيز المركزية",
                    invoiceNumber = "FL-9042",
                    generatorId = gen1Id,
                    dateTimestamp = now - 86400000L * 2,
                    notes = "دفعة وقود إضافية لمنتصف الشهر"
                )
            )

            // 6. Expenses (Fuel, Maintenance, Salaries, Oil/Filters, Misc)
            dao.insertExpense(
                ExpenseEntity(
                    title = "شراء وقود ديزل (صهريج 4,000 لتر)",
                    amount = 3000000.0,
                    category = "FUEL",
                    dateTimestamp = now - 86400000L * 12,
                    payee = "شركة النفط",
                    paymentMethod = "نقدي",
                    notes = "دفعة ديزل أولى"
                )
            )

            dao.insertExpense(
                ExpenseEntity(
                    title = "شراء وقود ديزل (صهريج 3,000 لتر)",
                    amount = 2280000.0,
                    category = "FUEL",
                    dateTimestamp = now - 86400000L * 2,
                    payee = "محطة التجهيز",
                    paymentMethod = "نقدي",
                    notes = "دفعة ديزل ثانية"
                )
            )

            dao.insertExpense(
                ExpenseEntity(
                    title = "صرف مرتبات كادر المحطة (3 عمال)",
                    amount = 1925000.0,
                    category = "SALARY",
                    dateTimestamp = now - 86400000L * 5,
                    payee = "كادر التشغيل والصيانة",
                    paymentMethod = "نقدي",
                    notes = "رواتب شهرية شاملة المكافآت"
                )
            )

            dao.insertExpense(
                ExpenseEntity(
                    title = "صيانة عطل شاحن الدينامو وتبديل سير المولد",
                    amount = 340000.0,
                    category = "MAINTENANCE",
                    dateTimestamp = now - 86400000L * 8,
                    payee = "ورشة الهدى الميكانيكية",
                    paymentMethod = "نقدي",
                    generatorId = gen1Id,
                    notes = "إصلاح عطل طارئ وتغيير السيور والمشدات"
                )
            )

            dao.insertExpense(
                ExpenseEntity(
                    title = "تبديل زيت المحرك (برميل ديزل موبيل) وفلاتر الديزل والزيت",
                    amount = 450000.0,
                    category = "OIL_FILTERS",
                    dateTimestamp = now - 86400000L * 14,
                    payee = "وكالة الزيوت الأصلية",
                    paymentMethod = "نقدي",
                    generatorId = gen1Id,
                    notes = "صيانة دورية عند 1750 ساعة"
                )
            )

            dao.insertExpense(
                ExpenseEntity(
                    title = "قواطع دورة كهربائية 250A وكابلات تمديد للمشتركين",
                    amount = 260000.0,
                    category = "SPARE_PARTS",
                    dateTimestamp = now - 86400000L * 6,
                    payee = "مكتب النجوم للمواد الكهربائية",
                    paymentMethod = "نقدي",
                    notes = "تحديث لوحة التوزيع الفرعية"
                )
            )

            dao.insertExpense(
                ExpenseEntity(
                    title = "أجور أرضية المحطة وإيجار الموقع",
                    amount = 400000.0,
                    category = "RENT_UTILITY",
                    dateTimestamp = now - 86400000L * 10,
                    payee = "صاحب الأرض",
                    paymentMethod = "نقدي",
                    notes = "إيجار الموقع للشهر الحالي"
                )
            )

            // 7. Subscribers
            val sub1Id = dao.insertSubscriber(
                SubscriberEntity(
                    name = "سوبرماركت البركة (حاج كريم)",
                    subscriberCode = "SUB-101",
                    phone = "07701112222",
                    sectorAddress = "الشارع التجاري - بناية 4",
                    meterNumber = "MTR-9901",
                    customPricePerKwh = 150.0,
                    customFixedFee = 5000.0,
                    lastReadingValue = 18450.0,
                    status = "ACTIVE",
                    balance = 0.0
                )
            )

            val sub2Id = dao.insertSubscriber(
                SubscriberEntity(
                    name = "مخبز وأفران السلام",
                    subscriberCode = "SUB-102",
                    phone = "07702223333",
                    sectorAddress = "السوق المركزي",
                    meterNumber = "MTR-9902",
                    customPricePerKwh = 150.0,
                    customFixedFee = 5000.0,
                    lastReadingValue = 34200.0,
                    status = "ACTIVE",
                    balance = 0.0
                )
            )

            val sub3Id = dao.insertSubscriber(
                SubscriberEntity(
                    name = "مجمع عيادات الشفاء",
                    subscriberCode = "SUB-103",
                    phone = "07703334444",
                    sectorAddress = "شارع الأطباء",
                    meterNumber = "MTR-9903",
                    customPricePerKwh = 150.0,
                    customFixedFee = 5000.0,
                    lastReadingValue = 22100.0,
                    status = "ACTIVE",
                    balance = 0.0
                )
            )

            val sub4Id = dao.insertSubscriber(
                SubscriberEntity(
                    name = "منزل الأستاذ أحمد مهدي",
                    subscriberCode = "SUB-104",
                    phone = "07704445555",
                    sectorAddress = "الحي السكني - زقاق 12",
                    meterNumber = "MTR-9904",
                    customPricePerKwh = 150.0,
                    customFixedFee = 5000.0,
                    lastReadingValue = 14350.0,
                    status = "ACTIVE",
                    balance = 0.0
                )
            )

            val sub5Id = dao.insertSubscriber(
                SubscriberEntity(
                    name = "منزل الحاج أبو عمار",
                    subscriberCode = "SUB-105",
                    phone = "07705556666",
                    sectorAddress = "الحي السكني - زقاق 14",
                    meterNumber = "MTR-9905",
                    customPricePerKwh = 150.0,
                    customFixedFee = 5000.0,
                    lastReadingValue = 9820.0,
                    status = "ACTIVE",
                    balance = 0.0
                )
            )

            val sub6Id = dao.insertSubscriber(
                SubscriberEntity(
                    name = "ورشة النجارة الحديثة",
                    subscriberCode = "SUB-106",
                    phone = "07706667777",
                    sectorAddress = "المنطقة الصناعية الفرعية",
                    meterNumber = "MTR-9906",
                    customPricePerKwh = 160.0,
                    customFixedFee = 8000.0,
                    lastReadingValue = 27600.0,
                    status = "ACTIVE",
                    balance = 0.0
                )
            )

            val sub7Id = dao.insertSubscriber(
                SubscriberEntity(
                    name = "كافيه ومطعم ريتاج",
                    subscriberCode = "SUB-107",
                    phone = "07707778888",
                    sectorAddress = "شارع المشجر الرئيسي",
                    meterNumber = "MTR-9907",
                    customPricePerKwh = 150.0,
                    customFixedFee = 5000.0,
                    lastReadingValue = 19800.0,
                    status = "ACTIVE",
                    balance = 0.0
                )
            )

            // 8. Meter Invoices (Electricity Sales)
            // Sub 1: 18450 - 15200 = 3250 kWh
            dao.insertInvoice(
                MeterInvoiceEntity(
                    subscriberId = sub1Id,
                    subscriberName = "سوبرماركت البركة (حاج كريم)",
                    meterNumber = "MTR-9901",
                    invoiceNumber = "INV-2026-001",
                    previousReading = 15200.0,
                    currentReading = 18450.0,
                    consumptionKwh = 3250.0,
                    pricePerKwh = 150.0,
                    fixedFee = 5000.0,
                    energyCost = 487500.0,
                    totalAmount = 492500.0,
                    paidAmount = 492500.0,
                    paymentStatus = "PAID",
                    invoiceDateTimestamp = now - 86400000L * 4,
                    paymentDateTimestamp = now - 86400000L * 3,
                    periodMonthYear = currentMonth
                )
            )

            // Sub 2: 34200 - 25800 = 8400 kWh (Bakery)
            dao.insertInvoice(
                MeterInvoiceEntity(
                    subscriberId = sub2Id,
                    subscriberName = "مخبز وأفران السلام",
                    meterNumber = "MTR-9902",
                    invoiceNumber = "INV-2026-002",
                    previousReading = 25800.0,
                    currentReading = 34200.0,
                    consumptionKwh = 8400.0,
                    pricePerKwh = 150.0,
                    fixedFee = 5000.0,
                    energyCost = 1260000.0,
                    totalAmount = 1265000.0,
                    paidAmount = 1265000.0,
                    paymentStatus = "PAID",
                    invoiceDateTimestamp = now - 86400000L * 4,
                    paymentDateTimestamp = now - 86400000L * 2,
                    periodMonthYear = currentMonth
                )
            )

            // Sub 3: 22100 - 17300 = 4800 kWh (Medical Clinics)
            dao.insertInvoice(
                MeterInvoiceEntity(
                    subscriberId = sub3Id,
                    subscriberName = "مجمع عيادات الشفاء",
                    meterNumber = "MTR-9903",
                    invoiceNumber = "INV-2026-003",
                    previousReading = 17300.0,
                    currentReading = 22100.0,
                    consumptionKwh = 4800.0,
                    pricePerKwh = 150.0,
                    fixedFee = 5000.0,
                    energyCost = 720000.0,
                    totalAmount = 725000.0,
                    paidAmount = 725000.0,
                    paymentStatus = "PAID",
                    invoiceDateTimestamp = now - 86400000L * 4,
                    paymentDateTimestamp = now - 86400000L * 2,
                    periodMonthYear = currentMonth
                )
            )

            // Sub 4: 14350 - 12900 = 1450 kWh (House)
            dao.insertInvoice(
                MeterInvoiceEntity(
                    subscriberId = sub4Id,
                    subscriberName = "منزل الأستاذ أحمد مهدي",
                    meterNumber = "MTR-9904",
                    invoiceNumber = "INV-2026-004",
                    previousReading = 12900.0,
                    currentReading = 14350.0,
                    consumptionKwh = 1450.0,
                    pricePerKwh = 150.0,
                    fixedFee = 5000.0,
                    energyCost = 217500.0,
                    totalAmount = 222500.0,
                    paidAmount = 222500.0,
                    paymentStatus = "PAID",
                    invoiceDateTimestamp = now - 86400000L * 3,
                    paymentDateTimestamp = now - 86400000L * 1,
                    periodMonthYear = currentMonth
                )
            )

            // Sub 5: 9820 - 8700 = 1120 kWh (House)
            dao.insertInvoice(
                MeterInvoiceEntity(
                    subscriberId = sub5Id,
                    subscriberName = "منزل الحاج أبو عمار",
                    meterNumber = "MTR-9905",
                    invoiceNumber = "INV-2026-005",
                    previousReading = 8700.0,
                    currentReading = 9820.0,
                    consumptionKwh = 1120.0,
                    pricePerKwh = 150.0,
                    fixedFee = 5000.0,
                    energyCost = 168000.0,
                    totalAmount = 173000.0,
                    paidAmount = 100000.0,
                    paymentStatus = "PARTIAL",
                    invoiceDateTimestamp = now - 86400000L * 3,
                    paymentDateTimestamp = now - 86400000L * 1,
                    periodMonthYear = currentMonth
                )
            )

            // Sub 6: 27600 - 21200 = 6400 kWh (Workshop)
            dao.insertInvoice(
                MeterInvoiceEntity(
                    subscriberId = sub6Id,
                    subscriberName = "ورشة النجارة الحديثة",
                    meterNumber = "MTR-9906",
                    invoiceNumber = "INV-2026-006",
                    previousReading = 21200.0,
                    currentReading = 27600.0,
                    consumptionKwh = 6400.0,
                    pricePerKwh = 160.0,
                    fixedFee = 8000.0,
                    energyCost = 1024000.0,
                    totalAmount = 1032000.0,
                    paidAmount = 1032000.0,
                    paymentStatus = "PAID",
                    invoiceDateTimestamp = now - 86400000L * 2,
                    paymentDateTimestamp = now - 86400000L * 1,
                    periodMonthYear = currentMonth
                )
            )

            // Sub 7: 19800 - 15300 = 4500 kWh (Cafe & Restaurant)
            dao.insertInvoice(
                MeterInvoiceEntity(
                    subscriberId = sub7Id,
                    subscriberName = "كافيه ومطعم ريتاج",
                    meterNumber = "MTR-9907",
                    invoiceNumber = "INV-2026-007",
                    previousReading = 15300.0,
                    currentReading = 19800.0,
                    consumptionKwh = 4500.0,
                    pricePerKwh = 150.0,
                    fixedFee = 5000.0,
                    energyCost = 675000.0,
                    totalAmount = 680000.0,
                    paidAmount = 680000.0,
                    paymentStatus = "PAID",
                    invoiceDateTimestamp = now - 86400000L * 2,
                    paymentDateTimestamp = now - 86400000L * 1,
                    periodMonthYear = currentMonth
                )
            )

            // 9. Production Logs
            dao.insertProductionLog(
                ProductionLogEntity(
                    generatorId = gen1Id,
                    generatorName = "المولد الرئيسي - كاتربيلر CAT C18",
                    runHours = 380.0,
                    kwhProduced = 32000.0,
                    fuelConsumedLiters = 5200.0,
                    logDateTimestamp = now - 86400000L * 1,
                    notes = "سجل التوليد لشهر العمل الحالي"
                )
            )
        }
    }
}
