package home.isavanzados.mx_flashlight

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import home.isavanzados.mx_flashlight.RVAdapters.MoviesAdapter
import home.isavanzados.mx_flashlight.database.Movie
import home.isavanzados.mx_flashlight.database.MovieOST
import home.isavanzados.mx_flashlight.database.MoviesDB
import home.isavanzados.mx_flashlight.utils.MyApp
import kotlinx.android.synthetic.main.activity_buttons.*

class ButtonsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buttons)

        setMoviestoAdapter()
    }
    fun setMoviestoAdapter(){
        object :AsyncTask<Void, Void, List<Movie>?>(){
            override fun doInBackground(vararg params: Void?): List<Movie>? {
                val movies = MoviesDB.getDatabase().moviesDao().getMovies()
                movies.forEach {
                    it.ost = MoviesDB.getDatabase().moviesDao().getMovieOst(it.id) as ArrayList<MovieOST>
                }
                return movies
            }

            override fun onPostExecute(result: List<Movie>?) {
                super.onPostExecute(result)
                val movieList = result as ArrayList
                rvMovies.layoutManager = LinearLayoutManager(this@ButtonsActivity)
                rvMovies.adapter = MoviesAdapter(movieList){
                    Toast.makeText(this@ButtonsActivity,"${it.imgUrl} Clicked", Toast.LENGTH_LONG).show()
                }
            }
        }.execute()

    }
}