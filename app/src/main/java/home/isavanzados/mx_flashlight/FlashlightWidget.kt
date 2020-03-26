package home.isavanzados.mx_flashlight

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import home.isavanzados.mx_flashlight.FlashlightWidget.Companion.ClickListener
import androidx.core.content.ContextCompat.getSystemService as getSystemService1

/**
 * Implementation of App Widget functionality.
 */
class FlashlightWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context,appWidgetManager,appWidgetId)

        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceive(ctx :Context, intent: Intent){
        super.onReceive(ctx, intent)

        if (ClickListener.equals(intent.action)) {
            try {
                torchStatus = !torchStatus
                val cameraManager = getSystemService1(ctx, CameraManager::class.java)
                val cameraId = cameraManager!!.cameraIdList[0]
                cameraManager.setTorchMode(cameraId, torchStatus)
                intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                val man = AppWidgetManager.getInstance(ctx)
                val ids = man.getAppWidgetIds(
                    ComponentName(ctx, FlashlightWidget::class.java)
                )
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                onReceive(ctx, intent)
                }catch (e:Exception){
                Log.e("widget", e.message!!)
            }

        }
    }

    companion object {
        val ClickListener = "ClickListener1"
        var torchStatus = MainActivity.torchStatus
    }

}


internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.flashlight_widget)
    //views.setTextViewText(R.id.appwidget_text, widgetText)
    views.setOnClickPendingIntent(R.id.layoutButton, getPendingIntent(context,ClickListener))
    if(FlashlightWidget.torchStatus){
        views.setInt(R.id.btnSwitchFlashlight, "setImageResource", R.drawable.ic_power_on)
    }else {
        views.setInt(R.id.btnSwitchFlashlight, "setImageResource", R.drawable.ic_power_off)
    }

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

private fun getPendingIntent(ctx :Context, action :String):PendingIntent{
    val intent = Intent(ctx, FlashlightWidget::class.java)
    intent.action = action
    return PendingIntent.getBroadcast(ctx, 0, intent, 0)
}
