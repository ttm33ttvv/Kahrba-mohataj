package com.aistudio.powerstation.mgrz.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
  /* اضف ألوانك هنا إن رغبت */
)

private val DarkColors = darkColorScheme(
  /* ألوان الوضع المظلم */
)

@Composable
fun PowerStationTheme(
  darkTheme: Boolean = false,
  content: @Composable () -> Unit
) {
  val colors = if (darkTheme) DarkColors else LightColors
  MaterialTheme(
    colorScheme = colors,
    typography = androidx.compose.material3.Typography(),
    content = content
  )
}
