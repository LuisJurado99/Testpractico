package developer.unam.testpractico.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import developer.unam.testpractico.R
import developer.unam.testpractico.db.AppDatabase
import developer.unam.testpractico.retrofit.movies.Result

class AdapterMovieMain(
    private val list: List<Result>,
    private val context: Context
) :
    RecyclerView.Adapter<AdapterMovieMain.RecyclerMovie>() {

    inner class RecyclerMovie(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgMovie = itemView.findViewById<ImageView>(R.id.imgMovieRow)
        val tvNameMovie = itemView.findViewById<TextView>(R.id.tvNameMovie)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerMovie =
        RecyclerMovie(LayoutInflater.from(context).inflate(R.layout.row_movies, parent, false))

    override fun onBindViewHolder(holder: RecyclerMovie, position: Int) {
        val item = list[position]
        Picasso.Builder(context).build()
            .load("https://image.tmdb.org/t/p/w500" + list[position].poster_path)
            .error(R.drawable.ic_cancel).into(holder.imgMovie)
        holder.tvNameMovie.text = list[position].title
    }

    override fun getItemCount(): Int = list.size
}