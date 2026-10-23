package com.example.ramcleaner; import android.app.Application
class RamCleanerApplication : Application() { companion object { lateinit var instance: RamCleanerApplication private set } override fun onCreate() { super.onCreate(); instance = this } }
