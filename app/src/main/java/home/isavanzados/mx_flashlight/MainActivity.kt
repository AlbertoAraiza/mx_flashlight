package home.isavanzados.mx_flashlight

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.camera2.CameraManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import home.isavanzados.mx_flashlight.receivers.AlertReceiver
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import java.util.*

class MainActivity : AppCompatActivity() {
    val CHANNEL_ID = "personal_notifications"
    val NOTIFICATION_ID = 1
    val CAMERA_REQUEST = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        window.statusBarColor = ContextCompat.getColor(this,R.color.bg_color)
        setAlarm()

        setContentView(R.layout.activity_main)
        val cameraPermision = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        btnEnable.isEnabled = !cameraPermision

        ibHeart.setOnClickListener {
            intent = Intent(this, ButtonsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
            startActivity(intent)
        }
        btnEnable.setOnClickListener {
            ActivityCompat.requestPermissions(this,  arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST)
        }

        ivFlashlight.setOnClickListener{
            turnFlashLight()
            // Send a broadcast so that the Operating system updates the widget
            val man = AppWidgetManager.getInstance(this)
            val ids = man.getAppWidgetIds(
                ComponentName(this, FlashlightWidget::class.java)
            )
            val updateIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            sendBroadcast(updateIntent)
        }

        btnNotification.setOnClickListener {
            createNotificationChannel()
            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            val landingIntent = Intent(this, MainActivity::class.java)
            landingIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK
            val pendingIntent = PendingIntent.getActivity(this, 0, landingIntent, PendingIntent.FLAG_ONE_SHOT)

            val notification = builder.setSmallIcon(R.drawable.ic_notification).setContentTitle(resources.getString(R.string.app_name))
                .setContentText("Encender Flash").setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent).build()

            val notificationManager = NotificationManagerCompat.from(this)

            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun turnFlashLight() {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try{
            torchStatus = !torchStatus
            val cameraId = cameraManager.cameraIdList[0]
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId,torchStatus)
            }else{
                val camera = Camera.open()
                val parameters = camera.parameters
                val modes = parameters.supportedFlashModes
                if (modes.contains(Camera.Parameters.FLASH_MODE_TORCH)){
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH)
                }else if(modes.contains(Camera.Parameters.FLASH_MODE_ON)){
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON)
                }
                camera.parameters = parameters
                if (!torchStatus) {
                    Objects.requireNonNull(camera).startPreview()
                    torchStatus = true
                } else {
                    Objects.requireNonNull(camera).stopPreview()
                    torchStatus = false
                }
            }
           if (torchStatus){
               ivFlashlight.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_green_dark), android.graphics.PorterDuff.Mode.SRC_IN)
           }else{
               ivFlashlight.setColorFilter(ContextCompat.getColor(this, android.R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)
           }
        }catch (e : Exception){
            Toast.makeText(this, "Error: " +  e.message, Toast.LENGTH_LONG).show()
        }
        // Send a broadcast so that the Operating system updates the widget
        /*val man = AppWidgetManager.getInstance(this)
        val ids = man.getAppWidgetIds(
            ComponentName(this, FlashlightWidget::class.java))
        val updateIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(updateIntent)*/
    }

    fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val c_name = "Personal Notifications"
            val c_description = "Notification from all personal channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val notificationChannel = NotificationChannel(CHANNEL_ID, c_name, importance)
            notificationChannel.description = c_description
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            CAMERA_REQUEST ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    btnEnable.setEnabled(false);
                    btnEnable.setText(R.string.enableCamera)
                    ivFlashlight.setEnabled(true);
                }else{
                    Toast.makeText(this, "Permission Denied for the Camera", Toast.LENGTH_SHORT).show();
                }
        }
    }

    fun setAlarm(){
        val c : Calendar = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 22)
        c.set(Calendar.MINUTE, 30)
        c.set(Calendar.SECOND, 0)

        val alarmManager :AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlertReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.timeInMillis, pendingIntent)
    }

    companion object CONSTANTS{
        var torchStatus = false
    }
}


