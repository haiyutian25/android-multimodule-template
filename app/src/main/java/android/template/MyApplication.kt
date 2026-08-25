package android.template

import android.app.Application
import android.template.sync.work.Sync
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize the sync process that keeps the app's data current.
        Sync.initialize(this)
    }
}
