package home.isavanzados.mx_flashlight.utils

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class MyApp :Application(){
    companion object{
        var instance :MyApp? = null
        fun getContext() :Context = instance as Context
    }

    override fun onCreate() {
        instance = this
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        super.onCreate()
    }
}