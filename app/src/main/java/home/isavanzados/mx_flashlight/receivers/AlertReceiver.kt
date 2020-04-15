package home.isavanzados.mx_flashlight.receivers

import android.Manifest
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.media.AudioAttributes
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.gms.location.FusedLocationProviderClient
import es.dmoral.toasty.Toasty
import home.isavanzados.mx_flashlight.MinuteService
import home.isavanzados.mx_flashlight.database.MoviesDB
import home.isavanzados.mx_flashlight.database.RegisterTime
import java.time.LocalDateTime
import java.util.*
import kotlin.concurrent.fixedRateTimer


class AlertReceiver : BroadcastReceiver() {
    lateinit var ctx :Context
    lateinit var mFusedLocationClient: FusedLocationProviderClient

    var notificationManager: NotificationManager? = null
    override fun onReceive(context: Context?, intent: Intent?) {
        notificationManager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        Log.e("SERV", "ejecutando...")
        if (intent!!.action == Intent.ACTION_BOOT_COMPLETED) {
            fixedRateTimer("MinuteTimer",false, 0, 60000){

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sendNotification(context)
                }
                //var currentLocation :String = if (isLocationEnabled()) getLastLocation() else "unknown"
                val currentLocation :String = "unknown"
                val gettedTime = Calendar.getInstance().time.toString()

                val x = RegisterTime(timeValue =  gettedTime,location =  currentLocation)
                MoviesDB.getDatabase().moviesDao().saveRegisterTime(x)
                Log.e("SERV", "Saved")
            }
            /*val serviceIntent = Intent(context, MinuteService::class.java)
            context!!.startService(serviceIntent)*/
        }
        /*
        if(intent!!.action.equals("android.intent.action.BOOT_COMPLETED")){
            val serviceIntent = Intent(context, MinuteService::class.java)
            context!!.startService(serviceIntent)
        }else{
            Toasty.info(context!!, "Hola mundo").show()
        }/*
        Log.e("SERV", "Iniciando Alarm")
        this.ctx = context!!
        // val builder = NotificationCompat.Builder(context) .setSmallIcon(R.drawable.ic_app) .setContentTitle("textTitle") .setContentText("textContent") .setPriority(NotificationCompat.PRIORITY_DEFAULT) .setAutoCancel(true)
        if(isLocationEnabled(context) && !isAppOnForeground(context)) {
            val location = getLastLocation()
            val minute = Calendar.getInstance().get(Calendar.MINUTE)
            val service = Intent(context, MinuteService::class.java)
            service.putExtra("location", location)
            service.putExtra("minute", minute)
            context.startService(service)
        }*/*/
    }

    private fun isAppOnForeground(context: Context): Boolean {
        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses =
            activityManager.runningAppProcesses ?: return false
        val packageName = context.packageName
        for (appProcess in appProcesses) {
            if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName == packageName) {
                return true
            }
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(){
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel("home.isavanzados.mx_flashlight","Flashlight_app", importance)
        channel.description = "Flashlught Application"
        channel.enableLights(true)
        channel.lightColor = Color.RED
        channel.enableVibration(true)
        channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 100)
        /*val att = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        channel.setSound(Settings.S-[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[ystem.DEFAULT_ALARM_ALERT_URI,att)*/
        notificationManager!!.createNotificationChannel(channel)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendNotification(ctx :Context){
        val currentTIme = LocalDateTime.now()
        var mins = currentTIme.minute.toString()
        if (currentTIme.minute <=9){
            mins = "0${mins}"
        }
        val notificationId = 101
        val channelId = "home.isavanzados.mx_flashlight"
        val notification = Notification.Builder(ctx,channelId)
            .setContentTitle("Flashlight App ${currentTIme.hour}:${currentTIme.minute}")
            .setContentText("Testing")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setChannelId(channelId).build()
        notificationManager!!.notify(notificationId, notification)

    }



}