package com.example.ramcleaner
import android.app.ActivityManager; import android.content.Context; import android.content.pm.ApplicationInfo; import android.os.Build; import android.util.Log
import androidx.lifecycle.LiveData; import androidx.lifecycle.MutableLiveData; import androidx.lifecycle.ViewModel; import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers; import kotlinx.coroutines.launch; import kotlinx.coroutines.withContext; import java.io.File

class MemoryService(private val context: Context) {
    private val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val pm = context.packageManager
    data class MemoryInfo(val totalRam: Long, val availableRam: Long, val usedRam: Long, val usedPercent: Int, val runningAppCount: Int, val isCleaning: Boolean = false, val lastCleanedAmount: Long = 0, val killedProcessCount: Int = 0)
    fun getMemoryInfo(): MemoryInfo {
        val memInfo = ActivityManager.MemoryInfo(); am.getMemoryInfo(memInfo)
        val totalRam = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) memInfo.totalMem else memInfo.availMem + 512*1024*1024
        val usedRam = totalRam - memInfo.availMem
        val usedPercent = if (totalRam > 0) ((usedRam * 100) / totalRam).toInt() else 0
        val count = am.getRunningAppProcesses()?.count { it.importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE } ?: 0
        return MemoryInfo(totalRam, memInfo.availMem, usedRam, usedPercent, count)
    }
    fun formatBytes(b: Long): String = when { b >= 1_073_741_824L -> "%.2f GB".format(b/1_073_741_824.0); b >= 1_048_576L -> "%.2f MB".format(b/1_048_576.0); b >= 1024L -> "%.2f KB".format(b/1024.0); else -> "$b B" }
    suspend fun cleanInternalCache(): Long = withContext(Dispatchers.IO) {
        var cleaned = 0L; listOf(context.cacheDir, context.codeCacheDir, context.externalCacheDir).forEach { f -> f?.let { cleaned += deleteRecursive(it) } }; cleaned
    }
    private fun deleteRecursive(file: File): Long { var freed=0L; if(file.isDirectory) file.listFiles()?.forEach{ freed+=deleteRecursive(it) }; val s=file.length(); if(file.delete()) freed+=s; return freed }
    suspend fun killBackgroundProcesses(): Int = withContext(Dispatchers.IO) {
        var killed=0; val myUid=android.os.Process.myUid()
        am.getRunningAppProcesses()?.forEach { p ->
            val sys = (p.info.flags and ApplicationInfo.FLAG_SYSTEM)!=0; val pers = (p.info.flags and ApplicationInfo.FLAG_PERSISTENT)!=0
            if (p.uid != myUid && !sys && !pers && p.importance >= ActivityManager.RunningAppProcessInfo.IMPORTANCE_CACHED) {
                try { am.killBackgroundProcesses(p.processName); killed++ } catch(e: SecurityException) {}
            }
        }; killed
    }
}
class MainViewModel(private val s: MemoryService) : ViewModel() {
    private val _st = MutableLiveData(s.getMemoryInfo()); val state: LiveData<MemoryInfo> = _st
    fun refresh() { _st.value = s.getMemoryInfo() }
    fun cleanRam(cb: (Long,Int)->Unit) { _st.value = _st.value?.copy(isCleaning=true); viewModelScope.launch { val f=s.cleanInternalCache(); val k=s.killBackgroundProcesses(); _st.value=s.getMemoryInfo().copy(isCleaning=false, lastCleanedAmount=f, killedProcessCount=k); cb(f,k) } }
}
