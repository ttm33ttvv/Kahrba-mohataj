package com.example.data.model

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * ميزات هندسية وتشغيلية متقدمة للمحطة
 */
enum class EngineeringFeature(
    val id: String,
    val title: String,
    val description: String,
    val category: String, // LOAD_ENGINEERING, FUEL_EFFICIENCY, MAINTENANCE_ENGINEERING, NETWORK_LOSSES, FIELD_TOOLS
    val defaultEnabled: Boolean = true
) {
    PEAK_LOAD_ANALYSIS(
        id = "peak_load_analysis",
        title = "تحليل منحنى الأحمال وساعات الذروة",
        description = "مراقبة وتحديد فترات الحمل الأقصى وحساب نسبة التحميل لمنع تشغيل المولدات تحت الحمل الخفيف (Wet Stacking)",
        category = "هندسة الأحمال والطاقة"
    ),
    POWER_FACTOR_CALCULATOR(
        id = "power_factor_calc",
        title = "حاسبة معامل القدرة (Power Factor cos φ)",
        description = "حساب القدرة الظاهرية kVA والقدرة الفعلية kW والقدرة غير الفعالة kVAR وتأثير تحسين معامل القدرة على خفض الوقود",
        category = "هندسة الأحمال والطاقة"
    ),
    SPECIFIC_FUEL_CONSUMPTION(
        id = "sfc_diagnostics",
        title = "معدل الاستهلاك النوعي (SFC) وتشخيص المحرك",
        description = "قياس دقيق لكفاءة الاحتراق (لتر/kWh) ومقارنتها بالمعيار المصنعي لاكتشاف انسداد الفلاتر وضعف البخاخات مبكراً",
        category = "كفاءة الوقود وكشف الهدر"
    ),
    FUEL_DISCREPANCY_DETECTOR(
        id = "fuel_discrepancy",
        title = "كاشف فروقات وهدر الوقود التلقائي",
        description = "مقارنة الوقود الفعلي المسحوب من الخزان مع الاستهلاك النظري لساعات العمل لكشف أي تسريب أو هدر فوري",
        category = "كفاءة الوقود وكشف الهدر"
    ),
    PREVENTIVE_MAINTENANCE_MATRIX(
        id = "preventive_matrix",
        title = "مصفوفة الصيانة الدورية التراكمية",
        description = "جدولة تنبيهات الخدمة الهندسية (250س، 500س، 1000س، 5000س، 10000س) وفحص الزيوت والفلاتر وسيور التبريد",
        category = "الصيانة الوقائية والتنبؤية"
    ),
    TECHNICAL_COMMERCIAL_LOSSES(
        id = "tech_comm_losses",
        title = "تحليل الفاقد الفني والتجاري للشبكة",
        description = "حساب نسبة الفاقد الكلي وتفصيل الفاقد الفني في الكابلات والمحولات مقابل الفاقد التجاري (التجاوزات والعدادات)",
        category = "إدارة الشبكة والفاقد"
    ),
    METER_ANOMALY_DETECTION(
        id = "meter_anomaly",
        title = "كاشف القراءات الشاذة والتلاعب بالعدادات",
        description = "تنبيه ذكي للجابي عند هبوط استهلاك المشترك بنسبة غير طبيعية مقارنة بمتوسط الشهور السابقة",
        category = "إدارة الشبكة والفاقد"
    ),
    THERMAL_RECEIPT_PRINTER(
        id = "thermal_printer",
        title = "طباعة الفواتير والإيصالات الحرارية (ESC/POS)",
        description = "تجهيز فواتير طباعة حرارية فورية متوافقة مع طابعات البلوتوث الميدانية أثناء التحصيل",
        category = "الأدوات والتقارير الميدانية"
    ),
    DATA_EXPORT_EXCEL(
        id = "data_export",
        title = "تصدير كشوفات الحسابات والبيانات (CSV / Excel)",
        description = "تصدير فوري لكشوفات المشتركين، مبيعات الطاقة، سجلات الصيانة، وجدول الأرباح لمشاركتها مع الإدارة والمحاسب",
        category = "الأدوات والتقارير الميدانية"
    )
}

/**
 * حالة تفعيل الميزات والصلاحيات
 */
data class FeatureControlState(
    val enabledFeatures: Set<String> = EngineeringFeature.entries.map { it.id }.toSet(),
    val masterPin: String = "1234", // رمز الصلاحية الافتراضي للمدير / المالك
    val authorizedRoleName: String = "المالك / المدير الفني"
) {
    fun isFeatureActive(feature: EngineeringFeature): Boolean {
        return enabledFeatures.contains(feature.id)
    }
}
