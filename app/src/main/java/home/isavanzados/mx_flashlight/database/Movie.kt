package home.isavanzados.mx_flashlight.database

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class Movie (
    val imgUrl: String
){
    @PrimaryKey var id :Int = 0
    @Ignore var ost : ArrayList<MovieOST>? = null
}