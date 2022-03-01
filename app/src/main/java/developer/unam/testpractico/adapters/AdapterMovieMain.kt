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

class AdapterMovieMain(private val context: Context) :
    RecyclerView.Adapter<AdapterMovieMain.RecyclerMovie>() {

    private val listResult = mutableListOf<Result>()

    fun addItems(list:MutableList<Result>){
        val upSize = listResult.size
        listResult.addAll(list)
        notifyItemRangeInserted(upSize,listResult.size)
    }

    inner class RecyclerMovie(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgMovie = itemView.findViewById<ImageView>(R.id.imgMovieRow)
        val tvNameMovie = itemView.findViewById<TextView>(R.id.tvNameMovie)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerMovie =
        RecyclerMovie(LayoutInflater.from(context).inflate(R.layout.row_movies, parent, false))

    override fun onBindViewHolder(holder: RecyclerMovie, position: Int) {
        val item = listResult[position]
        Picasso.Builder(context).build()
            .load("https://image.tmdb.org/t/p/w500" + item.poster_path)
            .error(R.drawable.ic_cancel).into(holder.imgMovie)
        holder.tvNameMovie.text = item.title
    }

    override fun getItemCount(): Int = listResult.size
}