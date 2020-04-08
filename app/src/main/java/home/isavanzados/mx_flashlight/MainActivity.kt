package home.isavanzados.mx_flashlight

import android.Manifest
import android.app.*
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import es.dmoral.toasty.Toasty
import home.isavanzados.mx_flashlight.receivers.AlertReceiver
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import java.util.*

class MainActivity : AppCompatActivity() {
    val CHANNEL_ID = "personal_notifications"
    val NOTIFICATION_ID = 1
    val CAMERA_REQUEST = 101
    val FILE_CHOOSER_REQUEST = 102

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

        btnFileChooser.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.setType("image/*")
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(Intent.createChooser(intent, "Seleccionar archivo"),FILE_CHOOSER_REQUEST)
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
        val powerManager:PowerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val packageName = packageName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!powerManager.isIgnoringBatteryOptimizations(packageName)){
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = Uri.parse("package:${packageName}")
                startActivity(intent)
            }else{
                Toasty.success(this,"Aplication aready on withelist").show()
            }
        }else{
            Toasty.success(this, "Android version doesn't need PowerManae config").show()
        }
        val c : Calendar = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 13)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)

        val alarmManager :AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlertReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0)
        //alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.timeInMillis, pendingIntent)
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,c.timeInMillis,60000,pendingIntent)
    }

    companion object CONSTANTS{
        var torchStatus = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_CHOOSER_REQUEST){
            if(resultCode == Activity.RESULT_OK){
                if (data?.clipData != null){
                    Toasty.info(this, "Multiples archivos seleccionados").show()
                    val dataSize :Int = data.clipData!!.itemCount - 1
                    for (i in 0..dataSize){
                        Log.e("PRINT", data.clipData!!.getItemAt(i).uri.path.toString())
                    }
                }else if (data?.data != null){
                    Log.e("PRINT", data.data!!.path.toString())
                    Toasty.info(this, "Solo un archivo seleccionado").show()
                }else{
                    Toasty.error(this, "Error al seleccionar archivo").show()
                }
            }else{
                Toasty.error(this, "Error al seleccionar archivos").show()
            }
        }
    }
}


