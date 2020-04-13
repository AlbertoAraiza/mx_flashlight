package home.isavanzados.mx_flashlight.receivers

import android.Manifest
import android.annotation.SuppressLint
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
import android.os.AsyncTask
import android.os.Build
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import home.isavanzados.mx_flashlight.MinuteService
import java.time.LocalDateTime
import java.util.*


class AlertReceiver : BroadcastReceiver() {
    lateinit var ctx :Context
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    override fun onReceive(context: Context?, intent: Intent?) {
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
        }
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

    private fun isLocationEnabled(ctx :Context): Boolean {
        val locationManager: LocationManager = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun getLastLocation() :String {
        var locationString = "desconocida"
        val locationManager: LocationManager = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                ctx,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                ctx,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)!!
            locationString = "${currentLocation.latitude}, ${currentLocation.longitude}"
            Log.e("SERV", locationString)
        }
        return locationString
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(notificationManager: NotificationManager){
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("home.isavanzados.mx_flashlight","Flashlight_app", importance)
        channel.description = "Flashlught Application"
        channel.enableLights(true)
        channel.lightColor = Color.RED
        channel.enableVibration(true)
        channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 100)
        val att = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        channel.setSound(Settings.System.DEFAULT_ALARM_ALERT_URI,att)
        notificationManager.createNotificationChannel(channel)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendNotification(ctx :Context, notificationManager: NotificationManager){
        val currentTIme = LocalDateTime.now()
        var mins = currentTIme.minute.toString()
        if (currentTIme.minute <=9){
            mins = "0${mins}"
        }
        val notificationId = 101
        val channelId = "home.isavanzados.mx_flashlight"
        val notification = Notification.Builder(ctx,channelId)
            .setContentTitle("Flashlight APp ${currentTIme.hour}:${currentTIme.minute}")
            .setContentText("Testing")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setChannelId(channelId).build()
        notificationManager.notify(notificationId, notification)

    }
}