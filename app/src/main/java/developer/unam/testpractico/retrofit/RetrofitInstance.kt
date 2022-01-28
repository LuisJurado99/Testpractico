package developer.unam.testpractico.retrofit

import developer.unam.testpractico.retrofit.movies.Movies
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.converter.gson.GsonConverterFactory

import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface RetrofitInstance {
    
    @GET("movie/{tipo}")
    fun getMoviesPopular(@Path("tipo") tipo:String, @Query("api_key") apiKey:String, @Query("page")  page:Int): Call<Movies>

    companion object {
        fun getApi(): RetrofitInstance {
            val logging = HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Asociamos el interceptor a las peticiones
            val httpClient = OkHttpClient.Builder();
            httpClient.addInterceptor(logging);

            val baseUrl = "https://api.themoviedb.org/3/"

            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
            return retrofit.create(RetrofitInstance::class.java)

        }
    }

}