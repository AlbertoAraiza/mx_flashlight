package home.isavanzados.mx_flashlight.RVAdapters

import android.media.AudioManager
import android.media.MediaPlayer
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.button.MaterialButton
import home.isavanzados.mx_flashlight.R
import home.isavanzados.mx_flashlight.database.Movie
import home.isavanzados.mx_flashlight.database.MovieOST
import home.isavanzados.mx_flashlight.database.MoviesDB
import home.isavanzados.mx_flashlight.utils.MyApp
import kotlinx.android.synthetic.main.movie_cardview.view.*

class MoviesAdapter(val movies : ArrayList<Movie>, val listener : (Movie)->Unit): RecyclerView.Adapter<MoviesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.movie_cardview, null)
        return(ViewHolder(view))
    }

    override fun getItemCount(): Int = movies.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(movies[position], listener)



    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun bind(movie: Movie, listener: (Movie) -> Unit) = with(itemView) {
            val imageView = this.findViewById<ImageView>(R.id.ivPoster)
            Glide.with(this.context)
                .load(movie.imgUrl)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .thumbnail(0.5f)
                .into(imageView)
            updateButton(btnPlay, movie)
            btnPrev.setOnClickListener{
                Toast.makeText(context, "Prev Pressed", Toast.LENGTH_LONG).show()
            }

            btnNext.setOnClickListener{
                Toast.makeText(context, "Next Pressed", Toast.LENGTH_LONG).show()
            }
            setOnClickListener{
                listener(movie)
            }
        }

        private fun updateButton(btnPlay: MaterialButton, movie: Movie) {
            if (!mediaPlayer.isPlaying){
                btnPlay.setIconResource(R.drawable.ic_play)
                btnPlay.setText(MyApp.instance!!.resources.getString(R.string.play))
                btnPlay.setOnClickListener {
                    mediaPlayer = MediaPlayer.create(MyApp.getContext(), movie.ost!!.get(0).resource_id)
                    mediaPlayer.start()
                    updateButton(btnPlay,movie)
                }
            }else {
                btnPlay.setIconResource(R.drawable.ic_stop)
                btnPlay.setText(MyApp.instance!!.resources.getString(R.string.stop))
                btnPlay.setOnClickListener {
                    mediaPlayer.stop()
                    mediaPlayer.release()
                    mediaPlayer = MediaPlayer()
                    updateButton(btnPlay, movie)
                }
            }
        }
    }

    companion object{
        var mediaPlayer = MediaPlayer()
        val currentSong = 0
    }
}
