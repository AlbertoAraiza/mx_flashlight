package home.isavanzados.mx_flashlight

import android.app.AlarmManager
import android.app.IntentService
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import home.isavanzados.mx_flashlight.receivers.AlertReceiver
import java.util.*


class MinuteService() : IntentService("BackgroundIntentService") {
    var minute: Int = 0
    var location :String? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onHandleIntent(intent: Intent?) {
        Log.e("SERV", "Iniciando MinuteService")
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val alarmIntent = Intent(this, AlertReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 1)
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().timeInMillis,60000,pendingIntent)
    }
}
