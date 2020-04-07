package home.isavanzados.mx_flashlight.receivers

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.os.AsyncTask
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi


class AlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        // val builder = NotificationCompat.Builder(context) .setSmallIcon(R.drawable.ic_app) .setContentTitle("textTitle") .setContentText("textContent") .setPriority(NotificationCompat.PRIORITY_DEFAULT) .setAutoCancel(true)
        val checkAppThread = object :AsyncTask<Void, Void, Void?>(){
            @SuppressLint("StaticFieldLeak")
            override fun doInBackground(vararg params: Void?): Void? {
                if (!isAppOnForeground(context!!)){
                    val notificationManager : NotificationManager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        createNotificationChannel(notificationManager)
                        sendNotification(context, notificationManager)
                    }
                }
                return null
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
        }
        checkAppThread.execute()
        //notificationManager.notify(0, builder.build())
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
        val notificationId = 101
        val channelId = "home.isavanzados.mx_flashlight"
        val notification = Notification.Builder(ctx,channelId)
            .setContentTitle("Flashlight APp")
            .setContentText("Testing")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setChannelId(channelId).build()
        notificationManager.notify(notificationId, notification)

    }
}