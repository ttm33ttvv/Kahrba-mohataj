package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.DateFilterPeriod
import com.example.data.model.FinancialAnalytics
import com.example.ui.screens.DashboardScreen
import com.example.ui.theme.PowerStationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dashboard_screenshot() {
        val sampleAnalytics = FinancialAnalytics(
            period = DateFilterPeriod.THIS_MONTH,
            currency = "د.ع",
            totalKwhGenerated = 32000.0,
            totalKwhSold = 28500.0,
            totalBilledRevenue = 4350000.0,
            totalCollectedCash = 4200000.0,
            totalExpenses = 2850000.0,
            fuelExpenses = 1900000.0,
            salaryExpenses = 600000.0,
            maintenanceExpenses = 350000.0,
            costPerKwh = 100.0,
            averageSellingPricePerKwh = 152.6,
            profitMarginPerKwh = 52.6,
            profitMarginPercentage = 34.4,
            netProfitOnBilled = 1500000.0,
            netProfitOnCollected = 1350000.0
        )

        composeTestRule.setContent {
            PowerStationTheme {
                DashboardScreen(
                    analytics = sampleAnalytics,
                    currentFuelInTank = 6800.0,
                    fuelTankCapacity = 12000.0,
                    selectedPeriod = DateFilterPeriod.THIS_MONTH,
                    onPeriodSelect = {},
                    onOpenAddExpense = {},
                    onOpenAddFuel = {},
                    onOpenRecordMeter = {},
                    onOpenAddSubscriber = {},
                    onOpenSettings = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/dashboard.png")
    }
}
