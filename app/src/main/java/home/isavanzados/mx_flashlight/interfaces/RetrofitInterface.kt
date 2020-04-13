package home.isavanzados.mx_flashlight.interfaces

import retrofit2.Call
import retrofit2.http.POST

interface RetrofitInterface {
    @POST("posts")
    fun sendData(): Call<Void>
}