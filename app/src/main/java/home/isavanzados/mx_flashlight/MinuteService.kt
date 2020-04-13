package home.isavanzados.mx_flashlight

import android.app.Service
import android.content.Intent
import android.location.LocationManager
import android.os.IBinder
import android.util.Log
import es.dmoral.toasty.Toasty
import home.isavanzados.mx_flashlight.interfaces.RetrofitInterface
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.util.*

class MinuteService : Service() {
    var minute: Int = 0
    var location :String? = null
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {

        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            location = intent!!.getStringExtra("location")
            minute = intent.getIntExtra("minute", 0)
            makeCallBack()
            /*
        while (true){
            Log.e("SERV", "Minute: ${i}")
            Thread.sleep(60000)
            i++
        }*/
        }catch (e:Exception){
            e.printStackTrace()
        }
        return START_NOT_STICKY
    }

    private fun makeCallBack() {
        Log.e("SERV", "trying to save minute ${minute} location: ${location}")
        val call = retrofitService.sendData()
        call.enqueue(object :Callback<Void>{
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("SERV", "Request Failiure: ${t.message}")
                Log.e("SERV", "retrying minute ${minute} in 60 seconds")
                makeCallBack()
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.e("SERV", "Minute ${minute} saved")
                onDestroy()
            }
        })
    }

    override fun onDestroy() {
        Log.e("SERV", "onDestroy")
        super.onDestroy()
    }

    companion object{
        val retrofit:Retrofit = Retrofit.Builder().baseUrl("https://jsonplaceholder.typicode.com/").addConverterFactory(GsonConverterFactory.create()).build()
        val retrofitService: RetrofitInterface = retrofit.create(RetrofitInterface::class.java)
    }
}
