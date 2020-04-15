package home.isavanzados.mx_flashlight.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp
import java.util.*

@Entity
data class RegisterTime (
    @PrimaryKey(autoGenerate = false) var uuid :String = UUID.randomUUID().toString(),
    val timeValue :String,
    val location :String,
    val addTime: String = Timestamp(System.currentTimeMillis()).toString()


) {
    override fun toString(): String {
        return "RegisterTime(uuid='$uuid', timeValue='$timeValue', location='$location', addTime='$addTime')"
    }
}