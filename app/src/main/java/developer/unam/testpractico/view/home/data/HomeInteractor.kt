package developer.unam.testpractico.view.home.data

import android.content.Context
import android.util.Log
import developer.unam.testpractico.R
import developer.unam.testpractico.db.AppDatabase
import developer.unam.testpractico.retrofit.RetrofitInstance
import developer.unam.testpractico.retrofit.movies.Movies
import developer.unam.testpractico.view.home.IHomeContract
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeInteractor(private val apiKey: String) {

    fun retrieveFavoriteChangePath(
        responseCallback: IHomeContract.CallbackNecesary,
        path: String,
        context: Context
    ) {
        val api = RetrofitInstance.getApi().getMoviesPopular(path, apiKey, 1)
        val dataBase = AppDatabase(context)
        api.enqueue(object : Callback<Movies> {
            override fun onResponse(call: Call<Movies>, response: Response<Movies>) {
                val results = response.body()?.results
                if (response.isSuccessful && results != null) {
                    when (path.toString()) {
                        context.getString(R.string.popular_r) -> {
                            dataBase.deleteMoviesPopular()
                            results.forEach { dataBase.insertMoviePopular(it) }
                        }
                        context.getString(R.string.now_playing_r) -> {
                            dataBase.deleteMoviesNowPlaying()
                            results.forEach { dataBase.insertMovieNowPlaying(it) }
                        }
                        context.getString(R.string.upcoming_r) -> {
                            dataBase.deleteMoviesUpcoming()
                            results.forEach { dataBase.insertMovieUpcoming(it) }
                        }
                        context.getString(R.string.top_rated_r) -> {
                            dataBase.deleteMoviesTopRated()
                            results.forEach { dataBase.insertMovieTopRated(it) }
                        }
                        else -> {
                            dataBase.deleteMoviesTopRated()
                            results.forEach {
                                dataBase.insertMovieTopRated(it)
                            }
                        }
                    }
                    responseCallback.onResponse(results, response.code())
                } else {
                    val listInsert = when (path.toString()) {
                        context.getString(R.string.popular_r) -> dataBase.getAllMoviesPopular()
                        context.getString(R.string.now_playing_r) -> dataBase.getAllMoviesNowPlaying()
                        context.getString(R.string.upcoming_r) -> dataBase.getAllMoviesUpcoming()
                        context.getString(R.string.top_rated_r) -> dataBase.getAllMoviesTopRated()
                        else -> dataBase.getAllMoviesNowPlaying()
                    }
                    if (listInsert.isNotEmpty())
                        responseCallback.onResponse(listInsert, response.code())
                    else
                        responseCallback.onError(400)
                }
            }

            override fun onFailure(call: Call<Movies>, t: Throwable) {
                val listInsert = when (path.toString()) {
                    context.getString(R.string.popular_r) -> dataBase.getAllMoviesPopular()
                    context.getString(R.string.now_playing_r) -> dataBase.getAllMoviesNowPlaying()
                    context.getString(R.string.upcoming_r) -> dataBase.getAllMoviesUpcoming()
                    context.getString(R.string.top_rated_r) -> dataBase.getAllMoviesTopRated()
                    else -> dataBase.getAllMoviesNowPlaying()
                }

                if (listInsert.isNotEmpty())
                    responseCallback.onResponse(listInsert, 400)
                else
                    responseCallback.onError(400)
            }

        })
    }

}