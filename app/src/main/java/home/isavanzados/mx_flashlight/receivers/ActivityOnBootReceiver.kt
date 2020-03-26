package home.isavanzados.mx_flashlight.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import home.isavanzados.mx_flashlight.MainActivity

class ActivityOnBootReceiver :BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Toast.makeText(context, "OnBootReceiver", Toast.LENGTH_LONG).show()
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent!!.action)){
            val i = Intent(context, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context!!.startActivity(i)
        }
        Log.e("RX", intent.action!!)
    }
}