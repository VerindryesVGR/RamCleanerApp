package com.example.ramcleaner
import android.os.Bundle; import android.widget.Toast; import androidx.activity.ComponentActivity; import androidx.activity.compose.setContent; import androidx.activity.viewModels
import androidx.compose.animation.animateFloatAsState; import androidx.compose.animation.core.spring; import androidx.compose.animation.core.stiffness; import androidx.compose.animation.core.dampingRatio
import androidx.compose.foundation.layout.Arrangement; import androidx.compose.foundation.layout.Box; import androidx.compose.foundation.layout.Column; import androidx.compose.foundation.layout.fillMaxSize; import androidx.compose.foundation.layout.fillMaxWidth; import androidx.compose.foundation.layout.padding; import androidx.compose.foundation.layout.size; import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button; import androidx.compose.material3.Card; import androidx.compose.material3.CircularProgressIndicator; import androidx.compose.material3.Icon; import androidx.compose.material3.Text; import androidx.compose.material3.TopAppBar; import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable; import androidx.compose.runtime.getValue; import androidx.compose.runtime.livedata.observeAsState; import androidx.compose.runtime.mutableStateOf; import androidx.compose.runtime.remember; import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment; import androidx.compose.ui.Modifier; import androidx.compose.ui.draw.clip; import androidx.compose.ui.graphics.Color; import androidx.compose.ui.res.painterResource; import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight; import androidx.compose.ui.text.style.TextAlign; import androidx.compose.ui.unit.dp; import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider; import kotlinx.coroutines.launch; import com.example.ramcleaner.ui.theme.RamCleanerTheme

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels(factoryProvider = { ViewModelProvider.Factory { MainViewModel(MemoryService(RamCleanerApplication.instance)) } })
    override fun onCreate(s: Bundle?) { super.onCreate(s); setContent { RamCleanerTheme { MainScreen(vm) } } }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class) @Composable
fun MainScreen(vm: MainViewModel) {
    val st by vm.state.observeAsState(vm.state.value!!); val svc = MemoryService(RamCleanerApplication.instance)
    val progress by animateFloatAsState(st.usedPercent/100f, spring(dampingRatio=Spring.DampingRatioMediumBouncy, stiffness=Spring.StiffnessLow))
    val clr = when { progress>0.9f -> Color.Red; progress>0.75f -> Color(0xFFFFB300); progress>0.5f -> Color(0xFFFFEB3B); else -> Color.Green }
    Column(Modifier.fillMaxSize(), Arrangement.spacedBy(16.dp), Alignment.CenterHorizontally) {
        TopAppBar(title={Text(stringResource(R.string.app_name), fontWeight=FontWeight.Bold, fontSize=20.sp)}, colors=TopAppBarDefaults.topAppBarColors(containerColor=RamCleanerTheme.colorScheme.primaryContainer, titleContentColor=RamCleanerTheme.colorScheme.onPrimaryContainer))
        Box(Modifier.size(240.dp)) {
            CircularProgressIndicator(progress=progress.coerceIn(0f,1f), strokeWidth=24.dp, color=clr, trackColor=RamCleanerTheme.colorScheme.primaryContainer.copy(alpha=0.5f), modifier=Modifier.size(240.dp))
            Column(Modifier.fillMaxSize(), Alignment.CenterHorizontally, Arrangement.Center) { Text("${st.usedPercent}%", 56.sp, FontWeight.Bold, RamCleanerTheme.colorScheme.onSurface); Text("${svc.formatBytes(st.usedRam)} / ${svc.formatBytes(st.totalRam)}", 16.sp, color=RamCleanerTheme.colorScheme.onSurfaceVariant) }
        }
        Column(Modifier.fillMaxWidth().padding(16.dp), Arrangement.spacedBy(12.dp)) {
            StatCard(stringResource(R.string.total_ram), svc.formatBytes(st.totalRam), android.R.drawable.ic_menu_rotate, RamCleanerTheme.colorScheme.primary)
            StatCard(stringResource(R.string.available_ram), svc.formatBytes(st.availableRam), android.R.drawable.ic_menu_save, Color.Green)
            StatCard(stringResource(R.string.running_apps), "${st.runningAppCount}", android.R.drawable.ic_menu_recent_history, RamCleanerTheme.colorScheme.secondary)
            if (st.lastCleanedAmount > 0 || st.killedProcessCount > 0) Card(modifier=Modifier.fillMaxWidth(), colors=androidx.compose.material3.CardDefaults.cardColors(containerColor=RamCleanerTheme.colorScheme.tertiaryContainer)) { androidx.compose.foundation.layout.Row(Modifier.padding(16.dp), Arrangement.spacedBy(16.dp), Alignment.CenterVertically) { Icon(painterResource(android.R.drawable.ic_menu_close_clear_cancel), null, RamCleanerTheme.colorScheme.onTertiaryContainer, Modifier.size(24.dp)); Column { Text(stringResource(R.string.cache_cleaned, svc.formatBytes(st.lastCleanedAmount)), 14.sp, RamCleanerTheme.colorScheme.onTertiaryContainer); Text(stringResource(R.string.process_killed, st.killedProcessCount), 12.sp, RamCleanerTheme.colorScheme.onTertiaryContainer.copy(alpha=0.8f)) } } }
        }
        Button(onClick={vm.cleanRam{ f,k -> Toast.makeText(RamCleanerApplication.instance, "Bersih: ${svc.formatBytes(f)} | Proses: $k", Toast.LENGTH_SHORT).show() }}, modifier=Modifier.fillMaxWidth().padding(16.dp).height(56.dp), enabled=!st.isCleaning, colors=androidx.compose.material3.ButtonDefaults.buttonColors(containerColor=if(st.isCleaning) RamCleanerTheme.colorScheme.onSurface.copy(alpha=0.12f) else RamCleanerTheme.colorScheme.primary, contentColor=if(st.isCleaning) RamCleanerTheme.colorScheme.onSurface.copy(alpha=0.38f) else RamCleanerTheme.colorScheme.onPrimary), shape=RoundedCornerShape(16.dp)) {
            if(st.isCleaning) androidx.compose.foundation.layout.Row(Arrangement.spacedBy(12.dp), Alignment.CenterVertically, Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)) { CircularProgressIndicator(color=RamCleanerTheme.colorScheme.onPrimary, strokeWidth=3.dp, Modifier.size(24.dp)); Text(stringResource(R.string.cleaning), 18.sp, FontWeight.Bold) } else Text(stringResource(R.string.clean_button), 18.sp, FontWeight.Bold, Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally))
        }
    }
}
@Composable fun StatCard(t:String, v:String, i:Int, c:Color) { Card(Modifier.fillMaxWidth(), elevation=androidx.compose.material3.CardDefaults.cardElevation(defaultElevation=4.dp), shape=RoundedCornerShape(16.dp)) { androidx.compose.foundation.layout.Row(Modifier.padding(16.dp), Arrangement.spacedBy(16.dp), Alignment.CenterVertically) { Box(Modifier.size(48.dp).background(c.copy(alpha=0.15f), RoundedCornerShape(12.dp)).padding(12.dp)) { Icon(painterResource(i), null, c) }; Column { Text(t, 14.sp, RamCleanerTheme.colorScheme.onSurfaceVariant); Text(v, 20.sp, FontWeight.Bold, RamCleanerTheme.colorScheme.onSurface) } } } }
