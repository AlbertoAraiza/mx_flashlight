package home.isavanzados.mx_flashlight.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import home.isavanzados.mx_flashlight.utils.MyApp

@Database(entities = [MovieOST::class, Movie::class, RegisterTime::class], version = 1, exportSchema = false)
public abstract class MoviesDB : RoomDatabase(){
    abstract fun moviesDao(): MovieDAO
    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: MoviesDB? = null

        fun getDatabase(): MoviesDB {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    MyApp.instance!!.applicationContext,
                    MoviesDB::class.java,
                    "test_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }

}