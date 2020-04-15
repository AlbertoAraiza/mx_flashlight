package home.isavanzados.mx_flashlight

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Camera
import android.hardware.camera2.CameraManager
import android.location.LocationManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.obsez.android.lib.filechooser.ChooserDialog
import es.dmoral.toasty.Toasty
import home.isavanzados.mx_flashlight.database.MoviesDB
import home.isavanzados.mx_flashlight.database.RegisterTime
import home.isavanzados.mx_flashlight.receivers.AlertReceiver
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.lang.Exception
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.fixedRateTimer

class MainActivity : AppCompatActivity() {
    val CHANNEL_ID = "personal_notifications"
    val NOTIFICATION_ID = 1
    val CAMERA_REQUEST = 101
    val FILE_CHOOSER_REQUEST = 102

    var notificationManager: NotificationManager? = null
    var alarmManager :AlarmManager? = null
    var locationManager: LocationManager? = null
    var pendingIntent :PendingIntent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        window.statusBarColor = ContextCompat.getColor(this,R.color.bg_color)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val alarmIntent = Intent(this, AlertReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent,0)

        setContentView(R.layout.activity_main)
        val cameraPermision = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        btnEnable.isEnabled = !cameraPermision

        ibHeart.setOnClickListener {
            intent = Intent(this, ButtonsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
            startActivity(intent)
        }
        btnEnable.setOnClickListener {
            ActivityCompat.requestPermissions(this,  arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), CAMERA_REQUEST)
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
           // val intent = Intent(Intent.ACTION_GET_CONTENT)
            //intent.type = "*/*"
            /*intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            val mimeTypes = arrayOf("image/*", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/pdf")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            startActivityForResult(Intent.createChooser(intent, "Seleccionar archivo"),FILE_CHOOSER_REQUEST)*/

             */
            val chooserDialog = ChooserDialog(it.context)
            chooserDialog.withFilter(true, false)
            chooserDialog.withChosenListener(object :ChooserDialog.Result {
                override fun onChoosePath(path: String?, pathFile: File?) {
                    Toasty.success(this@MainActivity, "Folder: $path").show()
                }
            })
            chooserDialog.build().show()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        btnAlarm.setOnClickListener {


        }

        btncancel.setOnClickListener {
            object :AsyncTask<Void, Void, Void?>(){
                override fun doInBackground(vararg params: Void?): Void? {
                    val registers = MoviesDB.getDatabase().moviesDao().getRegisteredTime() as ArrayList
                    registers.forEach {
                        Log.e("SERV",it.toString())
                    }
                    return null
                }
            }.execute()
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
            notificationManager!!.createNotificationChannel(notificationChannel)
        }
    }

    fun setAlarm(){
        Log.e("SERV", "Inicializanco alarma")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            Log.e("SERV", "Verion mayor a M")
            alarmManager!!.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, 0, pendingIntent)

            //alarmManager!!.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, 0, pendingIntent)
        }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            Log.e("SERV", "Version mayor a K")
            alarmManager!!.setExact(AlarmManager.RTC_WAKEUP, 0, pendingIntent)
        }else{
            Log.e("SERV", "Version inferior")
            alarmManager!!.set(AlarmManager.RTC_WAKEUP, 0, pendingIntent)
        }
    /*
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
            Toasty.success(this, "Android version doesn't need PowerManaer config").show()

        val c : Calendar = Calendar.getInstance()
        c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) + 1)
        Log.e("SERV", "Configurando alarm al minuto ${c.get(Calendar.MINUTE)}")

        val alarmManager :AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlertReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0)
        //alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.timeInMillis, pendingIntent)
        //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,c.timeInMillis,60000,pendingIntent)
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,c.timeInMillis,60000,pendingIntent)*/
    }

    fun cancelAlarm(){
        alarmManager!!.cancel(pendingIntent)
        Toasty.info(this, "Alarma cancelada").show()
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            CAMERA_REQUEST ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    btnEnable.setEnabled(false)
                    btnEnable.setText(R.string.enableCamera)
                    ivFlashlight.setEnabled(true)
                }else{
                    Toast.makeText(this, "Permission Denied for the Camera", Toast.LENGTH_SHORT).show();
                }
        }
    }



    private fun isLocationEnabled(): Boolean {
        return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager!!.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun getLastLocation() :String {
        var locationString = "desconocida"
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val currentLocation = locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)!!
            locationString = "${currentLocation.latitude}, ${currentLocation.longitude}"
            Log.e("SERV", locationString)
        }
        return locationString
    }
}


