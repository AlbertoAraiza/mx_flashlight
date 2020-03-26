package home.isavanzados.mx_flashlight.database

import androidx.room.*

@Entity(
    foreignKeys = [ForeignKey(entity = Movie::class, parentColumns = ["id"], childColumns = ["movie_id"])],
    indices = [Index("movie_id")]
)

data class MovieOST (
    val resource_id:Int,
    @ColumnInfo(name = "movie_id") val movieId :Int
){
    @PrimaryKey(autoGenerate = true) var id :Int = 0
}