package com.example.data.model

enum class ExpenseCategory(val displayName: String, val iconName: String) {
    FUEL("وقود وديزل", "LocalGasStation"),
    SALARY("مرتبات وأجور العمال", "Badge"),
    MAINTENANCE("أعطال وصيانة المولدات", "Build"),
    OIL_FILTERS("زيوت وفلاتر", "Opacity"),
    SPARE_PARTS("قطع غيار ومعدات", "Handyman"),
    RENT_UTILITY("إيجارات وفواتير", "HomeWork"),
    ADMIN_MISC("مصاريف إدارية وأخرى", "ReceiptLong");

    companion object {
        fun fromString(value: String): ExpenseCategory {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: ADMIN_MISC
        }
    }
}

enum class GeneratorStatus(val displayName: String) {
    RUNNING("يعمل حالياً"),
    STOPPED("متوقف"),
    MAINTENANCE("تحت الصيانة")
}

enum class PaymentStatus(val displayName: String) {
    PAID("مسدد بالكامل"),
    PARTIAL("مسدد جزئياً"),
    UNPAID("غير مسدد")
}

enum class SubscriberStatus(val displayName: String) {
    ACTIVE("نشط / متصل"),
    SUSPENDED("مفصول مؤقتاً"),
    DISCONNECTED("ملغي الاشتراك")
}

enum class DateFilterPeriod(val displayName: String) {
    ALL("كل الفترات"),
    TODAY("اليوم"),
    THIS_WEEK("هذا الأسبوع"),
    THIS_MONTH("هذا الشهر"),
    LAST_MONTH("الشهر الماضي"),
    THIS_YEAR("هذه السنة")
}

data class FinancialAnalytics(
    val period: DateFilterPeriod = DateFilterPeriod.THIS_MONTH,
    val currency: String = "د.ع",
    
    // Power Generation & Sales
    val totalKwhGenerated: Double = 0.0,
    val totalKwhSold: Double = 0.0,
    val unbilledKwhLoss: Double = 0.0,
    val networkLossPercentage: Double = 0.0,
    
    // Revenue & Invoices
    val totalBilledRevenue: Double = 0.0,
    val totalCollectedCash: Double = 0.0,
    val totalPendingReceivables: Double = 0.0,
    val totalInvoicesCount: Int = 0,
    val paidInvoicesCount: Int = 0,
    
    // Expenses Breakdown
    val totalExpenses: Double = 0.0,
    val fuelExpenses: Double = 0.0,
    val salaryExpenses: Double = 0.0,
    val maintenanceExpenses: Double = 0.0,
    val oilFiltersExpenses: Double = 0.0,
    val sparePartsExpenses: Double = 0.0,
    val otherExpenses: Double = 0.0,
    
    // Fuel Logistics & Efficiency
    val totalFuelLitersPurchased: Double = 0.0,
    val totalFuelLitersConsumed: Double = 0.0,
    val litersPerKwhGenerated: Double = 0.0,
    val kwhGeneratedPerLiter: Double = 0.0,
    val fuelCostPerKwh: Double = 0.0,
    
    // Key Unit Financial Metrics (Requested by user)
    val costPerKwh: Double = 0.0,            // تكلفة الكيلو واط على المحطة
    val averageSellingPricePerKwh: Double = 0.0, // سعر بيع الكيلو للمشتركين
    val profitMarginPerKwh: Double = 0.0,       // صافي الربح في كل كيلو واط
    val profitMarginPercentage: Double = 0.0,
    
    // Net Profit
    val netProfitOnBilled: Double = 0.0,        // صافي الربح المحاسبي (الإيرادات المفوترة - النفقات)
    val netProfitOnCollected: Double = 0.0      // صافي الربح النقدي الفعلي (المحصل نقداً - النفقات)
)
