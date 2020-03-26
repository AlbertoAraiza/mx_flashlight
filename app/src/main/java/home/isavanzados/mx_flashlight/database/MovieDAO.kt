package home.isavanzados.mx_flashlight.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MovieDAO {
    @Insert
    fun saveOst(ost :MovieOST)

    @Insert
    fun saveMovie(movie: Movie)

    @Query("SELECT * FROM MovieOST WHERE movie_id = :movie_id")
    fun getMovieOst(movie_id :Int): List<MovieOST>

    @Query("SELECT * FROM Movie")
    fun getMovies(): List<Movie>
}