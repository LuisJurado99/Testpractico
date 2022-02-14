package developer.unam.testpractico.view.home.data

import android.util.Log
import developer.unam.testpractico.retrofit.RetrofitInstance
import developer.unam.testpractico.retrofit.movies.Movies
import developer.unam.testpractico.view.home.IHomeContract
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeInteractor(private val apiKey:String) {
    fun retrieveFavoriteInternet(responseCallback:IHomeContract.CallbackNecesary,path:String){
        val api = RetrofitInstance.getApi().getMoviesPopular(path,apiKey,1)
        api.enqueue(object : Callback<Movies> {
            override fun onResponse(call: Call<Movies>, response: Response<Movies>) {
                if (response.code()!=200 && response.body()!=null)
                    responseCallback.onResponse(listOf(),response.code())
                else
                    responseCallback.onResponse(response.body()?.results?: listOf(),response.code())
            }

            override fun onFailure(call: Call<Movies>, t: Throwable) {
                Log.e("errorInteractor","error ${t.message}")
                responseCallback.onError(400)
            }

        })
    }
    fun retrieveFavoriteChangePath(responseCallback:IHomeContract.CallbackNecesary,path:String){
        val api = RetrofitInstance.getApi().getMoviesPopular(path,apiKey,1)
        api.enqueue(object : Callback<Movies> {
            override fun onResponse(call: Call<Movies>, response: Response<Movies>) {
                if (response.code()!=200 && response.body()!=null)
                    responseCallback.onResponse(listOf(),response.code())
                else
                    responseCallback.onResponse(response.body()?.results?: listOf(),response.code())
            }

            override fun onFailure(call: Call<Movies>, t: Throwable) {
                Log.e("errorInteractor","error ${t.message}")
                responseCallback.onError(400)
            }

        })
    }

}