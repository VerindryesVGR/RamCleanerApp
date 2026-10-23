package com.example.ramcleaner.ui.theme
import android.app.Activity; import androidx.compose.material3.MaterialTheme; import androidx.compose.material3.darkColorScheme; import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable; import androidx.compose.ui.graphics.Color; import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat; import androidx.core.view.WindowInsetsControllerCompat; import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.sp; import androidx.compose.material3.Typography; import androidx.compose.ui.text.font.FontFamily; import androidx.compose.ui.text.font.FontWeight

private val Dark = darkColorScheme(primary=Color(0xFF00E5FF), primaryContainer=Color(0xFF006064), secondary=Color(0xFFB388FF), secondaryContainer=Color(0xFF4A148C), surface=Color(0xFF121212), background=Color.Black, onSurface=Color.White, onSurfaceVariant=Color(0xFFB0BEC5))
private val Light = lightColorScheme(primary=Color(0xFF006064), primaryContainer=Color(0xFF00E5FF), secondary=Color(0xFF4A148C), secondaryContainer=Color(0xFFB388FF), surface=Color.White, background=Color(0xFFFAFAFA), onSurface=Color.Black, onSurfaceVariant=Color(0xFF37474F))
val Typography = Typography(headlineLarge=androidx.compose.material3.TextStyle(fontFamily=FontFamily.Default, fontWeight=FontWeight.Bold, fontSize=32.sp), bodyLarge=androidx.compose.material3.TextStyle(fontSize=16.sp), labelLarge=androidx.compose.material3.TextStyle(fontFamily=FontFamily.Default, fontWeight=FontWeight.Bold, fontSize=14.sp))

@Composable fun RamCleanerTheme(content: @Composable ()->Unit) {
    MaterialTheme(colorScheme=Dark, typography=Typography, content=content)
    val view = LocalView.current; if(view.context is Activity) { val w=(view.context as Activity).window; WindowCompat.setDecorFitsSystemWindows(w,false); WindowInsetsControllerCompat(w,w.decorView).isAppearanceLightStatusBars=false; w.statusBarColor=Dark.background.toArgb(); w.navigationBarColor=Dark.background.toArgb() }
}
