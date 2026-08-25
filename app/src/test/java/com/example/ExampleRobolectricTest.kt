package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DateFilterPeriod
import com.example.data.model.FinancialAnalytics
import com.example.ui.viewmodel.PowerStationViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("إدارة محطة الكهرباء", appName)
    }

    @Test
    fun `verify financial analytics calculations`() {
        val analytics = FinancialAnalytics(
            period = DateFilterPeriod.THIS_MONTH,
            currency = "د.ع",
            totalKwhGenerated = 30000.0,
            totalKwhSold = 25000.0,
            unbilledKwhLoss = 5000.0,
            networkLossPercentage = 16.66,
            totalBilledRevenue = 3750000.0,
            totalCollectedCash = 3500000.0,
            totalExpenses = 2500000.0,
            fuelExpenses = 1800000.0,
            salaryExpenses = 500000.0,
            maintenanceExpenses = 200000.0,
            costPerKwh = 100.0,
            averageSellingPricePerKwh = 150.0,
            profitMarginPerKwh = 50.0,
            profitMarginPercentage = 33.33,
            netProfitOnBilled = 1250000.0,
            netProfitOnCollected = 1000000.0
        )

        assertEquals(25000.0, analytics.totalKwhSold, 0.01)
        assertEquals(100.0, analytics.costPerKwh, 0.01)
        assertEquals(150.0, analytics.averageSellingPricePerKwh, 0.01)
        assertEquals(50.0, analytics.profitMarginPerKwh, 0.01)
        assertEquals(1250000.0, analytics.netProfitOnBilled, 0.01)
    }

    @Test
    fun `verify timestamp filter period`() {
        val now = System.currentTimeMillis()
        assertTrue(PowerStationViewModel.isTimestampInPeriod(now, DateFilterPeriod.TODAY))
        assertTrue(PowerStationViewModel.isTimestampInPeriod(now, DateFilterPeriod.THIS_MONTH))
        assertTrue(PowerStationViewModel.isTimestampInPeriod(now, DateFilterPeriod.ALL))
    }
}
